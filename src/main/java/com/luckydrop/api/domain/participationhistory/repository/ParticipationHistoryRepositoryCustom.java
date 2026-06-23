package com.luckydrop.api.domain.participationhistory.repository;

import com.luckydrop.api.domain.participationhistory.dto.ParticipationHistorySearchCondition;
import com.luckydrop.api.domain.participationhistory.entity.ParticipationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParticipationHistoryRepositoryCustom {

    Page<ParticipationHistory> searchMyParticipationHistories(
            Long userId,
            ParticipationHistorySearchCondition condition,
            Pageable pageable
    );
}
