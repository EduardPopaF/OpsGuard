package com.opsguard.team;

import com.opsguard.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_members")
public class TeamMember {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_role", nullable = false, length = 30)
    private TeamMemberRole membershipRole;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    protected TeamMember() {
    }

    public TeamMember(
            UUID id,
            Team team,
            User user,
            TeamMemberRole membershipRole,
            OffsetDateTime joinedAt
    ) {
        this.id = id;
        this.team = team;
        this.user = user;
        this.membershipRole = membershipRole;
        this.joinedAt = joinedAt;
    }

    public UUID getId() {
        return id;
    }

    public Team getTeam() {
        return team;
    }

    public User getUser() {
        return user;
    }

    public TeamMemberRole getMembershipRole() {
        return membershipRole;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }
}