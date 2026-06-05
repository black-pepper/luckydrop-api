package com.luckydrop.api.controller;

import com.luckydrop.api.common.exception.GlobalExceptionHandler;
import com.luckydrop.api.domain.inquiry.dto.InquiryResponse;
import com.luckydrop.api.domain.inquiry.entity.Inquiry;
import com.luckydrop.api.domain.inquiry.entity.InquiryStatus;
import com.luckydrop.api.domain.inquiry.entity.InquiryType;
import com.luckydrop.api.service.InquiryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InquiryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("local")
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InquiryService inquiryService;

    @Test
    void 내_문의_목록을_조회한다() throws Exception {
        Inquiry inquiry = Inquiry.builder()
                .type(InquiryType.BUG)
                .title("버그 문의")
                .content("버그 내용")
                .status(InquiryStatus.PENDING)
                .build();
        ReflectionTestUtils.setField(inquiry, "id", 10L);
        ReflectionTestUtils.setField(inquiry, "createdAt", OffsetDateTime.parse("2026-06-05T10:00:00Z"));

        when(inquiryService.getMyInquiries()).thenReturn(List.of(new InquiryResponse(inquiry)));

        mockMvc.perform(get("/user/inquiries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].createdAt").value("2026-06-05T10:00:00Z"))
                .andExpect(jsonPath("$.data[0].type").value("BUG"))
                .andExpect(jsonPath("$.data[0].title").value("버그 문의"))
                .andExpect(jsonPath("$.data[0].content").value("버그 내용"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }
}
