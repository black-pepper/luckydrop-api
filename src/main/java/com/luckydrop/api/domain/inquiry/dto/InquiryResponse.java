package com.luckydrop.api.domain.inquiry.dto;

import com.luckydrop.api.domain.inquiry.entity.Inquiry;
import com.luckydrop.api.domain.inquiry.entity.InquiryStatus;
import com.luckydrop.api.domain.inquiry.entity.InquiryType;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class InquiryResponse {

    private final Long id;
    private final OffsetDateTime createdAt;
    private final InquiryType type;
    private final String title;
    private final String content;
    private final InquiryStatus status;
    private final String answer;
    private final OffsetDateTime answeredAt;

    public InquiryResponse(Inquiry inquiry) {
        this.id = inquiry.getId();
        this.createdAt = inquiry.getCreatedAt();
        this.type = inquiry.getType();
        this.title = inquiry.getTitle();
        this.content = inquiry.getContent();
        this.status = inquiry.getStatus();
        this.answer = inquiry.getAnswer();
        this.answeredAt = inquiry.getAnsweredAt();
    }
}
