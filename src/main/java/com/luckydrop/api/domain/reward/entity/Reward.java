package com.luckydrop.api.domain.reward.entity;

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
@Table(name = "rewards")
@Getter
@NoArgsConstructor
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int weight;

    @Column
    private Integer stock;

    @Column(length = 500)
    private String image;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "allow_duplicate_reward")
    private Boolean allowDuplicateReward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public boolean isUnlimitedStock() {
        return this.stock == null;
    }

    public boolean isDuplicateAllowed() {
        return Boolean.TRUE.equals(this.allowDuplicateReward);
    }

    public void decreaseStock() {
        if (this.stock != null) {
            if (this.stock <= 0) {
                throw new IllegalStateException("Out of stock.");
            }
            this.stock--;
        }
    }
}
