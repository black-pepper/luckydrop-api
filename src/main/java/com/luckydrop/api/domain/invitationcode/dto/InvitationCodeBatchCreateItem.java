package com.luckydrop.api.domain.invitationcode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class InvitationCodeBatchCreateItem {

    @NotBlank(message = "초대 코드를 입력해 주세요.")
    @Size(max = 100, message = "초대 코드는 100자 이하여야 합니다.")
    private String code;

    private String name;

    @NotNull(message = "허용 추첨 횟수를 입력해 주세요.")
    @PositiveOrZero(message = "허용 추첨 횟수는 0 이상이어야 합니다.")
    private Integer allowedDrawCount;

    private OffsetDateTime expiresAt;
}
