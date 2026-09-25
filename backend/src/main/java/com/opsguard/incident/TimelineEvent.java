package com.opsguard.incident;

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
@Table(name = "incident_timeline_events")
public class TimelineEvent {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private TimelineEventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actorUser;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(columnDefinition = "TEXT")
    private String details;

    protected TimelineEvent() {
    }

    public TimelineEvent(
            UUID id,
            Incident incident,
            TimelineEventType eventType,
            User actorUser,
            OffsetDateTime occurredAt,
            String details
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Timeline event ID is required."
            );
        }

        if (incident == null) {
            throw new IllegalArgumentException(
                    "Incident is required."
            );
        }

        if (eventType == null) {
            throw new IllegalArgumentException(
                    "Timeline event type is required."
            );
        }

        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "Timeline event time is required."
            );
        }

        this.id = id;
        this.incident = incident;
        this.eventType = eventType;
        this.actorUser = actorUser;
        this.occurredAt = occurredAt;
        this.details = normalizeDetails(details);
    }

    private String normalizeDetails(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public TimelineEventType getEventType() {
        return eventType;
    }

    public User getActorUser() {
        return actorUser;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getDetails() {
        return details;
    }
}