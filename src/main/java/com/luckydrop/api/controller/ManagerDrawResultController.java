package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.drawresult.dto.DrawResultDeliveryUpdateRequest;
import com.luckydrop.api.domain.drawresult.dto.ManagerDrawResultResponse;
import com.luckydrop.api.domain.drawresult.dto.ManagerDrawResultSearchCondition;
import com.luckydrop.api.service.ManagerDrawResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manage/draw-results")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ManagerDrawResultController {

    private final ManagerDrawResultService managerDrawResultService;

    @Operation(
            summary = "관리자용 추첨 결과 목록 조회",
            description = "콘텐츠 코드 기준으로 본인 소유 콘텐츠의 추첨 결과를 페이지 단위로 조회한다. 정렬은 drawnAt DESC로 고정되며 sort 파라미터는 사용하지 않는다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ManagerDrawResultResponse>>> getManagerDrawResults(
            @Parameter(description = "조회할 콘텐츠 코드", example = "CONTENT-001")
            @RequestParam @NotBlank String contentCode,
            @ParameterObject @ModelAttribute ManagerDrawResultSearchCondition condition,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(managerDrawResultService.getManagerDrawResults(contentCode, condition, pageable)));
    }

    @Operation(summary = "상품 지급 여부 수정", description = "추첨 결과 ID 기준으로 상품 지급 여부를 수정한다. 본인 소유 콘텐츠의 결과만 수정할 수 있다.")
    @PutMapping("/{drawResultId}/delivery")
    public ResponseEntity<ApiResponse<ManagerDrawResultResponse>> updateDeliveryStatus(
            @Parameter(description = "수정할 추첨 결과 ID", example = "1")
            @PathVariable Long drawResultId,
            @RequestBody @Valid DrawResultDeliveryUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(managerDrawResultService.updateDeliveryStatus(drawResultId, request)));
    }
}
