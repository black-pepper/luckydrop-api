package com.luckydrop.api.service;

import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.repository.ContentRepository;
import com.luckydrop.api.domain.participationhistory.entity.ParticipationHistory;
import com.luckydrop.api.domain.participationhistory.repository.ParticipationHistoryRepository;
import com.luckydrop.api.domain.user.entity.User;
import com.luckydrop.api.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipationHistoryServiceTest {

    @Mock
    private ParticipationHistoryRepository participationHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ParticipationHistoryService participationHistoryService;

    @Test
    void recordAccessUpdatesAccessedAtWhenHistoryAlreadyExists() {
        ParticipationHistory history = createHistory();
        OffsetDateTime before = OffsetDateTime.now().minusDays(1);
        ReflectionTestUtils.setField(history, "accessedAt", before);

        when(participationHistoryRepository.findByUserIdAndContentIdAndInvitationCode(1L, 10L, "INVITE-001"))
                .thenReturn(Optional.of(history));

        participationHistoryService.recordAccess(1L, 10L, "INVITE-001");

        assertThat(history.getAccessedAt()).isAfter(before);
    }

    @Test
    void recordAccessRetriesUpdateWhenUniqueConstraintIsViolated() {
        ParticipationHistory history = createHistory();
        OffsetDateTime before = OffsetDateTime.now().minusDays(1);
        ReflectionTestUtils.setField(history, "accessedAt", before);

        User user = new User();
        Content content = new Content();

        when(participationHistoryRepository.findByUserIdAndContentIdAndInvitationCode(1L, 10L, "INVITE-001"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(history));
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(contentRepository.getReferenceById(10L)).thenReturn(content);
        when(participationHistoryRepository.saveAndFlush(any(ParticipationHistory.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        participationHistoryService.recordAccess(1L, 10L, "INVITE-001");

        assertThat(history.getAccessedAt()).isAfter(before);
        verify(participationHistoryRepository).saveAndFlush(any(ParticipationHistory.class));
    }

    private ParticipationHistory createHistory() {
        return new ParticipationHistory(new User(), new Content(), "INVITE-001");
    }
}
