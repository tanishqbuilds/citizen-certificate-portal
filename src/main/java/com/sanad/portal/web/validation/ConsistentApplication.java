package com.sanad.portal.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cross-field rules that depend on the certificate type (CCR-4):
 * income certificates need an income, marriage certificates need a spouse and an adult applicant,
 * and nobody is older than 120.
 */
@Documented
@Constraint(validatedBy = ConsistentApplicationValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConsistentApplication {
    String message() default "The application is inconsistent.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
