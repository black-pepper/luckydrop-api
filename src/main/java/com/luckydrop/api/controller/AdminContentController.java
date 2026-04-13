package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.content.dto.AdminContentCreateRequest;
import com.luckydrop.api.domain.content.dto.AdminContentDeleteResponse;
import com.luckydrop.api.domain.content.dto.AdminContentDetailResponse;
import com.luckydrop.api.domain.content.dto.AdminContentResponse;
import com.luckydrop.api.domain.content.dto.AdminContentUpdateRequest;
import com.luckydrop.api.service.AdminContentService;
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
public class AdminContentController {

    private final AdminContentService adminContentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AdminContentResponse>> create(
            @RequestBody @Valid AdminContentCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminContentService.create(request)));
    }

    @GetMapping("/{contentCode}")
    public ResponseEntity<ApiResponse<AdminContentDetailResponse>> getDetail(
            @PathVariable String contentCode) {
        return ResponseEntity.ok(ApiResponse.ok(adminContentService.getDetail(contentCode)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminContentResponse>>> getContents() {
        return ResponseEntity.ok(ApiResponse.ok(adminContentService.getContents()));
    }

    @PutMapping("/{contentCode}")
    public ResponseEntity<ApiResponse<AdminContentResponse>> update(
            @PathVariable String contentCode,
            @RequestBody @Valid AdminContentUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminContentService.update(contentCode, request)));
    }

    @DeleteMapping("/{contentCode}")
    public ResponseEntity<ApiResponse<AdminContentDeleteResponse>> delete(
            @PathVariable String contentCode) {
        return ResponseEntity.ok(ApiResponse.ok(adminContentService.delete(contentCode)));
    }
}
