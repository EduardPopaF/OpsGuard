package com.opsguard.user.api;

import com.opsguard.user.User;
import com.opsguard.user.UserService;
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
@RequestMapping("/api/organizations/{organizationId}/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateUserRequest request
    ) {
        User user = userService.create(
                organizationId,
                request.email(),
                request.firstName(),
                request.lastName(),
                request.role()
        );

        UserResponse response = UserResponse.from(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}