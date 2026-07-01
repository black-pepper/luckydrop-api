package com.luckydrop.api.domain.participant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ParticipantUpdateRequest {

    @NotBlank(message = "참여자 이름을 입력해 주세요.")
    private String participantName;

    private String memo;
}
