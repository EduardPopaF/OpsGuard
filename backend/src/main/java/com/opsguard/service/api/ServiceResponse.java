package com.opsguard.service.api;

import com.opsguard.service.Service;
import com.opsguard.service.ServiceCriticality;
import com.opsguard.service.ServiceStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ServiceResponse(
        UUID id,
        UUID organizationId,
        String name,
        String description,
        UUID ownerTeamId,
        ServiceCriticality criticality,
        ServiceStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static ServiceResponse from(Service service) {
        return new ServiceResponse(
                service.getId(),
                service.getOrganization().getId(),
                service.getName(),
                service.getDescription(),
                service.getOwnerTeam().getId(),
                service.getCriticality(),
                service.getStatus(),
                service.getCreatedAt(),
                service.getUpdatedAt()
        );
    }
}