package com.opsguard.incident.api;

import com.opsguard.incident.IncidentSeverity;
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

class CreateIncidentRequestValidationTest {

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
        CreateIncidentRequest request = new CreateIncidentRequest(
                "Payment API unavailable",
                "Customers cannot complete payments",
                IncidentSeverity.SEV1,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        Set<ConstraintViolation<CreateIncidentRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectBlankTitle() {
        CreateIncidentRequest request = new CreateIncidentRequest(
                "   ",
                null,
                IncidentSeverity.SEV1,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        Set<ConstraintViolation<CreateIncidentRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Incident title is required.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectTitleLongerThan200Characters() {
        CreateIncidentRequest request = new CreateIncidentRequest(
                "A".repeat(201),
                null,
                IncidentSeverity.SEV2,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        Set<ConstraintViolation<CreateIncidentRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Incident title must not exceed 200 characters.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectMissingSeverity() {
        CreateIncidentRequest request = new CreateIncidentRequest(
                "Payment API unavailable",
                null,
                null,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        Set<ConstraintViolation<CreateIncidentRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Incident severity is required.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectMissingServiceId() {
        CreateIncidentRequest request = new CreateIncidentRequest(
                "Payment API unavailable",
                null,
                IncidentSeverity.SEV1,
                null,
                UUID.randomUUID()
        );

        Set<ConstraintViolation<CreateIncidentRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Service ID is required.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectMissingCreatorUserId() {
        CreateIncidentRequest request = new CreateIncidentRequest(
                "Payment API unavailable",
                null,
                IncidentSeverity.SEV1,
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<CreateIncidentRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Creator user ID is required.",
                violations.iterator().next().getMessage()
        );
    }
}