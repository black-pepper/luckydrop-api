package com.luckydrop.api.service;

import com.luckydrop.api.domain.inquiry.dto.InquiryRequest;
import com.luckydrop.api.domain.inquiry.entity.Inquiry;
import com.luckydrop.api.domain.inquiry.entity.InquiryStatus;
import com.luckydrop.api.domain.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;

    public void create(InquiryRequest request) {
        inquiryRepository.save(Inquiry.builder()
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .status(InquiryStatus.PENDING)
                .build());
    }
}
