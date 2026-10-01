package com.luckydrop.api.domain.participant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ParticipantBatchCreateRequest {

    @NotEmpty(message = "참여자 목록을 입력해 주세요.")
    private List<@Valid ParticipantCreateRequest> participants;
}
