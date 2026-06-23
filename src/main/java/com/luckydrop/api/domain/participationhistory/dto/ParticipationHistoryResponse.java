package com.luckydrop.api.domain.participationhistory.dto;

import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.participationhistory.entity.ParticipationHistory;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class ParticipationHistoryResponse {

    private final String contentCode;
    private final String contentTitle;
    private final ParticipationContentStatus contentStatus;
    private final String invitationCode;
    private final OffsetDateTime accessedAt;
    private final OffsetDateTime startAt;
    private final OffsetDateTime endAt;

    public ParticipationHistoryResponse(ParticipationHistory history) {
        Content content = history.getContent();
        this.contentCode = content.getCode();
        this.contentTitle = content.getTitle();
        this.contentStatus = ParticipationContentStatus.from(content);
        this.invitationCode = history.getInvitationCode();
        this.accessedAt = history.getAccessedAt();
        this.startAt = content.getStartAt();
        this.endAt = content.getEndAt();
    }
}
