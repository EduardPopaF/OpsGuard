package com.opsguard.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    Optional<Team> findByOrganizationIdAndNameIgnoreCase(
            UUID organizationId,
            String name
    );

    boolean existsByOrganizationIdAndNameIgnoreCase(
            UUID organizationId,
            String name
    );

    List<Team> findAllByOrganizationId(
            UUID organizationId
    );
}