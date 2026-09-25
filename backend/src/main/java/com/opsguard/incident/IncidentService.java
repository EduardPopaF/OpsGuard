package com.opsguard.incident;

import com.opsguard.common.exception.AssignmentConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationRepository;
import com.opsguard.service.ServiceRepository;
import com.opsguard.team.Team;
import com.opsguard.team.TeamMemberRepository;
import com.opsguard.team.TeamRepository;
import com.opsguard.user.User;
import com.opsguard.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@org.springframework.stereotype.Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentAssignmentRepository incidentAssignmentRepository;
    private final OrganizationRepository organizationRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final IncidentNumberGenerator incidentNumberGenerator;

    public IncidentService(
            IncidentRepository incidentRepository,
            IncidentAssignmentRepository incidentAssignmentRepository,
            OrganizationRepository organizationRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository,
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            IncidentNumberGenerator incidentNumberGenerator
    ) {
        this.incidentRepository = incidentRepository;
        this.incidentAssignmentRepository = incidentAssignmentRepository;
        this.organizationRepository = organizationRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.incidentNumberGenerator = incidentNumberGenerator;
    }

    @Transactional
    public Incident create(
            UUID organizationId,
            String title,
            String description,
            IncidentSeverity severity,
            UUID serviceId,
            UUID createdByUserId
    ) {
        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization with id '"
                                + organizationId
                                + "' does not exist."
                ));

        com.opsguard.service.Service affectedService =
                serviceRepository
                        .findByIdAndOrganizationId(
                                serviceId,
                                organizationId
                        )
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Service with id '"
                                        + serviceId
                                        + "' does not exist in this organization."
                        ));

        User createdBy = userRepository
                .findByIdAndOrganizationId(
                        createdByUserId,
                        organizationId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id '"
                                + createdByUserId
                                + "' does not exist in this organization."
                ));

        String normalizedTitle = title.trim();

        String normalizedDescription =
                description == null || description.isBlank()
                        ? null
                        : description.trim();

        String incidentNumber =
                incidentNumberGenerator.nextIncidentNumber(
                        organizationId
                );

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        Incident incident = new Incident(
                UUID.randomUUID(),
                organization,
                incidentNumber,
                normalizedTitle,
                normalizedDescription,
                severity,
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

        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident executeLifecycleAction(
            UUID organizationId,
            UUID incidentId,
            IncidentLifecycleAction action
    ) {
        Incident incident = findIncident(
                organizationId,
                incidentId
        );

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        switch (action) {
            case ACKNOWLEDGE ->
                    incident.acknowledge(now);
            case START_INVESTIGATION ->
                    incident.startInvestigation(now);
            case MITIGATE ->
                    incident.mitigate(now);
            case START_MONITORING ->
                    incident.startMonitoring(now);
            case RESOLVE ->
                    incident.resolve(now);
            case CLOSE ->
                    incident.close(now);
        }

        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident assignTeam(
            UUID organizationId,
            UUID incidentId,
            UUID teamId
    ) {
        Incident incident = findIncident(
                organizationId,
                incidentId
        );

        Team team = teamRepository
                .findByIdAndOrganizationId(
                        teamId,
                        organizationId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team with id '"
                                + teamId
                                + "' does not exist in this organization."
                ));

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        Team currentTeam = incident.getAssignedTeam();

        if (currentTeam != null
                && currentTeam.getId().equals(team.getId())) {
            incident.assignTeam(
                    team,
                    now
            );

            return incidentRepository.save(incident);
        }

        closeActiveUserAssignment(
                incidentId,
                now
        );

        closeActiveTeamAssignment(
                incidentId,
                now
        );

        incident.assignTeam(
                team,
                now
        );

        IncidentAssignment assignment =
                new IncidentAssignment(
                        UUID.randomUUID(),
                        incident,
                        team,
                        null,
                        null,
                        now,
                        null,
                        null
                );

        incidentAssignmentRepository.save(
                assignment
        );

        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident assignUser(
            UUID organizationId,
            UUID incidentId,
            UUID userId
    ) {
        Incident incident = findIncident(
                organizationId,
                incidentId
        );

        Team assignedTeam = incident.getAssignedTeam();

        if (assignedTeam == null) {
            throw new AssignmentConflictException(
                    "An incident must have an assigned team before a user can be assigned."
            );
        }

        User user = userRepository
                .findByIdAndOrganizationId(
                        userId,
                        organizationId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id '"
                                + userId
                                + "' does not exist in this organization."
                ));

        boolean isTeamMember =
                teamMemberRepository.existsByTeamIdAndUserId(
                        assignedTeam.getId(),
                        userId
                );

        if (!isTeamMember) {
            throw new AssignmentConflictException(
                    "User with id '"
                            + userId
                            + "' is not a member of the assigned team."
            );
        }

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        User currentUser = incident.getAssignedUser();

        if (currentUser != null
                && currentUser.getId().equals(user.getId())) {
            incident.assignUser(
                    user,
                    now
            );

            return incidentRepository.save(incident);
        }

        closeActiveUserAssignment(
                incidentId,
                now
        );

        incident.assignUser(
                user,
                now
        );

        IncidentAssignment assignment =
                new IncidentAssignment(
                        UUID.randomUUID(),
                        incident,
                        null,
                        user,
                        null,
                        now,
                        null,
                        null
                );

        incidentAssignmentRepository.save(
                assignment
        );

        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident unassignUser(
            UUID organizationId,
            UUID incidentId
    ) {
        Incident incident = findIncident(
                organizationId,
                incidentId
        );

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        if (incident.getAssignedUser() == null) {
            incident.unassignUser(
                    now
            );

            return incidentRepository.save(incident);
        }

        closeActiveUserAssignment(
                incidentId,
                now
        );

        incident.unassignUser(
                now
        );

        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident unassignTeam(
            UUID organizationId,
            UUID incidentId
    ) {
        Incident incident = findIncident(
                organizationId,
                incidentId
        );

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        if (incident.getAssignedTeam() == null) {
            incident.unassignTeam(
                    now
            );

            return incidentRepository.save(incident);
        }

        closeActiveUserAssignment(
                incidentId,
                now
        );

        closeActiveTeamAssignment(
                incidentId,
                now
        );

        incident.unassignTeam(
                now
        );

        return incidentRepository.save(incident);
    }

    private void closeActiveTeamAssignment(
            UUID incidentId,
            OffsetDateTime occurredAt
    ) {
        incidentAssignmentRepository
                .findFirstByIncidentIdAndAssignedTeamIsNotNullAndUnassignedAtIsNullOrderByAssignedAtDesc(
                        incidentId
                )
                .ifPresent(assignment -> {
                    assignment.close(
                            occurredAt
                    );

                    incidentAssignmentRepository.save(
                            assignment
                    );
                });
    }

    private void closeActiveUserAssignment(
            UUID incidentId,
            OffsetDateTime occurredAt
    ) {
        incidentAssignmentRepository
                .findFirstByIncidentIdAndAssignedUserIsNotNullAndUnassignedAtIsNullOrderByAssignedAtDesc(
                        incidentId
                )
                .ifPresent(assignment -> {
                    assignment.close(
                            occurredAt
                    );

                    incidentAssignmentRepository.save(
                            assignment
                    );
                });
    }

    private Incident findIncident(
            UUID organizationId,
            UUID incidentId
    ) {
        return incidentRepository
                .findByIdAndOrganizationId(
                        incidentId,
                        organizationId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Incident with id '"
                                + incidentId
                                + "' does not exist in this organization."
                ));
    }
}