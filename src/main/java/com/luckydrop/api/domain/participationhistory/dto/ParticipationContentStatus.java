package com.luckydrop.api.domain.participationhistory.dto;

import com.luckydrop.api.domain.content.entity.Content;

public enum ParticipationContentStatus {
    ACTIVE,
    SCHEDULED,
    ENDED,
    DELETED;

    public static ParticipationContentStatus from(Content content) {
        if (content.isDeleted()) {
            return DELETED;
        }
        if (content.isNotStartedYet()) {
            return SCHEDULED;
        }
        if (content.isAlreadyEnded()) {
            return ENDED;
        }
        return ACTIVE;
    }
}
