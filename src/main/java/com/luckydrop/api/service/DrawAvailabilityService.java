package com.luckydrop.api.service;

import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.drawresult.repository.DrawResultRepository;
import com.luckydrop.api.domain.invitationcode.dto.DrawStatus;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.reward.entity.Reward;
import com.luckydrop.api.domain.reward.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DrawAvailabilityService {

    private final RewardRepository rewardRepository;
    private final DrawResultRepository drawResultRepository;

    @Transactional(readOnly = true)
    public DrawStatus getDrawStatus(InvitationCode invitationCode) {
        Content content = invitationCode.getContent();

        if (content.isNotStartedYet()) {
            return DrawStatus.CONTENT_NOT_STARTED;
        }
        if (content.isAlreadyEnded()) {
            return DrawStatus.CONTENT_EXPIRED;
        }
        if (invitationCode.hasNoRemaining()) {
            return DrawStatus.NO_REMAINING;
        }
        if (findAvailableRewards(invitationCode, false).isEmpty()) {
            return DrawStatus.NO_AVAILABLE_REWARD;
        }
        return DrawStatus.DRAWABLE;
    }

    @Transactional(readOnly = true)
    public List<Reward> findAvailableRewards(InvitationCode invitationCode, boolean withLock) {
        Set<Long> drawnRewardIds = drawResultRepository.findRewardIdsByDrawCodeId(invitationCode.getId());
        List<Reward> rewards = withLock
                ? rewardRepository.findAllAvailableByContentIdWithLock(invitationCode.getContent().getId())
                : rewardRepository.findAllAvailableByContentId(invitationCode.getContent().getId());

        return rewards.stream()
                .filter(reward -> reward.isDuplicateAllowed() || !drawnRewardIds.contains(reward.getId()))
                .toList();
    }
}
