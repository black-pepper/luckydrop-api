package com.luckydrop.api.domain.drawcode.repository;

import com.luckydrop.api.domain.drawcode.entity.DrawCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DrawCodeRepository extends JpaRepository<DrawCode, Long> {

    @Query("SELECT dc FROM DrawCode dc JOIN FETCH dc.participant WHERE dc.code = :code")
    Optional<DrawCode> findByCodeWithParticipant(@Param("code") String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT dc FROM DrawCode dc JOIN FETCH dc.participant WHERE dc.code = :code")
    Optional<DrawCode> findByCodeWithLock(@Param("code") String code);
}
