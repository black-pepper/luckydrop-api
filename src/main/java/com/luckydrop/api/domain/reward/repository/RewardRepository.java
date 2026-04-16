package com.luckydrop.api.domain.reward.repository;

import com.luckydrop.api.domain.reward.entity.Reward;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    @Query("""
        SELECT r
        FROM Reward r
        JOIN FETCH r.content c
        LEFT JOIN FETCH c.user
        WHERE r.id = :rewardId
          AND r.deletedAt IS NULL
        """)
    Optional<Reward> findByIdWithContentAndUser(@Param("rewardId") Long rewardId);

    @Query("""
        SELECT r
        FROM Reward r
        JOIN FETCH r.content c
        LEFT JOIN FETCH c.user
        WHERE c.id = :contentId
          AND r.deletedAt IS NULL
        ORDER BY r.createdAt DESC, r.id DESC
        """)
    List<Reward> findAllByContentId(@Param("contentId") Long contentId);

    @Query("""
        SELECT r
        FROM Reward r
        WHERE r.content.id = :contentId
          AND r.active = true
          AND r.deletedAt IS NULL
          AND (r.stock IS NULL OR r.stock > 0)
        ORDER BY r.id
        """)
    List<Reward> findAllAvailableByContentId(@Param("contentId") Long contentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reward r
        WHERE r.content.id = :contentId
          AND r.active = true
          AND r.deletedAt IS NULL
          AND (r.stock IS NULL OR r.stock > 0)
        ORDER BY r.id
        """)
    List<Reward> findAllAvailableByContentIdWithLock(@Param("contentId") Long contentId);
}
