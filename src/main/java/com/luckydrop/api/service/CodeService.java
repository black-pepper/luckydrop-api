package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.invitationcode.dto.CodeVerifyResponse;
import com.luckydrop.api.domain.invitationcode.dto.DrawStatus;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.invitationcode.repository.InvitationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CodeService {

    private final InvitationCodeRepository invitationCodeRepository;
    private final DrawAvailabilityService drawAvailabilityService;

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
        return new CodeVerifyResponse(code, drawStatus);
    }
}
