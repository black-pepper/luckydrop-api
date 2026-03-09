package com.luckydrop.api.domain.drawcode.dto;

import com.luckydrop.api.domain.drawcode.entity.DrawCode;
import lombok.Getter;

@Getter
public class CodeVerifyResponse {

    private final String maskedName;
    private final int remainingCount;
    private final boolean canDraw;

    public CodeVerifyResponse(DrawCode drawCode) {
        this.maskedName = drawCode.getParticipant().getName();
        this.remainingCount = drawCode.getRemainingCount();
        this.canDraw = drawCode.getRemainingCount() > 0;
    }
}
