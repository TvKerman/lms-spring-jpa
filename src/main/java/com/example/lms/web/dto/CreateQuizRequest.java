package com.example.lms.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateQuizRequest(
        @NotBlank @Size(max = 200) String title,
        @Positive Integer timeLimit
) {}