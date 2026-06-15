package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.domain.inquiry.dto.InquiryRequest;
import com.luckydrop.api.domain.inquiry.dto.InquiryResponse;
import com.luckydrop.api.domain.inquiry.entity.Inquiry;
import com.luckydrop.api.domain.inquiry.entity.InquiryStatus;
import com.luckydrop.api.domain.inquiry.repository.InquiryRepository;
import com.luckydrop.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<InquiryResponse> getMyInquiries() {
        Long currentUserId = currentUserService.getCurrentUserEntity().getId();

        return inquiryRepository.findAllByUserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(InquiryResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public InquiryResponse getMyInquiry(Long inquiryId) {
        Long currentUserId = currentUserService.getCurrentUserEntity().getId();

        Inquiry inquiry = inquiryRepository.findByIdAndUserId(inquiryId, currentUserId)
                .orElseThrow(() -> new DrawEventException(ErrorCode.INQUIRY_NOT_FOUND));

        return new InquiryResponse(inquiry);
    }
}
