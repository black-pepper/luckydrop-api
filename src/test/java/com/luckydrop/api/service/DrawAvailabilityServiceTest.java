package com.luckydrop.api.service;

import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.entity.ContentType;
import com.luckydrop.api.domain.drawresult.repository.DrawResultRepository;
import com.luckydrop.api.domain.invitationcode.dto.DrawStatus;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.reward.entity.Reward;
import com.luckydrop.api.domain.reward.repository.RewardRepository;
import com.luckydrop.api.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DrawAvailabilityServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private DrawResultRepository drawResultRepository;

    @InjectMocks
    private DrawAvailabilityService drawAvailabilityService;

    @Test
    void returnsContentNotStartedWhenContentHasNotStarted() {
        Content content = createContent("CONTENT-001");
        ReflectionTestUtils.setField(content, "startAt", OffsetDateTime.now().plusDays(1));
        InvitationCode invitationCode = createInvitationCode(content, 3, 0);

        assertThat(drawAvailabilityService.getDrawStatus(invitationCode))
                .isEqualTo(DrawStatus.CONTENT_NOT_STARTED);
    }

    @Test
    void returnsContentExpiredWhenContentEnded() {
        Content content = createContent("CONTENT-001");
        ReflectionTestUtils.setField(content, "endAt", OffsetDateTime.now().minusDays(1));
        InvitationCode invitationCode = createInvitationCode(content, 3, 0);

        assertThat(drawAvailabilityService.getDrawStatus(invitationCode))
                .isEqualTo(DrawStatus.CONTENT_EXPIRED);
    }

    @Test
    void returnsNoRemainingBeforeCheckingRewards() {
        Content content = createContent("CONTENT-001");
        InvitationCode invitationCode = createInvitationCode(content, 3, 3);

        assertThat(drawAvailabilityService.getDrawStatus(invitationCode))
                .isEqualTo(DrawStatus.NO_REMAINING);
    }

    @Test
    void returnsNoAvailableRewardWhenNoCandidateExists() {
        Content content = createContent("CONTENT-001");
        InvitationCode invitationCode = createInvitationCode(content, 3, 0);

        when(drawResultRepository.findRewardIdsByDrawCodeId(1L)).thenReturn(Set.of());
        when(rewardRepository.findAllAvailableByContentId(10L)).thenReturn(List.of());

        assertThat(drawAvailabilityService.getDrawStatus(invitationCode))
                .isEqualTo(DrawStatus.NO_AVAILABLE_REWARD);
    }

    @Test
    void returnsDrawableWhenRewardCandidateExists() {
        Content content = createContent("CONTENT-001");
        InvitationCode invitationCode = createInvitationCode(content, 3, 0);
        Reward reward = createReward(content, 100L, true);

        when(drawResultRepository.findRewardIdsByDrawCodeId(1L)).thenReturn(Set.of());
        when(rewardRepository.findAllAvailableByContentId(10L)).thenReturn(List.of(reward));

        assertThat(drawAvailabilityService.getDrawStatus(invitationCode))
                .isEqualTo(DrawStatus.DRAWABLE);
    }

    @Test
    void excludesAlreadyDrawnRewardWhenDuplicateNotAllowed() {
        Content content = createContent("CONTENT-001");
        InvitationCode invitationCode = createInvitationCode(content, 3, 0);
        Reward reward = createReward(content, 100L, false);

        when(drawResultRepository.findRewardIdsByDrawCodeId(1L)).thenReturn(Set.of(100L));
        when(rewardRepository.findAllAvailableByContentId(10L)).thenReturn(List.of(reward));

        assertThat(drawAvailabilityService.getDrawStatus(invitationCode))
                .isEqualTo(DrawStatus.NO_AVAILABLE_REWARD);
    }

    private Content createContent(String code) {
        Content content = new Content(code, ContentType.DRAW, new User(), "Lucky Drop", "Event description");
        ReflectionTestUtils.setField(content, "id", 10L);
        return content;
    }

    private InvitationCode createInvitationCode(Content content, int allowedDrawCount, int usedDrawCount) {
        InvitationCode invitationCode = new InvitationCode();
        ReflectionTestUtils.setField(invitationCode, "id", 1L);
        ReflectionTestUtils.setField(invitationCode, "code", "INVITE-001");
        ReflectionTestUtils.setField(invitationCode, "content", content);
        ReflectionTestUtils.setField(invitationCode, "allowedDrawCount", allowedDrawCount);
        ReflectionTestUtils.setField(invitationCode, "usedDrawCount", usedDrawCount);
        ReflectionTestUtils.setField(invitationCode, "active", true);
        return invitationCode;
    }

    private Reward createReward(Content content, Long id, boolean allowDuplicateReward) {
        Reward reward = new Reward();
        ReflectionTestUtils.setField(reward, "id", id);
        ReflectionTestUtils.setField(reward, "name", "Coffee Coupon");
        ReflectionTestUtils.setField(reward, "weight", 10);
        ReflectionTestUtils.setField(reward, "content", content);
        ReflectionTestUtils.setField(reward, "stock", 5);
        ReflectionTestUtils.setField(reward, "allowDuplicateReward", allowDuplicateReward);
        return reward;
    }
}
