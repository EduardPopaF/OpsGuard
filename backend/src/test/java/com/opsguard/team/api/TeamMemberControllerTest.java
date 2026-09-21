package com.opsguard.team.api;

import com.opsguard.common.api.GlobalExceptionHandler;
import com.opsguard.common.exception.ConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationStatus;
import com.opsguard.team.Team;
import com.opsguard.team.TeamMember;
import com.opsguard.team.TeamMemberRole;
import com.opsguard.team.TeamMemberService;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeamMemberControllerTest {

    private TeamMemberService teamMemberService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        teamMemberService = mock(TeamMemberService.class);

        TeamMemberController controller =
                new TeamMemberController(teamMemberService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldAddMemberToTeam() throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID teamMemberId = UUID.randomUUID();

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

        User user = new User(
                userId,
                organization,
                "andrei@example.com",
                "Andrei",
                "Popescu",
                UserRole.ENGINEER,
                UserStatus.ACTIVE,
                now,
                now
        );

        TeamMember teamMember = new TeamMember(
                teamMemberId,
                team,
                user,
                TeamMemberRole.LEAD,
                now
        );

        when(teamMemberService.addMember(
                organizationId,
                teamId,
                userId,
                TeamMemberRole.LEAD
        )).thenReturn(teamMember);

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}"
                                        + "/teams/{teamId}/members",
                                organizationId,
                                teamId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": "%s",
                                          "membershipRole": "LEAD"
                                        }
                                        """.formatted(userId))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(teamMemberId.toString()))
                .andExpect(jsonPath("$.teamId")
                        .value(teamId.toString()))
                .andExpect(jsonPath("$.userId")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.membershipRole")
                        .value("LEAD"))
                .andExpect(jsonPath("$.joinedAt").exists());

        verify(teamMemberService).addMember(
                organizationId,
                teamId,
                userId,
                TeamMemberRole.LEAD
        );
    }

    @Test
    void shouldReturnBadRequestWhenUserIdIsMissing()
            throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}"
                                        + "/teams/{teamId}/members",
                                organizationId,
                                teamId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "membershipRole": "MEMBER"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.userId")
                        .value("User ID is required."));

        verifyNoInteractions(teamMemberService);
    }

    @Test
    void shouldReturnBadRequestWhenMembershipRoleIsMissing()
            throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}"
                                        + "/teams/{teamId}/members",
                                organizationId,
                                teamId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": "%s"
                                        }
                                        """.formatted(userId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.membershipRole")
                        .value("Membership role is required."));

        verifyNoInteractions(teamMemberService);
    }

    @Test
    void shouldReturnBadRequestForInvalidMembershipRole()
            throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}"
                                        + "/teams/{teamId}/members",
                                organizationId,
                                teamId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": "%s",
                                          "membershipRole": "OWNER"
                                        }
                                        """.formatted(userId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("MALFORMED_REQUEST"));

        verifyNoInteractions(teamMemberService);
    }

    @Test
    void shouldReturnNotFoundWhenTeamDoesNotExist()
            throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(teamMemberService.addMember(
                organizationId,
                teamId,
                userId,
                TeamMemberRole.MEMBER
        )).thenThrow(
                new ResourceNotFoundException(
                        "Team with id '"
                                + teamId
                                + "' does not exist in this organization."
                )
        );

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}"
                                        + "/teams/{teamId}/members",
                                organizationId,
                                teamId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": "%s",
                                          "membershipRole": "MEMBER"
                                        }
                                        """.formatted(userId))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Team with id '"
                                        + teamId
                                        + "' does not exist in this organization."
                        ));

        verify(teamMemberService).addMember(
                organizationId,
                teamId,
                userId,
                TeamMemberRole.MEMBER
        );
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist()
            throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(teamMemberService.addMember(
                organizationId,
                teamId,
                userId,
                TeamMemberRole.MEMBER
        )).thenThrow(
                new ResourceNotFoundException(
                        "User with id '"
                                + userId
                                + "' does not exist in this organization."
                )
        );

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}"
                                        + "/teams/{teamId}/members",
                                organizationId,
                                teamId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": "%s",
                                          "membershipRole": "MEMBER"
                                        }
                                        """.formatted(userId))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "User with id '"
                                        + userId
                                        + "' does not exist in this organization."
                        ));

        verify(teamMemberService).addMember(
                organizationId,
                teamId,
                userId,
                TeamMemberRole.MEMBER
        );
    }

    @Test
    void shouldReturnConflictWhenUserIsAlreadyMember()
            throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(teamMemberService.addMember(
                organizationId,
                teamId,
                userId,
                TeamMemberRole.MEMBER
        )).thenThrow(
                new ConflictException(
                        "User with id '"
                                + userId
                                + "' is already a member of team '"
                                + teamId
                                + "'."
                )
        );

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}"
                                        + "/teams/{teamId}/members",
                                organizationId,
                                teamId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": "%s",
                                          "membershipRole": "MEMBER"
                                        }
                                        """.formatted(userId))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        verify(teamMemberService).addMember(
                organizationId,
                teamId,
                userId,
                TeamMemberRole.MEMBER
        );
    }

    @Test
    void shouldReturnBadRequestForInvalidOrganizationId()
            throws Exception {
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/not-a-uuid"
                                        + "/teams/{teamId}/members",
                                teamId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": "%s",
                                          "membershipRole": "MEMBER"
                                        }
                                        """.formatted(userId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.errors.organizationId")
                        .value("Invalid value."));

        verifyNoInteractions(teamMemberService);
    }

    @Test
    void shouldReturnBadRequestForInvalidTeamId()
            throws Exception {
        UUID organizationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}"
                                        + "/teams/not-a-uuid/members",
                                organizationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": "%s",
                                          "membershipRole": "MEMBER"
                                        }
                                        """.formatted(userId))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.errors.teamId")
                        .value("Invalid value."));

        verifyNoInteractions(teamMemberService);
    }
}