package com.luckydrop.api.controller;

import com.luckydrop.api.common.exception.GlobalExceptionHandler;
import com.luckydrop.api.domain.content.entity.Content;
import com.luckydrop.api.domain.content.entity.ContentType;
import com.luckydrop.api.domain.drawresult.dto.ManagerDrawResultResponse;
import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import com.luckydrop.api.domain.invitationcode.entity.InvitationCode;
import com.luckydrop.api.domain.reward.entity.Reward;
import com.luckydrop.api.domain.user.entity.User;
import com.luckydrop.api.service.ManagerDrawResultService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagerDrawResultController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("local")
class ManagerDrawResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManagerDrawResultService managerDrawResultService;

    @Test
    void ISO_8601_쿼리스트링과_기타_필터가_정상_바인딩되어_서비스에_전달된다() throws Exception {
        ManagerDrawResultResponse response = new ManagerDrawResultResponse(createDrawResult());
        PageRequest pageable = PageRequest.of(1, 5);
        when(managerDrawResultService.getManagerDrawResults(eq("CONTENT-001"), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), pageable, 11));

        mockMvc.perform(get("/api/manage/draw-results")
                        .param("contentCode", "CONTENT-001")
                        .param("drawnAtFrom", "2026-04-29T00:00:00+09:00")
                        .param("drawnAtTo", "2026-04-29T23:59:59Z")
                        .param("delivered", "false")
                        .param("invitationCode", "INVITE-001")
                        .param("rewardName", "경품")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].drawResultId").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(11))
                .andExpect(jsonPath("$.data.totalPages").value(3));

        ArgumentCaptor<com.luckydrop.api.domain.drawresult.dto.ManagerDrawResultSearchCondition> conditionCaptor =
                ArgumentCaptor.forClass(com.luckydrop.api.domain.drawresult.dto.ManagerDrawResultSearchCondition.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(managerDrawResultService).getManagerDrawResults(eq("CONTENT-001"), conditionCaptor.capture(), pageableCaptor.capture());

        assertThat(conditionCaptor.getValue().getDrawnAtFrom())
                .isEqualTo(OffsetDateTime.parse("2026-04-29T00:00:00+09:00"));
        assertThat(conditionCaptor.getValue().getDrawnAtTo())
                .isEqualTo(OffsetDateTime.parse("2026-04-29T23:59:59Z"));
        assertThat(conditionCaptor.getValue().getDelivered()).isFalse();
        assertThat(conditionCaptor.getValue().getInvitationCode()).isEqualTo("INVITE-001");
        assertThat(conditionCaptor.getValue().getRewardName()).isEqualTo("경품");
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void 잘못된_drawnAt_형식이면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/manage/draw-results")
                        .param("contentCode", "CONTENT-001")
                        .param("drawnAtFrom", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private DrawResult createDrawResult() {
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 1L);

        Content content = new Content("CONTENT-001", ContentType.DRAW, owner, "Lucky Drop", "Event");
        InvitationCode invitationCode = new InvitationCode("INVITE-001", "참가자A", content, 3, null);
        Reward reward = new Reward("경품", "설명", 1, null, 5, null, true, true, content);

        DrawResult drawResult = DrawResult.builder()
                .content(content)
                .invitationCode(invitationCode)
                .reward(reward)
                .drawNo(1)
                .build();

        ReflectionTestUtils.setField(drawResult, "id", 10L);
        ReflectionTestUtils.setField(drawResult, "drawnAt", OffsetDateTime.parse("2026-04-29T12:00:00+09:00"));
        return drawResult;
    }
}
