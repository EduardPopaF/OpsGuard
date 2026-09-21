package com.opsguard.team.api;

import com.opsguard.common.api.GlobalExceptionHandler;
import com.opsguard.common.exception.ConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationStatus;
import com.opsguard.team.Team;
import com.opsguard.team.TeamService;
import com.opsguard.team.TeamStatus;
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

class TeamControllerTest {

    private TeamService teamService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        teamService = mock(TeamService.class);

        TeamController controller = new TeamController(teamService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateTeam() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = new Organization(
                organizationId,
                "Acme Corporation",
                "acme-corporation",
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        Team team = new Team(
                teamId,
                organization,
                "Infrastructure",
                "Core infrastructure team",
                TeamStatus.ACTIVE,
                now,
                now
        );

        when(teamService.create(
                organizationId,
                "Infrastructure",
                "Core infrastructure team"
        )).thenReturn(team);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/teams",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Infrastructure",
                                          "description": "Core infrastructure team"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.organizationId")
                        .value(organizationId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Infrastructure"))
                .andExpect(jsonPath("$.description")
                        .value("Core infrastructure team"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(teamService).create(
                organizationId,
                "Infrastructure",
                "Core infrastructure team"
        );
    }

    @Test
    void shouldReturnBadRequestForBlankName() throws Exception {
        UUID organizationId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/teams",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "   ",
                                          "description": "Core infrastructure team"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed."))
                .andExpect(jsonPath("$.errors.name")
                        .value("Team name is required."))
                .andExpect(jsonPath("$.path")
                        .value(
                                "/api/organizations/"
                                        + organizationId
                                        + "/teams"
                        ))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(teamService);
    }

    @Test
    void shouldReturnNotFoundWhenOrganizationDoesNotExist()
            throws Exception {
        UUID organizationId = UUID.randomUUID();

        when(teamService.create(
                organizationId,
                "Infrastructure",
                "Core infrastructure team"
        )).thenThrow(
                new ResourceNotFoundException(
                        "Organization with id '"
                                + organizationId
                                + "' does not exist."
                )
        );

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/teams",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Infrastructure",
                                          "description": "Core infrastructure team"
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
                .andExpect(jsonPath("$.timestamp").exists());

        verify(teamService).create(
                organizationId,
                "Infrastructure",
                "Core infrastructure team"
        );
    }

    @Test
    void shouldReturnConflictForDuplicateTeamName()
            throws Exception {
        UUID organizationId = UUID.randomUUID();

        when(teamService.create(
                organizationId,
                "Infrastructure",
                null
        )).thenThrow(
                new ConflictException(
                        "A team with name 'Infrastructure' "
                                + "already exists in this organization."
                )
        );

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/teams",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Infrastructure"
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "A team with name 'Infrastructure' "
                                        + "already exists in this organization."
                        ))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(teamService).create(
                organizationId,
                "Infrastructure",
                null
        );
    }

    @Test
    void shouldReturnBadRequestForInvalidOrganizationId()
            throws Exception {
        mockMvc.perform(
                        post("/api/organizations/not-a-uuid/teams")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Infrastructure"
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
                        .value(
                                "/api/organizations/not-a-uuid/teams"
                        ))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(teamService);
    }
}