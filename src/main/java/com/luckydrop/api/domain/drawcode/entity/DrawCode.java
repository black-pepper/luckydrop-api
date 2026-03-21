package com.luckydrop.api.domain.drawcode.entity;

import com.luckydrop.api.domain.content.entity.Content;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "invitation_codes")
@Getter
@NoArgsConstructor
public class DrawCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "allowed_draw_count", nullable = false)
    private int allowedDrawCount;

    @Column(name = "used_draw_count", nullable = false)
    private int usedDrawCount;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public boolean isExpired() {
        return expiresAt != null && OffsetDateTime.now().isAfter(expiresAt);
    }

    public int getRemainingCount() {
        return Math.max(allowedDrawCount - usedDrawCount, 0);
    }

    public boolean hasNoRemaining() {
        return getRemainingCount() <= 0;
    }

    public void use() {
        if (hasNoRemaining()) {
            throw new IllegalStateException("No remaining draws.");
        }
        this.usedDrawCount++;
        this.lastUsedAt = OffsetDateTime.now();
    }
}
