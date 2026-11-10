package com.example.lms.web.dto;

import java.time.LocalDate;

public record AssignmentResponse(
        Long id,
        Long lessonId,
        String title,
        String description,
        LocalDate dueDate,
        Integer maxScore
) {}