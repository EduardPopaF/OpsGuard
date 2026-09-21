package com.opsguard.incident.api;

import com.opsguard.common.api.GlobalExceptionHandler;
import com.opsguard.common.exception.AssignmentConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.incident.Incident;
import com.opsguard.incident.IncidentService;
import com.opsguard.incident.IncidentSeverity;
import com.opsguard.incident.IncidentStatus;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationStatus;
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
import com.opsguard.service.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IncidentAssignmentControllerTest {

    private IncidentService incidentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        incidentService = mock(IncidentService.class);

        IncidentController incidentController =
                new IncidentController(incidentService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(incidentController)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
    }

    @Test
    void shouldAssignTeam()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(
                        organizationId,
                        now
                );

        Team team =
                createTeam(
                        teamId,
                        organization,
                        now
                );

        Incident incident =
                createIncident(
                        incidentId,
                        organization,
                        now
                );

        incident.assignTeam(
                team,
                now.plusMinutes(1)
        );

        when(incidentService.assignTeam(
                organizationId,
                incidentId,
                teamId
        )).thenReturn(incident);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/assignment/team",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "teamId": "%s"
                                        }
                                        """.formatted(teamId)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(incidentId.toString())
                )
                .andExpect(
                        jsonPath("$.assignedTeamId")
                                .value(teamId.toString())
                )
                .andExpect(
                        jsonPath("$.assignedUserId")
                                .doesNotExist()
                );

        verify(incidentService).assignTeam(
                organizationId,
                incidentId,
                teamId
        );
    }

    @Test
    void shouldAssignUser()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(
                        organizationId,
                        now
                );

        Team team =
                createTeam(
                        teamId,
                        organization,
                        now
                );

        User user =
                createUser(
                        userId,
                        organization,
                        now
                );

        Incident incident =
                createIncident(
                        incidentId,
                        organization,
                        now
                );

        incident.assignTeam(
                team,
                now.plusMinutes(1)
        );

        incident.assignUser(
                user,
                now.plusMinutes(2)
        );

        when(incidentService.assignUser(
                organizationId,
                incidentId,
                userId
        )).thenReturn(incident);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/assignment/user",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "userId": "%s"
                                        }
                                        """.formatted(userId)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(incidentId.toString())
                )
                .andExpect(
                        jsonPath("$.assignedTeamId")
                                .value(teamId.toString())
                )
                .andExpect(
                        jsonPath("$.assignedUserId")
                                .value(userId.toString())
                );

        verify(incidentService).assignUser(
                organizationId,
                incidentId,
                userId
        );
    }

    @Test
    void shouldRejectMissingTeamId()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/assignment/team",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "teamId": null
                                        }
                                        """
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.errors.teamId")
                                .value("Team ID is required.")
                );
    }

    @Test
    void shouldRejectMissingUserId()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/assignment/user",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "userId": null
                                        }
                                        """
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.errors.userId")
                                .value("User ID is required.")
                );
    }

    @Test
    void shouldReturnNotFoundWhenTeamDoesNotExistInOrganization()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        when(incidentService.assignTeam(
                organizationId,
                incidentId,
                teamId
        )).thenThrow(
                new ResourceNotFoundException(
                        "Team with id '"
                                + teamId
                                + "' does not exist in this organization."
                )
        );

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/assignment/team",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "teamId": "%s"
                                        }
                                        """.formatted(teamId)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("RESOURCE_NOT_FOUND")
                );
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExistInOrganization()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(incidentService.assignUser(
                organizationId,
                incidentId,
                userId
        )).thenThrow(
                new ResourceNotFoundException(
                        "User with id '"
                                + userId
                                + "' does not exist in this organization."
                )
        );

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/assignment/user",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "userId": "%s"
                                        }
                                        """.formatted(userId)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("RESOURCE_NOT_FOUND")
                );
    }

    @Test
    void shouldReturnConflictForAssignmentConflict()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(incidentService.assignUser(
                organizationId,
                incidentId,
                userId
        )).thenThrow(
                new AssignmentConflictException(
                        "User is not a member of the assigned team."
                )
        );

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/assignment/user",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "userId": "%s"
                                        }
                                        """.formatted(userId)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("ASSIGNMENT_CONFLICT")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "User is not a member of the assigned team."
                                )
                );
    }

    @Test
    void shouldRejectInvalidOrganizationId()
            throws Exception {

        UUID incidentId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/assignment/team",
                                "not-a-uuid",
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "teamId": "%s"
                                        }
                                        """.formatted(teamId)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_PARAMETER")
                );
    }

    @Test
    void shouldRejectInvalidIncidentId()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/assignment/user",
                                organizationId,
                                "not-a-uuid"
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "userId": "%s"
                                        }
                                        """.formatted(userId)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_PARAMETER")
                );
    }

    @Test
    void shouldRejectMalformedTeamId()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/assignment/team",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "teamId": "not-a-uuid"
                                        }
                                        """
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("MALFORMED_REQUEST")
                );
    }

    @Test
    void shouldRejectMalformedUserId()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/assignment/user",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "userId": "not-a-uuid"
                                        }
                                        """
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.code")
                                .value("MALFORMED_REQUEST")
                );
    }

    private Organization createOrganization(
            UUID organizationId,
            OffsetDateTime createdAt
    ) {
        return new Organization(
                organizationId,
                "Company A",
                "company-a",
                OrganizationStatus.ACTIVE,
                createdAt,
                createdAt
        );
    }

    private Team createTeam(
            UUID teamId,
            Organization organization,
            OffsetDateTime createdAt
    ) {
        return new Team(
                teamId,
                organization,
                "Platform Engineering",
                null,
                TeamStatus.ACTIVE,
                createdAt,
                createdAt
        );
    }

    private User createUser(
            UUID userId,
            Organization organization,
            OffsetDateTime createdAt
    ) {
        return new User(
                userId,
                organization,
                "engineer@company-a.com",
                "John",
                "Doe",
                UserRole.ENGINEER,
                UserStatus.ACTIVE,
                createdAt,
                createdAt
        );
    }

    private Incident createIncident(
        UUID incidentId,
        Organization organization,
        OffsetDateTime createdAt
) {
    UUID serviceId = UUID.randomUUID();

    Service service = mock(Service.class);

    when(service.getId())
            .thenReturn(serviceId);

    User createdBy = createUser(
            UUID.randomUUID(),
            organization,
            createdAt
    );

    return new Incident(
            incidentId,
            organization,
            "INC-000001",
            "Payment API unavailable",
            null,
            IncidentSeverity.SEV1,
            IncidentStatus.OPEN,
            service,
            null,
            null,
            createdBy,
            createdAt,
            null,
            null,
            null,
            createdAt
    );
}
}