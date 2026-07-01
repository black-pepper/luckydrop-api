package com.luckydrop.api.domain.participant.dto;

import com.luckydrop.api.domain.participant.entity.Participant;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class ParticipantResponse {

    private final Long id;
    private final String participantName;
    private final String memo;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public ParticipantResponse(Participant participant) {
        this.id = participant.getId();
        this.participantName = participant.getParticipantName();
        this.memo = participant.getMemo();
        this.createdAt = participant.getCreatedAt();
        this.updatedAt = participant.getUpdatedAt();
    }
}
