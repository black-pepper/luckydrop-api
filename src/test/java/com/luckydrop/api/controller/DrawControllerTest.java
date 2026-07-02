package com.luckydrop.api.controller;

import com.luckydrop.api.common.exception.DrawEventException;
import com.luckydrop.api.common.exception.ErrorCode;
import com.luckydrop.api.common.exception.GlobalExceptionHandler;
import com.luckydrop.api.domain.drawresult.dto.DrawRequest;
import com.luckydrop.api.service.CodeService;
import com.luckydrop.api.service.ContentService;
import com.luckydrop.api.service.DrawRateLimitService;
import com.luckydrop.api.service.DrawService;
import com.luckydrop.api.service.ResultService;
import com.luckydrop.api.service.RewardService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DrawController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("local")
class DrawControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CodeService codeService;

    @MockitoBean
    private ContentService contentService;

    @MockitoBean
    private DrawRateLimitService drawRateLimitService;

    @MockitoBean
    private DrawService drawService;

    @MockitoBean
    private ResultService resultService;

    @MockitoBean
    private RewardService rewardService;

    @Test
    void contentDetailChecksRateLimitBeforeServiceCall() throws Exception {
        mockMvc.perform(get("/api/draw/contents/{contentCode}", "CONTENT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(drawRateLimitService).checkContentDetail(any(HttpServletRequest.class), eq("CONTENT-001"));
        verify(contentService).getDetail("CONTENT-001");
    }

    @Test
    void verifyCodeChecksRateLimitBeforeServiceCall() throws Exception {
        mockMvc.perform(get("/api/draw/verify")
                        .param("contentCode", "CONTENT-001")
                        .param("invitationCode", "INVITE-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(drawRateLimitService).checkInvitationCodeRequest(
                any(HttpServletRequest.class),
                eq("CONTENT-001"),
                eq("INVITE-001")
        );
        verify(codeService).verifyCode("CONTENT-001", "INVITE-001");
    }

    @Test
    void rewardsChecksRateLimitBeforeServiceCall() throws Exception {
        mockMvc.perform(get("/api/draw/rewards")
                        .param("contentCode", "CONTENT-001")
                        .param("invitationCode", "INVITE-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(drawRateLimitService).checkInvitationCodeRequest(
                any(HttpServletRequest.class),
                eq("CONTENT-001"),
                eq("INVITE-001")
        );
        verify(rewardService).getAvailableRewards("CONTENT-001", "INVITE-001");
    }

    @Test
    void resultsChecksRateLimitBeforeServiceCall() throws Exception {
        mockMvc.perform(get("/api/draw/results")
                        .param("contentCode", "CONTENT-001")
                        .param("invitationCode", "INVITE-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(drawRateLimitService).checkInvitationCodeRequest(
                any(HttpServletRequest.class),
                eq("CONTENT-001"),
                eq("INVITE-001")
        );
        verify(resultService).getResultsByCode("CONTENT-001", "INVITE-001");
    }

    @Test
    void executeChecksRateLimitBeforeServiceCall() throws Exception {
        mockMvc.perform(post("/api/draw/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contentCode": "CONTENT-001",
                                  "invitationCode": "INVITE-001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(drawRateLimitService).checkExecuteRequest(
                any(HttpServletRequest.class),
                eq("CONTENT-001"),
                eq("INVITE-001")
        );
        verify(drawService).draw(any(DrawRequest.class));
    }

    @Test
    void returnsTooManyRequestsWhenRateLimitIsExceeded() throws Exception {
        doThrow(new DrawEventException(ErrorCode.RATE_LIMIT_EXCEEDED, 60L))
                .when(drawRateLimitService)
                .checkContentDetail(any(HttpServletRequest.class), eq("CONTENT-001"));

        mockMvc.perform(get("/api/draw/contents/{contentCode}", "CONTENT-001"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("요청이 너무 많습니다. 잠시 후 다시 시도해주세요."));

        verify(contentService, never()).getDetail("CONTENT-001");
    }
}
