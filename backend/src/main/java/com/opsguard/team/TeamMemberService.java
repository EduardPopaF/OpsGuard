package com.opsguard.team;

import com.opsguard.common.exception.ConflictException;
import com.opsguard.common.exception.ResourceNotFoundException;
import com.opsguard.user.User;
import com.opsguard.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamMemberService(
            TeamMemberRepository teamMemberRepository,
            TeamRepository teamRepository,
            UserRepository userRepository
    ) {
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TeamMember addMember(
            UUID organizationId,
            UUID teamId,
            UUID userId,
            TeamMemberRole membershipRole
    ) {
        Team team = teamRepository
                .findByIdAndOrganizationId(teamId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team with id '"
                                + teamId
                                + "' does not exist in this organization."
                ));

        User user = userRepository
                .findByIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id '"
                                + userId
                                + "' does not exist in this organization."
                ));

        if (teamMemberRepository.existsByTeamIdAndUserId(
                teamId,
                userId
        )) {
            throw new ConflictException(
                    "User with id '"
                            + userId
                            + "' is already a member of team '"
                            + teamId
                            + "'."
            );
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        TeamMember teamMember = new TeamMember(
                UUID.randomUUID(),
                team,
                user,
                membershipRole,
                now
        );

        return teamMemberRepository.save(teamMember);
    }
}