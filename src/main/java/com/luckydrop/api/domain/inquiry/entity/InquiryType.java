package com.luckydrop.api.domain.inquiry.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum InquiryType {
    GENERAL, BUG, FEATURE, ETC;
    @JsonCreator
    public static InquiryType from(String value) {
        if (value == null) {
            return null;
        }
        try {
            return InquiryType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
