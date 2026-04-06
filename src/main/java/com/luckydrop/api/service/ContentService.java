package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.dto.ParticipantContentDetailResponse;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;

    @Transactional(readOnly = true)
    public ParticipantContentDetailResponse getDetail(String contentCode) {
        Content content = contentRepository.findByCodeWithUser(contentCode)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CONTENT_NOT_FOUND));

        if (content.isDeleted()) {
            throw new DrawEventException(ErrorCode.CONTENT_NOT_FOUND);
        }

        return new ParticipantContentDetailResponse(content);
    }
}
