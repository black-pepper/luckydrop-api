package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.content.dto.ParticipantContentDetailResponse;
import com.luckydrop.api.domain.invitationcode.dto.CodeVerifyResponse;
import com.luckydrop.api.domain.drawresult.dto.DrawRequest;
import com.luckydrop.api.domain.drawresult.dto.DrawResponse;
import com.luckydrop.api.domain.drawresult.dto.DrawResultResponse;
import com.luckydrop.api.domain.reward.dto.RewardResponse;
import com.luckydrop.api.service.CodeService;
import com.luckydrop.api.service.ContentService;
import com.luckydrop.api.service.DrawRateLimitService;
import com.luckydrop.api.service.DrawService;
import com.luckydrop.api.service.ResultService;
import com.luckydrop.api.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ContentService contentService;
    private final DrawRateLimitService drawRateLimitService;
    private final DrawService drawService;
    private final ResultService resultService;
    private final RewardService rewardService;

    @Operation(summary = "초대 코드 검증", description = "콘텐츠 코드와 초대 코드를 함께 받아 해당 참가자의 추첨 가능 상태와 남은 횟수를 확인한다.")
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<CodeVerifyResponse>> verifyCode(
            HttpServletRequest httpServletRequest,
            @Parameter(description = "참가자가 진입한 콘텐츠 코드", example = "CONTENT-001")
            @RequestParam @NotBlank String contentCode,
            @Parameter(description = "콘텐츠 내부에서만 유니크한 초대 코드", example = "INVITE-001")
            @RequestParam @NotBlank String invitationCode) {
        drawRateLimitService.checkInvitationCodeRequest(httpServletRequest, contentCode, invitationCode);
        return ResponseEntity.ok(ApiResponse.ok(codeService.verifyCode(contentCode, invitationCode)));
    }

    @Operation(summary = "추첨 실행", description = "콘텐츠 코드와 초대 코드를 함께 받아 해당 참가자 기준으로 실제 추첨을 수행한다.")
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<DrawResponse>> executeDraw(
            HttpServletRequest httpServletRequest,
            @RequestBody @Valid DrawRequest request) {
        drawRateLimitService.checkExecuteRequest(
                httpServletRequest,
                request.getContentCode(),
                request.getInvitationCode()
        );
        return ResponseEntity.ok(ApiResponse.ok(drawService.draw(request)));
    }

    @Operation(summary = "참가자용 콘텐츠 상세 조회", description = "콘텐츠 코드만으로 참가자에게 공개 가능한 콘텐츠 상세 정보를 조회한다.")
    @GetMapping("/contents/{contentCode}")
    public ResponseEntity<ApiResponse<ParticipantContentDetailResponse>> getContentDetail(
            HttpServletRequest httpServletRequest,
            @Parameter(description = "조회할 콘텐츠 코드", example = "CONTENT-001")
            @PathVariable String contentCode) {
        drawRateLimitService.checkContentDetail(httpServletRequest, contentCode);
        return ResponseEntity.ok(ApiResponse.ok(contentService.getDetail(contentCode)));
    }

    @Operation(summary = "참가자 추첨 결과 조회", description = "콘텐츠 코드와 초대 코드를 함께 받아 해당 참가자의 추첨 결과 이력을 조회한다.")
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<DrawResultResponse>>> getResults(
            HttpServletRequest httpServletRequest,
            @Parameter(description = "참가자가 진입한 콘텐츠 코드", example = "CONTENT-001")
            @RequestParam @NotBlank String contentCode,
            @Parameter(description = "콘텐츠 내부에서만 유니크한 초대 코드", example = "INVITE-001")
            @RequestParam @NotBlank String invitationCode) {
        drawRateLimitService.checkInvitationCodeRequest(httpServletRequest, contentCode, invitationCode);
        return ResponseEntity.ok(ApiResponse.ok(resultService.getResultsByCode(contentCode, invitationCode)));
    }

    @Operation(summary = "참가자별 보상 목록 조회", description = "콘텐츠 코드와 초대 코드를 함께 받아 해당 참가자 기준으로 현재 추첨 가능한 보상 목록을 조회한다.")
    @GetMapping("/rewards")
    public ResponseEntity<ApiResponse<List<RewardResponse>>> getRewards(
            HttpServletRequest httpServletRequest,
            @Parameter(description = "참가자가 진입한 콘텐츠 코드", example = "CONTENT-001")
            @RequestParam @NotBlank String contentCode,
            @Parameter(description = "콘텐츠 내부에서만 유니크한 초대 코드", example = "INVITE-001")
            @RequestParam @NotBlank String invitationCode) {
        drawRateLimitService.checkInvitationCodeRequest(httpServletRequest, contentCode, invitationCode);
        return ResponseEntity.ok(ApiResponse.ok(rewardService.getAvailableRewards(contentCode, invitationCode)));
    }
}
