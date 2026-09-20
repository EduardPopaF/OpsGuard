package com.opsguard.organization.api;

import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse> create(
            @Valid @RequestBody CreateOrganizationRequest request
    ) {
        Organization organization = organizationService.create(
                request.name(),
                request.slug()
        );

        OrganizationResponse response =
                OrganizationResponse.from(organization);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}