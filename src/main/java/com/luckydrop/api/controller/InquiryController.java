package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.inquiry.dto.InquiryRequest;
import com.luckydrop.api.domain.inquiry.dto.InquiryResponse;
import com.luckydrop.api.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InquiryController {
    final private InquiryService inquiryService;

    @Operation(summary = "문의 등록", description = "문의를 등록한다.")
    @PostMapping("/inquiries")
    public ResponseEntity<ApiResponse<Void>> create(
            @RequestBody @Valid InquiryRequest request) {
        inquiryService.create(request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "내 문의 목록 조회", description = "현재 사용자가 작성한 문의 목록을 조회한다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/user/inquiries")
    public ResponseEntity<ApiResponse<List<InquiryResponse>>> getMyInquiries() {
        return ResponseEntity.ok(ApiResponse.ok(inquiryService.getMyInquiries()));
    }
}
