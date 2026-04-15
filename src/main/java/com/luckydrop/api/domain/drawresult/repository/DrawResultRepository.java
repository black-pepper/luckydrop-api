package com.luckydrop.api.domain.drawresult.repository;

import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface DrawResultRepository extends JpaRepository<DrawResult, Long> {

    @Query("""
        SELECT dr
        FROM DrawResult dr
        JOIN FETCH dr.reward
        JOIN FETCH dr.invitationCode
        WHERE dr.content.code = :contentCode
        ORDER BY dr.drawnAt DESC
        """)
    List<DrawResult> findAllByContentCodeOrderByDrawnAtDesc(@Param("contentCode") String contentCode);

    @Query("""
        SELECT dr
        FROM DrawResult dr
        JOIN FETCH dr.reward
        WHERE dr.invitationCode.id = :drawCodeId
        ORDER BY dr.drawnAt DESC
        """)
    List<DrawResult> findByDrawCodeIdOrderByDrawnAtDesc(@Param("drawCodeId") Long drawCodeId);

    @Query("SELECT COALESCE(MAX(dr.drawNo), 0) + 1 FROM DrawResult dr WHERE dr.invitationCode.id = :drawCodeId")
    int findNextDrawNo(@Param("drawCodeId") Long drawCodeId);

    @Query("SELECT DISTINCT dr.reward.id FROM DrawResult dr WHERE dr.invitationCode.id = :drawCodeId")
    Set<Long> findRewardIdsByDrawCodeId(@Param("drawCodeId") Long drawCodeId);
}
