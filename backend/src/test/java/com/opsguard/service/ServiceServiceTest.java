package com.opsguard.service;

import com.opsguard.common.exception.ConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationRepository;
import com.opsguard.organization.OrganizationStatus;
import com.opsguard.team.Team;
import com.opsguard.team.TeamRepository;
import com.opsguard.team.TeamStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
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

class ServiceServiceTest {

    private ServiceRepository serviceRepository;
    private OrganizationRepository organizationRepository;
    private TeamRepository teamRepository;
    private ServiceService serviceService;

    @BeforeEach
    void setUp() {
        serviceRepository = mock(ServiceRepository.class);
        organizationRepository = mock(OrganizationRepository.class);
        teamRepository = mock(TeamRepository.class);

        serviceService = new ServiceService(
                serviceRepository,
                organizationRepository,
                teamRepository
        );
    }

    @Test
    void shouldCreateActiveServiceAndNormalizeInput() {
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

        Team ownerTeam = new Team(
                teamId,
                organization,
                "Platform Engineering",
                "Platform engineering team",
                TeamStatus.ACTIVE,
                now,
                now
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(teamRepository.findByIdAndOrganizationId(
                teamId,
                organizationId
        )).thenReturn(Optional.of(ownerTeam));

        when(serviceRepository
                .existsByOrganizationIdAndNameIgnoreCase(
                        organizationId,
                        "Authentication API"
                ))
                .thenReturn(false);

        when(serviceRepository.save(any(Service.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Service result = serviceService.create(
                organizationId,
                "  Authentication API  ",
                "  Handles user authentication  ",
                teamId,
                ServiceCriticality.CRITICAL
        );

        assertNotNull(result.getId());
        assertEquals(
                organizationId,
                result.getOrganization().getId()
        );
        assertEquals("Authentication API", result.getName());
        assertEquals(
                "Handles user authentication",
                result.getDescription()
        );
        assertEquals(teamId, result.getOwnerTeam().getId());
        assertEquals(
                ServiceCriticality.CRITICAL,
                result.getCriticality()
        );
        assertEquals(ServiceStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void shouldConvertBlankDescriptionToNull() {
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

        Team ownerTeam = new Team(
                teamId,
                organization,
                "Platform Engineering",
                null,
                TeamStatus.ACTIVE,
                now,
                now
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(teamRepository.findByIdAndOrganizationId(
                teamId,
                organizationId
        )).thenReturn(Optional.of(ownerTeam));

        when(serviceRepository
                .existsByOrganizationIdAndNameIgnoreCase(
                        organizationId,
                        "Authentication API"
                ))
                .thenReturn(false);

        when(serviceRepository.save(any(Service.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Service result = serviceService.create(
                organizationId,
                "Authentication API",
                "   ",
                teamId,
                ServiceCriticality.CRITICAL
        );

        assertNull(result.getDescription());

        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void shouldThrowWhenOrganizationDoesNotExist() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> serviceService.create(
                        organizationId,
                        "Authentication API",
                        null,
                        teamId,
                        ServiceCriticality.CRITICAL
                )
        );

        assertEquals(
                "Organization with id '"
                        + organizationId
                        + "' does not exist.",
                exception.getMessage()
        );

        verify(teamRepository, never())
                .findByIdAndOrganizationId(any(), any());

        verify(serviceRepository, never())
                .save(any(Service.class));
    }

    @Test
    void shouldThrowWhenOwnerTeamDoesNotExistInOrganization() {
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

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(teamRepository.findByIdAndOrganizationId(
                teamId,
                organizationId
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> serviceService.create(
                        organizationId,
                        "Authentication API",
                        null,
                        teamId,
                        ServiceCriticality.CRITICAL
                )
        );

        assertEquals(
                "Team with id '"
                        + teamId
                        + "' does not exist in this organization.",
                exception.getMessage()
        );

        verify(serviceRepository, never())
                .save(any(Service.class));
    }

    @Test
    void shouldRejectOwnerTeamFromDifferentOrganization() {
        UUID organizationAId = UUID.randomUUID();
        UUID organizationBId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organizationA = new Organization(
                organizationAId,
                "Company A",
                "company-a",
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        when(organizationRepository.findById(organizationAId))
                .thenReturn(Optional.of(organizationA));

        when(teamRepository.findByIdAndOrganizationId(
                teamId,
                organizationAId
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> serviceService.create(
                        organizationAId,
                        "Authentication API",
                        null,
                        teamId,
                        ServiceCriticality.CRITICAL
                )
        );

        assertEquals(
                "Team with id '"
                        + teamId
                        + "' does not exist in this organization.",
                exception.getMessage()
        );

        verify(teamRepository)
                .findByIdAndOrganizationId(
                        teamId,
                        organizationAId
                );

        verify(teamRepository, never())
                .findByIdAndOrganizationId(
                        teamId,
                        organizationBId
                );

        verify(serviceRepository, never())
                .save(any(Service.class));
    }

    @Test
    void shouldThrowWhenServiceNameAlreadyExists() {
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

        Team ownerTeam = new Team(
                teamId,
                organization,
                "Platform Engineering",
                null,
                TeamStatus.ACTIVE,
                now,
                now
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(teamRepository.findByIdAndOrganizationId(
                teamId,
                organizationId
        )).thenReturn(Optional.of(ownerTeam));

        when(serviceRepository
                .existsByOrganizationIdAndNameIgnoreCase(
                        organizationId,
                        "Authentication API"
                ))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> serviceService.create(
                        organizationId,
                        "  Authentication API  ",
                        null,
                        teamId,
                        ServiceCriticality.CRITICAL
                )
        );

        assertEquals(
                "A service with name 'Authentication API' "
                        + "already exists in this organization.",
                exception.getMessage()
        );

        verify(serviceRepository, never())
                .save(any(Service.class));
    }
}