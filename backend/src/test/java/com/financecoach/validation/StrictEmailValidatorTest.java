package com.financecoach.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrictEmailValidatorTest {
    private final StrictEmailValidator validator = new StrictEmailValidator();

    @Test
    void acceptsNormalDomainEmail() {
        assertTrue(validator.isValid("person@example.com", null));
        assertTrue(validator.isValid("person+tag@example.co.in", null));
    }

    @Test
    void rejectsIncompleteAndMalformedEmail() {
        assertFalse(validator.isValid("person@example", null));
        assertFalse(validator.isValid("person@localhost", null));
        assertFalse(validator.isValid("person..name@example.com", null));
        assertFalse(validator.isValid("person @example.com", null));
        assertFalse(validator.isValid("@example.com", null));
    }

    @Test
    void allowsBlankValuesForNotBlankToReportRequiredMessage() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("   ", null));
    }
}
