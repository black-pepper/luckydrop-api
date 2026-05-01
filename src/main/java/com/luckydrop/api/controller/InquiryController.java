package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.inquiry.dto.InquiryRequest;
import com.luckydrop.api.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
