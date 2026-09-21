package com.opsguard.incident;

import com.opsguard.common.exception.IncidentModificationNotAllowedException;
import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationStatus;
import com.opsguard.team.Team;
import com.opsguard.team.TeamStatus;
import com.opsguard.user.User;
import com.opsguard.user.UserRole;
import com.opsguard.user.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidentAssignmentTest {

    @Test
    void shouldAssignTeam() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        Team team =
                createTeam(organization, createdAt);

        Incident incident =
                createIncident(organization, createdAt);

        OffsetDateTime assignedAt =
                createdAt.plusMinutes(1);

        incident.assignTeam(team, assignedAt);

        assertEquals(
                team,
                incident.getAssignedTeam()
        );

        assertEquals(
                assignedAt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldAssignUserWhenTeamIsAlreadyAssigned() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        Team team =
                createTeam(organization, createdAt);

        User user =
                createUser(organization, createdAt);

        Incident incident =
                createIncident(organization, createdAt);

        incident.assignTeam(
                team,
                createdAt.plusMinutes(1)
        );

        OffsetDateTime assignedAt =
                createdAt.plusMinutes(2);

        incident.assignUser(user, assignedAt);

        assertEquals(
                user,
                incident.getAssignedUser()
        );

        assertEquals(
                assignedAt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldRejectUserAssignmentWhenNoTeamIsAssigned() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        User user =
                createUser(organization, createdAt);

        Incident incident =
                createIncident(organization, createdAt);

        assertThrows(
                IllegalStateException.class,
                () -> incident.assignUser(
                        user,
                        createdAt.plusMinutes(1)
                )
        );

        assertNull(incident.getAssignedUser());

        assertEquals(
                createdAt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldUnassignUser() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        Team team =
                createTeam(organization, createdAt);

        User user =
                createUser(organization, createdAt);

        Incident incident =
                createIncident(organization, createdAt);

        incident.assignTeam(
                team,
                createdAt.plusMinutes(1)
        );

        incident.assignUser(
                user,
                createdAt.plusMinutes(2)
        );

        OffsetDateTime unassignedAt =
                createdAt.plusMinutes(3);

        incident.unassignUser(unassignedAt);

        assertNull(incident.getAssignedUser());

        assertEquals(
                unassignedAt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldUnassignUserWhenTeamIsUnassigned() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        Team team =
                createTeam(organization, createdAt);

        User user =
                createUser(organization, createdAt);

        Incident incident =
                createIncident(organization, createdAt);

        incident.assignTeam(
                team,
                createdAt.plusMinutes(1)
        );

        incident.assignUser(
                user,
                createdAt.plusMinutes(2)
        );

        OffsetDateTime unassignedAt =
                createdAt.plusMinutes(3);

        incident.unassignTeam(unassignedAt);

        assertNull(incident.getAssignedTeam());
        assertNull(incident.getAssignedUser());

        assertEquals(
                unassignedAt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldClearAssignedUserWhenTeamChanges() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        Team firstTeam =
                createTeam(
                        organization,
                        "Platform Engineering",
                        createdAt
                );

        Team secondTeam =
                createTeam(
                        organization,
                        "Infrastructure",
                        createdAt
                );

        User user =
                createUser(organization, createdAt);

        Incident incident =
                createIncident(organization, createdAt);

        incident.assignTeam(
                firstTeam,
                createdAt.plusMinutes(1)
        );

        incident.assignUser(
                user,
                createdAt.plusMinutes(2)
        );

        OffsetDateTime reassignedAt =
                createdAt.plusMinutes(3);

        incident.assignTeam(
                secondTeam,
                reassignedAt
        );

        assertEquals(
                secondTeam,
                incident.getAssignedTeam()
        );

        assertNull(incident.getAssignedUser());

        assertEquals(
                reassignedAt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldKeepAssignedUserWhenSameTeamIsAssignedAgain() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        Team team =
                createTeam(organization, createdAt);

        User user =
                createUser(organization, createdAt);

        Incident incident =
                createIncident(organization, createdAt);

        incident.assignTeam(
                team,
                createdAt.plusMinutes(1)
        );

        incident.assignUser(
                user,
                createdAt.plusMinutes(2)
        );

        incident.assignTeam(
                team,
                createdAt.plusMinutes(3)
        );

        assertEquals(
                team,
                incident.getAssignedTeam()
        );

        assertEquals(
                user,
                incident.getAssignedUser()
        );
    }

    @Test
    void shouldRejectNullTeamAssignment() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        Incident incident =
                createIncident(organization, createdAt);

        assertThrows(
                IllegalArgumentException.class,
                () -> incident.assignTeam(
                        null,
                        createdAt.plusMinutes(1)
                )
        );

        assertNull(incident.getAssignedTeam());

        assertEquals(
                createdAt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldRejectNullUserAssignment() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        Team team =
                createTeam(organization, createdAt);

        Incident incident =
                createIncident(organization, createdAt);

        incident.assignTeam(
                team,
                createdAt.plusMinutes(1)
        );

        OffsetDateTime beforeAttempt =
                incident.getUpdatedAt();

        assertThrows(
                IllegalArgumentException.class,
                () -> incident.assignUser(
                        null,
                        createdAt.plusMinutes(2)
                )
        );

        assertNull(incident.getAssignedUser());

        assertEquals(
                beforeAttempt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldRejectTeamAssignmentWhenIncidentIsClosed() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        Team team =
                createTeam(organization, createdAt);

        Incident incident =
                createClosedIncident(
                        organization,
                        createdAt
                );

        OffsetDateTime beforeAttempt =
                incident.getUpdatedAt();

        assertThrows(
                IncidentModificationNotAllowedException.class,
                () -> incident.assignTeam(
                        team,
                        createdAt.plusMinutes(1)
                )
        );

        assertNull(incident.getAssignedTeam());

        assertEquals(
                beforeAttempt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldRejectUserAssignmentWhenIncidentIsClosed() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        Team team =
                createTeam(organization, createdAt);

        User user =
                createUser(organization, createdAt);

        Incident incident =
                createClosedIncident(
                        organization,
                        team,
                        null,
                        createdAt
                );

        OffsetDateTime beforeAttempt =
                incident.getUpdatedAt();

        assertThrows(
                IncidentModificationNotAllowedException.class,
                () -> incident.assignUser(
                        user,
                        createdAt.plusMinutes(1)
                )
        );

        assertNull(incident.getAssignedUser());

        assertEquals(
                beforeAttempt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldRejectUserUnassignmentWhenIncidentIsClosed() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        Team team =
                createTeam(organization, createdAt);

        User user =
                createUser(organization, createdAt);

        Incident incident =
                createClosedIncident(
                        organization,
                        team,
                        user,
                        createdAt
                );

        OffsetDateTime beforeAttempt =
                incident.getUpdatedAt();

        assertThrows(
                IncidentModificationNotAllowedException.class,
                () -> incident.unassignUser(
                        createdAt.plusMinutes(1)
                )
        );

        assertEquals(
                user,
                incident.getAssignedUser()
        );

        assertEquals(
                beforeAttempt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldRejectTeamUnassignmentWhenIncidentIsClosed() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization =
                createOrganization(createdAt);

        Team team =
                createTeam(organization, createdAt);

        User user =
                createUser(organization, createdAt);

        Incident incident =
                createClosedIncident(
                        organization,
                        team,
                        user,
                        createdAt
                );

        OffsetDateTime beforeAttempt =
                incident.getUpdatedAt();

        assertThrows(
                IncidentModificationNotAllowedException.class,
                () -> incident.unassignTeam(
                        createdAt.plusMinutes(1)
                )
        );

        assertEquals(
                team,
                incident.getAssignedTeam()
        );

        assertEquals(
                user,
                incident.getAssignedUser()
        );

        assertEquals(
                beforeAttempt,
                incident.getUpdatedAt()
        );
    }

    private Organization createOrganization(
            OffsetDateTime createdAt
    ) {
        return new Organization(
                UUID.randomUUID(),
                "Acme Corporation",
                "acme-corporation",
                OrganizationStatus.ACTIVE,
                createdAt,
                createdAt
        );
    }

    private Team createTeam(
            Organization organization,
            OffsetDateTime createdAt
    ) {
        return createTeam(
                organization,
                "Platform Engineering",
                createdAt
        );
    }

    private Team createTeam(
            Organization organization,
            String name,
            OffsetDateTime createdAt
    ) {
        return new Team(
                UUID.randomUUID(),
                organization,
                name,
                null,
                TeamStatus.ACTIVE,
                createdAt,
                createdAt
        );
    }

    private User createUser(
            Organization organization,
            OffsetDateTime createdAt
    ) {
        return new User(
                UUID.randomUUID(),
                organization,
                "engineer@acme.com",
                "John",
                "Doe",
                UserRole.ENGINEER,
                UserStatus.ACTIVE,
                createdAt,
                createdAt
        );
    }

    private Incident createIncident(
            Organization organization,
            OffsetDateTime createdAt
    ) {
        return new Incident(
                UUID.randomUUID(),
                organization,
                "INC-000001",
                "Payment API unavailable",
                null,
                IncidentSeverity.SEV1,
                IncidentStatus.OPEN,
                null,
                null,
                null,
                null,
                createdAt,
                null,
                null,
                null,
                createdAt
        );
    }

    private Incident createClosedIncident(
            Organization organization,
            OffsetDateTime createdAt
    ) {
        return createClosedIncident(
                organization,
                null,
                null,
                createdAt
        );
    }

    private Incident createClosedIncident(
            Organization organization,
            Team assignedTeam,
            User assignedUser,
            OffsetDateTime createdAt
    ) {
        return new Incident(
                UUID.randomUUID(),
                organization,
                "INC-000001",
                "Payment API unavailable",
                null,
                IncidentSeverity.SEV1,
                IncidentStatus.CLOSED,
                null,
                assignedTeam,
                assignedUser,
                null,
                createdAt,
                createdAt.plusMinutes(1),
                createdAt.plusMinutes(2),
                createdAt.plusMinutes(3),
                createdAt.plusMinutes(3)
        );
    }
}