package com.luckydrop.api.domain.participationhistory.entity;

import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "participation_histories",
        uniqueConstraints = @UniqueConstraint(
                name = "participation_histories_user_id_content_id_invitation_code_key",
                columnNames = {"user_id", "content_id", "invitation_code"}
        )
)
@Getter
@NoArgsConstructor
public class ParticipationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "invitation_code", nullable = false, length = 100)
    private String invitationCode;

    @Column(name = "accessed_at", nullable = false)
    private OffsetDateTime accessedAt;

    public ParticipationHistory(User user, Content content, String invitationCode) {
        this.user = user;
        this.content = content;
        this.invitationCode = invitationCode;
        this.accessedAt = OffsetDateTime.now();
    }

    public void updateAccessedAt() {
        this.accessedAt = OffsetDateTime.now();
    }
}
