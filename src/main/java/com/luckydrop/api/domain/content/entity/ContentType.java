package com.luckydrop.api.domain.content.entity;

import lombok.Getter;

@Getter
public enum ContentType {
    DRAW,
    QUIZ;

    public static ContentType from(String value) {
        try {
            return ContentType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
