package com.sanad.portal.web.validation;

import com.sanad.portal.domain.CertificateType;
import com.sanad.portal.web.form.ApplicationForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class ConsistentApplicationValidator implements ConstraintValidator<ConsistentApplication, ApplicationForm> {

    static final int MAX_AGE = 120;
    static final int MARRIAGE_MIN_AGE = 18;

    @Override
    public boolean isValid(ApplicationForm form, ConstraintValidatorContext ctx) {
        if (form == null) {
            return true;
        }
        boolean ok = true;
        ctx.disableDefaultConstraintViolation();

        CertificateType type = form.getCertificateType();
        if (type == CertificateType.INCOME && form.getAnnualIncome() == null) {
            ok = fail(ctx, "annualIncome", "Annual income is required for an income certificate.");
        }
        if (type == CertificateType.MARRIAGE && (form.getSpouseName() == null || form.getSpouseName().isBlank())) {
            ok = fail(ctx, "spouseName", "Spouse's name is required for a marriage certificate.");
        }

        LocalDate dob = form.getDateOfBirth();
        if (dob != null && dob.isBefore(LocalDate.now())) {
            int age = Period.between(dob, LocalDate.now()).getYears();
            if (age > MAX_AGE) {
                ok = fail(ctx, "dateOfBirth", "Please check the date of birth — the applicant would be over 120.");
            } else if (type == CertificateType.MARRIAGE && age < MARRIAGE_MIN_AGE) {
                ok = fail(ctx, "dateOfBirth", "Applicants for a marriage certificate must be at least 18 years old.");
            }
        }
        return ok;
    }

    private static boolean fail(ConstraintValidatorContext ctx, String field, String message) {
        ctx.buildConstraintViolationWithTemplate(message).addPropertyNode(field).addConstraintViolation();
        return false;
    }
}
