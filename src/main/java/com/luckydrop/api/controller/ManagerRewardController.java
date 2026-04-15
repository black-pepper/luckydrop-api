package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.reward.dto.ManagerRewardResponse;
import com.luckydrop.api.domain.reward.dto.RewardCreateRequest;
import com.luckydrop.api.domain.reward.dto.RewardUpdateRequest;
import com.luckydrop.api.service.RewardManagerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/manage/rewards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ManagerRewardController {

    private final RewardManagerService rewardManagerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ManagerRewardResponse>>> getRewardsByContent(
            @RequestParam @NotBlank String contentCode) {
        return ResponseEntity.ok(ApiResponse.ok(rewardManagerService.getRewardsByContent(contentCode)));
    }

    @GetMapping("/{rewardId}")
    public ResponseEntity<ApiResponse<ManagerRewardResponse>> getReward(
            @PathVariable Long rewardId) {
        return ResponseEntity.ok(ApiResponse.ok(rewardManagerService.getReward(rewardId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ManagerRewardResponse>> createReward(
            @RequestBody @Valid RewardCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(rewardManagerService.createReward(request)));
    }

    @PutMapping("/{rewardId}")
    public ResponseEntity<ApiResponse<ManagerRewardResponse>> updateReward(
            @PathVariable Long rewardId,
            @RequestBody @Valid RewardUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(rewardManagerService.updateReward(rewardId, request)));
    }

    @DeleteMapping("/{rewardId}")
    public ResponseEntity<ApiResponse<Void>> deleteReward(
            @PathVariable Long rewardId) {
        rewardManagerService.deleteReward(rewardId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
