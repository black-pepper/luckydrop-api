package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.content.dto.ManagerContentCreateRequest;
import com.luckydrop.api.domain.content.dto.ManagerContentDeleteResponse;
import com.luckydrop.api.domain.content.dto.ManagerContentDetailResponse;
import com.luckydrop.api.domain.content.dto.ManagerContentResponse;
import com.luckydrop.api.domain.content.dto.ManagerContentUpdateRequest;
import com.luckydrop.api.service.ManagerContentService;
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
@RequestMapping("/api/manage/contents")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ManagerContentController {

    private final ManagerContentService managerContentService;

    @PostMapping
    public ResponseEntity<ApiResponse<ManagerContentResponse>> create(
            @RequestBody @Valid ManagerContentCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(managerContentService.create(request)));
    }

    @GetMapping("/{contentCode}")
    public ResponseEntity<ApiResponse<ManagerContentDetailResponse>> getDetail(
            @PathVariable String contentCode) {
        return ResponseEntity.ok(ApiResponse.ok(managerContentService.getDetail(contentCode)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ManagerContentResponse>>> getContents() {
        return ResponseEntity.ok(ApiResponse.ok(managerContentService.getContents()));
    }

    @PutMapping("/{contentCode}")
    public ResponseEntity<ApiResponse<ManagerContentResponse>> update(
            @PathVariable String contentCode,
            @RequestBody @Valid ManagerContentUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(managerContentService.update(contentCode, request)));
    }

    @DeleteMapping("/{contentCode}")
    public ResponseEntity<ApiResponse<ManagerContentDeleteResponse>> delete(
            @PathVariable String contentCode) {
        return ResponseEntity.ok(ApiResponse.ok(managerContentService.delete(contentCode)));
    }
}
