package com.luckydrop.api.domain.participant.repository;

import com.luckydrop.api.config.QuerydslConfig;
import com.luckydrop.api.domain.participant.entity.Participant;
import com.luckydrop.api.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Import(QuerydslConfig.class)
class ParticipantRepositoryTest {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void participantNamesAreStoredAndLoadedAsJsonArray() {
        User owner = new User();
        owner.setName("owner");
        entityManager.persist(owner);

        Participant saved = participantRepository.saveAndFlush(
                new Participant(List.of("김철수", "이영희"), "A조", owner)
        );
        entityManager.clear();

        Participant participant = participantRepository.findById(saved.getId()).orElseThrow();

        assertThat(participant.getParticipantNames()).containsExactly("김철수", "이영희");
        assertThat(participant.getMemo()).isEqualTo("A조");
    }
}
