package com.luckydrop.api.domain.reward.repository;

import com.luckydrop.api.domain.reward.entity.Reward;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    @Query("SELECT r FROM Reward r WHERE r.active = true AND (r.stock IS NULL OR r.stock > 0)")
    List<Reward> findAllAvailable();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reward r WHERE r.active = true AND (r.stock IS NULL OR r.stock > 0)")
    List<Reward> findAllAvailableWithLock();
}
