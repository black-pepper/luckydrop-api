package com.luckydrop.api.domain.drawresult.repository;

import com.luckydrop.api.domain.drawresult.dto.ManagerDrawResultSearchCondition;
import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import com.luckydrop.api.domain.drawresult.entity.QDrawResult;
import com.luckydrop.api.domain.invitationcode.entity.QInvitationCode;
import com.luckydrop.api.domain.reward.entity.QReward;
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
public class DrawResultRepositoryImpl implements DrawResultRepositoryCustom {

    private static final QDrawResult drawResult = QDrawResult.drawResult;
    private static final QInvitationCode invitationCode = QInvitationCode.invitationCode;
    private static final QReward reward = QReward.reward;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<DrawResult> searchManagerDrawResults(
            String contentCode,
            ManagerDrawResultSearchCondition condition,
            Pageable pageable
    ) {
        List<DrawResult> content = queryFactory
                .selectFrom(drawResult)
                .join(drawResult.invitationCode, invitationCode).fetchJoin()
                .join(drawResult.reward, reward).fetchJoin()
                .where(
                        drawResult.content.code.eq(contentCode),
                        drawnAtGoe(condition.getDrawnAtFrom()),
                        drawnAtLoe(condition.getDrawnAtTo()),
                        deliveredEq(condition.getDelivered()),
                        invitationCodeEq(condition.getInvitationCode()),
                        rewardNameContains(condition.getRewardName())
                )
                .orderBy(drawResult.drawnAt.desc(), drawResult.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(drawResult.count())
                .from(drawResult)
                .join(drawResult.invitationCode, invitationCode)
                .join(drawResult.reward, reward)
                .where(
                        drawResult.content.code.eq(contentCode),
                        drawnAtGoe(condition.getDrawnAtFrom()),
                        drawnAtLoe(condition.getDrawnAtTo()),
                        deliveredEq(condition.getDelivered()),
                        invitationCodeEq(condition.getInvitationCode()),
                        rewardNameContains(condition.getRewardName())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression drawnAtGoe(OffsetDateTime drawnAtFrom) {
        return drawnAtFrom != null ? drawResult.drawnAt.goe(drawnAtFrom) : null;
    }

    private BooleanExpression drawnAtLoe(OffsetDateTime drawnAtTo) {
        return drawnAtTo != null ? drawResult.drawnAt.loe(drawnAtTo) : null;
    }

    private BooleanExpression deliveredEq(Boolean delivered) {
        return delivered != null ? drawResult.delivered.eq(delivered) : null;
    }

    private BooleanExpression invitationCodeEq(String invitationCodeValue) {
        return StringUtils.hasText(invitationCodeValue) ? invitationCode.code.eq(invitationCodeValue) : null;
    }

    private BooleanExpression rewardNameContains(String rewardName) {
        return StringUtils.hasText(rewardName) ? reward.name.containsIgnoreCase(rewardName) : null;
    }
}
