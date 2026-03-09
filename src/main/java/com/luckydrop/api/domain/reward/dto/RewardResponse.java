package com.luckydrop.api.domain.reward.dto;

import com.luckydrop.api.domain.reward.entity.Reward;
import lombok.Getter;

import java.util.List;

@Getter
public class RewardResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final int weight;
    private final Double probability;
    private final Integer stock;
    private final boolean unlimited;
    private final String imageUrl;

    public RewardResponse(Reward reward, int totalWeight) {
        this.id = reward.getId();
        this.name = reward.getName();
        this.description = reward.getDescription();
        this.weight = reward.getWeight();
        this.probability = totalWeight > 0
                ? Math.round((double) reward.getWeight() / totalWeight * 10000.0) / 100.0
                : 0.0;
        this.stock = reward.getStock();
        this.unlimited = reward.isUnlimitedStock();
        this.imageUrl = reward.getImage();
    }

    public static List<RewardResponse> of(List<Reward> rewards) {
        int totalWeight = rewards.stream().mapToInt(Reward::getWeight).sum();
        return rewards.stream()
                .map(r -> new RewardResponse(r, totalWeight))
                .toList();
    }
}
