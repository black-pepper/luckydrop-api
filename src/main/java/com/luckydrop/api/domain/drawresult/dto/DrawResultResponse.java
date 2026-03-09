package com.luckydrop.api.domain.drawresult.dto;

import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DrawResultResponse {

    private final Long drawResultId;
    private final String rewardName;
    private final int drawNo;
    private final LocalDateTime drawnAt;

    public DrawResultResponse(DrawResult result) {
        this.drawResultId = result.getId();
        this.rewardName = result.getRewardNameSnapshot();
        this.drawNo = result.getDrawNo();
        this.drawnAt = result.getDrawnAt();
    }
}
