package com.example.lms.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCourseRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        @Size(max = 50) String duration,
        LocalDate startDate,
        @NotNull Long categoryId,
        @NotNull Long teacherId
) {}