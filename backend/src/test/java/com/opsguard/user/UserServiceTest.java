package com.opsguard.user;

import com.opsguard.common.exception.ConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationRepository;
import com.opsguard.organization.OrganizationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserRepository userRepository;
    private OrganizationRepository organizationRepository;
    private UserService userService;

    private UUID organizationId;
    private Organization organization;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        organizationRepository = mock(OrganizationRepository.class);

        userService = new UserService(
                userRepository,
                organizationRepository
        );

        organizationId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        organization = new Organization(
                organizationId,
                "Acme",
                "acme",
                OrganizationStatus.ACTIVE,
                now,
                now
        );
    }

    @Test
    void shouldCreateActiveUserWithNormalizedEmail() {
        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(userRepository.existsByOrganizationIdAndEmailIgnoreCase(
                organizationId,
                "john@example.com"
        )).thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.create(
                organizationId,
                "  John@Example.COM  ",
                "John",
                "Doe",
                UserRole.ENGINEER
        );

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();

        assertNotNull(saved.getId());
        assertEquals(organizationId, saved.getOrganization().getId());
        assertEquals("john@example.com", saved.getEmail());
        assertEquals("John", saved.getFirstName());
        assertEquals("Doe", saved.getLastName());
        assertEquals(UserRole.ENGINEER, saved.getRole());
        assertEquals(UserStatus.ACTIVE, saved.getStatus());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        assertEquals(saved.getId(), result.getId());
    }

    @Test
    void shouldRejectUserWhenOrganizationDoesNotExist() {
        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.create(
                        organizationId,
                        "john@example.com",
                        "John",
                        "Doe",
                        UserRole.ENGINEER
                )
        );

        assertEquals(
                "Organization with id '" + organizationId + "' does not exist.",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmailWithinOrganization() {
        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(userRepository.existsByOrganizationIdAndEmailIgnoreCase(
                organizationId,
                "john@example.com"
        )).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.create(
                        organizationId,
                        "  John@Example.COM  ",
                        "John",
                        "Doe",
                        UserRole.ENGINEER
                )
        );

        assertEquals(
                "A user with email 'john@example.com' already exists in this organization.",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));
    }
}