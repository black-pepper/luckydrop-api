package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.repository.ContentRepository;
import com.luckydrop.api.domain.reward.dto.ManagerRewardResponse;
import com.luckydrop.api.domain.reward.dto.RewardCreateRequest;
import com.luckydrop.api.domain.reward.dto.RewardUpdateRequest;
import com.luckydrop.api.domain.reward.entity.Reward;
import com.luckydrop.api.domain.reward.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerRewardService {

    private final RewardRepository rewardRepository;
    private final ContentRepository contentRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<ManagerRewardResponse> getRewardsByContent(String contentCode) {
        Content content = getOwnedContent(contentCode);
        return rewardRepository.findAllByContentId(content.getId())
                .stream()
                .map(ManagerRewardResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ManagerRewardResponse getReward(Long rewardId) {
        return new ManagerRewardResponse(getOwnedReward(rewardId));
    }

    @Transactional
    public ManagerRewardResponse createReward(RewardCreateRequest request) {
        Content content = getOwnedContent(request.getContentCode());
        Reward reward = new Reward(
                request.getName(),
                request.getDescription(),
                request.getWeight(),
                request.getStock(),
                request.getImageUrl(),
                request.getAllowDuplicateReward(),
                content
        );
        return new ManagerRewardResponse(rewardRepository.save(reward));
    }

    @Transactional
    public ManagerRewardResponse updateReward(Long rewardId, RewardUpdateRequest request) {
        Reward reward = getOwnedReward(rewardId);
        reward.update(
                request.getName(),
                request.getDescription(),
                request.getWeight(),
                request.getStock(),
                request.getImageUrl(),
                request.getAllowDuplicateReward()
        );
        return new ManagerRewardResponse(reward);
    }

    @Transactional
    public void deleteReward(Long rewardId) {
        Reward reward = getOwnedReward(rewardId);
        reward.delete();
    }

    private Content getOwnedContent(String contentCode) {
        Long currentUserId = currentUserService.getCurrentUserEntity().getId();
        return contentRepository.findByCodeWithUser(contentCode)
                .map(content -> {
                    if (content.isDeleted()) {
                        throw new DrawEventException(ErrorCode.CONTENT_NOT_FOUND);
                    }
                    if (content.getUser() == null || !content.getUser().getId().equals(currentUserId)) {
                        throw new DrawEventException(ErrorCode.FORBIDDEN_CONTENT_ACCESS);
                    }
                    return content;
                })
                .orElseThrow(() -> new DrawEventException(ErrorCode.CONTENT_NOT_FOUND));
    }

    private Reward getOwnedReward(Long rewardId) {
        Long currentUserId = currentUserService.getCurrentUserEntity().getId();
        return rewardRepository.findByIdWithContentAndUser(rewardId)
                .map(reward -> {
                    if (reward.isDeleted()) {
                        throw new DrawEventException(ErrorCode.REWARD_NOT_FOUND);
                    }
                    if (reward.getContent() == null || reward.getContent().isDeleted()) {
                        throw new DrawEventException(ErrorCode.CONTENT_NOT_FOUND);
                    }
                    if (reward.getContent().getUser() == null
                            || !reward.getContent().getUser().getId().equals(currentUserId)) {
                        throw new DrawEventException(ErrorCode.FORBIDDEN_CONTENT_ACCESS);
                    }
                    return reward;
                })
                .orElseThrow(() -> new DrawEventException(ErrorCode.REWARD_NOT_FOUND));
    }
}
