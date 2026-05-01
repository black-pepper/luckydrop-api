package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.invitationcode.repository.InvitationCodeRepository;
import com.luckydrop.api.domain.drawresult.dto.DrawResultResponse;
import com.luckydrop.api.domain.drawresult.repository.DrawResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final DrawResultRepository drawResultRepository;
    private final InvitationCodeRepository invitationCodeRepository;

    @Transactional(readOnly = true)
    public List<DrawResultResponse> getResultsByCode(String contentCode, String invitationCode) {
        InvitationCode code = invitationCodeRepository.findByContentCodeAndCodeWithContent(contentCode, invitationCode)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CODE_NOT_FOUND));

        return drawResultRepository.findByDrawCodeIdOrderByDrawnAtDesc(code.getId())
                .stream()
                .map(DrawResultResponse::new)
                .toList();
    }
}
