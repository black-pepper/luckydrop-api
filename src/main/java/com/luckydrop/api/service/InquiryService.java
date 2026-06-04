package com.luckydrop.api.service;

import com.luckydrop.api.domain.inquiry.dto.InquiryRequest;
import com.luckydrop.api.domain.inquiry.entity.Inquiry;
import com.luckydrop.api.domain.inquiry.entity.InquiryStatus;
import com.luckydrop.api.domain.inquiry.repository.InquiryRepository;
import com.luckydrop.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public void create(InquiryRequest request) {
        User user = currentUserService.getCurrentUserEntityOptional().orElse(null);
        inquiryRepository.save(Inquiry.builder()
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .status(InquiryStatus.PENDING)
                .user(user)
                .build());
    }
}
