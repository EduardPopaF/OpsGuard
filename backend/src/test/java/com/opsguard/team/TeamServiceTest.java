package com.opsguard.team;

import com.opsguard.common.exception.ConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationRepository;
import com.opsguard.organization.OrganizationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeamServiceTest {

    private TeamRepository teamRepository;
    private OrganizationRepository organizationRepository;
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamRepository = mock(TeamRepository.class);
        organizationRepository = mock(OrganizationRepository.class);

        teamService = new TeamService(
                teamRepository,
                organizationRepository
        );
    }

    @Test
    void shouldCreateActiveTeamAndNormalizeInput() {
        UUID organizationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = new Organization(
                organizationId,
                "Acme Corporation",
                "acme-corporation",
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(java.util.Optional.of(organization));

        when(teamRepository.existsByOrganizationIdAndNameIgnoreCase(
                organizationId,
                "Infrastructure"
        )).thenReturn(false);

        when(teamRepository.save(any(Team.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Team result = teamService.create(
                organizationId,
                "  Infrastructure  ",
                "  Core infrastructure team  "
        );

        assertNotNull(result.getId());
        assertEquals(organizationId, result.getOrganization().getId());
        assertEquals("Infrastructure", result.getName());
        assertEquals(
                "Core infrastructure team",
                result.getDescription()
        );
        assertEquals(TeamStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(teamRepository)
                .existsByOrganizationIdAndNameIgnoreCase(
                        organizationId,
                        "Infrastructure"
                );

        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void shouldConvertBlankDescriptionToNull() {
        UUID organizationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = new Organization(
                organizationId,
                "Acme Corporation",
                "acme-corporation",
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(java.util.Optional.of(organization));

        when(teamRepository.existsByOrganizationIdAndNameIgnoreCase(
                organizationId,
                "Infrastructure"
        )).thenReturn(false);

        when(teamRepository.save(any(Team.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Team result = teamService.create(
                organizationId,
                "Infrastructure",
                "   "
        );

        assertNull(result.getDescription());

        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void shouldThrowWhenOrganizationDoesNotExist() {
        UUID organizationId = UUID.randomUUID();

        when(organizationRepository.findById(organizationId))
                .thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> teamService.create(
                        organizationId,
                        "Infrastructure",
                        "Core infrastructure team"
                )
        );

        assertEquals(
                "Organization with id '"
                        + organizationId
                        + "' does not exist.",
                exception.getMessage()
        );

        verify(teamRepository, never())
                .save(any(Team.class));
    }

    @Test
    void shouldThrowWhenTeamNameAlreadyExists() {
        UUID organizationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = new Organization(
                organizationId,
                "Acme Corporation",
                "acme-corporation",
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(java.util.Optional.of(organization));

        when(teamRepository.existsByOrganizationIdAndNameIgnoreCase(
                organizationId,
                "Infrastructure"
        )).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> teamService.create(
                        organizationId,
                        "  Infrastructure  ",
                        "Core infrastructure team"
                )
        );

        assertEquals(
                "A team with name 'Infrastructure' "
                        + "already exists in this organization.",
                exception.getMessage()
        );

        verify(teamRepository, never())
                .save(any(Team.class));
    }
}