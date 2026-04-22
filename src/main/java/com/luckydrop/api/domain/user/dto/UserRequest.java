package com.luckydrop.api.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRequest {
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;
}
