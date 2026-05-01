package com.luckydrop.api.domain.invitationcode.dto;

import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class InvitationCodeResponse {

    private final Long id;
    private final String contentCode;
    private final String code;
    private final String name;
    private final int allowedDrawCount;
    private final int usedDrawCount;
    private final int remainingCount;
    private final boolean active;
    private final OffsetDateTime expiresAt;
    private final OffsetDateTime lastUsedAt;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public InvitationCodeResponse(InvitationCode invitationCode) {
        this.id = invitationCode.getId();
        this.contentCode = invitationCode.getContent().getCode();
        this.code = invitationCode.getCode();
        this.name = invitationCode.getName();
        this.allowedDrawCount = invitationCode.getAllowedDrawCount();
        this.usedDrawCount = invitationCode.getUsedDrawCount();
        this.remainingCount = invitationCode.getRemainingCount();
        this.active = invitationCode.isActive();
        this.expiresAt = invitationCode.getExpiresAt();
        this.lastUsedAt = invitationCode.getLastUsedAt();
        this.createdAt = invitationCode.getCreatedAt();
        this.updatedAt = invitationCode.getUpdatedAt();
    }
}
