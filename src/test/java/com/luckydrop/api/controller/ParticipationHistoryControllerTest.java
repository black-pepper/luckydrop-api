package com.luckydrop.api.controller;

import com.luckydrop.api.common.exception.GlobalExceptionHandler;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.entity.ContentType;
import com.luckydrop.api.domain.participationhistory.dto.ParticipationHistoryResponse;
import com.luckydrop.api.domain.participationhistory.entity.ParticipationHistory;
import com.luckydrop.api.domain.user.entity.User;
import com.luckydrop.api.service.ParticipationHistoryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParticipationHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("local")
class ParticipationHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParticipationHistoryService participationHistoryService;

    @Test
    void searchConditionAndPageableAreBoundToService() throws Exception {
        when(participationHistoryService.getMyParticipationHistories(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new ParticipationHistoryResponse(createHistory()))));

        mockMvc.perform(get("/api/me/participation-histories")
                        .param("keyword", "룰렛")
                        .param("status", "ACTIVE")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].contentCode").value("CONTENT-001"))
                .andExpect(jsonPath("$.data.content[0].contentTitle").value("행운의 룰렛 이벤트"))
                .andExpect(jsonPath("$.data.content[0].contentStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.content[0].invitationCode").value("INVITE-001"));

        ArgumentCaptor<com.luckydrop.api.domain.participationhistory.dto.ParticipationHistorySearchCondition> conditionCaptor =
                ArgumentCaptor.forClass(com.luckydrop.api.domain.participationhistory.dto.ParticipationHistorySearchCondition.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(participationHistoryService).getMyParticipationHistories(conditionCaptor.capture(), pageableCaptor.capture());

        assertThat(conditionCaptor.getValue().getKeyword()).isEqualTo("룰렛");
        assertThat(conditionCaptor.getValue().getStatus().name()).isEqualTo("ACTIVE");
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }

    private ParticipationHistory createHistory() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);

        OffsetDateTime now = OffsetDateTime.now();
        Content content = new Content("CONTENT-001", ContentType.DRAW, null, "행운의 룰렛 이벤트", "Event",
                now.minusDays(1),
                now.plusDays(10));
        ReflectionTestUtils.setField(content, "id", 10L);

        ParticipationHistory history = new ParticipationHistory(user, content, "INVITE-001");
        ReflectionTestUtils.setField(history, "accessedAt", OffsetDateTime.parse("2026-06-16T10:30:00+09:00"));
        return history;
    }
}
