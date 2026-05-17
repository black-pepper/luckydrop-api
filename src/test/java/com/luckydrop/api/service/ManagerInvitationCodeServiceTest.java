package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.entity.ContentType;
import com.luckydrop.api.domain.content.repository.ContentRepository;
import com.luckydrop.api.domain.invitationcode.dto.InvitationCodeBatchCreateItem;
import com.luckydrop.api.domain.invitationcode.dto.InvitationCodeBatchCreateRequest;
import com.luckydrop.api.domain.invitationcode.dto.InvitationCodeResponse;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.invitationcode.repository.InvitationCodeRepository;
import com.luckydrop.api.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerInvitationCodeServiceTest {

    @Mock
    private InvitationCodeRepository invitationCodeRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ManagerInvitationCodeService managerInvitationCodeService;

    @Test
    void createInvitationCodesCreatesMultipleCodesForOwnedContent() {
        User currentUser = createUser(1L);
        Content content = createContent(10L, currentUser, false);
        InvitationCodeBatchCreateRequest request = new InvitationCodeBatchCreateRequest();
        ReflectionTestUtils.setField(request, "contentCode", "CONTENT-001");
        ReflectionTestUtils.setField(request, "invitationCodes", List.of(
                createBatchItem("INVITE-001", "파트너 A", 3, OffsetDateTime.parse("2026-06-01T00:00:00Z")),
                createBatchItem("INVITE-002", "파트너 B", 1, null)
        ));

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(contentRepository.findByCodeWithUser("CONTENT-001")).thenReturn(Optional.of(content));
        when(invitationCodeRepository.existsByContentIdAndCodeAndDeletedAtIsNull(10L, "INVITE-001")).thenReturn(false);
        when(invitationCodeRepository.existsByContentIdAndCodeAndDeletedAtIsNull(10L, "INVITE-002")).thenReturn(false);
        when(invitationCodeRepository.saveAll(anyList())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<InvitationCode> invitationCodes = invocation.getArgument(0);
            ReflectionTestUtils.setField(invitationCodes.get(0), "id", 100L);
            ReflectionTestUtils.setField(invitationCodes.get(0), "createdAt", OffsetDateTime.parse("2026-04-12T10:00:00+09:00"));
            ReflectionTestUtils.setField(invitationCodes.get(0), "updatedAt", OffsetDateTime.parse("2026-04-12T10:00:00+09:00"));
            ReflectionTestUtils.setField(invitationCodes.get(1), "id", 101L);
            ReflectionTestUtils.setField(invitationCodes.get(1), "createdAt", OffsetDateTime.parse("2026-04-12T10:01:00+09:00"));
            ReflectionTestUtils.setField(invitationCodes.get(1), "updatedAt", OffsetDateTime.parse("2026-04-12T10:01:00+09:00"));
            return invitationCodes;
        });

        List<InvitationCodeResponse> responses = managerInvitationCodeService.createInvitationCodes(request);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(100L);
        assertThat(responses.get(0).getContentCode()).isEqualTo("CONTENT-001");
        assertThat(responses.get(0).getCode()).isEqualTo("INVITE-001");
        assertThat(responses.get(1).getId()).isEqualTo(101L);
        assertThat(responses.get(1).getCode()).isEqualTo("INVITE-002");
    }

    @Test
    void createInvitationCodesThrowsWhenRequestContainsDuplicateCodes() {
        User currentUser = createUser(1L);
        Content content = createContent(10L, currentUser, false);
        InvitationCodeBatchCreateRequest request = new InvitationCodeBatchCreateRequest();
        ReflectionTestUtils.setField(request, "contentCode", "CONTENT-001");
        ReflectionTestUtils.setField(request, "invitationCodes", List.of(
                createBatchItem("INVITE-001", "파트너 A", 3, null),
                createBatchItem("INVITE-001", "파트너 B", 1, null)
        ));

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(contentRepository.findByCodeWithUser("CONTENT-001")).thenReturn(Optional.of(content));

        assertThatThrownBy(() -> managerInvitationCodeService.createInvitationCodes(request))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DRAW_CODE_DUPLICATE);

        verify(invitationCodeRepository, never()).saveAll(anyList());
    }

    @Test
    void createInvitationCodesThrowsWhenCodeAlreadyExists() {
        User currentUser = createUser(1L);
        Content content = createContent(10L, currentUser, false);
        InvitationCodeBatchCreateRequest request = new InvitationCodeBatchCreateRequest();
        ReflectionTestUtils.setField(request, "contentCode", "CONTENT-001");
        ReflectionTestUtils.setField(request, "invitationCodes", List.of(
                createBatchItem("INVITE-001", "파트너 A", 3, null),
                createBatchItem("INVITE-002", "파트너 B", 1, null)
        ));

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(contentRepository.findByCodeWithUser("CONTENT-001")).thenReturn(Optional.of(content));
        when(invitationCodeRepository.existsByContentIdAndCodeAndDeletedAtIsNull(10L, "INVITE-001")).thenReturn(false);
        when(invitationCodeRepository.existsByContentIdAndCodeAndDeletedAtIsNull(10L, "INVITE-002")).thenReturn(true);

        assertThatThrownBy(() -> managerInvitationCodeService.createInvitationCodes(request))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DRAW_CODE_DUPLICATE);

        verify(invitationCodeRepository, never()).saveAll(anyList());
    }

    @Test
    void createInvitationCodesThrowsForbiddenWhenContentOwnerIsDifferent() {
        User currentUser = createUser(1L);
        User otherUser = createUser(2L);
        Content otherContent = createContent(10L, otherUser, false);
        InvitationCodeBatchCreateRequest request = new InvitationCodeBatchCreateRequest();
        ReflectionTestUtils.setField(request, "contentCode", "CONTENT-001");
        ReflectionTestUtils.setField(request, "invitationCodes", List.of(
                createBatchItem("INVITE-001", "파트너 A", 3, null)
        ));

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(contentRepository.findByCodeWithUser("CONTENT-001")).thenReturn(Optional.of(otherContent));

        assertThatThrownBy(() -> managerInvitationCodeService.createInvitationCodes(request))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_CONTENT_ACCESS);
    }

    @Test
    void createInvitationCodesThrowsWhenContentIsDeleted() {
        User currentUser = createUser(1L);
        Content deletedContent = createContent(10L, currentUser, true);
        InvitationCodeBatchCreateRequest request = new InvitationCodeBatchCreateRequest();
        ReflectionTestUtils.setField(request, "contentCode", "CONTENT-001");
        ReflectionTestUtils.setField(request, "invitationCodes", List.of(
                createBatchItem("INVITE-001", "파트너 A", 3, null)
        ));

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(contentRepository.findByCodeWithUser("CONTENT-001")).thenReturn(Optional.of(deletedContent));

        assertThatThrownBy(() -> managerInvitationCodeService.createInvitationCodes(request))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONTENT_NOT_FOUND);
    }

    private InvitationCodeBatchCreateItem createBatchItem(
            String code,
            String name,
            Integer allowedDrawCount,
            OffsetDateTime expiresAt
    ) {
        InvitationCodeBatchCreateItem item = new InvitationCodeBatchCreateItem();
        ReflectionTestUtils.setField(item, "code", code);
        ReflectionTestUtils.setField(item, "name", name);
        ReflectionTestUtils.setField(item, "allowedDrawCount", allowedDrawCount);
        ReflectionTestUtils.setField(item, "expiresAt", expiresAt);
        return item;
    }

    private User createUser(Long id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Content createContent(Long id, User user, boolean deleted) {
        Content content = new Content("CONTENT-001", ContentType.DRAW, user, "Lucky Drop", "Event description");
        ReflectionTestUtils.setField(content, "id", id);
        ReflectionTestUtils.setField(content, "createdAt", OffsetDateTime.parse("2026-04-12T09:00:00+09:00"));
        if (deleted) {
            ReflectionTestUtils.setField(content, "deletedAt", OffsetDateTime.parse("2026-04-12T09:30:00+09:00"));
        }
        return content;
    }
}
