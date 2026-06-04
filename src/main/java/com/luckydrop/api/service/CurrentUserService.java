package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.user.dto.CurrentUser;
import com.luckydrop.api.domain.user.dto.UserRequest;
import com.luckydrop.api.domain.user.entity.User;
import com.luckydrop.api.domain.user.repository.UserRepository;
import com.luckydrop.api.security.CurrentUserAuthIdProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final CurrentUserAuthIdProvider currentUserAuthIdProvider;
    private final UserRepository userRepository;

    @Transactional
    public CurrentUser getCurrentUser() {
        return CurrentUser.from(getCurrentUserEntity());
    }

    @Transactional
    public User getCurrentUserEntity() {
        UUID authId = currentUserAuthIdProvider.getCurrentAuthId();
        return getOrCreateUser(authId);
    }

    @Transactional
    public Optional<User> getCurrentUserEntityOptional() {
        return currentUserAuthIdProvider.getCurrentAuthIdOptional()
                .map(this::getOrCreateUser);
    }

    @Transactional
    public UUID softDeleteCurrentUser() {
        User user = getCurrentUserEntity();
        if (user.isDeleted()) {
            throw new DrawEventException(ErrorCode.USER_ALREADY_DELETED);
        }
        UUID authId = user.getAuthId();
        user.delete();
        return authId;
    }

    @Transactional
    public User updateCurrentUserEntity(UserRequest request) {
        User user = userRepository.findByAuthIdAndDeletedAtIsNull(currentUserAuthIdProvider.getCurrentAuthId())
                .orElseThrow(() -> new DrawEventException(ErrorCode.USER_NOT_FOUND));
        user.setName(request.getName());
        return user;
    }

    private User getOrCreateUser(UUID authId) {
        return userRepository.findByAuthIdAndDeletedAtIsNull(authId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setAuthId(authId);
                    newUser.setName(currentUserAuthIdProvider.getCurrentUserDisplayName());
                    return userRepository.save(newUser);
                });
    }
}
