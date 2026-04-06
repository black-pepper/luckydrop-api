package com.luckydrop.api.domain.content.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminContentCreateRequest {

    @NotBlank(message = "콘텐츠 타입을 입력해주세요.")
    private String type;

    @NotBlank(message = "콘텐츠 제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "콘텐츠 설명을 입력해주세요.")
    private String description;
}
