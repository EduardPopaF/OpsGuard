package com.opsguard.incident;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentAssignmentRepository
        extends JpaRepository<IncidentAssignment, UUID> {

    List<IncidentAssignment>
    findAllByIncidentIdOrderByAssignedAtAsc(
            UUID incidentId
    );

    Optional<IncidentAssignment>
    findFirstByIncidentIdAndAssignedTeamIsNotNullAndUnassignedAtIsNullOrderByAssignedAtDesc(
            UUID incidentId
    );

    Optional<IncidentAssignment>
    findFirstByIncidentIdAndAssignedUserIsNotNullAndUnassignedAtIsNullOrderByAssignedAtDesc(
            UUID incidentId
    );
}