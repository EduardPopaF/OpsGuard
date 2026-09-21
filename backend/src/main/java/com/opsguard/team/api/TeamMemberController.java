package com.opsguard.team.api;

import com.opsguard.team.TeamMember;
import com.opsguard.team.TeamMemberService;
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
@RequestMapping(
        "/api/organizations/{organizationId}/teams/{teamId}/members"
)
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    public TeamMemberController(
            TeamMemberService teamMemberService
    ) {
        this.teamMemberService = teamMemberService;
    }

    @PostMapping
    public ResponseEntity<TeamMemberResponse> addMember(
            @PathVariable UUID organizationId,
            @PathVariable UUID teamId,
            @Valid @RequestBody AddTeamMemberRequest request
    ) {
        TeamMember teamMember = teamMemberService.addMember(
                organizationId,
                teamId,
                request.userId(),
                request.membershipRole()
        );

        TeamMemberResponse response =
                TeamMemberResponse.from(teamMember);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}