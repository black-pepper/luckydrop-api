package com.luckydrop.api.domain.reward.dto;

import com.luckydrop.api.domain.reward.entity.Reward;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class AdminRewardResponse {

    private final Long id;
    private final Long contentId;
    private final String name;
    private final String description;
    private final int weight;
    private final Integer stock;
    private final boolean unlimited;
    private final String imageUrl;
    private final boolean active;
    private final boolean allowDuplicateReward;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public AdminRewardResponse(Reward reward) {
        this.id = reward.getId();
        this.contentId = reward.getContent().getId();
        this.name = reward.getName();
        this.description = reward.getDescription();
        this.weight = reward.getWeight();
        this.stock = reward.getStock();
        this.unlimited = reward.isUnlimitedStock();
        this.imageUrl = reward.getImage();
        this.active = reward.isActive();
        this.allowDuplicateReward = reward.isDuplicateAllowed();
        this.createdAt = reward.getCreatedAt();
        this.updatedAt = reward.getUpdatedAt();
    }
}
