package com.luckydrop.api.domain.reward.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RewardModeValidator implements ConstraintValidator<ValidRewardMode, RewardModeValidatable> {

    @Override
    public boolean isValid(RewardModeValidatable value, ConstraintValidatorContext context) {
        boolean hasWeight = value.getWeight() != null;
        boolean hasPoolCount = value.getPoolCount() != null;

        if (!(hasWeight ^ hasPoolCount)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("weight 또는 pool_count 중 하나만 입력해 주세요.")
                    .addConstraintViolation();
            return false;
        }

        if (hasPoolCount && value.getStock() != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("제비뽑기형에서는 stock을 설정할 수 없습니다.")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
