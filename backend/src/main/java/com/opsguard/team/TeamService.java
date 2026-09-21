package com.opsguard.team;

import com.opsguard.common.exception.ConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final OrganizationRepository organizationRepository;

    public TeamService(
            TeamRepository teamRepository,
            OrganizationRepository organizationRepository
    ) {
        this.teamRepository = teamRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public Team create(
            UUID organizationId,
            String name,
            String description
    ) {
        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization with id '"
                                + organizationId
                                + "' does not exist."
                ));

        String normalizedName = name.trim();

        String normalizedDescription =
                description == null || description.isBlank()
                        ? null
                        : description.trim();

        if (teamRepository.existsByOrganizationIdAndNameIgnoreCase(
                organizationId,
                normalizedName
        )) {
            throw new ConflictException(
                    "A team with name '"
                            + normalizedName
                            + "' already exists in this organization."
            );
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Team team = new Team(
                UUID.randomUUID(),
                organization,
                normalizedName,
                normalizedDescription,
                TeamStatus.ACTIVE,
                now,
                now
        );

        return teamRepository.save(team);
    }
}