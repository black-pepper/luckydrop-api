package com.luckydrop.api.domain.participationhistory.dto;

import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.participationhistory.entity.ParticipationHistory;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class ParticipationHistoryResponse {

    private final Long contentId;
    private final String contentTitle;
    private final ParticipationContentStatus contentStatus;
    private final String code;
    private final OffsetDateTime accessedAt;
    private final OffsetDateTime startAt;
    private final OffsetDateTime endAt;

    public ParticipationHistoryResponse(ParticipationHistory history) {
        Content content = history.getContent();
        this.contentId = content.getId();
        this.contentTitle = content.getTitle();
        this.contentStatus = ParticipationContentStatus.from(content);
        this.code = history.getInvitationCode();
        this.accessedAt = history.getAccessedAt();
        this.startAt = content.getStartAt();
        this.endAt = content.getEndAt();
    }
}
