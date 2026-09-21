package com.opsguard.team.api;

import com.opsguard.team.Team;
import com.opsguard.team.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations/{organizationId}/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<TeamResponse> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateTeamRequest request
    ) {
        Team team = teamService.create(
                organizationId,
                request.name(),
                request.description()
        );

        TeamResponse response = TeamResponse.from(team);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}