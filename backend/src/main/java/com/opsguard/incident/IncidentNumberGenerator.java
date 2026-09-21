package com.opsguard.incident;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IncidentNumberGenerator {

    private final JdbcTemplate jdbcTemplate;

    public IncidentNumberGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String nextIncidentNumber(UUID organizationId) {
        Long number = jdbcTemplate.queryForObject(
                """
                INSERT INTO incident_counters (
                    organization_id,
                    next_number
                )
                VALUES (?, 2)
                ON CONFLICT (organization_id)
                DO UPDATE
                    SET next_number =
                        incident_counters.next_number + 1
                RETURNING next_number - 1
                """,
                Long.class,
                organizationId
        );

        if (number == null) {
            throw new IllegalStateException(
                    "Failed to generate incident number."
            );
        }

        return "INC-%06d".formatted(number);
    }
}