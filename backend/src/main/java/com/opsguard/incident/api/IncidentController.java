package com.opsguard.incident.api;

import com.opsguard.incident.Incident;
import com.opsguard.incident.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations/{organizationId}/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse createIncident(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateIncidentRequest request
    ) {
        Incident incident = incidentService.create(
                organizationId,
                request.title(),
                request.description(),
                request.severity(),
                request.serviceId(),
                request.createdByUserId()
        );

        return IncidentResponse.from(incident);
    }

    @PostMapping("/{incidentId}/lifecycle")
    public IncidentResponse executeLifecycleAction(
            @PathVariable UUID organizationId,
            @PathVariable UUID incidentId,
            @Valid @RequestBody IncidentLifecycleRequest request
    ) {
        Incident incident =
                incidentService.executeLifecycleAction(
                        organizationId,
                        incidentId,
                        request.action()
                );

        return IncidentResponse.from(incident);
    }

    @PostMapping("/{incidentId}/assignment/team")
    public IncidentResponse assignTeam(
            @PathVariable UUID organizationId,
            @PathVariable UUID incidentId,
            @Valid @RequestBody AssignIncidentTeamRequest request
    ) {
        Incident incident =
                incidentService.assignTeam(
                        organizationId,
                        incidentId,
                        request.teamId()
                );

        return IncidentResponse.from(incident);
    }

    @PostMapping("/{incidentId}/assignment/user")
    public IncidentResponse assignUser(
            @PathVariable UUID organizationId,
            @PathVariable UUID incidentId,
            @Valid @RequestBody AssignIncidentUserRequest request
    ) {
        Incident incident =
                incidentService.assignUser(
                        organizationId,
                        incidentId,
                        request.userId()
                );

        return IncidentResponse.from(incident);
    }
}