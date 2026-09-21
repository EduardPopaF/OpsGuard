package com.opsguard.user.api;

import com.opsguard.user.User;
import com.opsguard.user.UserRole;
import com.opsguard.user.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID organizationId,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        UserStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getOrganization().getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}