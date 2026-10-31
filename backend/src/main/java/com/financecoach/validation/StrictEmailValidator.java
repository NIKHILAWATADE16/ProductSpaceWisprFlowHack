package com.financecoach.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrictEmailValidator implements ConstraintValidator<ValidEmail, String> {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9](?:[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]{0,62}[A-Za-z0-9])?@"
            + "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
            + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String email = value.trim();
        return email.length() <= 150
            && !email.contains("..")
            && EMAIL_PATTERN.matcher(email).matches();
    }
}
