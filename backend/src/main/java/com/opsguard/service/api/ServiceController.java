package com.opsguard.service.api;

import com.opsguard.service.Service;
import com.opsguard.service.ServiceService;
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
@RequestMapping("/api/organizations/{organizationId}/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse createService(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateServiceRequest request
    ) {
        Service service = serviceService.create(
                organizationId,
                request.name(),
                request.description(),
                request.ownerTeamId(),
                request.criticality()
        );

        return ServiceResponse.from(service);
    }
}