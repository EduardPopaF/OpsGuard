package com.opsguard.incident;

import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationRepository;
import com.opsguard.service.ServiceRepository;
import com.opsguard.user.User;
import com.opsguard.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@org.springframework.stereotype.Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final OrganizationRepository organizationRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final IncidentNumberGenerator incidentNumberGenerator;

    public IncidentService(
            IncidentRepository incidentRepository,
            OrganizationRepository organizationRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository,
            IncidentNumberGenerator incidentNumberGenerator
    ) {
        this.incidentRepository = incidentRepository;
        this.organizationRepository = organizationRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
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
}