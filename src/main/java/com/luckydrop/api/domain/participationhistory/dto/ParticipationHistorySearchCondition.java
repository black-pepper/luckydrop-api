package com.luckydrop.api.domain.participationhistory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParticipationHistorySearchCondition {

    @Schema(description = "콘텐츠 제목 검색어", example = "행운")
    private String keyword;

    @Schema(description = "콘텐츠 상태", example = "ACTIVE")
    private ParticipationContentStatus status;
}
