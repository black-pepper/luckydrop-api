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

@Slf4j
@Service
@RequiredArgsConstructor
public class DrawService {

    private final DrawCodeRepository drawCodeRepository;
    private final RewardRepository rewardRepository;
    private final DrawResultRepository drawResultRepository;
    private final Random random = new Random();

    /**
     * 뽑기 실행
     * 동시성: DrawCode + Reward 모두 PESSIMISTIC_WRITE 락
     * 트랜잭션: draw_result 저장 + remaining_count 차감 + stock 차감 원자 처리
     */
    @Transactional
    public DrawResponse draw(DrawRequest request) {
        // 1. 코드 비관적 락 조회
        DrawCode drawCode = drawCodeRepository.findByCodeWithLock(request.getCode())
                .orElseThrow(() -> new DrawEventException(ErrorCode.CODE_NOT_FOUND));

        // 2. 유효성 검증
        validateDrawCode(drawCode);

        // 3. 보상 목록 비관적 락 조회
        List<Reward> availableRewards = rewardRepository.findAllAvailableWithLock();
        if (availableRewards.isEmpty()) {
            throw new DrawEventException(ErrorCode.NO_AVAILABLE_REWARD);
        }

        // 4. weight 기반 보상 선택
        Reward selectedReward = selectRewardByWeight(availableRewards);

        // 5. 재고 차감
        selectedReward.decreaseStock();

        // 6. draw_no 계산
        int drawNo = drawResultRepository.findNextDrawNo(drawCode.getId());

        // 7. 결과 저장
        DrawResult result = DrawResult.builder()
                .drawCode(drawCode)
                .participant(drawCode.getParticipant())
                .reward(selectedReward)
                .rewardNameSnapshot(selectedReward.getName())
                .drawNo(drawNo)
                .build();
        drawResultRepository.save(result);

        // 8. 남은 횟수 차감
        drawCode.use();

        log.info("뽑기 완료 - code: {}, reward: {}, drawNo: {}", request.getCode(), selectedReward.getName(), drawNo);

        return new DrawResponse(result, drawCode.getRemainingCount());
    }

    /**
     * weight 기반 상대 확률 선택
     * 예) A(5), B(3), C(2) → 총합 10 → 0~4:A, 5~7:B, 8~9:C
     */
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
        if (drawCode.getParticipant().isInactive()) {
            throw new DrawEventException(ErrorCode.PARTICIPANT_INACTIVE);
        }
        if (drawCode.isExpired()) {
            throw new DrawEventException(ErrorCode.CODE_EXPIRED);
        }
        if (drawCode.hasNoRemaining()) {
            throw new DrawEventException(ErrorCode.CODE_NO_REMAINING);
        }
    }
}
