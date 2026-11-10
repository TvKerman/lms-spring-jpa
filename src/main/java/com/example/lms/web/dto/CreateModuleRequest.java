package com.example.lms.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateModuleRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull Integer orderIndex,
        @Size(max = 5000) String description
) {}