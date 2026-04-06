package com.luckydrop.api.domain.content.dto;

import lombok.Getter;

@Getter
public class AdminContentDeleteResponse {

    private final String code;
    private final boolean deleted;

    public AdminContentDeleteResponse(String code) {
        this.code = code;
        this.deleted = true;
    }
}
