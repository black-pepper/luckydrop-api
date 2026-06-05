package com.luckydrop.api.service;

import com.luckydrop.api.domain.inquiry.dto.InquiryResponse;
import com.luckydrop.api.domain.inquiry.entity.Inquiry;
import com.luckydrop.api.domain.inquiry.entity.InquiryStatus;
import com.luckydrop.api.domain.inquiry.entity.InquiryType;
import com.luckydrop.api.domain.inquiry.repository.InquiryRepository;
import com.luckydrop.api.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private InquiryService inquiryService;

    @Test
    void 현재_사용자가_작성한_문의_목록을_조회한다() {
        User currentUser = new User();
        ReflectionTestUtils.setField(currentUser, "id", 1L);

        Inquiry inquiry = Inquiry.builder()
                .type(InquiryType.GENERAL)
                .title("문의 제목")
                .content("문의 내용")
                .status(InquiryStatus.PENDING)
                .user(currentUser)
                .build();
        ReflectionTestUtils.setField(inquiry, "id", 10L);
        ReflectionTestUtils.setField(inquiry, "createdAt", OffsetDateTime.parse("2026-06-05T10:00:00Z"));

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(inquiryRepository.findAllByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(inquiry));

        List<InquiryResponse> responses = inquiryService.getMyInquiries();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getId()).isEqualTo(10L);
        assertThat(responses.getFirst().getCreatedAt()).isEqualTo(OffsetDateTime.parse("2026-06-05T10:00:00Z"));
        assertThat(responses.getFirst().getType()).isEqualTo(InquiryType.GENERAL);
        assertThat(responses.getFirst().getTitle()).isEqualTo("문의 제목");
        assertThat(responses.getFirst().getContent()).isEqualTo("문의 내용");
        assertThat(responses.getFirst().getStatus()).isEqualTo(InquiryStatus.PENDING);
        verify(inquiryRepository).findAllByUserIdOrderByCreatedAtDesc(1L);
    }
}
