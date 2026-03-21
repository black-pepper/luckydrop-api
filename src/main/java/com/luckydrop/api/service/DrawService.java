package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.drawcode.entity.DrawCode;
import com.luckydrop.api.domain.drawcode.repository.DrawCodeRepository;
import com.luckydrop.api.domain.drawresult.dto.DrawRequest;
import com.luckydrop.api.domain.drawresult.dto.DrawResponse;
import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import com.luckydrop.api.domain.drawresult.repository.DrawResultRepository;
import com.luckydrop.api.domain.reward.entity.Reward;
import com.luckydrop.api.domain.reward.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrawService {

    private final DrawCodeRepository drawCodeRepository;
    private final RewardRepository rewardRepository;
    private final DrawResultRepository drawResultRepository;
    private final Random random = new Random();

    @Transactional
    public DrawResponse draw(DrawRequest request) {
        DrawCode drawCode = drawCodeRepository.findByCodeWithLock(request.getCode())
                .orElseThrow(() -> new DrawEventException(ErrorCode.CODE_NOT_FOUND));

        validateDrawCode(drawCode);

        Set<Long> drawnRewardIds = drawResultRepository.findRewardIdsByDrawCodeId(drawCode.getId());
        List<Reward> availableRewards = rewardRepository.findAllAvailableByContentIdWithLock(drawCode.getContent().getId())
                .stream()
                .filter(reward -> reward.isDuplicateAllowed() || !drawnRewardIds.contains(reward.getId()))
                .toList();

        if (availableRewards.isEmpty()) {
            throw new DrawEventException(ErrorCode.NO_AVAILABLE_REWARD);
        }

        Reward selectedReward = selectRewardByWeight(availableRewards);
        selectedReward.decreaseStock();

        int drawNo = drawResultRepository.findNextDrawNo(drawCode.getId());

        DrawResult result = DrawResult.builder()
                .drawCode(drawCode)
                .content(drawCode.getContent())
                .reward(selectedReward)
                .drawNo(drawNo)
                .build();
        drawResultRepository.save(result);

        drawCode.use();

        log.info("Draw completed - code: {}, reward: {}, drawNo: {}", request.getCode(), selectedReward.getName(), drawNo);

        return new DrawResponse(result, drawCode.getRemainingCount());
    }

    private Reward selectRewardByWeight(List<Reward> rewards) {
        int totalWeight = rewards.stream().mapToInt(Reward::getWeight).sum();
        int pick = random.nextInt(totalWeight);
        int cumulative = 0;
        for (Reward reward : rewards) {
            cumulative += reward.getWeight();
            if (pick < cumulative) {
                return reward;
            }
        }
        return rewards.get(rewards.size() - 1);
    }

    private void validateDrawCode(DrawCode drawCode) {
        if (!drawCode.isActive()) {
            throw new DrawEventException(ErrorCode.CODE_INACTIVE);
        }
        if (drawCode.isExpired()) {
            throw new DrawEventException(ErrorCode.CODE_EXPIRED);
        }
        if (drawCode.getContent().isDeleted()) {
            throw new DrawEventException(ErrorCode.CODE_INACTIVE);
        }
        if (drawCode.hasNoRemaining()) {
            throw new DrawEventException(ErrorCode.CODE_NO_REMAINING);
        }
    }
}
