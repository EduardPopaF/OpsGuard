package com.opsguard.service;

import com.opsguard.common.exception.ConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationRepository;
import com.opsguard.team.Team;
import com.opsguard.team.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final OrganizationRepository organizationRepository;
    private final TeamRepository teamRepository;

    public ServiceService(
            ServiceRepository serviceRepository,
            OrganizationRepository organizationRepository,
            TeamRepository teamRepository
    ) {
        this.serviceRepository = serviceRepository;
        this.organizationRepository = organizationRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public com.opsguard.service.Service create(
            UUID organizationId,
            String name,
            String description,
            UUID ownerTeamId,
            ServiceCriticality criticality
    ) {
        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization with id '"
                                + organizationId
                                + "' does not exist."
                ));

        Team ownerTeam = teamRepository
                .findByIdAndOrganizationId(
                        ownerTeamId,
                        organizationId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team with id '"
                                + ownerTeamId
                                + "' does not exist in this organization."
                ));

        String normalizedName = name.trim();

        String normalizedDescription =
                description == null || description.isBlank()
                        ? null
                        : description.trim();

        if (serviceRepository
                .existsByOrganizationIdAndNameIgnoreCase(
                        organizationId,
                        normalizedName
                )) {
            throw new ConflictException(
                    "A service with name '"
                            + normalizedName
                            + "' already exists in this organization."
            );
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        com.opsguard.service.Service service =
                new com.opsguard.service.Service(
                        UUID.randomUUID(),
                        organization,
                        normalizedName,
                        normalizedDescription,
                        ownerTeam,
                        criticality,
                        ServiceStatus.ACTIVE,
                        now,
                        now
                );

        return serviceRepository.save(service);
    }
}