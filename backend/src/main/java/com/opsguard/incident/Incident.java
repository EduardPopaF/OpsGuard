package com.opsguard.incident;

import com.opsguard.common.exception.IncidentModificationNotAllowedException;
import com.opsguard.common.exception.InvalidStateTransitionException;
import com.opsguard.organization.Organization;
import com.opsguard.service.Service;
import com.opsguard.team.Team;
import com.opsguard.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "incident_number", nullable = false, length = 50)
    private String incidentNumber;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_team_id")
    private Team assignedTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "acknowledged_at")
    private OffsetDateTime acknowledgedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Incident() {
    }

    public Incident(
            UUID id,
            Organization organization,
            String incidentNumber,
            String title,
            String description,
            IncidentSeverity severity,
            IncidentStatus status,
            Service service,
            Team assignedTeam,
            User assignedUser,
            User createdBy,
            OffsetDateTime createdAt,
            OffsetDateTime acknowledgedAt,
            OffsetDateTime resolvedAt,
            OffsetDateTime closedAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.organization = organization;
        this.incidentNumber = incidentNumber;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.service = service;
        this.assignedTeam = assignedTeam;
        this.assignedUser = assignedUser;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.acknowledgedAt = acknowledgedAt;
        this.resolvedAt = resolvedAt;
        this.closedAt = closedAt;
        this.updatedAt = updatedAt;
    }

    public void acknowledge(OffsetDateTime occurredAt) {
        requireStatus(IncidentStatus.OPEN);

        status = IncidentStatus.ACKNOWLEDGED;
        acknowledgedAt = occurredAt;
        updatedAt = occurredAt;
    }

    public void startInvestigation(OffsetDateTime occurredAt) {
        requireStatus(IncidentStatus.ACKNOWLEDGED);

        status = IncidentStatus.INVESTIGATING;
        updatedAt = occurredAt;
    }

    public void mitigate(OffsetDateTime occurredAt) {
        requireStatus(IncidentStatus.INVESTIGATING);

        status = IncidentStatus.MITIGATED;
        updatedAt = occurredAt;
    }

    public void startMonitoring(OffsetDateTime occurredAt) {
        requireStatus(IncidentStatus.MITIGATED);

        status = IncidentStatus.MONITORING;
        updatedAt = occurredAt;
    }

    public void resolve(OffsetDateTime occurredAt) {
        requireStatus(IncidentStatus.MONITORING);

        status = IncidentStatus.RESOLVED;
        resolvedAt = occurredAt;
        updatedAt = occurredAt;
    }

    public void close(OffsetDateTime occurredAt) {
        requireStatus(IncidentStatus.RESOLVED);

        status = IncidentStatus.CLOSED;
        closedAt = occurredAt;
        updatedAt = occurredAt;
    }

    public void assignTeam(
            Team team,
            OffsetDateTime occurredAt
    ) {
        requireModifiable();

        if (team == null) {
            throw new IllegalArgumentException(
                    "Assigned team is required."
            );
        }

        if (assignedTeam == null
                || !assignedTeam.getId().equals(team.getId())) {
            assignedUser = null;
        }

        assignedTeam = team;
        updatedAt = occurredAt;
    }

    public void assignUser(
            User user,
            OffsetDateTime occurredAt
    ) {
        requireModifiable();

        if (user == null) {
            throw new IllegalArgumentException(
                    "Assigned user is required."
            );
        }

        if (assignedTeam == null) {
            throw new IllegalStateException(
                    "An incident must have an assigned team before a user can be assigned."
            );
        }

        assignedUser = user;
        updatedAt = occurredAt;
    }

    public void unassignUser(OffsetDateTime occurredAt) {
        requireModifiable();

        assignedUser = null;
        updatedAt = occurredAt;
    }

    public void unassignTeam(OffsetDateTime occurredAt) {
        requireModifiable();

        assignedTeam = null;
        assignedUser = null;
        updatedAt = occurredAt;
    }

    private void requireStatus(IncidentStatus requiredStatus) {
        if (status != requiredStatus) {
            throw new InvalidStateTransitionException(
                    "Incident must be in status "
                            + requiredStatus
                            + " but is currently "
                            + status
                            + "."
            );
        }
    }

    private void requireModifiable() {
        if (status == IncidentStatus.CLOSED) {
            throw new IncidentModificationNotAllowedException(
                    "A closed incident cannot be modified."
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public String getIncidentNumber() {
        return incidentNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public Service getService() {
        return service;
    }

    public Team getAssignedTeam() {
        return assignedTeam;
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}