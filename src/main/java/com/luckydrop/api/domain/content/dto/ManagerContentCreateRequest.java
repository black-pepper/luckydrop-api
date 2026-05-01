package com.luckydrop.api.domain.content.dto;

import com.luckydrop.api.domain.content.entity.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class ManagerContentCreateRequest {

    @NotNull(message = "콘텐츠 타입을 입력해주세요.")
    private ContentType type;

    @NotBlank(message = "콘텐츠 제목을 입력해주세요.")
    private String title;

    private String description;

    private OffsetDateTime startAt;

    private OffsetDateTime endAt;
}
