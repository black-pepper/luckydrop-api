package com.luckydrop.api.domain.drawcode.entity;

import com.luckydrop.api.domain.participant.entity.Participant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "draw_code")
@Getter
@NoArgsConstructor
public class DrawCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "remaining_count", nullable = false)
    private int remainingCount;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasNoRemaining() {
        return remainingCount <= 0;
    }

    public void use() {
        if (hasNoRemaining()) {
            throw new IllegalStateException("남은 횟수가 없습니다.");
        }
        this.remainingCount--;
        this.lastUsedAt = LocalDateTime.now();
    }
}
