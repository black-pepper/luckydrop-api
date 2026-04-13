package com.luckydrop.api.domain.invitationcode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class InvitationCodeCreateRequest {

    @NotBlank(message = "콘텐츠 코드를 입력해 주세요.")
    private String contentCode;

    @NotBlank(message = "초대 코드를 입력해 주세요.")
    @Size(max = 100, message = "초대 코드는 100자 이하여야 합니다.")
    private String code;

    private String name;

    @NotNull(message = "허용 추첨 횟수를 입력해 주세요.")
    @Positive(message = "허용 추첨 횟수는 1 이상이어야 합니다.")
    private Integer allowedDrawCount;

    private OffsetDateTime expiresAt;
}
