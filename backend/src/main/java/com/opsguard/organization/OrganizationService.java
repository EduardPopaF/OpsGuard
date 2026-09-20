package com.opsguard.organization;

import com.opsguard.common.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public Organization create(String name, String slug) {
        if (organizationRepository.existsBySlug(slug)) {
            throw new ConflictException(
                    "An organization with slug '" + slug + "' already exists."
            );
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = new Organization(
                UUID.randomUUID(),
                name,
                slug,
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        return organizationRepository.save(organization);
    }
}