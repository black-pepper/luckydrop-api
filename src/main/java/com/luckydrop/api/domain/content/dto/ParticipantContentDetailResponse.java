package com.luckydrop.api.domain.content.dto;

import com.luckydrop.api.domain.content.entity.Content;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class ParticipantContentDetailResponse {

    private final String code;
    private final String type;
    private final String title;
    private final String description;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime startAt;
    private final OffsetDateTime endAt;

    public ParticipantContentDetailResponse(Content content) {
        this.code = content.getCode();
        this.type = content.getType();
        this.title = content.getTitle();
        this.description = content.getDescription();
        this.createdAt = content.getCreatedAt();
        this.startAt = content.getStartAt();
        this.endAt = content.getEndAt();
    }
}
