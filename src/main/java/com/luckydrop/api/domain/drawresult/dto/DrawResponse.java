package com.luckydrop.api.domain.drawresult.dto;

import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DrawResponse {

    private final Long drawResultId;
    private final String rewardName;
    private final int drawNo;
    private final int remainingCount;
    private final LocalDateTime drawnAt;

    public DrawResponse(DrawResult result, int remainingCount) {
        this.drawResultId = result.getId();
        this.rewardName = result.getRewardNameSnapshot();
        this.drawNo = result.getDrawNo();
        this.remainingCount = remainingCount;
        this.drawnAt = result.getDrawnAt();
    }
}
