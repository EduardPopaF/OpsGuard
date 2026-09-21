package com.opsguard.service.api;

import com.opsguard.service.ServiceCriticality;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateServiceRequest(

        @NotBlank(message = "Service name is required.")
        @Size(
                max = 150,
                message = "Service name must not exceed 150 characters."
        )
        String name,

        @Size(
                max = 500,
                message = "Service description must not exceed 500 characters."
        )
        String description,

        @NotNull(message = "Owner team ID is required.")
        UUID ownerTeamId,

        @NotNull(message = "Service criticality is required.")
        ServiceCriticality criticality
) {
}