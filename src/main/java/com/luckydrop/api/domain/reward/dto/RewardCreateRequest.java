package com.luckydrop.api.domain.reward.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RewardCreateRequest {

    @NotBlank(message = "콘텐츠 코드를 입력해 주세요.")
    private String contentCode;

    @NotBlank(message = "보상 이름을 입력해 주세요.")
    @Size(max = 200, message = "보상 이름은 200자 이하여야 합니다.")
    private String name;

    private String description;

    @NotNull(message = "가중치를 입력해 주세요.")
    @Positive(message = "가중치는 1 이상이어야 합니다.")
    private Integer weight;

    @PositiveOrZero(message = "재고는 0 이상이어야 합니다.")
    private Integer stock;

    @Size(max = 500, message = "이미지url는 500자 이하여야 합니다.")
    private String imageUrl;

    private Boolean allowDuplicateReward;
}
