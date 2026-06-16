package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.invitationcode.dto.CodeVerifyResponse;
import com.luckydrop.api.domain.invitationcode.dto.DrawStatus;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.invitationcode.repository.InvitationCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeService {

    private final InvitationCodeRepository invitationCodeRepository;
    private final DrawAvailabilityService drawAvailabilityService;
    private final ParticipationHistoryService participationHistoryService;

    @Transactional(readOnly = true)
    public CodeVerifyResponse verifyCode(String contentCode, String invitationCode) {
        InvitationCode code = invitationCodeRepository.findByContentCodeAndCodeWithContent(contentCode, invitationCode)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CODE_NOT_FOUND));

        if (!code.isActive()) {
            throw new DrawEventException(ErrorCode.CODE_INACTIVE);
        }
        if (code.isExpired()) {
            throw new DrawEventException(ErrorCode.CODE_EXPIRED);
        }

        DrawStatus drawStatus = drawAvailabilityService.getDrawStatus(code);
        CodeVerifyResponse response = new CodeVerifyResponse(code, drawStatus);
        recordParticipationHistory(code);
        return response;
    }

    private void recordParticipationHistory(InvitationCode code) {
        try {
            participationHistoryService.recordCurrentUserAccessIfAuthenticated(code.getContent(), code.getCode());
        } catch (Exception e) {
            log.warn(
                    "Failed to record participation history - contentCode: {}, invitationCode: {}",
                    code.getContent().getCode(),
                    code.getCode(),
                    e
            );
        }
    }
}
