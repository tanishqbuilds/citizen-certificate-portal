package com.sanad.portal.web.validation;

import com.sanad.portal.service.NationalIds;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NationalIdValidator implements ConstraintValidator<NationalId, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // @NotBlank reports the empty case
        }
        String normalized = NationalIds.normalize(value);
        if (!normalized.matches("\\d{12}")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The national ID must have exactly 12 digits.")
                    .addConstraintViolation();
            return false;
        }
        return NationalIds.isValid(normalized);
    }
}
