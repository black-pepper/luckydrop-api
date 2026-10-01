package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.participant.dto.ParticipantBatchCreateRequest;
import com.luckydrop.api.domain.participant.dto.ParticipantCreateRequest;
import com.luckydrop.api.domain.participant.dto.ParticipantResponse;
import com.luckydrop.api.domain.participant.dto.ParticipantUpdateRequest;
import com.luckydrop.api.service.ManagerParticipantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manage/participants")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ManagerParticipantController {

    private final ManagerParticipantService participantService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParticipantResponse>>> getParticipants() {
        return ResponseEntity.ok(ApiResponse.ok(participantService.getParticipants()));
    }

    @GetMapping("/{participantId}")
    public ResponseEntity<ApiResponse<ParticipantResponse>> getParticipant(
            @PathVariable Long participantId) {
        return ResponseEntity.ok(ApiResponse.ok(participantService.getParticipant(participantId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ParticipantResponse>> createParticipant(
            @RequestBody @Valid ParticipantCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(participantService.createParticipant(request)));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<ParticipantResponse>>> createParticipants(
            @RequestBody @Valid ParticipantBatchCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(participantService.createParticipants(request)));
    }

    @PutMapping("/{participantId}")
    public ResponseEntity<ApiResponse<ParticipantResponse>> updateParticipant(
            @PathVariable Long participantId,
            @RequestBody @Valid ParticipantUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(participantService.updateParticipant(participantId, request)));
    }

    @DeleteMapping("/{participantId}")
    public ResponseEntity<ApiResponse<Void>> deleteParticipant(@PathVariable Long participantId) {
        participantService.deleteParticipant(participantId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
