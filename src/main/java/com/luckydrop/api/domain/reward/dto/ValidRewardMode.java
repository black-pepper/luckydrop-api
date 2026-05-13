package com.luckydrop.api.domain.reward.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RewardModeValidator.class)
public @interface ValidRewardMode {
    String message() default "weight 또는 pool_count 중 하나만 입력해 주세요.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
