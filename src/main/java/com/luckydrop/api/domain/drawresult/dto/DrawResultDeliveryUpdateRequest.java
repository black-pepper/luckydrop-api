package com.luckydrop.api.domain.drawresult.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DrawResultDeliveryUpdateRequest {

    @NotNull
    private Boolean delivered;
}
