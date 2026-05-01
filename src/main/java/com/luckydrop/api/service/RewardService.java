package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.invitationcode.repository.InvitationCodeRepository;
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

    private final InvitationCodeRepository invitationCodeRepository;
    private final RewardRepository rewardRepository;
    private final DrawResultRepository drawResultRepository;

    @Transactional(readOnly = true)
    public List<RewardResponse> getAvailableRewards(String contentCode, String invitationCode) {
        InvitationCode code = invitationCodeRepository.findByContentCodeAndCodeWithContent(contentCode, invitationCode)
                .orElseThrow(() -> new DrawEventException(ErrorCode.CODE_NOT_FOUND));

        Set<Long> drawnRewardIds = drawResultRepository.findRewardIdsByDrawCodeId(code.getId());
        List<Reward> rewards = rewardRepository.findAllAvailableByContentId(code.getContent().getId())
                .stream()
                .filter(reward -> reward.isDuplicateAllowed() || !drawnRewardIds.contains(reward.getId()))
                .toList();

        return RewardResponse.of(rewards);
    }
}
