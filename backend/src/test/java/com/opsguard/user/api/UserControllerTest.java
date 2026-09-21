package com.opsguard.user.api;

import com.opsguard.common.api.GlobalExceptionHandler;
import com.opsguard.common.exception.ConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationStatus;
import com.opsguard.user.User;
import com.opsguard.user.UserRole;
import com.opsguard.user.UserService;
import com.opsguard.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);

        UserController controller = new UserController(userService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateUser() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = new Organization(
                organizationId,
                "Acme Corporation",
                "acme-corporation",
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        User user = new User(
                userId,
                organization,
                "john.smith@acme.com",
                "John",
                "Smith",
                UserRole.ENGINEER,
                UserStatus.ACTIVE,
                now,
                now
        );

        when(userService.create(
                organizationId,
                "john.smith@acme.com",
                "John",
                "Smith",
                UserRole.ENGINEER
        )).thenReturn(user);

        mockMvc.perform(
                        post("/api/organizations/{organizationId}/users",
                                organizationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "john.smith@acme.com",
                                          "firstName": "John",
                                          "lastName": "Smith",
                                          "role": "ENGINEER"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.organizationId")
                        .value(organizationId.toString()))
                .andExpect(jsonPath("$.email")
                        .value("john.smith@acme.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.role").value("ENGINEER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(userService).create(
                organizationId,
                "john.smith@acme.com",
                "John",
                "Smith",
                UserRole.ENGINEER
        );
    }

    @Test
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        UUID organizationId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/organizations/{organizationId}/users",
                                organizationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "not-an-email",
                                          "firstName": "John",
                                          "lastName": "Smith",
                                          "role": "ENGINEER"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed."))
                .andExpect(jsonPath("$.errors.email")
                        .value("Email must be valid."))
                .andExpect(jsonPath("$.path")
                        .value(
                                "/api/organizations/"
                                        + organizationId
                                        + "/users"
                        ))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturnNotFoundWhenOrganizationDoesNotExist()
            throws Exception {
        UUID organizationId = UUID.randomUUID();

        when(userService.create(
                organizationId,
                "john.smith@acme.com",
                "John",
                "Smith",
                UserRole.ENGINEER
        )).thenThrow(
                new ResourceNotFoundException(
                        "Organization with id '"
                                + organizationId
                                + "' does not exist."
                )
        );

        mockMvc.perform(
                        post("/api/organizations/{organizationId}/users",
                                organizationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "john.smith@acme.com",
                                          "firstName": "John",
                                          "lastName": "Smith",
                                          "role": "ENGINEER"
                                        }
                                        """)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Organization with id '"
                                        + organizationId
                                        + "' does not exist."
                        ))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.path")
                        .value(
                                "/api/organizations/"
                                        + organizationId
                                        + "/users"
                        ))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService).create(
                organizationId,
                "john.smith@acme.com",
                "John",
                "Smith",
                UserRole.ENGINEER
        );
    }

    @Test
    void shouldReturnConflictForDuplicateEmail() throws Exception {
        UUID organizationId = UUID.randomUUID();

        when(userService.create(
                organizationId,
                "john.smith@acme.com",
                "John",
                "Smith",
                UserRole.ENGINEER
        )).thenThrow(
                new ConflictException(
                        "A user with email 'john.smith@acme.com' "
                                + "already exists in this organization."
                )
        );

        mockMvc.perform(
                        post("/api/organizations/{organizationId}/users",
                                organizationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "john.smith@acme.com",
                                          "firstName": "John",
                                          "lastName": "Smith",
                                          "role": "ENGINEER"
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "A user with email 'john.smith@acme.com' "
                                        + "already exists in this organization."
                        ))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.path")
                        .value(
                                "/api/organizations/"
                                        + organizationId
                                        + "/users"
                        ))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService).create(
                organizationId,
                "john.smith@acme.com",
                "John",
                "Smith",
                UserRole.ENGINEER
        );
    }

    @Test
    void shouldReturnBadRequestForInvalidRole() throws Exception {
        UUID organizationId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/organizations/{organizationId}/users",
                                organizationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "john.smith@acme.com",
                                          "firstName": "John",
                                          "lastName": "Smith",
                                          "role": "SUPER_ADMIN"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Request body is malformed or contains invalid values."
                        ))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(userService);
    }

    @Test
void shouldReturnBadRequestForInvalidOrganizationId() throws Exception {
    mockMvc.perform(
                    post("/api/organizations/not-a-uuid/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "john.smith@acme.com",
                                      "firstName": "John",
                                      "lastName": "Smith",
                                      "role": "ENGINEER"
                                    }
                                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code")
                    .value("INVALID_PARAMETER"))
            .andExpect(jsonPath("$.message")
                    .value("Request contains an invalid parameter."))
            .andExpect(jsonPath("$.errors.organizationId")
                    .value("Invalid value."))
            .andExpect(jsonPath("$.path")
                    .value("/api/organizations/not-a-uuid/users"))
            .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(userService);
}

}