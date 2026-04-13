package com.luckydrop.api.domain.invitationcode.dto;

import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import lombok.Getter;

@Getter
public class CodeVerifyResponse {

    private final String maskedName;
    private final int remainingCount;
    private final boolean canDraw;

    public CodeVerifyResponse(InvitationCode invitationCode) {
        this.maskedName = invitationCode.getName() == null || invitationCode.getName().isBlank()
                ? invitationCode.getCode()
                : invitationCode.getName();
        this.remainingCount = invitationCode.getRemainingCount();
        this.canDraw = invitationCode.getRemainingCount() > 0;
    }
}
