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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/draw")
@RequiredArgsConstructor
public class DrawController {

    private final CodeService codeService;
    private final DrawService drawService;
    private final ResultService resultService;
    private final RewardService rewardService;

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<CodeVerifyResponse>> verifyCode(
            @RequestParam @NotBlank String code) {
        return ResponseEntity.ok(ApiResponse.ok(codeService.verifyCode(code)));
    }

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<DrawResponse>> executeDraw(
            @RequestBody @Valid DrawRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(drawService.draw(request)));
    }

    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<DrawResultResponse>>> getResults(
            @RequestParam @NotBlank String code) {
        return ResponseEntity.ok(ApiResponse.ok(resultService.getResultsByCode(code)));
    }

    @GetMapping("/rewards")
    public ResponseEntity<ApiResponse<List<RewardResponse>>> getRewards(
            @RequestParam @NotBlank String code) {
        return ResponseEntity.ok(ApiResponse.ok(rewardService.getAvailableRewards(code)));
    }
}
