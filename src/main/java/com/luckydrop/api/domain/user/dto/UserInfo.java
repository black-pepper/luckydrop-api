package com.luckydrop.api.domain.user.dto;

import com.luckydrop.api.domain.user.entity.User;

public record UserInfo(
        String name
) {

    public static UserInfo from(User user) {
        return new UserInfo(user.getName());
    }
}
