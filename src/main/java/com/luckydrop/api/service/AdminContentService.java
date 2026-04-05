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
import com.luckydrop.api.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public AdminContentResponse create(AdminContentCreateRequest request) {
        String type = normalizeType(request.getType());
        User user = getUser(request.getUserId());

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
        return new AdminContentDetailResponse(getActiveContent(contentCode));
    }

    @Transactional(readOnly = true)
    public List<AdminContentResponse> getContents() {
        return contentRepository.findAllActiveWithUser()
                .stream()
                .map(AdminContentResponse::new)
                .toList();
    }

    @Transactional
    public AdminContentResponse update(String contentCode, AdminContentUpdateRequest request) {
        Content content = getUpdatableContent(contentCode);
        String type = normalizeType(request.getType());
        User user = getUser(request.getUserId());

        content.update(type, user, request.getTitle(), request.getDescription());

        return new AdminContentResponse(content);
    }

    @Transactional
    public AdminContentDeleteResponse delete(String contentCode) {
        Content content = getUpdatableContent(contentCode);
        content.delete();
        return new AdminContentDeleteResponse(content.getCode());
    }

    private Content getActiveContent(String contentCode) {
        Content content = contentRepository.findByCodeWithUser(contentCode)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CONTENT_NOT_FOUND));

        if (content.isDeleted()) {
            throw new DrawEventException(ErrorCode.CONTENT_NOT_FOUND);
        }
        return content;
    }

    private Content getUpdatableContent(String contentCode) {
        Content content = contentRepository.findByCodeWithUser(contentCode)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CONTENT_NOT_FOUND));

        if (content.isDeleted()) {
            throw new DrawEventException(ErrorCode.CONTENT_ALREADY_DELETED);
        }
        return content;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CONTENT_USER_NOT_FOUND));
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
