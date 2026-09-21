package com.opsguard.incident;

import com.opsguard.common.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidentLifecycleTest {

    @Test
    void shouldExecuteCompleteLifecycle() {
        OffsetDateTime createdAt =
                OffsetDateTime.of(
                        2026,
                        9,
                        22,
                        10,
                        0,
                        0,
                        0,
                        ZoneOffset.UTC
                );

        Incident incident = createOpenIncident(createdAt);

        OffsetDateTime acknowledgedAt =
                createdAt.plusMinutes(5);

        incident.acknowledge(acknowledgedAt);

        assertEquals(
                IncidentStatus.ACKNOWLEDGED,
                incident.getStatus()
        );
        assertEquals(
                acknowledgedAt,
                incident.getAcknowledgedAt()
        );
        assertEquals(
                acknowledgedAt,
                incident.getUpdatedAt()
        );

        OffsetDateTime investigatingAt =
                createdAt.plusMinutes(10);

        incident.startInvestigation(investigatingAt);

        assertEquals(
                IncidentStatus.INVESTIGATING,
                incident.getStatus()
        );
        assertEquals(
                investigatingAt,
                incident.getUpdatedAt()
        );

        OffsetDateTime mitigatedAt =
                createdAt.plusMinutes(20);

        incident.mitigate(mitigatedAt);

        assertEquals(
                IncidentStatus.MITIGATED,
                incident.getStatus()
        );
        assertEquals(
                mitigatedAt,
                incident.getUpdatedAt()
        );

        OffsetDateTime monitoringAt =
                createdAt.plusMinutes(25);

        incident.startMonitoring(monitoringAt);

        assertEquals(
                IncidentStatus.MONITORING,
                incident.getStatus()
        );
        assertEquals(
                monitoringAt,
                incident.getUpdatedAt()
        );

        OffsetDateTime resolvedAt =
                createdAt.plusMinutes(30);

        incident.resolve(resolvedAt);

        assertEquals(
                IncidentStatus.RESOLVED,
                incident.getStatus()
        );
        assertEquals(
                resolvedAt,
                incident.getResolvedAt()
        );
        assertEquals(
                resolvedAt,
                incident.getUpdatedAt()
        );

        OffsetDateTime closedAt =
                createdAt.plusMinutes(40);

        incident.close(closedAt);

        assertEquals(
                IncidentStatus.CLOSED,
                incident.getStatus()
        );
        assertEquals(
                closedAt,
                incident.getClosedAt()
        );
        assertEquals(
                closedAt,
                incident.getUpdatedAt()
        );

        assertNotNull(incident.getAcknowledgedAt());
        assertNotNull(incident.getResolvedAt());
        assertNotNull(incident.getClosedAt());
    }

    @Test
    void shouldRejectClosingOpenIncident() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Incident incident = createOpenIncident(createdAt);

        InvalidStateTransitionException exception =
                assertThrows(
                        InvalidStateTransitionException.class,
                        () -> incident.close(
                                createdAt.plusMinutes(1)
                        )
                );

        assertEquals(
                "Incident must be in status RESOLVED but is currently OPEN.",
                exception.getMessage()
        );

        assertEquals(
                IncidentStatus.OPEN,
                incident.getStatus()
        );

        assertNull(incident.getClosedAt());

        assertEquals(
                createdAt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldRejectInvestigationBeforeAcknowledgement() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Incident incident = createOpenIncident(createdAt);

        assertThrows(
                InvalidStateTransitionException.class,
                () -> incident.startInvestigation(
                        createdAt.plusMinutes(1)
                )
        );

        assertEquals(
                IncidentStatus.OPEN,
                incident.getStatus()
        );

        assertNull(incident.getAcknowledgedAt());

        assertEquals(
                createdAt,
                incident.getUpdatedAt()
        );
    }

    @Test
    void shouldRejectResolutionBeforeMonitoring() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Incident incident = createOpenIncident(createdAt);

        incident.acknowledge(
                createdAt.plusMinutes(1)
        );

        incident.startInvestigation(
                createdAt.plusMinutes(2)
        );

        assertThrows(
                InvalidStateTransitionException.class,
                () -> incident.resolve(
                        createdAt.plusMinutes(3)
                )
        );

        assertEquals(
                IncidentStatus.INVESTIGATING,
                incident.getStatus()
        );

        assertNull(incident.getResolvedAt());
    }

    @Test
    void shouldRejectAcknowledgingIncidentTwice() {
        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        Incident incident = createOpenIncident(createdAt);

        OffsetDateTime firstAcknowledgement =
                createdAt.plusMinutes(1);

        incident.acknowledge(firstAcknowledgement);

        assertThrows(
                InvalidStateTransitionException.class,
                () -> incident.acknowledge(
                        createdAt.plusMinutes(2)
                )
        );

        assertEquals(
                IncidentStatus.ACKNOWLEDGED,
                incident.getStatus()
        );

        assertEquals(
                firstAcknowledgement,
                incident.getAcknowledgedAt()
        );

        assertEquals(
                firstAcknowledgement,
                incident.getUpdatedAt()
        );
    }

    private Incident createOpenIncident(
            OffsetDateTime createdAt
    ) {
        return new Incident(
                UUID.randomUUID(),
                null,
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
}