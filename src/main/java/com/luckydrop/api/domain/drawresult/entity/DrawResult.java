package com.luckydrop.api.domain.drawresult.entity;

import com.luckydrop.api.domain.drawcode.entity.DrawCode;
import com.luckydrop.api.domain.participant.entity.Participant;
import com.luckydrop.api.domain.reward.entity.Reward;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "draw_result")
@Getter
@NoArgsConstructor
public class DrawResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draw_code_id", nullable = false)
    private DrawCode drawCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @Column(name = "reward_name_snapshot", nullable = false, length = 200)
    private String rewardNameSnapshot;

    @Column(name = "draw_no", nullable = false)
    private int drawNo;

    @Column(name = "drawn_at", nullable = false)
    private LocalDateTime drawnAt;

    @Builder
    public DrawResult(DrawCode drawCode, Participant participant, Reward reward,
                      String rewardNameSnapshot, int drawNo) {
        this.drawCode = drawCode;
        this.participant = participant;
        this.reward = reward;
        this.rewardNameSnapshot = rewardNameSnapshot;
        this.drawNo = drawNo;
        this.drawnAt = LocalDateTime.now();
    }
}
