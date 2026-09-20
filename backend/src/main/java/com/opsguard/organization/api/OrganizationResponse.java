package com.opsguard.organization.api;

import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String slug,
        OrganizationStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static OrganizationResponse from(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                organization.getStatus(),
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }
}