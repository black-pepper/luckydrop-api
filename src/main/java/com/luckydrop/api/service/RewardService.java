package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.drawcode.entity.DrawCode;
import com.luckydrop.api.domain.drawcode.repository.DrawCodeRepository;
import com.luckydrop.api.domain.drawresult.repository.DrawResultRepository;
import com.luckydrop.api.domain.reward.dto.RewardResponse;
import com.luckydrop.api.domain.reward.entity.Reward;
import com.luckydrop.api.domain.reward.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final DrawCodeRepository drawCodeRepository;
    private final RewardRepository rewardRepository;
    private final DrawResultRepository drawResultRepository;

    @Transactional(readOnly = true)
    public List<RewardResponse> getAvailableRewards(String code) {
        DrawCode drawCode = drawCodeRepository.findByCodeWithContent(code)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CODE_NOT_FOUND));

        Set<Long> drawnRewardIds = drawResultRepository.findRewardIdsByDrawCodeId(drawCode.getId());
        List<Reward> rewards = rewardRepository.findAllAvailableByContentId(drawCode.getContent().getId())
                .stream()
                .filter(reward -> reward.isDuplicateAllowed() || !drawnRewardIds.contains(reward.getId()))
                .toList();

        return RewardResponse.of(rewards);
    }
}
