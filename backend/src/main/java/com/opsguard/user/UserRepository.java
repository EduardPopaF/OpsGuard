package com.opsguard.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

    Optional<User> findByOrganizationIdAndEmailIgnoreCase(
            UUID organizationId,
            String email
    );

    boolean existsByOrganizationIdAndEmailIgnoreCase(
            UUID organizationId,
            String email
    );
}