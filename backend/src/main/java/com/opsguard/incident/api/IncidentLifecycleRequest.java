package com.opsguard.incident.api;

import com.opsguard.incident.IncidentLifecycleAction;
import jakarta.validation.constraints.NotNull;

public record IncidentLifecycleRequest(

        @NotNull(message = "Lifecycle action is required.")
        IncidentLifecycleAction action

) {
}