package com.luckydrop.api.domain.reward.dto;

import com.luckydrop.api.domain.reward.entity.Reward;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class ManagerRewardResponse {

    private final Long id;
    private final String contentCode;
    private final String name;
    private final String description;
    private final Integer weight;
    private final Integer poolCount;
    private final Integer stock;
    private final boolean unlimited;
    private final String imageUrl;
    private final boolean active;
    private final boolean allowDuplicateReward;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public ManagerRewardResponse(Reward reward) {
        this.id = reward.getId();
        this.contentCode = reward.getContent().getCode();
        this.name = reward.getName();
        this.description = reward.getDescription();
        this.weight = reward.getWeight();
        this.poolCount = reward.getPoolCount();
        this.stock = reward.getStock();
        this.unlimited = reward.isUnlimitedStock();
        this.imageUrl = reward.getImage();
        this.active = reward.isActive();
        this.allowDuplicateReward = reward.isDuplicateAllowed();
        this.createdAt = reward.getCreatedAt();
        this.updatedAt = reward.getUpdatedAt();
    }
}
