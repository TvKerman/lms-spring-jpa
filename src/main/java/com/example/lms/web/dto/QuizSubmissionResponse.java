package com.example.lms.web.dto;

import java.time.LocalDateTime;

public record QuizSubmissionResponse(
        Long id,
        Long quizId,
        String quizTitle,
        Long studentId,
        String studentName,
        Integer score,
        LocalDateTime takenAt
) {}