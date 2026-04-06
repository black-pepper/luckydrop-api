package com.luckydrop.api.domain.drawcode.repository;

import com.luckydrop.api.domain.drawcode.entity.DrawCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DrawCodeRepository extends JpaRepository<DrawCode, Long> {

    @Query("""
            SELECT dc
            FROM DrawCode dc
            JOIN FETCH dc.content c
            WHERE c.code = :contentCode
              AND dc.code = :invitationCode
            """)
    Optional<DrawCode> findByContentCodeAndCodeWithContent(
            @Param("contentCode") String contentCode,
            @Param("invitationCode") String invitationCode
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT dc
            FROM DrawCode dc
            JOIN FETCH dc.content c
            WHERE c.code = :contentCode
              AND dc.code = :invitationCode
            """)
    Optional<DrawCode> findByContentCodeAndCodeWithLock(
            @Param("contentCode") String contentCode,
            @Param("invitationCode") String invitationCode
    );
}
