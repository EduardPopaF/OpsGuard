package com.opsguard.organization;

import com.opsguard.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizationServiceTest {

    private OrganizationRepository organizationRepository;
    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationRepository = mock(OrganizationRepository.class);
        organizationService = new OrganizationService(organizationRepository);
    }

    @Test
    void shouldCreateActiveOrganization() {
        when(organizationRepository.existsBySlug("acme"))
                .thenReturn(false);

        when(organizationRepository.save(any(Organization.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Organization result = organizationService.create("Acme", "acme");

        ArgumentCaptor<Organization> captor =
                ArgumentCaptor.forClass(Organization.class);

        verify(organizationRepository).save(captor.capture());

        Organization saved = captor.getValue();

        assertNotNull(saved.getId());
        assertEquals("Acme", saved.getName());
        assertEquals("acme", saved.getSlug());
        assertEquals(OrganizationStatus.ACTIVE, saved.getStatus());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        assertEquals(saved.getId(), result.getId());
    }

    @Test
    void shouldRejectDuplicateSlug() {
        when(organizationRepository.existsBySlug("acme"))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> organizationService.create("Another Acme", "acme")
        );

        assertEquals(
                "An organization with slug 'acme' already exists.",
                exception.getMessage()
        );

        verify(organizationRepository, never())
                .save(any(Organization.class));
    }
}