package com.luckydrop.api.domain.participationhistory.repository;

import com.luckydrop.api.domain.participationhistory.entity.ParticipationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipationHistoryRepository extends JpaRepository<ParticipationHistory, Long>, ParticipationHistoryRepositoryCustom {

    Optional<ParticipationHistory> findByUserIdAndContentIdAndInvitationCode(
            Long userId,
            Long contentId,
            String invitationCode
    );
}
