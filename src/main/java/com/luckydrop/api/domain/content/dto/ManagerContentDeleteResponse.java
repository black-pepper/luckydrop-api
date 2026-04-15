package com.luckydrop.api.domain.content.dto;

import lombok.Getter;

@Getter
public class ManagerContentDeleteResponse {

    private final String code;
    private final boolean deleted;

    public ManagerContentDeleteResponse(String code) {
        this.code = code;
        this.deleted = true;
    }
}
