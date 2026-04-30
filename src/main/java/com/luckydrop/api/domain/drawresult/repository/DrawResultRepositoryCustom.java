package com.luckydrop.api.domain.drawresult.repository;

import com.luckydrop.api.domain.drawresult.dto.ManagerDrawResultSearchCondition;
import com.luckydrop.api.domain.drawresult.entity.DrawResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DrawResultRepositoryCustom {

    Page<DrawResult> searchManagerDrawResults(
            String contentCode,
            ManagerDrawResultSearchCondition condition,
            Pageable pageable
    );
}
