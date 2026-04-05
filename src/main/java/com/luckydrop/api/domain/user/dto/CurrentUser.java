package com.luckydrop.api.domain.user.dto;

import com.luckydrop.api.domain.user.entity.User;

import java.util.UUID;

public record CurrentUser(
        Long id,
        String name,
        UUID authId
) {

    public static CurrentUser from(User user) {
        return new CurrentUser(user.getId(), user.getName(), user.getAuthId());
    }
}
