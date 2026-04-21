package com.luckydrop.api.domain.content.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum ContentType {
    DRAW,
    QUIZ;

    @JsonCreator
    public static ContentType from(String value) {
        if (value == null) {
            return null;
        }
        try {
            return ContentType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
