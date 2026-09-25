package com.opsguard.incident;

import com.opsguard.common.exception.AssignmentConflictException;
import com.opsguard.common.exception.InvalidStateTransitionException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationRepository;
import com.opsguard.organization.OrganizationStatus;
import com.opsguard.service.ServiceCriticality;
import com.opsguard.service.ServiceRepository;
import com.opsguard.service.ServiceStatus;
import com.opsguard.team.Team;
import com.opsguard.team.TeamMemberRepository;
import com.opsguard.team.TeamRepository;
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
    private IncidentAssignmentRepository incidentAssignmentRepository;
    private TimelineEventRepository timelineEventRepository;
    private OrganizationRepository organizationRepository;
    private ServiceRepository serviceRepository;
    private UserRepository userRepository;
    private TeamRepository teamRepository;
    private TeamMemberRepository teamMemberRepository;
    private IncidentNumberGenerator incidentNumberGenerator;
    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        timelineEventRepository =
        mock(TimelineEventRepository.class);
        incidentRepository = mock(IncidentRepository.class);
        incidentAssignmentRepository =
                mock(IncidentAssignmentRepository.class);
        organizationRepository = mock(OrganizationRepository.class);
        serviceRepository = mock(ServiceRepository.class);
        userRepository = mock(UserRepository.class);
        teamRepository = mock(TeamRepository.class);
        teamMemberRepository = mock(TeamMemberRepository.class);
        incidentNumberGenerator = mock(IncidentNumberGenerator.class);

        incidentService = new IncidentService(
        incidentRepository,
        incidentAssignmentRepository,
        timelineEventRepository,
        organizationRepository,
        serviceRepository,
        userRepository,
        teamRepository,
        teamMemberRepository,
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

        Organization organization = createOrganization(
                organizationId,
                "Acme Corporation",
                "acme-corporation",
                now
        );

        Team ownerTeam = createTeam(
                teamId,
                organization,
                "Platform Engineering",
                now
        );

        com.opsguard.service.Service affectedService =
                createService(
                        serviceId,
                        organization,
                        ownerTeam,
                        now
                );

        User createdBy = createUser(
                userId,
                organization,
                "engineer@acme.com",
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
void shouldRecordTimelineEventWhenIncidentIsCreated() {
    UUID organizationId = UUID.randomUUID();
    UUID teamId = UUID.randomUUID();
    UUID serviceId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    OffsetDateTime now =
            OffsetDateTime.now(ZoneOffset.UTC);

    Organization organization = createOrganization(
            organizationId,
            "Acme Corporation",
            "acme-corporation",
            now
    );

    Team ownerTeam = createTeam(
            teamId,
            organization,
            "Platform Engineering",
            now
    );

    com.opsguard.service.Service affectedService =
            createService(
                    serviceId,
                    organization,
                    ownerTeam,
                    now
            );

    User createdBy = createUser(
            userId,
            organization,
            "engineer@acme.com",
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

    incidentService.create(
            organizationId,
            "Payment API unavailable",
            "Customers cannot complete payments",
            IncidentSeverity.SEV1,
            serviceId,
            userId
    );

    verify(timelineEventRepository)
            .save(any(TimelineEvent.class));
}     
    @Test
    void shouldConvertBlankDescriptionToNull() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = createOrganization(
                organizationId,
                "Acme Corporation",
                "acme-corporation",
                now
        );

        Team ownerTeam = createTeam(
                teamId,
                organization,
                "Platform Engineering",
                now
        );

        com.opsguard.service.Service affectedService =
                createService(
                        serviceId,
                        organization,
                        ownerTeam,
                        now
                );

        User createdBy = createUser(
                userId,
                organization,
                "engineer@acme.com",
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

        Organization organization = createOrganization(
                organizationId,
                "Company A",
                "company-a",
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

        Organization organization = createOrganization(
                organizationId,
                "Company A",
                "company-a",
                now
        );

        Team ownerTeam = createTeam(
                teamId,
                organization,
                "Platform Engineering",
                now
        );

        com.opsguard.service.Service affectedService =
                createService(
                        serviceId,
                        organization,
                        ownerTeam,
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
void shouldRecordTimelineEventWhenIncidentIsAcknowledged() {
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

    verify(timelineEventRepository)
            .save(any(TimelineEvent.class));
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

    @Test
    void shouldAssignTeamFromSameOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = createOrganization(
                organizationId,
                "Company A",
                "company-a",
                now
        );

        Team team = createTeam(
                teamId,
                organization,
                "Platform Engineering",
                now
        );

        Incident incident = createIncident(
                incidentId,
                IncidentStatus.OPEN,
                now
        );

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.of(incident));

        when(teamRepository.findByIdAndOrganizationId(
                teamId,
                organizationId
        )).thenReturn(Optional.of(team));

        when(incidentAssignmentRepository
                .findFirstByIncidentIdAndAssignedUserIsNotNullAndUnassignedAtIsNullOrderByAssignedAtDesc(
                        incidentId
                ))
                .thenReturn(Optional.empty());

        when(incidentAssignmentRepository
                .findFirstByIncidentIdAndAssignedTeamIsNotNullAndUnassignedAtIsNullOrderByAssignedAtDesc(
                        incidentId
                ))
                .thenReturn(Optional.empty());

        when(incidentRepository.save(incident))
                .thenReturn(incident);

        Incident result = incidentService.assignTeam(
                organizationId,
                incidentId,
                teamId
        );

        assertEquals(teamId, result.getAssignedTeam().getId());

        verify(teamRepository).findByIdAndOrganizationId(
                teamId,
                organizationId
        );

        verify(incidentAssignmentRepository)
                .save(any(IncidentAssignment.class));

        verify(incidentRepository).save(incident);
    }

        @Test
void shouldRecordTimelineEventWhenTeamIsAssigned() {
    UUID organizationId = UUID.randomUUID();
    UUID incidentId = UUID.randomUUID();
    UUID teamId = UUID.randomUUID();

    OffsetDateTime now =
            OffsetDateTime.now(ZoneOffset.UTC);

    Organization organization = createOrganization(
            organizationId,
            "Company A",
            "company-a",
            now
    );

    Team team = createTeam(
            teamId,
            organization,
            "Platform Engineering",
            now
    );

    Incident incident = createIncident(
            incidentId,
            IncidentStatus.OPEN,
            now
    );

    when(incidentRepository.findByIdAndOrganizationId(
            incidentId,
            organizationId
    )).thenReturn(Optional.of(incident));

    when(teamRepository.findByIdAndOrganizationId(
            teamId,
            organizationId
    )).thenReturn(Optional.of(team));

    when(incidentAssignmentRepository
            .findFirstByIncidentIdAndAssignedUserIsNotNullAndUnassignedAtIsNullOrderByAssignedAtDesc(
                    incidentId
            ))
            .thenReturn(Optional.empty());

    when(incidentAssignmentRepository
            .findFirstByIncidentIdAndAssignedTeamIsNotNullAndUnassignedAtIsNullOrderByAssignedAtDesc(
                    incidentId
            ))
            .thenReturn(Optional.empty());

    when(incidentRepository.save(incident))
            .thenReturn(incident);

    incidentService.assignTeam(
            organizationId,
            incidentId,
            teamId
    );

    verify(timelineEventRepository)
            .save(any(TimelineEvent.class));
}

        @Test
void shouldNotRecordTimelineEventWhenSameTeamIsAssignedAgain() {
    UUID organizationId = UUID.randomUUID();
    UUID incidentId = UUID.randomUUID();
    UUID teamId = UUID.randomUUID();

    OffsetDateTime now =
            OffsetDateTime.now(ZoneOffset.UTC);

    Organization organization = createOrganization(
            organizationId,
            "Company A",
            "company-a",
            now
    );

    Team team = createTeam(
            teamId,
            organization,
            "Platform Engineering",
            now
    );

    Incident incident = createIncident(
            incidentId,
            IncidentStatus.OPEN,
            now
    );

    incident.assignTeam(
            team,
            now
    );

    when(incidentRepository.findByIdAndOrganizationId(
            incidentId,
            organizationId
    )).thenReturn(Optional.of(incident));

    when(teamRepository.findByIdAndOrganizationId(
            teamId,
            organizationId
    )).thenReturn(Optional.of(team));

    when(incidentRepository.save(incident))
            .thenReturn(incident);

    incidentService.assignTeam(
            organizationId,
            incidentId,
            teamId
    );

    verify(timelineEventRepository, never())
            .save(any(TimelineEvent.class));
}

    @Test
    void shouldRejectTeamFromDifferentOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        Incident incident = createIncident(
                incidentId,
                IncidentStatus.OPEN,
                now
        );

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.of(incident));

        when(teamRepository.findByIdAndOrganizationId(
                teamId,
                organizationId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentService.assignTeam(
                        organizationId,
                        incidentId,
                        teamId
                )
        );

        assertNull(incident.getAssignedTeam());

        verify(incidentAssignmentRepository, never())
                .save(any(IncidentAssignment.class));

        verify(incidentRepository, never())
                .save(any(Incident.class));
    }

    @Test
    void shouldAssignUserWhenUserBelongsToAssignedTeam() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = createOrganization(
                organizationId,
                "Company A",
                "company-a",
                now
        );

        Team team = createTeam(
                teamId,
                organization,
                "Platform Engineering",
                now
        );

        User user = createUser(
                userId,
                organization,
                "engineer@company-a.com",
                now
        );

        Incident incident = createIncident(
                incidentId,
                IncidentStatus.OPEN,
                now
        );

        incident.assignTeam(
                team,
                now.plusMinutes(1)
        );

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.of(incident));

        when(userRepository.findByIdAndOrganizationId(
                userId,
                organizationId
        )).thenReturn(Optional.of(user));

        when(teamMemberRepository.existsByTeamIdAndUserId(
                teamId,
                userId
        )).thenReturn(true);

        when(incidentAssignmentRepository
                .findFirstByIncidentIdAndAssignedUserIsNotNullAndUnassignedAtIsNullOrderByAssignedAtDesc(
                        incidentId
                ))
                .thenReturn(Optional.empty());

        when(incidentRepository.save(incident))
                .thenReturn(incident);

        Incident result = incidentService.assignUser(
                organizationId,
                incidentId,
                userId
        );

        assertEquals(userId, result.getAssignedUser().getId());

        verify(userRepository).findByIdAndOrganizationId(
                userId,
                organizationId
        );

        verify(teamMemberRepository).existsByTeamIdAndUserId(
                teamId,
                userId
        );

        verify(incidentAssignmentRepository)
                .save(any(IncidentAssignment.class));

        verify(incidentRepository).save(incident);
    }

    @Test
    void shouldRejectUserAssignmentWhenIncidentHasNoAssignedTeam() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        Incident incident = createIncident(
                incidentId,
                IncidentStatus.OPEN,
                now
        );

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.of(incident));

        assertThrows(
                AssignmentConflictException.class,
                () -> incidentService.assignUser(
                        organizationId,
                        incidentId,
                        userId
                )
        );

        verify(userRepository, never())
                .findByIdAndOrganizationId(any(), any());

        verify(teamMemberRepository, never())
                .existsByTeamIdAndUserId(any(), any());

        verify(incidentAssignmentRepository, never())
                .save(any(IncidentAssignment.class));

        verify(incidentRepository, never())
                .save(any(Incident.class));
    }

    @Test
    void shouldRejectUserWhoIsNotMemberOfAssignedTeam() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = createOrganization(
                organizationId,
                "Company A",
                "company-a",
                now
        );

        Team team = createTeam(
                teamId,
                organization,
                "Platform Engineering",
                now
        );

        User user = createUser(
                userId,
                organization,
                "engineer@company-a.com",
                now
        );

        Incident incident = createIncident(
                incidentId,
                IncidentStatus.OPEN,
                now
        );

        incident.assignTeam(
                team,
                now.plusMinutes(1)
        );

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.of(incident));

        when(userRepository.findByIdAndOrganizationId(
                userId,
                organizationId
        )).thenReturn(Optional.of(user));

        when(teamMemberRepository.existsByTeamIdAndUserId(
                teamId,
                userId
        )).thenReturn(false);

        assertThrows(
                AssignmentConflictException.class,
                () -> incidentService.assignUser(
                        organizationId,
                        incidentId,
                        userId
                )
        );

        assertNull(incident.getAssignedUser());

        verify(incidentAssignmentRepository, never())
                .save(any(IncidentAssignment.class));

        verify(incidentRepository, never())
                .save(any(Incident.class));
    }

    @Test
    void shouldRejectUserFromDifferentOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = createOrganization(
                organizationId,
                "Company A",
                "company-a",
                now
        );

        Team team = createTeam(
                teamId,
                organization,
                "Platform Engineering",
                now
        );

        Incident incident = createIncident(
                incidentId,
                IncidentStatus.OPEN,
                now
        );

        incident.assignTeam(
                team,
                now.plusMinutes(1)
        );

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.of(incident));

        when(userRepository.findByIdAndOrganizationId(
                userId,
                organizationId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentService.assignUser(
                        organizationId,
                        incidentId,
                        userId
                )
        );

        assertNull(incident.getAssignedUser());

        verify(teamMemberRepository, never())
                .existsByTeamIdAndUserId(any(), any());

        verify(incidentAssignmentRepository, never())
                .save(any(IncidentAssignment.class));

        verify(incidentRepository, never())
                .save(any(Incident.class));
    }

    @Test
    void shouldUnassignUser() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        OffsetDateTime now =
        OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5);

        Organization organization = createOrganization(
                organizationId,
                "Company A",
                "company-a",
                now
        );

        User user = createUser(
                UUID.randomUUID(),
                organization,
                "engineer@company-a.com",
                now
        );

        Team team = createTeam(
                UUID.randomUUID(),
                organization,
                "Platform Engineering",
                now
        );

        Incident incident = createIncident(
                incidentId,
                IncidentStatus.OPEN,
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

        IncidentAssignment activeUserAssignment =
                new IncidentAssignment(
                        UUID.randomUUID(),
                        incident,
                        null,
                        user,
                        null,
                        now.plusMinutes(2),
                        null,
                        null
                );

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.of(incident));

        when(incidentAssignmentRepository
                .findFirstByIncidentIdAndAssignedUserIsNotNullAndUnassignedAtIsNullOrderByAssignedAtDesc(
                        incidentId
                ))
                .thenReturn(Optional.of(activeUserAssignment));

        when(incidentRepository.save(incident))
                .thenReturn(incident);

        Incident result = incidentService.unassignUser(
                organizationId,
                incidentId
        );

        assertNull(result.getAssignedUser());
        assertNotNull(activeUserAssignment.getUnassignedAt());

        verify(incidentAssignmentRepository)
                .save(activeUserAssignment);

        verify(incidentRepository).save(incident);
    }

    @Test
    void shouldUnassignTeamAndUserTogether() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        OffsetDateTime now =
        OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5);

        Organization organization = createOrganization(
                organizationId,
                "Company A",
                "company-a",
                now
        );

        Team team = createTeam(
                UUID.randomUUID(),
                organization,
                "Platform Engineering",
                now
        );

        User user = createUser(
                UUID.randomUUID(),
                organization,
                "engineer@company-a.com",
                now
        );

        Incident incident = createIncident(
                incidentId,
                IncidentStatus.OPEN,
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

        IncidentAssignment activeTeamAssignment =
                new IncidentAssignment(
                        UUID.randomUUID(),
                        incident,
                        team,
                        null,
                        null,
                        now.plusMinutes(1),
                        null,
                        null
                );

        IncidentAssignment activeUserAssignment =
                new IncidentAssignment(
                        UUID.randomUUID(),
                        incident,
                        null,
                        user,
                        null,
                        now.plusMinutes(2),
                        null,
                        null
                );

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.of(incident));

        when(incidentAssignmentRepository
                .findFirstByIncidentIdAndAssignedUserIsNotNullAndUnassignedAtIsNullOrderByAssignedAtDesc(
                        incidentId
                ))
                .thenReturn(Optional.of(activeUserAssignment));

        when(incidentAssignmentRepository
                .findFirstByIncidentIdAndAssignedTeamIsNotNullAndUnassignedAtIsNullOrderByAssignedAtDesc(
                        incidentId
                ))
                .thenReturn(Optional.of(activeTeamAssignment));

        when(incidentRepository.save(incident))
                .thenReturn(incident);

        Incident result = incidentService.unassignTeam(
                organizationId,
                incidentId
        );

        assertNull(result.getAssignedTeam());
        assertNull(result.getAssignedUser());

        assertNotNull(activeTeamAssignment.getUnassignedAt());
        assertNotNull(activeUserAssignment.getUnassignedAt());

        verify(incidentAssignmentRepository)
                .save(activeTeamAssignment);

        verify(incidentAssignmentRepository)
                .save(activeUserAssignment);

        verify(incidentRepository).save(incident);
    }

    @Test
    void shouldRejectAssignmentWhenIncidentIsOutsideOrganization() {
        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();

        when(incidentRepository.findByIdAndOrganizationId(
                incidentId,
                organizationId
        )).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentService.assignTeam(
                        organizationId,
                        incidentId,
                        teamId
                )
        );

        verify(teamRepository, never())
                .findByIdAndOrganizationId(any(), any());

        verify(incidentAssignmentRepository, never())
                .save(any(IncidentAssignment.class));

        verify(incidentRepository, never())
                .save(any(Incident.class));
    }

    private Organization createOrganization(
            UUID organizationId,
            String name,
            String slug,
            OffsetDateTime createdAt
    ) {
        return new Organization(
                organizationId,
                name,
                slug,
                OrganizationStatus.ACTIVE,
                createdAt,
                createdAt
        );
    }

    private Team createTeam(
            UUID teamId,
            Organization organization,
            String name,
            OffsetDateTime createdAt
    ) {
        return new Team(
                teamId,
                organization,
                name,
                null,
                TeamStatus.ACTIVE,
                createdAt,
                createdAt
        );
    }

    private User createUser(
            UUID userId,
            Organization organization,
            String email,
            OffsetDateTime createdAt
    ) {
        return new User(
                userId,
                organization,
                email,
                "John",
                "Doe",
                UserRole.ENGINEER,
                UserStatus.ACTIVE,
                createdAt,
                createdAt
        );
    }

    private com.opsguard.service.Service createService(
            UUID serviceId,
            Organization organization,
            Team ownerTeam,
            OffsetDateTime createdAt
    ) {
        return new com.opsguard.service.Service(
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