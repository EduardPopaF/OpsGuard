package com.opsguard.user.api;

import com.opsguard.user.UserRole;
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

class CreateUserRequestValidationTest {

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
        CreateUserRequest request = new CreateUserRequest(
                "john@example.com",
                "John",
                "Doe",
                UserRole.ENGINEER
        );

        Set<ConstraintViolation<CreateUserRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectInvalidEmail() {
        CreateUserRequest request = new CreateUserRequest(
                "not-an-email",
                "John",
                "Doe",
                UserRole.ENGINEER
        );

        Set<ConstraintViolation<CreateUserRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Email must be valid.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectBlankFirstName() {
        CreateUserRequest request = new CreateUserRequest(
                "john@example.com",
                "   ",
                "Doe",
                UserRole.ENGINEER
        );

        Set<ConstraintViolation<CreateUserRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "First name is required.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectBlankLastName() {
        CreateUserRequest request = new CreateUserRequest(
                "john@example.com",
                "John",
                "",
                UserRole.ENGINEER
        );

        Set<ConstraintViolation<CreateUserRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Last name is required.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void shouldRejectMissingRole() {
        CreateUserRequest request = new CreateUserRequest(
                "john@example.com",
                "John",
                "Doe",
                null
        );

        Set<ConstraintViolation<CreateUserRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "Role is required.",
                violations.iterator().next().getMessage()
        );
    }
}