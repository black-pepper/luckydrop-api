package com.luckydrop.api.domain.drawresult.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DrawRequest {

    @NotBlank(message = "코드를 입력해주세요.")
    private String code;
}
