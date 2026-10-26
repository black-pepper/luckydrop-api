package com.luckydrop.api.domain.participant.dto;

import com.luckydrop.api.domain.participant.entity.Participant;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
public class ParticipantResponse {

    private final Long id;
    private final List<String> participantNames;
    private final String memo;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public ParticipantResponse(Participant participant) {
        this.id = participant.getId();
        this.participantNames = List.copyOf(participant.getParticipantNames());
        this.memo = participant.getMemo();
        this.createdAt = participant.getCreatedAt();
        this.updatedAt = participant.getUpdatedAt();
    }
}
