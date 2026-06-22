package com.luckydrop.api.service;

import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.repository.ContentRepository;
import com.luckydrop.api.domain.participationhistory.dto.ParticipationHistoryResponse;
import com.luckydrop.api.domain.participationhistory.dto.ParticipationHistorySearchCondition;
import com.luckydrop.api.domain.participationhistory.entity.ParticipationHistory;
import com.luckydrop.api.domain.participationhistory.repository.ParticipationHistoryRepository;
import com.luckydrop.api.domain.user.entity.User;
import com.luckydrop.api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipationHistoryService {

    private final ParticipationHistoryRepository participationHistoryRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public Page<ParticipationHistoryResponse> getMyParticipationHistories(
            ParticipationHistorySearchCondition condition,
            Pageable pageable
    ) {
        User currentUser = currentUserService.getCurrentUserEntity();
        return participationHistoryRepository.searchMyParticipationHistories(currentUser.getId(), condition, pageable)
                .map(ParticipationHistoryResponse::new);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordCurrentUserAccessIfAuthenticated(Content content, String invitationCode) {
        currentUserService.getCurrentUserEntityOptional()
                .ifPresent(user -> doRecordAccess(user.getId(), content.getId(), invitationCode));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAccess(Long userId, Long contentId, String invitationCode) {
        doRecordAccess(userId, contentId, invitationCode);
    }

    private void doRecordAccess(Long userId, Long contentId, String invitationCode) {
        participationHistoryRepository.findByUserIdAndContentIdAndInvitationCode(userId, contentId, invitationCode)
                .ifPresentOrElse(
                        ParticipationHistory::updateAccessedAt,
                        () -> saveNewHistory(userId, contentId, invitationCode)
                );
    }

    private void saveNewHistory(Long userId, Long contentId, String invitationCode) {
        try {
            User user = userRepository.getReferenceById(userId);
            Content content = contentRepository.getReferenceById(contentId);
            participationHistoryRepository.saveAndFlush(new ParticipationHistory(user, content, invitationCode));
        } catch (DataIntegrityViolationException e) {
            ParticipationHistory history = participationHistoryRepository
                    .findByUserIdAndContentIdAndInvitationCode(userId, contentId, invitationCode)
                    .orElseThrow(() -> e);
            history.updateAccessedAt();
        }
    }
}
