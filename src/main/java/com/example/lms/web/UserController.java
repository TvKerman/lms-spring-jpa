package com.example.lms.web;

import com.example.lms.entity.User;
import com.example.lms.enums.Role;
import com.example.lms.service.UserService;
import com.example.lms.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> list(@RequestParam(required = false) Role role) {
        List<User> users = role == null ? userService.findAll() : userService.findByRole(role);
        return users.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return toResponse(userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        User user = userService.createUser(req.name(), req.email(), req.role());
        return ResponseEntity
                .created(URI.create("/api/users/" + user.getId()))
                .body(toResponse(user));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserResponse> upsertProfile(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateProfileRequest req) {
        userService.createOrUpdateProfile(id, req.bio(), req.avatarUrl());
        return ResponseEntity.ok(toResponse(userService.findById(id)));
    }

    private UserResponse toResponse(User u) {
        String bio = null;
        String avatar = null;
        if (u.getProfile() != null) {
            bio = u.getProfile().getBio();
            avatar = u.getProfile().getAvatarUrl();
        }
        return new UserResponse(u.getId(), u.getName(), u.getEmail(),
                u.getRole().name(), bio, avatar);
    }
}