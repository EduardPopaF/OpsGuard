package com.opsguard.incident;

import com.opsguard.common.exception.InvalidStateTransitionException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationRepository;
import com.opsguard.organization.OrganizationStatus;
import com.opsguard.service.ServiceCriticality;
import com.opsguard.service.ServiceRepository;
import com.opsguard.service.ServiceStatus;
import com.opsguard.team.Team;
import com.opsguard.team.TeamStatus;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncidentServiceTest {

    private IncidentRepository incidentRepository;
    private OrganizationRepository organizationRepository;
    private ServiceRepository serviceRepository;
    private UserRepository userRepository;
    private IncidentNumberGenerator incidentNumberGenerator;
    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        incidentRepository = mock(IncidentRepository.class);
        organizationRepository = mock(OrganizationRepository.class);
        serviceRepository = mock(ServiceRepository.class);
        userRepository = mock(UserRepository.class);
        incidentNumberGenerator = mock(IncidentNumberGenerator.class);

        incidentService = new IncidentService(
                incidentRepository,
                organizationRepository,
                serviceRepository,
                userRepository,
                incidentNumberGenerator
        );
    }

    @Test
    void shouldCreateOpenIncidentAndNormalizeInput() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
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

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(serviceRepository.findByIdAndOrganizationId(
                serviceId,
                organizationId
        )).thenReturn(Optional.of(affectedService));

        when(userRepository.findByIdAndOrganizationId(
                userId,
                organizationId
        )).thenReturn(Optional.of(createdBy));

        when(incidentNumberGenerator.nextIncidentNumber(
                organizationId
        )).thenReturn("INC-000001");

        when(incidentRepository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incident result = incidentService.create(
                organizationId,
                "  Payment API unavailable  ",
                "  Customers cannot complete payments  ",
                IncidentSeverity.SEV1,
                serviceId,
                userId
        );

        assertNotNull(result.getId());
        assertEquals("INC-000001", result.getIncidentNumber());
        assertEquals("Payment API unavailable", result.getTitle());
        assertEquals(
                "Customers cannot complete payments",
                result.getDescription()
        );
        assertEquals(IncidentSeverity.SEV1, result.getSeverity());
        assertEquals(IncidentStatus.OPEN, result.getStatus());
        assertEquals(serviceId, result.getService().getId());
        assertEquals(userId, result.getCreatedBy().getId());

        assertNull(result.getAssignedTeam());
        assertNull(result.getAssignedUser());
        assertNull(result.getAcknowledgedAt());
        assertNull(result.getResolvedAt());
        assertNull(result.getClosedAt());

        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    void shouldConvertBlankDescriptionToNull() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
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

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(serviceRepository.findByIdAndOrganizationId(
                serviceId,
                organizationId
        )).thenReturn(Optional.of(affectedService));

        when(userRepository.findByIdAndOrganizationId(
                userId,
                organizationId
        )).thenReturn(Optional.of(createdBy));

        when(incidentNumberGenerator.nextIncidentNumber(
                organizationId
        )).thenReturn("INC-000001");

        when(incidentRepository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incident result = incidentService.create(
                organizationId,
                "Payment API unavailable",
                "   ",
                IncidentSeverity.SEV1,
                serviceId,
                userId
        );

        assertNull(result.getDescription());
    }

    @Test
    void shouldThrowWhenOrganizationDoesNotExist() {
        UUID organizationId = UUID.randomUUID();

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentService.create(
                        organizationId,
                        "Payment API unavailable",
                        null,
                        IncidentSeverity.SEV1,
                        UUID.randomUUID(),
                        UUID.randomUUID()
                )
        );

        verify(incidentNumberGenerator, never())
                .nextIncidentNumber(any());

        verify(incidentRepository, never())
                .save(any(Incident.class));
    }

    @Test
    void shouldRejectServiceFromDifferentOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = new Organization(
                organizationId,
                "Company A",
                "company-a",
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(serviceRepository.findByIdAndOrganizationId(
                serviceId,
                organizationId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentService.create(
                        organizationId,
                        "Payment API unavailable",
                        null,
                        IncidentSeverity.SEV1,
                        serviceId,
                        userId
                )
        );

        verify(userRepository, never())
                .findByIdAndOrganizationId(any(), any());

        verify(incidentNumberGenerator, never())
                .nextIncidentNumber(any());

        verify(incidentRepository, never())
                .save(any(Incident.class));
    }

    @Test
    void shouldRejectCreatorFromDifferentOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = new Organization(
                organizationId,
                "Company A",
                "company-a",
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

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(serviceRepository.findByIdAndOrganizationId(
                serviceId,
                organizationId
        )).thenReturn(Optional.of(affectedService));

        when(userRepository.findByIdAndOrganizationId(
                userId,
                organizationId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentService.create(
                        organizationId,
                        "Payment API unavailable",
                        null,
                        IncidentSeverity.SEV1,
                        serviceId,
                        userId
                )
        );

        verify(incidentNumberGenerator, never())
                .nextIncidentNumber(any());

        verify(incidentRepository, never())
                .save(any(Incident.class));
    }

    @Test
    void shouldAcknowledgeOpenIncident() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Incident incident = createIncident(
                incidentId,
                IncidentStatus.OPEN,
                createdAt
        );

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.of(incident));

        when(incidentRepository.save(incident))
                .thenReturn(incident);

        Incident result =
                incidentService.executeLifecycleAction(
                        organizationId,
                        incidentId,
                        IncidentLifecycleAction.ACKNOWLEDGE
                );

        assertEquals(
                IncidentStatus.ACKNOWLEDGED,
                result.getStatus()
        );

        assertNotNull(result.getAcknowledgedAt());

        verify(incidentRepository).save(incident);
    }

    @Test
    void shouldExecuteCompleteLifecycle() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Incident incident = createIncident(
                incidentId,
                IncidentStatus.OPEN,
                createdAt
        );

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.of(incident));

        when(incidentRepository.save(incident))
                .thenReturn(incident);

        incidentService.executeLifecycleAction(
                organizationId,
                incidentId,
                IncidentLifecycleAction.ACKNOWLEDGE
        );

        incidentService.executeLifecycleAction(
                organizationId,
                incidentId,
                IncidentLifecycleAction.START_INVESTIGATION
        );

        incidentService.executeLifecycleAction(
                organizationId,
                incidentId,
                IncidentLifecycleAction.MITIGATE
        );

        incidentService.executeLifecycleAction(
                organizationId,
                incidentId,
                IncidentLifecycleAction.START_MONITORING
        );

        incidentService.executeLifecycleAction(
                organizationId,
                incidentId,
                IncidentLifecycleAction.RESOLVE
        );

        Incident result =
                incidentService.executeLifecycleAction(
                        organizationId,
                        incidentId,
                        IncidentLifecycleAction.CLOSE
                );

        assertEquals(
                IncidentStatus.CLOSED,
                result.getStatus()
        );

        assertNotNull(result.getAcknowledgedAt());
        assertNotNull(result.getResolvedAt());
        assertNotNull(result.getClosedAt());
    }

    @Test
    void shouldRejectInvalidLifecycleTransition() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Incident incident = createIncident(
                incidentId,
                IncidentStatus.OPEN,
                createdAt
        );

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.of(incident));

        assertThrows(
                InvalidStateTransitionException.class,
                () -> incidentService.executeLifecycleAction(
                        organizationId,
                        incidentId,
                        IncidentLifecycleAction.CLOSE
                )
        );

        assertEquals(
                IncidentStatus.OPEN,
                incident.getStatus()
        );

        verify(incidentRepository, never())
                .save(any(Incident.class));
    }

    @Test
    void shouldRejectLifecycleActionForIncidentOutsideOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentService.executeLifecycleAction(
                        organizationId,
                        incidentId,
                        IncidentLifecycleAction.ACKNOWLEDGE
                )
        );

        verify(incidentRepository, never())
                .save(any(Incident.class));
    }

    private Incident createIncident(
            UUID incidentId,
            IncidentStatus status,
            OffsetDateTime createdAt
    ) {
        return new Incident(
                incidentId,
                null,
                "INC-000001",
                "Payment API unavailable",
                null,
                IncidentSeverity.SEV1,
                status,
                null,
                null,
                null,
                null,
                createdAt,
                null,
                null,
                null,
                createdAt
        );
    }
}