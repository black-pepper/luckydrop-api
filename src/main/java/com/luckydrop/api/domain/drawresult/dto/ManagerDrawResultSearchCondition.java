package com.luckydrop.api.domain.drawresult.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ManagerDrawResultSearchCondition {

    @Schema(
            description = "조회할 추첨 시각 시작값(이상)",
            example = "2026-04-29T00:00:00+09:00"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime drawnAtFrom;

    @Schema(
            description = "조회할 추첨 시각 종료값(이하)",
            example = "2026-04-29T23:59:59+09:00"
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime drawnAtTo;

    @Schema(description = "상품 지급 여부", example = "false")
    private Boolean delivered;

    @Schema(description = "콘텐츠 내부에서만 유니크한 초대 코드(정확 일치)", example = "INVITE-001")
    private String invitationCode;

    @Schema(description = "보상 이름(부분 일치)", example = "경품")
    private String rewardName;
}
