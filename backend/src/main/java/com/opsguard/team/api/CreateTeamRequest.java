package com.opsguard.team.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(

        @NotBlank(message = "Team name is required.")
        @Size(
                max = 150,
                message = "Team name must not exceed 150 characters."
        )
        String name,

        @Size(
                max = 500,
                message = "Team description must not exceed 500 characters."
        )
        String description
) {
}