package com.example.lms.web.dto;

public record QuizResponse(
        Long id,
        Long moduleId,
        String title,
        Integer timeLimit,
        long questionsCount
) {}