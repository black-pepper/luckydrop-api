package com.luckydrop.api.infrastructure.supabase;

import com.luckydrop.api.security.SecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Slf4j
@Component
public class SupabaseAdminClient {

    private final RestClient restClient;
    private final String adminApiUrl;

    public SupabaseAdminClient(SecurityProperties securityProperties) {
        String serviceRoleKey = securityProperties.getSupabase().getServiceRoleKey();
        this.adminApiUrl = securityProperties.getSupabase().getAdminApiUrl();
        this.restClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + serviceRoleKey)
                .defaultHeader("apikey", serviceRoleKey)
                .build();
    }

    public void deleteUser(UUID authId) {
        restClient.delete()
                .uri(adminApiUrl + "/admin/users/" + authId)
                .retrieve()
                .toBodilessEntity();
    }
}
