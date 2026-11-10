package com.example.lms.web.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateCourseRequest(
        @Size(max = 200) String title,
        @Size(max = 5000) String description,
        @Size(max = 50) String duration,
        LocalDate startDate
) {}