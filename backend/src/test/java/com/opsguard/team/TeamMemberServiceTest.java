package com.opsguard.team;

import com.opsguard.common.exception.ConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationStatus;
import com.opsguard.user.User;
import com.opsguard.user.UserRepository;
import com.opsguard.user.UserRole;
import com.opsguard.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeamMemberServiceTest {

    private TeamMemberRepository teamMemberRepository;
    private TeamRepository teamRepository;
    private UserRepository userRepository;
    private TeamMemberService teamMemberService;

    @BeforeEach
    void setUp() {
        teamMemberRepository = mock(TeamMemberRepository.class);
        teamRepository = mock(TeamRepository.class);
        userRepository = mock(UserRepository.class);

        teamMemberService = new TeamMemberService(
                teamMemberRepository,
                teamRepository,
                userRepository
        );
    }

    @Test
    void shouldAddMemberToTeam() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
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

        when(teamRepository.findByIdAndOrganizationId(
                teamId,
                organizationId
        )).thenReturn(Optional.of(team));

        when(userRepository.findByIdAndOrganizationId(
                userId,
                organizationId
        )).thenReturn(Optional.of(user));

        when(teamMemberRepository.existsByTeamIdAndUserId(
                teamId,
                userId
        )).thenReturn(false);

        when(teamMemberRepository.save(any(TeamMember.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TeamMember result = teamMemberService.addMember(
                organizationId,
                teamId,
                userId,
                TeamMemberRole.LEAD
        );

        assertNotNull(result.getId());
        assertEquals(teamId, result.getTeam().getId());
        assertEquals(userId, result.getUser().getId());
        assertEquals(
                TeamMemberRole.LEAD,
                result.getMembershipRole()
        );
        assertNotNull(result.getJoinedAt());

        verify(teamMemberRepository)
                .existsByTeamIdAndUserId(teamId, userId);

        verify(teamMemberRepository)
                .save(any(TeamMember.class));
    }

    @Test
    void shouldThrowWhenTeamDoesNotExistInOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(teamRepository.findByIdAndOrganizationId(
                teamId,
                organizationId
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> teamMemberService.addMember(
                        organizationId,
                        teamId,
                        userId,
                        TeamMemberRole.MEMBER
                )
        );

        assertEquals(
                "Team with id '"
                        + teamId
                        + "' does not exist in this organization.",
                exception.getMessage()
        );

        verify(userRepository, never())
                .findByIdAndOrganizationId(any(), any());

        verify(teamMemberRepository, never())
                .save(any(TeamMember.class));
    }

    @Test
    void shouldThrowWhenUserDoesNotExistInOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
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

        Team team = new Team(
                teamId,
                organization,
                "Infrastructure",
                null,
                TeamStatus.ACTIVE,
                now,
                now
        );

        when(teamRepository.findByIdAndOrganizationId(
                teamId,
                organizationId
        )).thenReturn(Optional.of(team));

        when(userRepository.findByIdAndOrganizationId(
                userId,
                organizationId
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> teamMemberService.addMember(
                        organizationId,
                        teamId,
                        userId,
                        TeamMemberRole.MEMBER
                )
        );

        assertEquals(
                "User with id '"
                        + userId
                        + "' does not exist in this organization.",
                exception.getMessage()
        );

        verify(teamMemberRepository, never())
                .save(any(TeamMember.class));
    }

    @Test
    void shouldRejectUserFromDifferentOrganization() {
        UUID organizationAId = UUID.randomUUID();
        UUID organizationBId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organizationA = new Organization(
                organizationAId,
                "Company A",
                "company-a",
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        Team team = new Team(
                teamId,
                organizationA,
                "Infrastructure",
                null,
                TeamStatus.ACTIVE,
                now,
                now
        );

        when(teamRepository.findByIdAndOrganizationId(
                teamId,
                organizationAId
        )).thenReturn(Optional.of(team));

        /*
         * The user may exist globally and belong to organization B.
         * For an operation scoped to organization A, however, the
         * tenant-scoped repository lookup must behave as not found.
         */
        when(userRepository.findByIdAndOrganizationId(
                userId,
                organizationAId
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> teamMemberService.addMember(
                        organizationAId,
                        teamId,
                        userId,
                        TeamMemberRole.MEMBER
                )
        );

        assertEquals(
                "User with id '"
                        + userId
                        + "' does not exist in this organization.",
                exception.getMessage()
        );

        verify(userRepository)
                .findByIdAndOrganizationId(
                        userId,
                        organizationAId
                );

        verify(userRepository, never())
                .findByIdAndOrganizationId(
                        userId,
                        organizationBId
                );

        verify(teamMemberRepository, never())
                .existsByTeamIdAndUserId(any(), any());

        verify(teamMemberRepository, never())
                .save(any(TeamMember.class));
    }

    @Test
    void shouldThrowWhenUserIsAlreadyMemberOfTeam() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
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

        Team team = new Team(
                teamId,
                organization,
                "Infrastructure",
                null,
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

        when(teamRepository.findByIdAndOrganizationId(
                teamId,
                organizationId
        )).thenReturn(Optional.of(team));

        when(userRepository.findByIdAndOrganizationId(
                userId,
                organizationId
        )).thenReturn(Optional.of(user));

        when(teamMemberRepository.existsByTeamIdAndUserId(
                teamId,
                userId
        )).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> teamMemberService.addMember(
                        organizationId,
                        teamId,
                        userId,
                        TeamMemberRole.MEMBER
                )
        );

        assertEquals(
                "User with id '"
                        + userId
                        + "' is already a member of team '"
                        + teamId
                        + "'.",
                exception.getMessage()
        );

        verify(teamMemberRepository, never())
                .save(any(TeamMember.class));
    }
}