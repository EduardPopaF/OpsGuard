package com.opsguard.service.api;

import com.opsguard.service.ServiceCriticality;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateServiceRequestValidationTest {

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
        CreateServiceRequest request = new CreateServiceRequest(
                "Authentication API",
                "Handles user authentication",
                UUID.randomUUID(),
                ServiceCriticality.CRITICAL
        );

        Set<ConstraintViolation<CreateServiceRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectBlankName() {
        CreateServiceRequest request = new CreateServiceRequest(
                "   ",
                "Handles user authentication",
                UUID.randomUUID(),
                ServiceCriticality.CRITICAL
        );

        Set<ConstraintViolation<CreateServiceRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Service name is required.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectNameLongerThan150Characters() {
        CreateServiceRequest request = new CreateServiceRequest(
                "A".repeat(151),
                null,
                UUID.randomUUID(),
                ServiceCriticality.HIGH
        );

        Set<ConstraintViolation<CreateServiceRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Service name must not exceed 150 characters.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectDescriptionLongerThan500Characters() {
        CreateServiceRequest request = new CreateServiceRequest(
                "Authentication API",
                "A".repeat(501),
                UUID.randomUUID(),
                ServiceCriticality.MEDIUM
        );

        Set<ConstraintViolation<CreateServiceRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Service description must not exceed 500 characters.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectMissingOwnerTeamId() {
        CreateServiceRequest request = new CreateServiceRequest(
                "Authentication API",
                null,
                null,
                ServiceCriticality.CRITICAL
        );

        Set<ConstraintViolation<CreateServiceRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Owner team ID is required.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectMissingCriticality() {
        CreateServiceRequest request = new CreateServiceRequest(
                "Authentication API",
                null,
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<CreateServiceRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Service criticality is required.",
                violations.iterator().next().getMessage()
        );
    }
}