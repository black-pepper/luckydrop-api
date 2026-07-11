package com.luckydrop.api.domain.participant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ParticipantUpdateRequest {

    @NotEmpty(message = "참여자 목록을 입력해 주세요.")
    private List<@NotBlank(message = "참여자 이름을 입력해 주세요.") String> participantNames;

    private String memo;
}
