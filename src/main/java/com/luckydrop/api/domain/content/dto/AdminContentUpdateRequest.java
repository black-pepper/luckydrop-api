package com.luckydrop.api.domain.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminContentUpdateRequest {

    @NotBlank(message = "콘텐츠 타입을 입력해주세요.")
    private String type;

    @NotNull(message = "사용자 ID를 입력해주세요.")
    private Long userId;

    @NotBlank(message = "콘텐츠 제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "콘텐츠 설명을 입력해주세요.")
    private String description;
}
