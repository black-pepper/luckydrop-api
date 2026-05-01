package com.luckydrop.api.domain.invitationcode.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class InvitationCodeUpdateRequest {

    private String name;

    @NotNull(message = "허용 추첨 횟수를 입력해 주세요.")
    @PositiveOrZero(message = "허용 추첨 횟수는 0 이상이어야 합니다.")
    private Integer allowedDrawCount;

    private OffsetDateTime expiresAt;

    @NotNull(message = "활성화 여부를 입력해 주세요.")
    private Boolean active;
}
