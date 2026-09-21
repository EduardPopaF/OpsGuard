package com.opsguard.incident;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository
        extends JpaRepository<Incident, UUID> {

    Optional<Incident> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    Optional<Incident> findByOrganizationIdAndIncidentNumber(
            UUID organizationId,
            String incidentNumber
    );

    boolean existsByOrganizationIdAndIncidentNumber(
            UUID organizationId,
            String incidentNumber
    );

    List<Incident> findAllByOrganizationId(
            UUID organizationId
    );

    List<Incident> findAllByOrganizationIdAndStatus(
            UUID organizationId,
            IncidentStatus status
    );

    List<Incident> findAllByOrganizationIdAndSeverity(
            UUID organizationId,
            IncidentSeverity severity
    );

    List<Incident> findAllByOrganizationIdAndServiceId(
            UUID organizationId,
            UUID serviceId
    );

    List<Incident> findAllByOrganizationIdAndAssignedTeamId(
            UUID organizationId,
            UUID assignedTeamId
    );

    List<Incident> findAllByOrganizationIdAndAssignedUserId(
            UUID organizationId,
            UUID assignedUserId
    );
}