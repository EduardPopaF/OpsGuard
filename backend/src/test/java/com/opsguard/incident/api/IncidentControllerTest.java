package com.opsguard.incident.api;

import com.opsguard.common.api.GlobalExceptionHandler;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.incident.Incident;
import com.opsguard.incident.IncidentLifecycleAction;
import com.opsguard.incident.IncidentService;
import com.opsguard.incident.IncidentSeverity;
import com.opsguard.incident.IncidentStatus;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationStatus;
import com.opsguard.service.ServiceCriticality;
import com.opsguard.service.ServiceStatus;
import com.opsguard.team.Team;
import com.opsguard.team.TeamStatus;
import com.opsguard.user.User;
import com.opsguard.user.UserRole;
import com.opsguard.user.UserStatus;
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

class IncidentControllerTest {

    private IncidentService incidentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        incidentService = mock(IncidentService.class);

        IncidentController controller =
                new IncidentController(incidentService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateIncident() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

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

        com.opsguard.service.Service affectedService =
                new com.opsguard.service.Service(
                        serviceId,
                        organization,
                        "Payment API",
                        null,
                        ownerTeam,
                        ServiceCriticality.CRITICAL,
                        ServiceStatus.ACTIVE,
                        now,
                        now
                );

        User createdBy = new User(
                userId,
                organization,
                "engineer@acme.com",
                "John",
                "Doe",
                UserRole.ENGINEER,
                UserStatus.ACTIVE,
                now,
                now
        );

        Incident incident = new Incident(
                incidentId,
                organization,
                "INC-000001",
                "Payment API unavailable",
                "Customers cannot complete payments",
                IncidentSeverity.SEV1,
                IncidentStatus.OPEN,
                affectedService,
                null,
                null,
                createdBy,
                now,
                null,
                null,
                null,
                now
        );

        when(incidentService.create(
                organizationId,
                "Payment API unavailable",
                "Customers cannot complete payments",
                IncidentSeverity.SEV1,
                serviceId,
                userId
        )).thenReturn(incident);

        String requestBody = """
                {
                  "title": "Payment API unavailable",
                  "description": "Customers cannot complete payments",
                  "severity": "SEV1",
                  "serviceId": "%s",
                  "createdByUserId": "%s"
                }
                """.formatted(serviceId, userId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(incidentId.toString()))
                .andExpect(jsonPath("$.organizationId")
                        .value(organizationId.toString()))
                .andExpect(jsonPath("$.incidentNumber")
                        .value("INC-000001"))
                .andExpect(jsonPath("$.title")
                        .value("Payment API unavailable"))
                .andExpect(jsonPath("$.severity")
                        .value("SEV1"))
                .andExpect(jsonPath("$.status")
                        .value("OPEN"))
                .andExpect(jsonPath("$.serviceId")
                        .value(serviceId.toString()))
                .andExpect(jsonPath("$.createdByUserId")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.assignedTeamId")
                        .doesNotExist())
                .andExpect(jsonPath("$.assignedUserId")
                        .doesNotExist());

        verify(incidentService).create(
                organizationId,
                "Payment API unavailable",
                "Customers cannot complete payments",
                IncidentSeverity.SEV1,
                serviceId,
                userId
        );
    }

    @Test
    void shouldRejectBlankTitle() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String requestBody = """
                {
                  "title": "   ",
                  "severity": "SEV1",
                  "serviceId": "%s",
                  "createdByUserId": "%s"
                }
                """.formatted(serviceId, userId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents",
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
    void shouldRejectMissingServiceId() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String requestBody = """
                {
                  "title": "Payment API unavailable",
                  "severity": "SEV1",
                  "createdByUserId": "%s"
                }
                """.formatted(userId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents",
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
    void shouldRejectMissingCreatorUserId() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();

        String requestBody = """
                {
                  "title": "Payment API unavailable",
                  "severity": "SEV1",
                  "serviceId": "%s"
                }
                """.formatted(serviceId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents",
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
    void shouldRejectInvalidSeverity() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String requestBody = """
                {
                  "title": "Payment API unavailable",
                  "severity": "SEV0",
                  "serviceId": "%s",
                  "createdByUserId": "%s"
                }
                """.formatted(serviceId, userId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents",
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
    void shouldReturnNotFoundWhenServiceDoesNotExist()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(incidentService.create(
                eq(organizationId),
                eq("Payment API unavailable"),
                any(),
                eq(IncidentSeverity.SEV1),
                eq(serviceId),
                eq(userId)
        )).thenThrow(
                new ResourceNotFoundException(
                        "Service does not exist in this organization."
                )
        );

        String requestBody = """
                {
                  "title": "Payment API unavailable",
                  "severity": "SEV1",
                  "serviceId": "%s",
                  "createdByUserId": "%s"
                }
                """.formatted(serviceId, userId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents",
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
    void shouldReturnNotFoundWhenCreatorDoesNotExist()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(incidentService.create(
                eq(organizationId),
                eq("Payment API unavailable"),
                any(),
                eq(IncidentSeverity.SEV1),
                eq(serviceId),
                eq(userId)
        )).thenThrow(
                new ResourceNotFoundException(
                        "User does not exist in this organization."
                )
        );

        String requestBody = """
                {
                  "title": "Payment API unavailable",
                  "severity": "SEV1",
                  "serviceId": "%s",
                  "createdByUserId": "%s"
                }
                """.formatted(serviceId, userId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents",
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
    void shouldRejectInvalidOrganizationId() throws Exception {
        UUID serviceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        String requestBody = """
                {
                  "title": "Payment API unavailable",
                  "severity": "SEV1",
                  "serviceId": "%s",
                  "createdByUserId": "%s"
                }
                """.formatted(serviceId, userId);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents",
                                "not-a-uuid"
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_PARAMETER"));
    }

    @Test
    void shouldAcknowledgeIncidentThroughLifecycleEndpoint()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = new Organization(
                organizationId,
                "Acme Corporation",
                "acme-corporation",
                OrganizationStatus.ACTIVE,
                createdAt,
                createdAt
        );

        Team ownerTeam = new Team(
                teamId,
                organization,
                "Platform Engineering",
                null,
                TeamStatus.ACTIVE,
                createdAt,
                createdAt
        );

        com.opsguard.service.Service affectedService =
                new com.opsguard.service.Service(
                        serviceId,
                        organization,
                        "Payment API",
                        null,
                        ownerTeam,
                        ServiceCriticality.CRITICAL,
                        ServiceStatus.ACTIVE,
                        createdAt,
                        createdAt
                );

        User createdBy = new User(
                userId,
                organization,
                "engineer@acme.com",
                "John",
                "Doe",
                UserRole.ENGINEER,
                UserStatus.ACTIVE,
                createdAt,
                createdAt
        );

        OffsetDateTime acknowledgedAt =
                createdAt.plusMinutes(5);

        Incident incident = new Incident(
                incidentId,
                organization,
                "INC-000001",
                "Payment API unavailable",
                null,
                IncidentSeverity.SEV1,
                IncidentStatus.ACKNOWLEDGED,
                affectedService,
                null,
                null,
                createdBy,
                createdAt,
                acknowledgedAt,
                null,
                null,
                acknowledgedAt
        );

        when(incidentService.executeLifecycleAction(
                organizationId,
                incidentId,
                IncidentLifecycleAction.ACKNOWLEDGE
        )).thenReturn(incident);

        String requestBody = """
                {
                  "action": "ACKNOWLEDGE"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/lifecycle",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(incidentId.toString()))
                .andExpect(jsonPath("$.incidentNumber")
                        .value("INC-000001"))
                .andExpect(jsonPath("$.status")
                        .value("ACKNOWLEDGED"))
                .andExpect(jsonPath("$.acknowledgedAt")
                        .exists());

        verify(incidentService).executeLifecycleAction(
                organizationId,
                incidentId,
                IncidentLifecycleAction.ACKNOWLEDGE
        );
    }

    @Test
    void shouldRejectMissingLifecycleAction() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/lifecycle",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectInvalidLifecycleAction() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        String requestBody = """
                {
                  "action": "DELETE_EVERYTHING"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/lifecycle",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("MALFORMED_REQUEST"));
    }

    @Test
    void shouldReturnNotFoundWhenLifecycleIncidentDoesNotExist()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        when(incidentService.executeLifecycleAction(
                organizationId,
                incidentId,
                IncidentLifecycleAction.ACKNOWLEDGE
        )).thenThrow(
                new ResourceNotFoundException(
                        "Incident does not exist in this organization."
                )
        );

        String requestBody = """
                {
                  "action": "ACKNOWLEDGE"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/lifecycle",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldRejectInvalidLifecycleIncidentId() throws Exception {
        UUID organizationId = UUID.randomUUID();

        String requestBody = """
                {
                  "action": "ACKNOWLEDGE"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/lifecycle",
                                organizationId,
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