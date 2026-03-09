package com.luckydrop.api.domain.reward.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward")
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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isUnlimitedStock() {
        return this.stock == null;
    }

    public void decreaseStock() {
        if (this.stock != null) {
            if (this.stock <= 0) {
                throw new IllegalStateException("재고가 없습니다.");
            }
            this.stock--;
        }
    }
}
