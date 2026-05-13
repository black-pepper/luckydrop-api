package com.luckydrop.api.domain.reward.dto;

import com.luckydrop.api.domain.reward.entity.Reward;
import lombok.Getter;

import java.util.List;

@Getter
public class RewardResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final Integer weight;
    private final Integer poolCount;
    private final Double probability;
    private final Integer stock;
    private final boolean unlimited;
    private final String imageUrl;

    public RewardResponse(Reward reward, int totalWeight) {
        this.id = reward.getId();
        this.name = reward.getName();
        this.description = reward.getDescription();
        this.weight = reward.getWeight();
        this.poolCount = reward.getPoolCount();
        this.probability = totalWeight > 0
                ? Math.round((double) reward.getEffectiveWeight() / totalWeight * 10000.0) / 100.0
                : 0.0;
        this.stock = reward.getStock();
        this.unlimited = reward.isUnlimitedStock();
        this.imageUrl = reward.getImage();
    }

    public static List<RewardResponse> of(List<Reward> rewards) {
        int total = rewards.stream().mapToInt(Reward::getEffectiveWeight).sum();
        return rewards.stream()
                .map(r -> new RewardResponse(r, total))
                .toList();
    }
}
