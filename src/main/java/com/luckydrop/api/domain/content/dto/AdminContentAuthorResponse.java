package com.luckydrop.api.domain.content.dto;

import com.luckydrop.api.domain.user.entity.User;
import lombok.Getter;

@Getter
public class AdminContentAuthorResponse {

    private final Long userId;
    private final String name;

    public AdminContentAuthorResponse(User user) {
        this.userId = user == null ? null : user.getId();
        this.name = user == null ? null : user.getName();
    }
}
