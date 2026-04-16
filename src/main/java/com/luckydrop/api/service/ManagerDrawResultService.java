package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.repository.ContentRepository;
import com.luckydrop.api.domain.drawresult.dto.ManagerDrawResultResponse;
import com.luckydrop.api.domain.drawresult.dto.DrawResultDeliveryUpdateRequest;
import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import com.luckydrop.api.domain.drawresult.repository.DrawResultRepository;
import com.luckydrop.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerDrawResultService {

    private final DrawResultRepository drawResultRepository;
    private final ContentRepository contentRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<ManagerDrawResultResponse> getManagerDrawResults(String contentCode) {
        getOwnedActiveContent(contentCode);

        return drawResultRepository.findAllByContentCodeOrderByDrawnAtDesc(contentCode)
                .stream()
                .map(ManagerDrawResultResponse::new)
                .toList();
    }

    @Transactional
    public ManagerDrawResultResponse updateDeliveryStatus(Long drawResultId, DrawResultDeliveryUpdateRequest request) {
        DrawResult drawResult = getOwnedDrawResult(drawResultId);
        drawResult.updateDelivered(request.getDelivered());
        return new ManagerDrawResultResponse(drawResult);
    }

    private DrawResult getOwnedDrawResult(Long drawResultId) {
        DrawResult drawResult = drawResultRepository.findById(drawResultId)
                .orElseThrow(() -> new DrawEventException(ErrorCode.DRAW_RESULT_NOT_FOUND));

        if (!isOwnedByCurrentUser(drawResult.getContent())) {
            throw new DrawEventException(ErrorCode.FORBIDDEN_CONTENT_ACCESS);
        }
        return drawResult;
    }

    private Content getOwnedActiveContent(String contentCode) {
        Content content = contentRepository.findByCodeWithUser(contentCode)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CONTENT_NOT_FOUND));

        if (content.isDeleted() || !isOwnedByCurrentUser(content)) {
            throw new DrawEventException(ErrorCode.CONTENT_NOT_FOUND);
        }
        return content;
    }

    private boolean isOwnedByCurrentUser(Content content) {
        User currentUser = currentUserService.getCurrentUserEntity();
        return content.getUser() != null && content.getUser().getId().equals(currentUser.getId());
    }
}
