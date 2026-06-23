package com.luckydrop.api.domain.participationhistory.repository;

import com.luckydrop.api.config.QuerydslConfig;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.entity.ContentType;
import com.luckydrop.api.domain.participationhistory.dto.ParticipationContentStatus;
import com.luckydrop.api.domain.participationhistory.dto.ParticipationHistorySearchCondition;
import com.luckydrop.api.domain.participationhistory.entity.ParticipationHistory;
import com.luckydrop.api.domain.user.entity.User;
import jakarta.persistence.EntityManager;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never"
})
@Import(QuerydslConfig.class)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
class ParticipationHistoryRepositoryTest {

    @Autowired
    private ParticipationHistoryRepository participationHistoryRepository;

    @Autowired
    private EntityManager entityManager;

    private User targetUser;
    private User otherUser;
    private Content activeContent;
    private Content endedContent;
    private Content scheduledContent;

    @BeforeEach
    void setUp() {
        targetUser = new User();
        targetUser.setName("target");
        entityManager.persist(targetUser);

        otherUser = new User();
        otherUser.setName("other");
        entityManager.persist(otherUser);

        activeContent = new Content("ACTIVE-001", ContentType.DRAW, null, "행운의 룰렛 이벤트", "Event");
        endedContent = new Content("ENDED-001", ContentType.DRAW, null, "지난 이벤트", "Event",
                OffsetDateTime.now().minusDays(10), OffsetDateTime.now().minusDays(1));
        scheduledContent = new Content("SCHEDULED-001", ContentType.DRAW, null, "예정 이벤트", "Event",
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(10));
        Content otherContent = new Content("OTHER-001", ContentType.DRAW, null, "다른 사용자 이벤트", "Event");

        entityManager.persist(activeContent);
        entityManager.persist(endedContent);
        entityManager.persist(scheduledContent);
        entityManager.persist(otherContent);

        persistHistory(targetUser, activeContent, "INVITE-A", "2026-06-16T10:30:00+09:00");
        persistHistory(targetUser, endedContent, "INVITE-B", "2026-06-16T09:30:00+09:00");
        persistHistory(targetUser, scheduledContent, "INVITE-C", "2026-06-16T11:30:00+09:00");
        persistHistory(otherUser, otherContent, "INVITE-Z", "2026-06-16T12:30:00+09:00");

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void searchReturnsOnlyCurrentUserHistoriesInLatestAccessOrder() {
        Page<ParticipationHistory> page = participationHistoryRepository.searchMyParticipationHistories(
                targetUser.getId(),
                new ParticipationHistorySearchCondition(),
                PageRequest.of(0, 10)
        );

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent())
                .extracting(ParticipationHistory::getInvitationCode)
                .containsExactly("INVITE-C", "INVITE-A", "INVITE-B");
    }

    @Test
    void searchFiltersByContentTitleKeyword() {
        ParticipationHistorySearchCondition condition = new ParticipationHistorySearchCondition();
        condition.setKeyword("룰렛");

        Page<ParticipationHistory> page = participationHistoryRepository.searchMyParticipationHistories(
                targetUser.getId(),
                condition,
                PageRequest.of(0, 10)
        );

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getContent().getTitle()).isEqualTo("행운의 룰렛 이벤트");
    }

    @Test
    void searchFiltersByContentStatus() {
        ParticipationHistorySearchCondition activeCondition = new ParticipationHistorySearchCondition();
        activeCondition.setStatus(ParticipationContentStatus.ACTIVE);

        ParticipationHistorySearchCondition endedCondition = new ParticipationHistorySearchCondition();
        endedCondition.setStatus(ParticipationContentStatus.ENDED);

        Page<ParticipationHistory> activePage = participationHistoryRepository.searchMyParticipationHistories(
                targetUser.getId(),
                activeCondition,
                PageRequest.of(0, 10)
        );
        Page<ParticipationHistory> endedPage = participationHistoryRepository.searchMyParticipationHistories(
                targetUser.getId(),
                endedCondition,
                PageRequest.of(0, 10)
        );

        assertThat(activePage.getContent())
                .extracting(history -> history.getContent().getCode())
                .containsExactly("ACTIVE-001");
        assertThat(endedPage.getContent())
                .extracting(history -> history.getContent().getCode())
                .containsExactly("ENDED-001");
    }

    private void persistHistory(User user, Content content, String invitationCode, String accessedAt) {
        ParticipationHistory history = new ParticipationHistory(user, content, invitationCode);
        ReflectionTestUtils.setField(history, "accessedAt", OffsetDateTime.parse(accessedAt));
        entityManager.persist(history);
    }
}
