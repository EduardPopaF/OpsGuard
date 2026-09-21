package com.opsguard.incident;

import com.opsguard.organization.Organization;
import com.opsguard.organization.OrganizationRepository;
import com.opsguard.organization.OrganizationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class IncidentNumberGeneratorTest {

    @Autowired
    private IncidentNumberGenerator incidentNumberGenerator;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID organizationId;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization organization = new Organization(
                organizationId,
                "Incident Test Organization",
                "incident-test-" + organizationId,
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        organizationRepository.saveAndFlush(organization);
    }

    @Test
    void shouldGenerateSequentialIncidentNumbersForOrganization() {
        String first =
                incidentNumberGenerator.nextIncidentNumber(
                        organizationId
                );

        String second =
                incidentNumberGenerator.nextIncidentNumber(
                        organizationId
                );

        String third =
                incidentNumberGenerator.nextIncidentNumber(
                        organizationId
                );

        assertEquals("INC-000001", first);
        assertEquals("INC-000002", second);
        assertEquals("INC-000003", third);
    }

    @Test
    void shouldMaintainIndependentCountersPerOrganization() {
        UUID secondOrganizationId = UUID.randomUUID();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Organization secondOrganization = new Organization(
                secondOrganizationId,
                "Second Test Organization",
                "second-test-" + secondOrganizationId,
                OrganizationStatus.ACTIVE,
                now,
                now
        );

        organizationRepository.saveAndFlush(secondOrganization);

        String organizationAFirst =
                incidentNumberGenerator.nextIncidentNumber(
                        organizationId
                );

        String organizationASecond =
                incidentNumberGenerator.nextIncidentNumber(
                        organizationId
                );

        String organizationBFirst =
                incidentNumberGenerator.nextIncidentNumber(
                        secondOrganizationId
                );

        assertEquals(
                "INC-000001",
                organizationAFirst
        );

        assertEquals(
                "INC-000002",
                organizationASecond
        );

        assertEquals(
                "INC-000001",
                organizationBFirst
        );
    }

    @Test
    void shouldPersistExpectedNextNumberInDatabase() {
        incidentNumberGenerator.nextIncidentNumber(
                organizationId
        );

        incidentNumberGenerator.nextIncidentNumber(
                organizationId
        );

        Long nextNumber = jdbcTemplate.queryForObject(
                """
                SELECT next_number
                FROM incident_counters
                WHERE organization_id = ?
                """,
                Long.class,
                organizationId
        );

        assertEquals(3L, nextNumber);
    }
}