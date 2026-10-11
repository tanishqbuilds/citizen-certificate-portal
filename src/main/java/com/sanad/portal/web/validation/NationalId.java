package com.sanad.portal.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** A 12-digit national ID with a valid Verhoeff check digit. Spaces and dashes are ignored. */
@Documented
@Constraint(validatedBy = NationalIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NationalId {
    String message() default "Enter a valid 12-digit national ID — the check digit doesn't match.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
