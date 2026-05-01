package com.luckydrop.api.domain.drawresult.repository;

import com.luckydrop.api.config.QuerydslConfig;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.entity.ContentType;
import com.luckydrop.api.domain.drawresult.dto.ManagerDrawResultResponse;
import com.luckydrop.api.domain.drawresult.dto.ManagerDrawResultSearchCondition;
import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.reward.entity.Reward;
import com.luckydrop.api.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never"
})
@Import(QuerydslConfig.class)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
class DrawResultRepositoryTest {

    @Autowired
    private DrawResultRepository drawResultRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Content targetContent;
    private InvitationCode inviteA;
    private InvitationCode inviteB;
    private Reward coffeeReward;
    private Reward mugReward;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setName("owner");
        entityManager.persist(owner);

        targetContent = new Content("CONTENT-001", ContentType.DRAW, owner, "Lucky Drop", "Event");
        entityManager.persist(targetContent);

        Content otherContent = new Content("CONTENT-002", ContentType.DRAW, owner, "Other", "Other Event");
        entityManager.persist(otherContent);

        inviteA = new InvitationCode("INVITE-001", "참가자A", targetContent, 3, null);
        inviteB = new InvitationCode("INVITE-002", "참가자B", targetContent, 3, null);
        InvitationCode otherInvite = new InvitationCode("INVITE-999", "외부", otherContent, 3, null);
        entityManager.persist(inviteA);
        entityManager.persist(inviteB);
        entityManager.persist(otherInvite);

        coffeeReward = new Reward("커피 쿠폰", "설명", 1, 5, null, true, targetContent);
        mugReward = new Reward("머그컵", "설명", 1, 5, null, true, targetContent);
        Reward otherReward = new Reward("외부 경품", "설명", 1, 5, null, true, otherContent);
        entityManager.persist(coffeeReward);
        entityManager.persist(mugReward);
        entityManager.persist(otherReward);

        persistDrawResult(targetContent, inviteA, coffeeReward, 1, false, "2026-04-29T00:00:00+09:00");
        persistDrawResult(targetContent, inviteA, mugReward, 2, true, "2026-04-29T12:00:00+09:00");
        persistDrawResult(targetContent, inviteB, coffeeReward, 3, false, "2026-04-29T23:59:59+09:00");
        persistDrawResult(targetContent, inviteB, mugReward, 4, false, "2026-04-30T09:00:00+09:00");
        persistDrawResult(otherContent, otherInvite, otherReward, 1, false, "2026-04-29T10:00:00+09:00");

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void 필터_조합과_drawnAt_경계가_의도대로_동작한다() {
        ManagerDrawResultSearchCondition condition = new ManagerDrawResultSearchCondition();
        ReflectionTestUtils.setField(condition, "drawnAtFrom", OffsetDateTime.parse("2026-04-29T00:00:00+09:00"));
        ReflectionTestUtils.setField(condition, "drawnAtTo", OffsetDateTime.parse("2026-04-29T23:59:59+09:00"));
        ReflectionTestUtils.setField(condition, "delivered", false);
        ReflectionTestUtils.setField(condition, "rewardName", "커피");

        Page<DrawResult> page = drawResultRepository.searchManagerDrawResults("CONTENT-001", condition, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(result -> result.getInvitationCode().getCode())
                .containsExactly("INVITE-002", "INVITE-001");
        assertThat(page.getContent())
                .extracting(DrawResult::getDrawnAt)
                .containsExactly(
                        OffsetDateTime.parse("2026-04-29T23:59:59+09:00"),
                        OffsetDateTime.parse("2026-04-29T00:00:00+09:00")
                );
    }

    @Test
    void invitationCode_정확일치와_페이지_메타데이터가_정확하다() {
        ManagerDrawResultSearchCondition condition = new ManagerDrawResultSearchCondition();
        ReflectionTestUtils.setField(condition, "invitationCode", "INVITE-002");

        Page<DrawResult> page = drawResultRepository.searchManagerDrawResults("CONTENT-001", condition, PageRequest.of(0, 1));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getInvitationCode().getCode()).isEqualTo("INVITE-002");
        assertThat(page.getContent().getFirst().getDrawNo()).isEqualTo(4);
    }

    @Test
    void 결과_매핑시_fetch_join으로_추가_select가_발생하지_않는다() {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        Page<DrawResult> page = drawResultRepository.searchManagerDrawResults(
                "CONTENT-001",
                new ManagerDrawResultSearchCondition(),
                PageRequest.of(0, 2)
        );

        List<ManagerDrawResultResponse> responses = page.map(ManagerDrawResultResponse::new).getContent();

        assertThat(responses).hasSize(2);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
    }

    private void persistDrawResult(
            Content content,
            InvitationCode invitationCode,
            Reward reward,
            int drawNo,
            boolean delivered,
            String drawnAt
    ) {
        DrawResult drawResult = DrawResult.builder()
                .content(content)
                .invitationCode(invitationCode)
                .reward(reward)
                .drawNo(drawNo)
                .build();
        ReflectionTestUtils.setField(drawResult, "delivered", delivered);
        ReflectionTestUtils.setField(drawResult, "drawnAt", OffsetDateTime.parse(drawnAt));
        entityManager.persist(drawResult);
    }
}
