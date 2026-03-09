package com.luckydrop.api.service;

import com.luckydrop.api.domain.reward.dto.RewardResponse;
import com.luckydrop.api.domain.reward.entity.Reward;
import com.luckydrop.api.domain.reward.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;

    @Transactional(readOnly = true)
    public List<RewardResponse> getAvailableRewards() {
        List<Reward> rewards = rewardRepository.findAllAvailable();
        return RewardResponse.of(rewards);
    }
}
