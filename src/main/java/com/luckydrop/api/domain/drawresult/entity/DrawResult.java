package com.luckydrop.api.domain.drawresult.entity;

import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.reward.entity.Reward;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "draw_results")
@Getter
@NoArgsConstructor
public class DrawResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_code_id", nullable = false)
    private InvitationCode invitationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @Column(name = "draw_no", nullable = false)
    private int drawNo;

    @Column(name = "drawn_at", nullable = false)
    private OffsetDateTime drawnAt;

    @Builder
    public DrawResult(InvitationCode invitationCode, Content content, Reward reward, int drawNo) {
        this.invitationCode = invitationCode;
        this.content = content;
        this.reward = reward;
        this.drawNo = drawNo;
        this.drawnAt = OffsetDateTime.now();
    }
}
