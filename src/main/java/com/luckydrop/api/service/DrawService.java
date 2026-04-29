package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.invitationcode.repository.InvitationCodeRepository;
import com.luckydrop.api.domain.drawresult.dto.DrawRequest;
import com.luckydrop.api.domain.drawresult.dto.DrawResponse;
import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import com.luckydrop.api.domain.drawresult.repository.DrawResultRepository;
import com.luckydrop.api.domain.reward.entity.Reward;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrawService {

    private final InvitationCodeRepository invitationCodeRepository;
    private final DrawResultRepository drawResultRepository;
    private final DrawAvailabilityService drawAvailabilityService;
    private final Random random = new Random();

    @Transactional
    public DrawResponse draw(DrawRequest request) {
        InvitationCode invitationCode = invitationCodeRepository.findByContentCodeAndCodeWithLock(
                        request.getContentCode(),
                        request.getInvitationCode()
                )
                .orElseThrow(() -> new DrawEventException(ErrorCode.CODE_NOT_FOUND));

        validateInvitationCode(invitationCode);

        List<Reward> availableRewards = drawAvailabilityService.findAvailableRewards(invitationCode, true);

        if (availableRewards.isEmpty()) {
            throw new DrawEventException(ErrorCode.NO_AVAILABLE_REWARD);
        }

        Reward selectedReward = selectRewardByWeight(availableRewards);
        selectedReward.decreaseStock();

        int drawNo = drawResultRepository.findNextDrawNo(invitationCode.getId());

        DrawResult result = DrawResult.builder()
                .invitationCode(invitationCode)
                .content(invitationCode.getContent())
                .reward(selectedReward)
                .drawNo(drawNo)
                .build();
        drawResultRepository.save(result);

        invitationCode.use();

        log.info(
                "Draw completed - contentCode: {}, invitationCode: {}, reward: {}, drawNo: {}",
                request.getContentCode(),
                request.getInvitationCode(),
                selectedReward.getName(),
                drawNo
        );

        return new DrawResponse(result, invitationCode.getRemainingCount());
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

    private void validateInvitationCode(InvitationCode invitationCode) {
        if (!invitationCode.isActive()) {
            throw new DrawEventException(ErrorCode.CODE_INACTIVE);
        }
        if (invitationCode.isExpired()) {
            throw new DrawEventException(ErrorCode.CODE_EXPIRED);
        }
        if (invitationCode.getContent().isDeleted()) {
            throw new DrawEventException(ErrorCode.CODE_INACTIVE);
        }
        if (invitationCode.hasNoRemaining()) {
            throw new DrawEventException(ErrorCode.CODE_NO_REMAINING);
        }
        validateContentPeriod(invitationCode.getContent());
    }

    private void validateContentPeriod(Content content) {
        if (content.isNotStartedYet()) {
            throw new DrawEventException(ErrorCode.CONTENT_NOT_STARTED);
        }
        if (content.isAlreadyEnded()) {
            throw new DrawEventException(ErrorCode.CONTENT_EXPIRED);
        }
    }
}
