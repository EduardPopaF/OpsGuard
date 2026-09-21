package com.opsguard.team.api;

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

class CreateTeamRequestValidationTest {

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
        CreateTeamRequest request = new CreateTeamRequest(
                "Infrastructure",
                "Core infrastructure team"
        );

        Set<ConstraintViolation<CreateTeamRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldAcceptNullDescription() {
        CreateTeamRequest request = new CreateTeamRequest(
                "Infrastructure",
                null
        );

        Set<ConstraintViolation<CreateTeamRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectBlankName() {
        CreateTeamRequest request = new CreateTeamRequest(
                "   ",
                "Core infrastructure team"
        );

        Set<ConstraintViolation<CreateTeamRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Team name is required.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectNameLongerThan150Characters() {
        CreateTeamRequest request = new CreateTeamRequest(
                "a".repeat(151),
                null
        );

        Set<ConstraintViolation<CreateTeamRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Team name must not exceed 150 characters.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectDescriptionLongerThan500Characters() {
        CreateTeamRequest request = new CreateTeamRequest(
                "Infrastructure",
                "a".repeat(501)
        );

        Set<ConstraintViolation<CreateTeamRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Team description must not exceed 500 characters.",
                violations.iterator().next().getMessage()
        );
    }
}