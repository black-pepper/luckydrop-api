package com.luckydrop.api.domain.reward.repository;

import com.luckydrop.api.domain.reward.entity.Reward;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    @Query("""
        SELECT r
        FROM Reward r
        WHERE r.content.id = :contentId
          AND r.active = true
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
          AND (r.stock IS NULL OR r.stock > 0)
        ORDER BY r.id
        """)
    List<Reward> findAllAvailableByContentIdWithLock(@Param("contentId") Long contentId);
}
