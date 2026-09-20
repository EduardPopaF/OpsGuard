package com.opsguard.organization.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateOrganizationRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidRequest() {
        CreateOrganizationRequest request =
                new CreateOrganizationRequest(
                        "Acme Corporation",
                        "acme-corporation"
                );

        Set<ConstraintViolation<CreateOrganizationRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectBlankName() {
        CreateOrganizationRequest request =
                new CreateOrganizationRequest(
                        "   ",
                        "acme"
                );

        Set<ConstraintViolation<CreateOrganizationRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Organization name is required.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectInvalidSlug() {
        CreateOrganizationRequest request =
                new CreateOrganizationRequest(
                        "Acme Corporation",
                        "Acme Corporation"
                );

        Set<ConstraintViolation<CreateOrganizationRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Organization slug must contain only lowercase letters, numbers, and single hyphens.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectSlugWithConsecutiveHyphens() {
        CreateOrganizationRequest request =
                new CreateOrganizationRequest(
                        "Acme Corporation",
                        "acme--corporation"
                );

        Set<ConstraintViolation<CreateOrganizationRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
    }
}