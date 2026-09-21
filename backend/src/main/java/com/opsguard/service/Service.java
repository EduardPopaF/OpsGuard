package com.opsguard.service;

import com.opsguard.organization.Organization;
import com.opsguard.team.Team;
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
@Table(name = "services")
public class Service {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_team_id", nullable = false)
    private Team ownerTeam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceCriticality criticality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Service() {
    }

    public Service(
            UUID id,
            Organization organization,
            String name,
            String description,
            Team ownerTeam,
            ServiceCriticality criticality,
            ServiceStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.organization = organization;
        this.name = name;
        this.description = description;
        this.ownerTeam = ownerTeam;
        this.criticality = criticality;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Team getOwnerTeam() {
        return ownerTeam;
    }

    public ServiceCriticality getCriticality() {
        return criticality;
    }

    public ServiceStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}