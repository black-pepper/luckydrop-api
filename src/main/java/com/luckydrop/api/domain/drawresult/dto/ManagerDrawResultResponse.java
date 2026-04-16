package com.luckydrop.api.domain.drawresult.dto;

import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class ManagerDrawResultResponse {

    private final Long drawResultId;
    private final String invitationCode;
    private final String invitationCodeName;
    private final String rewardName;
    private final int drawNo;
    private final OffsetDateTime drawnAt;
    private final boolean delivered;

    public ManagerDrawResultResponse(DrawResult result) {
        this.drawResultId = result.getId();
        this.invitationCode = result.getInvitationCode().getCode();
        this.invitationCodeName = result.getInvitationCode().getName();
        this.rewardName = result.getReward().getName();
        this.drawNo = result.getDrawNo();
        this.drawnAt = result.getDrawnAt();
        this.delivered = result.isDelivered();
    }
}
