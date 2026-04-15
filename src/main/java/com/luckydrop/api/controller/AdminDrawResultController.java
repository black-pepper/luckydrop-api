package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.drawresult.dto.AdminDrawResultResponse;
import com.luckydrop.api.domain.drawresult.dto.DrawResultDeliveryUpdateRequest;
import com.luckydrop.api.service.AdminDrawResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manage/draw-results")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminDrawResultController {

    private final AdminDrawResultService adminDrawResultService;

    @Operation(summary = "관리자용 추첨 결과 목록 조회", description = "콘텐츠 코드 기준으로 본인 소유 콘텐츠의 추첨 결과 전체 목록을 조회한다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminDrawResultResponse>>> getAdminDrawResults(
            @Parameter(description = "조회할 콘텐츠 코드", example = "CONTENT-001")
            @RequestParam @NotBlank String contentCode) {
        return ResponseEntity.ok(ApiResponse.ok(adminDrawResultService.getAdminDrawResults(contentCode)));
    }

    @Operation(summary = "상품 지급 여부 수정", description = "추첨 결과 ID 기준으로 상품 지급 여부를 수정한다. 본인 소유 콘텐츠의 결과만 수정할 수 있다.")
    @PutMapping("/{drawResultId}/delivery")
    public ResponseEntity<ApiResponse<AdminDrawResultResponse>> updateDeliveryStatus(
            @Parameter(description = "수정할 추첨 결과 ID", example = "1")
            @PathVariable Long drawResultId,
            @RequestBody @Valid DrawResultDeliveryUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminDrawResultService.updateDeliveryStatus(drawResultId, request)));
    }
}
