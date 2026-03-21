package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.drawcode.dto.CodeVerifyResponse;
import com.luckydrop.api.domain.drawcode.entity.DrawCode;
import com.luckydrop.api.domain.drawcode.repository.DrawCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CodeService {

    private final DrawCodeRepository drawCodeRepository;

    @Transactional(readOnly = true)
    public CodeVerifyResponse verifyCode(String code) {
        DrawCode drawCode = drawCodeRepository.findByCodeWithContent(code)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CODE_NOT_FOUND));

        if (!drawCode.isActive()) {
            throw new DrawEventException(ErrorCode.CODE_INACTIVE);
        }
        if (drawCode.isExpired()) {
            throw new DrawEventException(ErrorCode.CODE_EXPIRED);
        }

        return new CodeVerifyResponse(drawCode);
    }
}
