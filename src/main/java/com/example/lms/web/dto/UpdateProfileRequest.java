package com.example.lms.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 5000) String bio,
        @Size(max = 500) String avatarUrl
) {}