package com.luckydrop.api.domain.drawresult.repository;

import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DrawResultRepository extends JpaRepository<DrawResult, Long> {

    @Query("SELECT dr FROM DrawResult dr JOIN FETCH dr.reward WHERE dr.drawCode.id = :drawCodeId ORDER BY dr.drawnAt DESC")
    List<DrawResult> findByDrawCodeIdOrderByDrawnAtDesc(@Param("drawCodeId") Long drawCodeId);

    @Query("SELECT dr FROM DrawResult dr JOIN FETCH dr.reward JOIN FETCH dr.drawCode WHERE dr.participant.id = :participantId ORDER BY dr.drawnAt DESC")
    List<DrawResult> findByParticipantIdOrderByDrawnAtDesc(@Param("participantId") Long participantId);

    @Query("SELECT COALESCE(MAX(dr.drawNo), 0) + 1 FROM DrawResult dr WHERE dr.drawCode.id = :drawCodeId")
    int findNextDrawNo(@Param("drawCodeId") Long drawCodeId);
}
