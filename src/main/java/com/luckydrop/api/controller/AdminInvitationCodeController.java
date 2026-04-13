package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.invitationcode.dto.InvitationCodeCreateRequest;
import com.luckydrop.api.domain.invitationcode.dto.InvitationCodeResponse;
import com.luckydrop.api.domain.invitationcode.dto.InvitationCodeUpdateRequest;
import com.luckydrop.api.service.InvitationCodeAdminService;
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
@RequestMapping("/api/manage/invitation-codes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminInvitationCodeController {

    private final InvitationCodeAdminService invitationCodeAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvitationCodeResponse>>> getInvitationCodesByContent(
            @RequestParam @NotBlank String contentCode) {
        return ResponseEntity.ok(ApiResponse.ok(invitationCodeAdminService.getInvitationCodesByContent(contentCode)));
    }

    @GetMapping("/{invitationCodeId}")
    public ResponseEntity<ApiResponse<InvitationCodeResponse>> getInvitationCode(
            @PathVariable Long invitationCodeId) {
        return ResponseEntity.ok(ApiResponse.ok(invitationCodeAdminService.getInvitationCode(invitationCodeId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvitationCodeResponse>> createInvitationCode(
            @RequestBody @Valid InvitationCodeCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(invitationCodeAdminService.createInvitationCode(request)));
    }

    @PutMapping("/{invitationCodeId}")
    public ResponseEntity<ApiResponse<InvitationCodeResponse>> updateInvitationCode(
            @PathVariable Long invitationCodeId,
            @RequestBody @Valid InvitationCodeUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(invitationCodeAdminService.updateInvitationCode(invitationCodeId, request)));
    }

    @DeleteMapping("/{invitationCodeId}")
    public ResponseEntity<ApiResponse<Void>> deleteInvitationCode(
            @PathVariable Long invitationCodeId) {
        invitationCodeAdminService.deleteInvitationCode(invitationCodeId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
