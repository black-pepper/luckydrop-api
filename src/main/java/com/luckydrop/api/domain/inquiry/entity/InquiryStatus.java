package com.luckydrop.api.domain.inquiry.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum InquiryStatus {
    PENDING, DONE;

    @JsonCreator
    public static InquiryStatus from(String value) {
        if (value == null) {
            return null;
        }
        try {
            return InquiryStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
