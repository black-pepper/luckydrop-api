package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.repository.ContentRepository;
import com.luckydrop.api.domain.reward.dto.AdminRewardResponse;
import com.luckydrop.api.domain.reward.dto.RewardCreateRequest;
import com.luckydrop.api.domain.reward.dto.RewardUpdateRequest;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardAdminServiceTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private RewardAdminService rewardAdminService;

    @Test
    void getRewardsByContentReturnsOwnedRewards() {
        User currentUser = createUser(1L);
        Content content = createContent(10L, currentUser, false);
        Reward reward = createReward(100L, content, true);

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(contentRepository.findByIdWithUser(10L)).thenReturn(Optional.of(content));
        when(rewardRepository.findAllByContentId(10L)).thenReturn(List.of(reward));

        List<AdminRewardResponse> responses = rewardAdminService.getRewardsByContent(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getId()).isEqualTo(100L);
        assertThat(responses.getFirst().isActive()).isTrue();
    }

    @Test
    void getRewardThrowsForbiddenWhenContentOwnerIsDifferent() {
        User currentUser = createUser(1L);
        User otherUser = createUser(2L);
        Content otherContent = createContent(10L, otherUser, false);
        Reward reward = createReward(100L, otherContent, true);

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(rewardRepository.findByIdWithContentAndUser(100L)).thenReturn(Optional.of(reward));

        assertThatThrownBy(() -> rewardAdminService.getReward(100L))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_CONTENT_ACCESS);
    }

    @Test
    void createRewardCreatesRewardForOwnedContent() {
        User currentUser = createUser(1L);
        Content content = createContent(10L, currentUser, false);
        RewardCreateRequest request = new RewardCreateRequest();
        ReflectionTestUtils.setField(request, "contentId", 10L);
        ReflectionTestUtils.setField(request, "name", "1등 경품");
        ReflectionTestUtils.setField(request, "description", "최고 보상");
        ReflectionTestUtils.setField(request, "weight", 10);
        ReflectionTestUtils.setField(request, "stock", 3);
        ReflectionTestUtils.setField(request, "imageUrl", "https://example.com/reward.png");
        ReflectionTestUtils.setField(request, "allowDuplicateReward", true);

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(contentRepository.findByIdWithUser(10L)).thenReturn(Optional.of(content));
        when(rewardRepository.save(any(Reward.class))).thenAnswer(invocation -> {
            Reward savedReward = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedReward, "id", 100L);
            ReflectionTestUtils.setField(savedReward, "createdAt", OffsetDateTime.parse("2026-04-12T10:00:00+09:00"));
            ReflectionTestUtils.setField(savedReward, "updatedAt", OffsetDateTime.parse("2026-04-12T10:00:00+09:00"));
            return savedReward;
        });

        AdminRewardResponse response = rewardAdminService.createReward(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getContentId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("1등 경품");
        assertThat(response.isAllowDuplicateReward()).isTrue();
    }

    @Test
    void updateRewardUpdatesOwnedReward() {
        User currentUser = createUser(1L);
        Content content = createContent(10L, currentUser, false);
        Reward reward = createReward(100L, content, true);
        RewardUpdateRequest request = new RewardUpdateRequest();
        ReflectionTestUtils.setField(request, "name", "수정된 경품");
        ReflectionTestUtils.setField(request, "description", "수정 설명");
        ReflectionTestUtils.setField(request, "weight", 5);
        ReflectionTestUtils.setField(request, "stock", 7);
        ReflectionTestUtils.setField(request, "imageUrl", "https://example.com/updated.png");
        ReflectionTestUtils.setField(request, "allowDuplicateReward", false);

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(rewardRepository.findByIdWithContentAndUser(100L)).thenReturn(Optional.of(reward));

        AdminRewardResponse response = rewardAdminService.updateReward(100L, request);

        assertThat(response.getName()).isEqualTo("수정된 경품");
        assertThat(response.getWeight()).isEqualTo(5);
        assertThat(response.isAllowDuplicateReward()).isFalse();
    }

    @Test
    void deleteRewardDeactivatesOwnedReward() {
        User currentUser = createUser(1L);
        Content content = createContent(10L, currentUser, false);
        Reward reward = createReward(100L, content, true);

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(rewardRepository.findByIdWithContentAndUser(100L)).thenReturn(Optional.of(reward));

        rewardAdminService.deleteReward(100L);

        assertThat(reward.isActive()).isFalse();
    }

    private User createUser(Long id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Content createContent(Long id, User user, boolean deleted) {
        Content content = new Content("CONTENT-001", "DRAW", user, "Lucky Drop", "Event description");
        ReflectionTestUtils.setField(content, "id", id);
        ReflectionTestUtils.setField(content, "createdAt", OffsetDateTime.parse("2026-04-12T09:00:00+09:00"));
        if (deleted) {
            ReflectionTestUtils.setField(content, "deletedAt", OffsetDateTime.parse("2026-04-12T09:30:00+09:00"));
        }
        return content;
    }

    private Reward createReward(Long id, Content content, boolean active) {
        Reward reward = new Reward("기본 경품", "설명", 3, 10, "https://example.com/reward.png", false, content);
        ReflectionTestUtils.setField(reward, "id", id);
        ReflectionTestUtils.setField(reward, "active", active);
        ReflectionTestUtils.setField(reward, "createdAt", OffsetDateTime.parse("2026-04-12T09:10:00+09:00"));
        ReflectionTestUtils.setField(reward, "updatedAt", OffsetDateTime.parse("2026-04-12T09:10:00+09:00"));
        return reward;
    }
}
