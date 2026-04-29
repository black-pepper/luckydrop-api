package com.luckydrop.api.domain.invitationcode.dto;

import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import lombok.Getter;

@Getter
public class CodeVerifyResponse {

    private final String maskedName;
    private final int remainingCount;
    private final boolean canDraw;
    private final DrawStatus drawStatus;

    public CodeVerifyResponse(InvitationCode invitationCode, DrawStatus drawStatus) {
        this.maskedName = invitationCode.getName() == null || invitationCode.getName().isBlank()
                ? invitationCode.getCode()
                : invitationCode.getName();
        this.remainingCount = invitationCode.getRemainingCount();
        this.canDraw = drawStatus == DrawStatus.DRAWABLE;
        this.drawStatus = drawStatus;
    }
}
