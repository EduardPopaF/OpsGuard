package com.opsguard.service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository
        extends JpaRepository<Service, UUID> {

    Optional<Service> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    Optional<Service> findByOrganizationIdAndNameIgnoreCase(
            UUID organizationId,
            String name
    );

    boolean existsByOrganizationIdAndNameIgnoreCase(
            UUID organizationId,
            String name
    );

    List<Service> findAllByOrganizationId(
            UUID organizationId
    );

    List<Service> findAllByOrganizationIdAndOwnerTeamId(
            UUID organizationId,
            UUID ownerTeamId
    );
}