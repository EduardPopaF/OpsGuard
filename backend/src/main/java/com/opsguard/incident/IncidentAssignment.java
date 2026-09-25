package com.opsguard.incident;

import com.opsguard.team.Team;
import com.opsguard.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "incident_assignments")
public class IncidentAssignment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_team_id")
    private Team assignedTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "unassigned_at")
    private OffsetDateTime unassignedAt;

    @Column(length = 500)
    private String reason;

    protected IncidentAssignment() {
    }

    public IncidentAssignment(
            UUID id,
            Incident incident,
            Team assignedTeam,
            User assignedUser,
            User assignedBy,
            OffsetDateTime assignedAt,
            OffsetDateTime unassignedAt,
            String reason
    ) {
        if (incident == null) {
            throw new IllegalArgumentException(
                    "Incident is required."
            );
        }

        if (assignedTeam == null && assignedUser == null) {
            throw new IllegalArgumentException(
                    "An assignment must contain a team or a user."
            );
        }

        if (assignedAt == null) {
            throw new IllegalArgumentException(
                    "Assignment time is required."
            );
        }

        if (unassignedAt != null
                && unassignedAt.isBefore(assignedAt)) {
            throw new IllegalArgumentException(
                    "Unassignment time cannot be before assignment time."
            );
        }

        this.id = id;
        this.incident = incident;
        this.assignedTeam = assignedTeam;
        this.assignedUser = assignedUser;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
        this.unassignedAt = unassignedAt;
        this.reason = normalizeReason(reason);
    }

    public void close(OffsetDateTime occurredAt) {
        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "Unassignment time is required."
            );
        }

        if (occurredAt.isBefore(assignedAt)) {
            throw new IllegalArgumentException(
                    "Unassignment time cannot be before assignment time."
            );
        }

        if (unassignedAt != null) {
            throw new IllegalStateException(
                    "Assignment is already closed."
            );
        }

        unassignedAt = occurredAt;
    }

    private String normalizeReason(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public Team getAssignedTeam() {
        return assignedTeam;
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public OffsetDateTime getUnassignedAt() {
        return unassignedAt;
    }

    public String getReason() {
        return reason;
    }

    public boolean isActive() {
        return unassignedAt == null;
    }
}