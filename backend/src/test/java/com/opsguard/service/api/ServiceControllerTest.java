package com.opsguard.service.api;

import com.opsguard.common.api.GlobalExceptionHandler;
import com.opsguard.common.exception.ConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationStatus;
import com.opsguard.service.Service;
import com.opsguard.service.ServiceCriticality;
import com.opsguard.service.ServiceService;
import com.opsguard.service.ServiceStatus;
import com.opsguard.team.Team;
import com.opsguard.team.TeamStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServiceControllerTest {

    private ServiceService serviceService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        serviceService = mock(ServiceService.class);

        ServiceController controller =
                new ServiceController(serviceService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateService() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = new Organization(
                organizationId,
                "Acme Corporation",
                "acme-corporation",
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        Team ownerTeam = new Team(
                teamId,
                organization,
                "Platform Engineering",
                null,
                TeamStatus.ACTIVE,
                now,
                now
        );

        Service service = new Service(
                serviceId,
                organization,
                "Authentication API",
                "Handles user authentication",
                ownerTeam,
                ServiceCriticality.CRITICAL,
                ServiceStatus.ACTIVE,
                now,
                now
        );

        when(serviceService.create(
                organizationId,
                "Authentication API",
                "Handles user authentication",
                teamId,
                ServiceCriticality.CRITICAL
        )).thenReturn(service);

        String requestBody = """
                {
                  "name": "Authentication API",
                  "description": "Handles user authentication",
                  "ownerTeamId": "%s",
                  "criticality": "CRITICAL"
                }
                """.formatted(teamId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/services",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(serviceId.toString()))
                .andExpect(jsonPath("$.organizationId")
                        .value(organizationId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Authentication API"))
                .andExpect(jsonPath("$.description")
                        .value("Handles user authentication"))
                .andExpect(jsonPath("$.ownerTeamId")
                        .value(teamId.toString()))
                .andExpect(jsonPath("$.criticality")
                        .value("CRITICAL"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));

        verify(serviceService).create(
                organizationId,
                "Authentication API",
                "Handles user authentication",
                teamId,
                ServiceCriticality.CRITICAL
        );
    }

    @Test
    void shouldRejectBlankServiceName() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        String requestBody = """
                {
                  "name": "   ",
                  "description": "Handles user authentication",
                  "ownerTeamId": "%s",
                  "criticality": "CRITICAL"
                }
                """.formatted(teamId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/services",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectMissingOwnerTeamId() throws Exception {
        UUID organizationId = UUID.randomUUID();

        String requestBody = """
                {
                  "name": "Authentication API",
                  "criticality": "CRITICAL"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/services",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectMissingCriticality() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        String requestBody = """
                {
                  "name": "Authentication API",
                  "ownerTeamId": "%s"
                }
                """.formatted(teamId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/services",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectInvalidCriticality() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        String requestBody = """
                {
                  "name": "Authentication API",
                  "ownerTeamId": "%s",
                  "criticality": "EXTREME"
                }
                """.formatted(teamId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/services",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("MALFORMED_REQUEST"));
    }

    @Test
    void shouldReturnNotFoundWhenOrganizationDoesNotExist()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        when(serviceService.create(
                eq(organizationId),
                eq("Authentication API"),
                any(),
                eq(teamId),
                eq(ServiceCriticality.CRITICAL)
        )).thenThrow(
                new ResourceNotFoundException(
                        "Organization does not exist."
                )
        );

        String requestBody = """
                {
                  "name": "Authentication API",
                  "ownerTeamId": "%s",
                  "criticality": "CRITICAL"
                }
                """.formatted(teamId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/services",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnNotFoundWhenOwnerTeamDoesNotExist()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        when(serviceService.create(
                eq(organizationId),
                eq("Authentication API"),
                any(),
                eq(teamId),
                eq(ServiceCriticality.CRITICAL)
        )).thenThrow(
                new ResourceNotFoundException(
                        "Team does not exist in this organization."
                )
        );

        String requestBody = """
                {
                  "name": "Authentication API",
                  "ownerTeamId": "%s",
                  "criticality": "CRITICAL"
                }
                """.formatted(teamId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/services",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnConflictWhenServiceNameAlreadyExists()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        when(serviceService.create(
                eq(organizationId),
                eq("Authentication API"),
                any(),
                eq(teamId),
                eq(ServiceCriticality.CRITICAL)
        )).thenThrow(
                new ConflictException(
                        "Service already exists."
                )
        );

        String requestBody = """
                {
                  "name": "Authentication API",
                  "ownerTeamId": "%s",
                  "criticality": "CRITICAL"
                }
                """.formatted(teamId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/services",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("CONFLICT"));
    }

    @Test
    void shouldRejectInvalidOrganizationId() throws Exception {
        UUID teamId = UUID.randomUUID();

        String requestBody = """
                {
                  "name": "Authentication API",
                  "ownerTeamId": "%s",
                  "criticality": "CRITICAL"
                }
                """.formatted(teamId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/services",
                                "not-a-uuid"
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_PARAMETER"));
    }
}