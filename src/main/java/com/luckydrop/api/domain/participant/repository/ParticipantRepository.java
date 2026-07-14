package com.luckydrop.api.domain.participant.repository;

import com.luckydrop.api.domain.participant.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("""
            SELECT pc
            FROM Participant pc
            JOIN FETCH pc.user u
            WHERE u.id = :userId
              AND pc.deletedAt IS NULL
            ORDER BY pc.createdAt DESC
            """)
    List<Participant> findAllByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT pc
            FROM Participant pc
            JOIN FETCH pc.user u
            WHERE pc.id = :participantId
              AND pc.deletedAt IS NULL
            """)
    Optional<Participant> findByIdWithUser(@Param("participantId") Long participantId);
}
