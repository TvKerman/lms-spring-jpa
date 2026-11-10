package com.example.lms.web.dto;

public record LessonResponse(
        Long id,
        String title,
        String content,
        String videoUrl
) {}