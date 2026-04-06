package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.drawcode.entity.DrawCode;
import com.luckydrop.api.domain.drawcode.repository.DrawCodeRepository;
import com.luckydrop.api.domain.drawresult.dto.DrawRequest;
import com.luckydrop.api.domain.drawresult.dto.DrawResponse;
import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import com.luckydrop.api.domain.drawresult.repository.DrawResultRepository;
import com.luckydrop.api.domain.reward.entity.Reward;
import com.luckydrop.api.domain.reward.repository.RewardRepository;
import com.luckydrop.api.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DrawServiceTest {

    @Mock
    private DrawCodeRepository drawCodeRepository;

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private DrawResultRepository drawResultRepository;

    @InjectMocks
    private DrawService drawService;

    @Test
    void drawSucceedsWhenContentCodeAndInvitationCodeMatch() {
        DrawRequest request = createDrawRequest("CONTENT-001", "INVITE-001");
        Content content = createContent("CONTENT-001");
        DrawCode drawCode = createDrawCode("INVITE-001", content);
        Reward reward = createReward(content);

        when(drawCodeRepository.findByContentCodeAndCodeWithLock("CONTENT-001", "INVITE-001"))
                .thenReturn(Optional.of(drawCode));
        when(drawResultRepository.findRewardIdsByDrawCodeId(1L)).thenReturn(Set.of());
        when(rewardRepository.findAllAvailableByContentIdWithLock(10L)).thenReturn(List.of(reward));
        when(drawResultRepository.findNextDrawNo(1L)).thenReturn(1);

        DrawResponse response = drawService.draw(request);

        assertThat(response.getRewardName()).isEqualTo("Coffee Coupon");
        assertThat(response.getDrawNo()).isEqualTo(1);
        assertThat(response.getRemainingCount()).isEqualTo(2);

        ArgumentCaptor<DrawResult> captor = ArgumentCaptor.forClass(DrawResult.class);
        verify(drawResultRepository).save(captor.capture());
        assertThat(captor.getValue().getContent().getCode()).isEqualTo("CONTENT-001");
    }

    @Test
    void drawThrowsWhenInvitationCodeDoesNotBelongToContent() {
        DrawRequest request = createDrawRequest("CONTENT-001", "INVITE-002");
        when(drawCodeRepository.findByContentCodeAndCodeWithLock("CONTENT-001", "INVITE-002"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> drawService.draw(request))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CODE_NOT_FOUND);
    }

    private DrawRequest createDrawRequest(String contentCode, String invitationCode) {
        DrawRequest request = new DrawRequest();
        ReflectionTestUtils.setField(request, "contentCode", contentCode);
        ReflectionTestUtils.setField(request, "invitationCode", invitationCode);
        return request;
    }

    private Content createContent(String code) {
        Content content = new Content(code, "DRAW", new User(), "Lucky Drop", "Event description");
        ReflectionTestUtils.setField(content, "id", 10L);
        ReflectionTestUtils.setField(content, "createdAt", OffsetDateTime.parse("2026-04-06T10:15:30+09:00"));
        return content;
    }

    private DrawCode createDrawCode(String code, Content content) {
        DrawCode drawCode = new DrawCode();
        ReflectionTestUtils.setField(drawCode, "id", 1L);
        ReflectionTestUtils.setField(drawCode, "code", code);
        ReflectionTestUtils.setField(drawCode, "content", content);
        ReflectionTestUtils.setField(drawCode, "allowedDrawCount", 3);
        ReflectionTestUtils.setField(drawCode, "usedDrawCount", 0);
        ReflectionTestUtils.setField(drawCode, "active", true);
        return drawCode;
    }

    private Reward createReward(Content content) {
        Reward reward = new Reward();
        ReflectionTestUtils.setField(reward, "id", 100L);
        ReflectionTestUtils.setField(reward, "name", "Coffee Coupon");
        ReflectionTestUtils.setField(reward, "weight", 10);
        ReflectionTestUtils.setField(reward, "content", content);
        ReflectionTestUtils.setField(reward, "stock", 5);
        ReflectionTestUtils.setField(reward, "image", "https://example.com/coffee.png");
        ReflectionTestUtils.setField(reward, "allowDuplicateReward", true);
        return reward;
    }
}
