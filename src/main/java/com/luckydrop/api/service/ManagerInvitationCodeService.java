package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.repository.ContentRepository;
import com.luckydrop.api.domain.invitationcode.dto.InvitationCodeCreateRequest;
import com.luckydrop.api.domain.invitationcode.dto.InvitationCodeResponse;
import com.luckydrop.api.domain.invitationcode.dto.InvitationCodeUpdateRequest;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.invitationcode.repository.InvitationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerInvitationCodeService {

    private final InvitationCodeRepository invitationCodeRepository;
    private final ContentRepository contentRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<InvitationCodeResponse> getInvitationCodesByContent(String contentCode) {
        Content content = getOwnedContent(contentCode);
        return invitationCodeRepository.findAllByContentId(content.getId())
                .stream()
                .map(InvitationCodeResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvitationCodeResponse getInvitationCode(Long invitationCodeId) {
        return new InvitationCodeResponse(getOwnedInvitationCode(invitationCodeId));
    }

    @Transactional
    public InvitationCodeResponse createInvitationCode(InvitationCodeCreateRequest request) {
        Content content = getOwnedContent(request.getContentCode());
        if (invitationCodeRepository.existsByContentIdAndCodeAndDeletedAtIsNull(content.getId(), request.getCode())) {
            throw new DrawEventException(ErrorCode.DRAW_CODE_DUPLICATE);
        }
        InvitationCode invitationCode = new InvitationCode(
                request.getCode(),
                request.getName(),
                content,
                request.getAllowedDrawCount(),
                request.getExpiresAt()
        );
        return new InvitationCodeResponse(invitationCodeRepository.save(invitationCode));
    }

    @Transactional
    public InvitationCodeResponse updateInvitationCode(Long invitationCodeId, InvitationCodeUpdateRequest request) {
        InvitationCode invitationCode = getOwnedInvitationCode(invitationCodeId);
        invitationCode.update(
                request.getName(),
                request.getAllowedDrawCount(),
                request.getExpiresAt(),
                request.getActive()
        );
        return new InvitationCodeResponse(invitationCode);
    }

    @Transactional
    public void deleteInvitationCode(Long invitationCodeId) {
        InvitationCode invitationCode = getOwnedInvitationCode(invitationCodeId);
        invitationCode.delete();
    }

    private Content getOwnedContent(String contentCode) {
        Long currentUserId = currentUserService.getCurrentUserEntity().getId();
        return contentRepository.findByCodeWithUser(contentCode)
                .map(content -> {
                    if (content.isDeleted()) {
                        throw new DrawEventException(ErrorCode.CONTENT_NOT_FOUND);
                    }
                    if (content.getUser() == null || !content.getUser().getId().equals(currentUserId)) {
                        throw new DrawEventException(ErrorCode.FORBIDDEN_CONTENT_ACCESS);
                    }
                    return content;
                })
                .orElseThrow(() -> new DrawEventException(ErrorCode.CONTENT_NOT_FOUND));
    }

    private InvitationCode getOwnedInvitationCode(Long invitationCodeId) {
        Long currentUserId = currentUserService.getCurrentUserEntity().getId();
        return invitationCodeRepository.findByIdWithContentAndUser(invitationCodeId)
                .map(invitationCode -> {
                    if (invitationCode.isDeleted()) {
                        throw new DrawEventException(ErrorCode.DRAW_CODE_NOT_FOUND);
                    }
                    if (invitationCode.getContent() == null || invitationCode.getContent().isDeleted()) {
                        throw new DrawEventException(ErrorCode.CONTENT_NOT_FOUND);
                    }
                    if (invitationCode.getContent().getUser() == null
                            || !invitationCode.getContent().getUser().getId().equals(currentUserId)) {
                        throw new DrawEventException(ErrorCode.FORBIDDEN_CONTENT_ACCESS);
                    }
                    return invitationCode;
                })
                .orElseThrow(() -> new DrawEventException(ErrorCode.DRAW_CODE_NOT_FOUND));
    }
}
