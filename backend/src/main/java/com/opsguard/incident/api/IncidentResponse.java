package com.opsguard.incident.api;

import com.opsguard.incident.Incident;
import com.opsguard.incident.IncidentSeverity;
import com.opsguard.incident.IncidentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record IncidentResponse(
        UUID id,
        UUID organizationId,
        String incidentNumber,
        String title,
        String description,
        IncidentSeverity severity,
        IncidentStatus status,
        UUID serviceId,
        UUID assignedTeamId,
        UUID assignedUserId,
        UUID createdByUserId,
        OffsetDateTime createdAt,
        OffsetDateTime acknowledgedAt,
        OffsetDateTime resolvedAt,
        OffsetDateTime closedAt,
        OffsetDateTime updatedAt
) {

    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getOrganization().getId(),
                incident.getIncidentNumber(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getService().getId(),
                incident.getAssignedTeam() == null
                        ? null
                        : incident.getAssignedTeam().getId(),
                incident.getAssignedUser() == null
                        ? null
                        : incident.getAssignedUser().getId(),
                incident.getCreatedBy().getId(),
                incident.getCreatedAt(),
                incident.getAcknowledgedAt(),
                incident.getResolvedAt(),
                incident.getClosedAt(),
                incident.getUpdatedAt()
        );
    }
}