package com.luckydrop.api.domain.reward.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RewardBatchCreateRequest {

    @NotBlank(message = "콘텐츠 코드를 입력해 주세요.")
    private String contentCode;

    @NotEmpty(message = "보상 목록을 입력해 주세요.")
    @Valid
    private List<RewardUpdateRequest> rewards;
}
