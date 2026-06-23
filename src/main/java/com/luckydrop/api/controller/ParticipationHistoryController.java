package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.participationhistory.dto.ParticipationHistoryResponse;
import com.luckydrop.api.domain.participationhistory.dto.ParticipationHistorySearchCondition;
import com.luckydrop.api.service.ParticipationHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/participation-histories")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ParticipationHistoryController {

    private final ParticipationHistoryService participationHistoryService;

    @Operation(summary = "내 참여 내역 목록 조회", description = "현재 로그인 사용자의 참여 내역을 최신 접근순으로 조회한다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ParticipationHistoryResponse>>> getMyParticipationHistories(
            @ParameterObject @ModelAttribute ParticipationHistorySearchCondition condition,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(participationHistoryService.getMyParticipationHistories(condition, pageable)));
    }
}
