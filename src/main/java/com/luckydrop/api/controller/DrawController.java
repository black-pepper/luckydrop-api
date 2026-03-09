package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.drawcode.dto.CodeVerifyResponse;
import com.luckydrop.api.domain.drawresult.dto.DrawRequest;
import com.luckydrop.api.domain.drawresult.dto.DrawResponse;
import com.luckydrop.api.domain.drawresult.dto.DrawResultResponse;
import com.luckydrop.api.domain.reward.dto.RewardResponse;
import com.luckydrop.api.service.CodeService;
import com.luckydrop.api.service.DrawService;
import com.luckydrop.api.service.ResultService;
import com.luckydrop.api.service.RewardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/draw")
@RequiredArgsConstructor
public class DrawController {

    private final CodeService codeService;
    private final DrawService drawService;
    private final ResultService resultService;
    private final RewardService rewardService;

    /**
     * GET /api/draw/verify?code=XXX
     * 코드 검증 및 현재 상태 조회
     */
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<CodeVerifyResponse>> verifyCode(
            @RequestParam @NotBlank String code) {
        return ResponseEntity.ok(ApiResponse.ok(codeService.verifyCode(code)));
    }

    /**
     * POST /api/draw/execute
     * 뽑기 실행
     */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<DrawResponse>> executeDraw(
            @RequestBody @Valid DrawRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(drawService.draw(request)));
    }

    /**
     * GET /api/draw/results?code=XXX&scope=code|participant
     * 결과 이력 조회 (scope=code: 코드 기준 / scope=participant: 참가자 전체)
     */
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<DrawResultResponse>>> getResults(
            @RequestParam @NotBlank String code,
            @RequestParam(defaultValue = "code") String scope) {

        List<DrawResultResponse> results = "participant".equals(scope)
                ? resultService.getResultsByParticipant(code)
                : resultService.getResultsByCode(code);

        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    /**
     * GET /api/draw/rewards
     * 뽑기 가능한 보상 목록 조회 (확률 포함)
     */
    @GetMapping("/rewards")
    public ResponseEntity<ApiResponse<List<RewardResponse>>> getRewards() {
        return ResponseEntity.ok(ApiResponse.ok(rewardService.getAvailableRewards()));
    }
}
