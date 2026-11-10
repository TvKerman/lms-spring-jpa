package com.example.lms.web.dto;

import java.time.LocalDate;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        String studentName,
        Long courseId,
        String courseTitle,
        LocalDate enrollDate,
        String status
) {}