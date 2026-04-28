package com.luckydrop.api.controller;

import com.luckydrop.api.common.response.ApiResponse;
import com.luckydrop.api.domain.user.dto.UserInfo;
import com.luckydrop.api.domain.user.dto.UserRequest;
import com.luckydrop.api.service.CurrentUserService;
import com.luckydrop.api.service.UserWithdrawalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final CurrentUserService currentUserService;
    private final UserWithdrawalService userWithdrawalService;

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.ok(UserInfo.from(currentUserService.getCurrentUserEntity())));
    }

    @PutMapping("/user")
    public ResponseEntity<ApiResponse<UserInfo>> updateCurrentUser(@RequestBody @Valid UserRequest request) {
        UserInfo userInfo = UserInfo.from(currentUserService.updateCurrentUserEntity(request));
        return ResponseEntity.ok(ApiResponse.ok(userInfo));
    }

    @DeleteMapping("/user")
    public ResponseEntity<ApiResponse<Void>> withdrawUser() {
        userWithdrawalService.withdraw();
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
