package com.luckydrop.api.domain.drawresult.dto;

import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class DrawResultResponse {

    private final Long drawResultId;
    private final String rewardName;
    private final String rewardImageUrl;
    private final int drawNo;
    private final OffsetDateTime drawnAt;

    public DrawResultResponse(DrawResult result) {
        this.drawResultId = result.getId();
        this.rewardName = result.getReward().getName();
        this.rewardImageUrl = result.getReward().getImage();
        this.drawNo = result.getDrawNo();
        this.drawnAt = result.getDrawnAt();
    }
}
