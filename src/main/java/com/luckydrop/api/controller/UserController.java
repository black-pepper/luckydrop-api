package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.user.dto.UserInfo;
import com.luckydrop.api.service.CurrentUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final CurrentUserService currentUserService;

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.ok(UserInfo.from(currentUserService.getCurrentUserEntity())));
    }
}
