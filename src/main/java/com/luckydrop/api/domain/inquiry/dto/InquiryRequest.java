package com.luckydrop.api.domain.inquiry.dto;

import com.luckydrop.api.domain.inquiry.entity.InquiryType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryRequest {
    @NotNull(message = "문의 타입을 입력해주세요.")
    private InquiryType type;
    @NotNull(message = "문의 제목을 입력해주세요.")
    private String title;
    @NotNull(message = "문의 내용을 입력해주세요.")
    private String content;
}
