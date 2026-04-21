package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.entity.ContentType;
import com.luckydrop.api.domain.content.repository.ContentRepository;
import com.luckydrop.api.domain.drawresult.dto.ManagerDrawResultResponse;
import com.luckydrop.api.domain.drawresult.dto.DrawResultDeliveryUpdateRequest;
import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import com.luckydrop.api.domain.drawresult.repository.DrawResultRepository;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.reward.entity.Reward;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerDrawResultServiceTest {

    @Mock
    private DrawResultRepository drawResultRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ManagerDrawResultService managerDrawResultService;

    @Test
    void 본인_소유_콘텐츠의_추첨_결과_목록을_조회할_수_있다() {
        User owner = createUser(1L);
        Content content = createContent("CONTENT-001", owner);
        InvitationCode invitationCode = createInvitationCode("INVITE-001", "참가자A", content);
        DrawResult drawResult = createDrawResult(10L, content, invitationCode, false);

        when(currentUserService.getCurrentUserEntity()).thenReturn(owner);
        when(contentRepository.findByCodeWithUser("CONTENT-001")).thenReturn(Optional.of(content));
        when(drawResultRepository.findAllByContentCodeOrderByDrawnAtDesc("CONTENT-001"))
                .thenReturn(List.of(drawResult));

        List<ManagerDrawResultResponse> results = managerDrawResultService.getManagerDrawResults("CONTENT-001");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getDrawResultId()).isEqualTo(10L);
        assertThat(results.getFirst().getInvitationCode()).isEqualTo("INVITE-001");
        assertThat(results.getFirst().getInvitationCodeName()).isEqualTo("참가자A");
        assertThat(results.getFirst().isDelivered()).isFalse();
    }

    @Test
    void 타인_소유_콘텐츠의_추첨_결과_조회는_차단된다() {
        User currentUser = createUser(1L);
        User otherUser = createUser(2L);
        Content otherContent = createContent("CONTENT-002", otherUser);

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(contentRepository.findByCodeWithUser("CONTENT-002")).thenReturn(Optional.of(otherContent));

        assertThatThrownBy(() -> managerDrawResultService.getManagerDrawResults("CONTENT-002"))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONTENT_NOT_FOUND);
    }

    @Test
    void 지급_여부를_수정할_수_있다() {
        User owner = createUser(1L);
        Content content = createContent("CONTENT-001", owner);
        InvitationCode invitationCode = createInvitationCode("INVITE-001", "참가자A", content);
        DrawResult drawResult = createDrawResult(10L, content, invitationCode, false);
        DrawResultDeliveryUpdateRequest request = new DrawResultDeliveryUpdateRequest();
        ReflectionTestUtils.setField(request, "delivered", true);

        when(currentUserService.getCurrentUserEntity()).thenReturn(owner);
        when(drawResultRepository.findById(10L)).thenReturn(Optional.of(drawResult));

        ManagerDrawResultResponse response = managerDrawResultService.updateDeliveryStatus(10L, request);

        assertThat(response.isDelivered()).isTrue();
    }

    @Test
    void 존재하지_않는_추첨_결과_수정_시_예외가_발생한다() {
        when(drawResultRepository.findById(999L)).thenReturn(Optional.empty());

        DrawResultDeliveryUpdateRequest request = new DrawResultDeliveryUpdateRequest();
        ReflectionTestUtils.setField(request, "delivered", true);

        assertThatThrownBy(() -> managerDrawResultService.updateDeliveryStatus(999L, request))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DRAW_RESULT_NOT_FOUND);
    }

    @Test
    void 타인_소유_콘텐츠의_추첨_결과_지급_여부_수정은_차단된다() {
        User currentUser = createUser(1L);
        User otherUser = createUser(2L);
        Content otherContent = createContent("CONTENT-002", otherUser);
        InvitationCode invitationCode = createInvitationCode("INVITE-001", "참가자A", otherContent);
        DrawResult drawResult = createDrawResult(10L, otherContent, invitationCode, false);
        DrawResultDeliveryUpdateRequest request = new DrawResultDeliveryUpdateRequest();
        ReflectionTestUtils.setField(request, "delivered", true);

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(drawResultRepository.findById(10L)).thenReturn(Optional.of(drawResult));

        assertThatThrownBy(() -> managerDrawResultService.updateDeliveryStatus(10L, request))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_CONTENT_ACCESS);
    }

    private User createUser(Long id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Content createContent(String code, User user) {
        Content content = new Content(code, ContentType.DRAW, user, "Lucky Drop", "Event description");
        ReflectionTestUtils.setField(content, "createdAt", OffsetDateTime.parse("2026-04-01T10:00:00+09:00"));
        return content;
    }

    private InvitationCode createInvitationCode(String code, String name, Content content) {
        InvitationCode invitationCode = new InvitationCode(code, name, content, 3, null);
        ReflectionTestUtils.setField(invitationCode, "id", 1L);
        return invitationCode;
    }

    private DrawResult createDrawResult(Long id, Content content, InvitationCode invitationCode, boolean delivered) {
        Reward reward = new Reward("경품", "설명", 1, 5, null, true, content);
        ReflectionTestUtils.setField(reward, "id", 100L);

        DrawResult drawResult = DrawResult.builder()
                .content(content)
                .invitationCode(invitationCode)
                .reward(reward)
                .drawNo(1)
                .build();
        ReflectionTestUtils.setField(drawResult, "id", id);
        ReflectionTestUtils.setField(drawResult, "delivered", delivered);
        return drawResult;
    }
}
