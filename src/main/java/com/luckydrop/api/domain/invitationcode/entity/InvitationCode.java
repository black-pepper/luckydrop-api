package com.luckydrop.api.domain.invitationcode.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "invitation_codes",
        uniqueConstraints = @UniqueConstraint(name = "invitation_codes_content_id_code_key", columnNames = {"content_id", "code"})
)
@Getter
@NoArgsConstructor
public class InvitationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
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

    public InvitationCode(String code, String name, Content content, int allowedDrawCount, OffsetDateTime expiresAt) {
        this.code = code;
        this.name = name;
        this.content = content;
        this.allowedDrawCount = allowedDrawCount;
        this.expiresAt = expiresAt;
        this.active = true;
        this.usedDrawCount = 0;
    }

    public void update(String name, int allowedDrawCount, OffsetDateTime expiresAt, boolean active) {
        this.name = name;
        this.allowedDrawCount = allowedDrawCount;
        this.expiresAt = expiresAt;
        this.active = active;
    }

    public void delete() {
        this.active = false;
    }

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
