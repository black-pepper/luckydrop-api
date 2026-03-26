package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.drawcode.entity.DrawCode;
import com.luckydrop.api.domain.drawcode.repository.DrawCodeRepository;
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
    private final DrawCodeRepository drawCodeRepository;

    @Transactional(readOnly = true)
    public List<DrawResultResponse> getResultsByCode(String code) {
        DrawCode drawCode = drawCodeRepository.findByCodeWithContent(code)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CODE_NOT_FOUND));

        return drawResultRepository.findByDrawCodeIdOrderByDrawnAtDesc(drawCode.getId())
                .stream()
                .map(DrawResultResponse::new)
                .toList();
    }
}
