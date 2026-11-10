package com.example.lms.web.dto;

import java.time.LocalDateTime;

public record SubmissionResponse(
        Long id,
        Long assignmentId,
        Long studentId,
        String studentName,
        LocalDateTime submittedAt,
        String content,
        Integer score,
        String feedback
) {}