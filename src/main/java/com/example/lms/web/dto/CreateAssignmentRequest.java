package com.example.lms.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAssignmentRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        LocalDate dueDate,
        @NotNull @Positive Integer maxScore
) {}