package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.entity.ContentType;
import com.luckydrop.api.domain.invitationcode.dto.CodeVerifyResponse;
import com.luckydrop.api.domain.invitationcode.dto.DrawStatus;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeServiceTest {

    @Mock
    private InvitationCodeRepository invitationCodeRepository;

    @Mock
    private DrawAvailabilityService drawAvailabilityService;

    @InjectMocks
    private CodeService codeService;

    @Test
    void verifyCodeReturnsDrawableStatus() {
        Content content = createContent("CONTENT-001");
        InvitationCode invitationCode = createInvitationCode("INVITE-001", content, 3, 0, true, null);

        when(invitationCodeRepository.findByContentCodeAndCodeWithContent("CONTENT-001", "INVITE-001"))
                .thenReturn(Optional.of(invitationCode));
        when(drawAvailabilityService.getDrawStatus(invitationCode)).thenReturn(DrawStatus.DRAWABLE);

        CodeVerifyResponse response = codeService.verifyCode("CONTENT-001", "INVITE-001");

        assertThat(response.getName()).isEqualTo("INVITE-001");
        assertThat(response.getRemainingCount()).isEqualTo(3);
        assertThat(response.isCanDraw()).isTrue();
        assertThat(response.getDrawStatus()).isEqualTo(DrawStatus.DRAWABLE);
    }

    @Test
    void verifyCodeReturnsNonDrawableStatus() {
        Content content = createContent("CONTENT-001");
        InvitationCode invitationCode = createInvitationCode("INVITE-001", content, 3, 3, true, null);

        when(invitationCodeRepository.findByContentCodeAndCodeWithContent("CONTENT-001", "INVITE-001"))
                .thenReturn(Optional.of(invitationCode));
        when(drawAvailabilityService.getDrawStatus(invitationCode)).thenReturn(DrawStatus.NO_REMAINING);

        CodeVerifyResponse response = codeService.verifyCode("CONTENT-001", "INVITE-001");

        assertThat(response.isCanDraw()).isFalse();
        assertThat(response.getDrawStatus()).isEqualTo(DrawStatus.NO_REMAINING);
        assertThat(response.getRemainingCount()).isZero();
    }

    @Test
    void verifyCodeThrowsWhenCodeNotFound() {
        when(invitationCodeRepository.findByContentCodeAndCodeWithContent("CONTENT-001", "INVITE-404"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> codeService.verifyCode("CONTENT-001", "INVITE-404"))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CODE_NOT_FOUND);
    }

    @Test
    void verifyCodeThrowsWhenCodeInactive() {
        Content content = createContent("CONTENT-001");
        InvitationCode invitationCode = createInvitationCode("INVITE-001", content, 3, 0, false, null);

        when(invitationCodeRepository.findByContentCodeAndCodeWithContent("CONTENT-001", "INVITE-001"))
                .thenReturn(Optional.of(invitationCode));

        assertThatThrownBy(() -> codeService.verifyCode("CONTENT-001", "INVITE-001"))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CODE_INACTIVE);
    }

    @Test
    void verifyCodeThrowsWhenCodeExpired() {
        Content content = createContent("CONTENT-001");
        InvitationCode invitationCode = createInvitationCode(
                "INVITE-001",
                content,
                3,
                0,
                true,
                OffsetDateTime.now().minusDays(1)
        );

        when(invitationCodeRepository.findByContentCodeAndCodeWithContent("CONTENT-001", "INVITE-001"))
                .thenReturn(Optional.of(invitationCode));

        assertThatThrownBy(() -> codeService.verifyCode("CONTENT-001", "INVITE-001"))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CODE_EXPIRED);
    }

    private Content createContent(String code) {
        Content content = new Content(code, ContentType.DRAW, new User(), "Lucky Drop", "Event description");
        ReflectionTestUtils.setField(content, "id", 10L);
        return content;
    }

    private InvitationCode createInvitationCode(
            String code,
            Content content,
            int allowedDrawCount,
            int usedDrawCount,
            boolean active,
            OffsetDateTime expiresAt
    ) {
        InvitationCode invitationCode = new InvitationCode();
        ReflectionTestUtils.setField(invitationCode, "id", 1L);
        ReflectionTestUtils.setField(invitationCode, "code", code);
        ReflectionTestUtils.setField(invitationCode, "content", content);
        ReflectionTestUtils.setField(invitationCode, "allowedDrawCount", allowedDrawCount);
        ReflectionTestUtils.setField(invitationCode, "usedDrawCount", usedDrawCount);
        ReflectionTestUtils.setField(invitationCode, "active", active);
        ReflectionTestUtils.setField(invitationCode, "expiresAt", expiresAt);
        return invitationCode;
    }
}
