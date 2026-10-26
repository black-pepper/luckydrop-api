package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.participant.dto.ParticipantBatchCreateRequest;
import com.luckydrop.api.domain.participant.dto.ParticipantCreateRequest;
import com.luckydrop.api.domain.participant.dto.ParticipantResponse;
import com.luckydrop.api.domain.participant.dto.ParticipantUpdateRequest;
import com.luckydrop.api.domain.participant.entity.Participant;
import com.luckydrop.api.domain.participant.repository.ParticipantRepository;
import com.luckydrop.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerParticipantService {

    private final ParticipantRepository participantRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<ParticipantResponse> getParticipants() {
        Long currentUserId = currentUserService.getCurrentUserEntity().getId();
        return participantRepository.findAllByUserId(currentUserId)
                .stream()
                .map(ParticipantResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParticipantResponse getParticipant(Long participantId) {
        return new ParticipantResponse(getOwnedParticipant(participantId));
    }

    @Transactional
    public ParticipantResponse createParticipant(ParticipantCreateRequest request) {
        User user = currentUserService.getCurrentUserEntity();
        Participant participant = new Participant(
                request.getParticipantNames(),
                request.getMemo(),
                user
        );
        return new ParticipantResponse(participantRepository.save(participant));
    }

    @Transactional
    public List<ParticipantResponse> createParticipants(ParticipantBatchCreateRequest request) {
        User user = currentUserService.getCurrentUserEntity();
        return request.getParticipants()
                .stream()
                .map(participant -> new Participant(
                        participant.getParticipantNames(),
                        participant.getMemo(),
                        user
                ))
                .map(participantRepository::save)
                .map(ParticipantResponse::new)
                .toList();
    }

    @Transactional
    public ParticipantResponse updateParticipant(Long participantId, ParticipantUpdateRequest request) {
        Participant participant = getOwnedParticipant(participantId);
        participant.update(request.getParticipantNames(), request.getMemo());
        return new ParticipantResponse(participant);
    }

    @Transactional
    public void deleteParticipant(Long participantId) {
        Participant participant = getOwnedParticipant(participantId);
        participant.delete();
    }

    private Participant getOwnedParticipant(Long participantId) {
        Long currentUserId = currentUserService.getCurrentUserEntity().getId();
        return participantRepository.findByIdWithUser(participantId)
                .map(participant -> {
                    if (participant.isDeleted()
                            || participant.getUser() == null
                            || !participant.getUser().getId().equals(currentUserId)) {
                        throw new DrawEventException(ErrorCode.PARTICIPANT_NOT_FOUND);
                    }
                    return participant;
                })
                .orElseThrow(() -> new DrawEventException(ErrorCode.PARTICIPANT_NOT_FOUND));
    }
}
