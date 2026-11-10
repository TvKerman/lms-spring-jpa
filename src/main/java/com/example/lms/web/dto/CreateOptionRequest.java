package com.example.lms.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOptionRequest(
        @NotBlank @Size(max = 500) String text,
        boolean correct
) {}