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
        WHERE dr.drawCode.id = :drawCodeId
        ORDER BY dr.drawnAt DESC
        """)
    List<DrawResult> findByDrawCodeIdOrderByDrawnAtDesc(@Param("drawCodeId") Long drawCodeId);

    @Query("SELECT COALESCE(MAX(dr.drawNo), 0) + 1 FROM DrawResult dr WHERE dr.drawCode.id = :drawCodeId")
    int findNextDrawNo(@Param("drawCodeId") Long drawCodeId);

    @Query("SELECT DISTINCT dr.reward.id FROM DrawResult dr WHERE dr.drawCode.id = :drawCodeId")
    Set<Long> findRewardIdsByDrawCodeId(@Param("drawCodeId") Long drawCodeId);
}
