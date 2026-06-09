package com.luckydrop.api.service;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        ReflectionTestUtils.setField(inquiry, "answer", "답변 내용");
        ReflectionTestUtils.setField(inquiry, "answeredAt", OffsetDateTime.parse("2026-06-09T03:30:00Z"));

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
        assertThat(responses.getFirst().getAnswer()).isEqualTo("답변 내용");
        assertThat(responses.getFirst().getAnsweredAt()).isEqualTo(OffsetDateTime.parse("2026-06-09T03:30:00Z"));
        verify(inquiryRepository).findAllByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void 현재_사용자가_작성한_문의_상세를_조회한다() {
        User currentUser = new User();
        ReflectionTestUtils.setField(currentUser, "id", 1L);

        Inquiry inquiry = Inquiry.builder()
                .type(InquiryType.BUG)
                .title("상세 문의")
                .content("상세 내용")
                .status(InquiryStatus.DONE)
                .user(currentUser)
                .build();
        ReflectionTestUtils.setField(inquiry, "id", 10L);
        ReflectionTestUtils.setField(inquiry, "createdAt", OffsetDateTime.parse("2026-06-05T10:00:00Z"));
        ReflectionTestUtils.setField(inquiry, "answer", "확인 후 수정 완료했습니다.");
        ReflectionTestUtils.setField(inquiry, "answeredAt", OffsetDateTime.parse("2026-06-09T03:30:00Z"));

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(inquiryRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(inquiry));

        InquiryResponse response = inquiryService.getMyInquiry(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(InquiryStatus.DONE);
        assertThat(response.getAnswer()).isEqualTo("확인 후 수정 완료했습니다.");
        assertThat(response.getAnsweredAt()).isEqualTo(OffsetDateTime.parse("2026-06-09T03:30:00Z"));
        verify(inquiryRepository).findByIdAndUserId(10L, 1L);
    }

    @Test
    void 본인_문의가_아니거나_존재하지_않으면_예외가_발생한다() {
        User currentUser = new User();
        ReflectionTestUtils.setField(currentUser, "id", 1L);

        when(currentUserService.getCurrentUserEntity()).thenReturn(currentUser);
        when(inquiryRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.getMyInquiry(999L))
                .isInstanceOf(DrawEventException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INQUIRY_NOT_FOUND);
    }
}
