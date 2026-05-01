package com.luckydrop.api.domain.user.dto;

import com.luckydrop.api.domain.user.entity.User;

import java.time.OffsetDateTime;

public record UserInfo(
        String name,
        OffsetDateTime createdAt
) {

    public static UserInfo from(User user) {
        return new UserInfo(user.getName(), user.getCreatedAt());
    }
}
