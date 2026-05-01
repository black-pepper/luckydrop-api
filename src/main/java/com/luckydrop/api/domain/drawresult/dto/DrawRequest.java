package com.luckydrop.api.domain.drawresult.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DrawRequest {

    @Schema(description = "참가자가 진입한 콘텐츠 코드", example = "CONTENT-001")
    @NotBlank(message = "콘텐츠 코드를 입력해주세요.")
    private String contentCode;

    @Schema(description = "콘텐츠 내부에서만 유니크한 초대 코드", example = "INVITE-001")
    @NotBlank(message = "초대 코드를 입력해주세요.")
    private String invitationCode;
}
