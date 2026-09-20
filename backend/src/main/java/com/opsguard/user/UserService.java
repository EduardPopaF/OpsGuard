package com.opsguard.user;

import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public UserService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public User create(
            UUID organizationId,
            String email,
            String firstName,
            String lastName,
            UserRole role
    ) {
        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organization with id '" + organizationId + "' does not exist."
                ));

        String normalizedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByOrganizationIdAndEmailIgnoreCase(
                organizationId,
                normalizedEmail
        )) {
            throw new IllegalArgumentException(
                    "A user with email '" + normalizedEmail
                            + "' already exists in this organization."
            );
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        User user = new User(
                UUID.randomUUID(),
                organization,
                normalizedEmail,
                firstName,
                lastName,
                role,
                UserStatus.ACTIVE,
                now,
                now
        );

        return userRepository.save(user);
    }
}