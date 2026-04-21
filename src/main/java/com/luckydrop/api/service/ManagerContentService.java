package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.dto.ManagerContentCreateRequest;
import com.luckydrop.api.domain.content.dto.ManagerContentDeleteResponse;
import com.luckydrop.api.domain.content.dto.ManagerContentDetailResponse;
import com.luckydrop.api.domain.content.dto.ManagerContentResponse;
import com.luckydrop.api.domain.content.dto.ManagerContentUpdateRequest;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.entity.ContentType;
import com.luckydrop.api.domain.content.repository.ContentRepository;
import com.luckydrop.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManagerContentService {

    private final ContentRepository contentRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public ManagerContentResponse create(ManagerContentCreateRequest request) {
        ContentType type = ContentType.from(request.getType());
        if (type == null) {
            throw new DrawEventException(ErrorCode.CONTENT_TYPE_INVALID);
        }
        User user = currentUserService.getCurrentUserEntity();
        validatePeriod(request.getStartAt(), request.getEndAt());

        Content content = new Content(
                generateUniqueCode(),
                type,
                user,
                request.getTitle(),
                request.getDescription(),
                request.getStartAt(),
                request.getEndAt()
        );

        return new ManagerContentResponse(contentRepository.save(content));
    }

    @Transactional(readOnly = true)
    public ManagerContentDetailResponse getDetail(String contentCode) {
        return new ManagerContentDetailResponse(getOwnedActiveContent(contentCode));
    }

    @Transactional(readOnly = true)
    public List<ManagerContentResponse> getContents() {
        Long currentUserId = currentUserService.getCurrentUserEntity().getId();

        return contentRepository.findAllActiveByUserIdWithUser(currentUserId)
                .stream()
                .map(ManagerContentResponse::new)
                .toList();
    }

    @Transactional
    public ManagerContentResponse update(String contentCode, ManagerContentUpdateRequest request) {
        Content content = getOwnedUpdatableContent(contentCode);
        ContentType type = ContentType.from(request.getType());
        if (type == null) {
            throw new DrawEventException(ErrorCode.CONTENT_TYPE_INVALID);
        }
        User user = currentUserService.getCurrentUserEntity();
        validatePeriod(request.getStartAt(), request.getEndAt());

        content.update(type, user, request.getTitle(), request.getDescription(),
                request.getStartAt(), request.getEndAt());

        return new ManagerContentResponse(content);
    }

    @Transactional
    public ManagerContentDeleteResponse delete(String contentCode) {
        Content content = getOwnedUpdatableContent(contentCode);
        content.delete();
        return new ManagerContentDeleteResponse(content.getCode());
    }

    private Content getOwnedActiveContent(String contentCode) {
        Content content = contentRepository.findByCodeWithUser(contentCode)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CONTENT_NOT_FOUND));

        if (content.isDeleted() || !isOwnedByCurrentUser(content)) {
            throw new DrawEventException(ErrorCode.CONTENT_NOT_FOUND);
        }
        return content;
    }

    private Content getOwnedUpdatableContent(String contentCode) {
        Content content = contentRepository.findByCodeWithUser(contentCode)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CONTENT_NOT_FOUND));

        if (!isOwnedByCurrentUser(content)) {
            throw new DrawEventException(ErrorCode.CONTENT_NOT_FOUND);
        }
        if (content.isDeleted()) {
            throw new DrawEventException(ErrorCode.CONTENT_ALREADY_DELETED);
        }
        return content;
    }

    private boolean isOwnedByCurrentUser(Content content) {
        User currentUser = currentUserService.getCurrentUserEntity();
        return content.getUser() != null && content.getUser().getId().equals(currentUser.getId());
    }

    private void validatePeriod(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt != null && endAt != null && startAt.isAfter(endAt)) {
            throw new DrawEventException(ErrorCode.CONTENT_PERIOD_INVALID);
        }
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        } while (contentRepository.existsByCode(code));
        return code;
    }
}
