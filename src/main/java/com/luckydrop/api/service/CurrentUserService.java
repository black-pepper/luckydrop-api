package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.user.dto.CurrentUser;
import com.luckydrop.api.domain.user.entity.User;
import com.luckydrop.api.domain.user.repository.UserRepository;
import com.luckydrop.api.security.CurrentUserAuthIdProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final CurrentUserAuthIdProvider currentUserAuthIdProvider;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CurrentUser getCurrentUser() {
        return CurrentUser.from(getCurrentUserEntity());
    }

    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        return userRepository.findByAuthId(currentUserAuthIdProvider.getCurrentAuthId())
                .orElseThrow(() -> new DrawEventException(ErrorCode.USER_NOT_FOUND));
    }
}
