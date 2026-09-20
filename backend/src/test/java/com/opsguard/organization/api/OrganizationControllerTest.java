package com.opsguard.organization.api;

import com.opsguard.common.api.GlobalExceptionHandler;
import com.opsguard.common.exception.ConflictException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationService;
import com.opsguard.organization.OrganizationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrganizationControllerTest {

    private OrganizationService organizationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        organizationService = mock(OrganizationService.class);

        OrganizationController controller =
                new OrganizationController(organizationService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateOrganization() throws Exception {
        UUID organizationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = new Organization(
                organizationId,
                "Acme Corporation",
                "acme-corporation",
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        when(organizationService.create(
                "Acme Corporation",
                "acme-corporation"
        )).thenReturn(organization);

        mockMvc.perform(
                        post("/api/organizations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Acme Corporation",
                                          "slug": "acme-corporation"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(organizationId.toString()))
                .andExpect(jsonPath("$.name").value("Acme Corporation"))
                .andExpect(jsonPath("$.slug").value("acme-corporation"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(organizationService).create(
                "Acme Corporation",
                "acme-corporation"
        );
    }

    @Test
    void shouldReturnBadRequestForInvalidSlug() throws Exception {
        mockMvc.perform(
                        post("/api/organizations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Acme Corporation",
                                          "slug": "Acme Corporation"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed."))
                .andExpect(jsonPath("$.errors.slug")
                        .value(
                                "Organization slug must contain only lowercase letters, numbers, and single hyphens."
                        ))
                .andExpect(jsonPath("$.path")
                        .value("/api/organizations"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(organizationService);
    }

    @Test
    void shouldReturnConflictForDuplicateSlug() throws Exception {
        when(organizationService.create(
                "Acme Corporation",
                "acme"
        )).thenThrow(
                new ConflictException(
                        "An organization with slug 'acme' already exists."
                )
        );

        mockMvc.perform(
                        post("/api/organizations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Acme Corporation",
                                          "slug": "acme"
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "An organization with slug 'acme' already exists."
                        ))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.path")
                        .value("/api/organizations"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(organizationService).create(
                "Acme Corporation",
                "acme"
        );
    }

    @Test
void shouldReturnBadRequestForMalformedJson() throws Exception {
    mockMvc.perform(
                    post("/api/organizations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Acme Corporation",
                                      "slug":
                                    }
                                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
            .andExpect(jsonPath("$.message")
                    .value(
                            "Request body is malformed or contains invalid values."
                    ))
            .andExpect(jsonPath("$.errors").isEmpty())
            .andExpect(jsonPath("$.path")
                    .value("/api/organizations"))
            .andExpect(jsonPath("$.timestamp").exists());

    verifyNoInteractions(organizationService);
}

}