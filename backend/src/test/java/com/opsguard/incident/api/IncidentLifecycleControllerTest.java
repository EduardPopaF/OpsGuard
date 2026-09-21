package com.opsguard.incident.api;

import com.opsguard.common.api.GlobalExceptionHandler;
import com.opsguard.common.exception.InvalidStateTransitionException;
import com.opsguard.incident.IncidentLifecycleAction;
import com.opsguard.incident.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IncidentLifecycleControllerTest {

    private IncidentService incidentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        incidentService = mock(IncidentService.class);

        IncidentController controller =
                new IncidentController(incidentService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnConflictForInvalidLifecycleTransition()
            throws Exception {

        UUID organizationId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();

        when(incidentService.executeLifecycleAction(
                organizationId,
                incidentId,
                IncidentLifecycleAction.CLOSE
        )).thenThrow(
                new InvalidStateTransitionException(
                        "Incident must be in status RESOLVED but is currently OPEN."
                )
        );

        String requestBody = """
                {
                  "action": "CLOSE"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/organizations/{organizationId}/incidents/{incidentId}/lifecycle",
                                organizationId,
                                incidentId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_STATE_TRANSITION"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Incident must be in status RESOLVED but is currently OPEN."
                        ));
    }
}