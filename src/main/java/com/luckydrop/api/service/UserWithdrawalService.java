package com.luckydrop.api.service;

import com.luckydrop.api.infrastructure.supabase.SupabaseAdminClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserWithdrawalService {

    private final CurrentUserService currentUserService;
    private final SupabaseAdminClient supabaseAdminClient;

    public void withdraw() {
        UUID authId = currentUserService.softDeleteCurrentUser();

        try {
            supabaseAdminClient.deleteUser(authId);
        } catch (Exception e) {
            log.error("Supabase auth 삭제 실패 (authId={}): {}", authId, e.getMessage());
        }
    }
}
