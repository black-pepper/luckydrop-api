package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.dto.AdminContentCreateRequest;
import com.luckydrop.api.domain.content.dto.AdminContentDeleteResponse;
import com.luckydrop.api.domain.content.dto.AdminContentDetailResponse;
import com.luckydrop.api.domain.content.dto.AdminContentResponse;
import com.luckydrop.api.domain.content.dto.AdminContentUpdateRequest;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.repository.ContentRepository;
import com.luckydrop.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminContentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("DRAW", "QUIZ");

    private final ContentRepository contentRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public AdminContentResponse create(AdminContentCreateRequest request) {
        String type = normalizeType(request.getType());
        User user = currentUserService.getCurrentUserEntity();

        Content content = new Content(
                generateUniqueCode(),
                type,
                user,
                request.getTitle(),
                request.getDescription()
        );

        return new AdminContentResponse(contentRepository.save(content));
    }

    @Transactional(readOnly = true)
    public AdminContentDetailResponse getDetail(String contentCode) {
        return new AdminContentDetailResponse(getOwnedActiveContent(contentCode));
    }

    @Transactional(readOnly = true)
    public List<AdminContentResponse> getContents() {
        Long currentUserId = currentUserService.getCurrentUserEntity().getId();

        return contentRepository.findAllActiveByUserIdWithUser(currentUserId)
                .stream()
                .map(AdminContentResponse::new)
                .toList();
    }

    @Transactional
    public AdminContentResponse update(String contentCode, AdminContentUpdateRequest request) {
        Content content = getOwnedUpdatableContent(contentCode);
        String type = normalizeType(request.getType());
        User user = currentUserService.getCurrentUserEntity();

        content.update(type, user, request.getTitle(), request.getDescription());

        return new AdminContentResponse(content);
    }

    @Transactional
    public AdminContentDeleteResponse delete(String contentCode) {
        Content content = getOwnedUpdatableContent(contentCode);
        content.delete();
        return new AdminContentDeleteResponse(content.getCode());
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

    private String normalizeType(String type) {
        String normalizedType = type.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(normalizedType)) {
            throw new DrawEventException(ErrorCode.CONTENT_TYPE_INVALID);
        }
        return normalizedType;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        } while (contentRepository.existsByCode(code));
        return code;
    }
}
