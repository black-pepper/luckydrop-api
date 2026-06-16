package com.luckydrop.api.domain.participationhistory.repository;

import com.luckydrop.api.domain.content.entity.QContent;
import com.luckydrop.api.domain.participationhistory.dto.ParticipationContentStatus;
import com.luckydrop.api.domain.participationhistory.dto.ParticipationHistorySearchCondition;
import com.luckydrop.api.domain.participationhistory.entity.ParticipationHistory;
import com.luckydrop.api.domain.participationhistory.entity.QParticipationHistory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ParticipationHistoryRepositoryImpl implements ParticipationHistoryRepositoryCustom {

    private static final QParticipationHistory participationHistory = QParticipationHistory.participationHistory;
    private static final QContent content = QContent.content;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ParticipationHistory> searchMyParticipationHistories(
            Long userId,
            ParticipationHistorySearchCondition condition,
            Pageable pageable
    ) {
        OffsetDateTime now = OffsetDateTime.now();

        List<ParticipationHistory> items = queryFactory
                .selectFrom(participationHistory)
                .join(participationHistory.content, content).fetchJoin()
                .where(
                        participationHistory.user.id.eq(userId),
                        contentTitleContains(condition.getKeyword()),
                        contentStatusEq(condition.getStatus(), now)
                )
                .orderBy(participationHistory.accessedAt.desc(), participationHistory.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(participationHistory.count())
                .from(participationHistory)
                .join(participationHistory.content, content)
                .where(
                        participationHistory.user.id.eq(userId),
                        contentTitleContains(condition.getKeyword()),
                        contentStatusEq(condition.getStatus(), now)
                )
                .fetchOne();

        return new PageImpl<>(items, pageable, total != null ? total : 0L);
    }

    private BooleanExpression contentTitleContains(String keyword) {
        return StringUtils.hasText(keyword) ? content.title.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression contentStatusEq(ParticipationContentStatus status, OffsetDateTime now) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case ACTIVE -> content.deletedAt.isNull()
                    .and(content.startAt.isNull().or(content.startAt.loe(now)))
                    .and(content.endAt.isNull().or(content.endAt.goe(now)));
            case SCHEDULED -> content.deletedAt.isNull()
                    .and(content.startAt.isNotNull())
                    .and(content.startAt.gt(now));
            case ENDED -> content.deletedAt.isNull()
                    .and(content.endAt.isNotNull())
                    .and(content.endAt.lt(now));
            case DELETED -> content.deletedAt.isNotNull();
        };
    }
}
