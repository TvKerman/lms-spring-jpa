package com.example.lms.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLessonRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String content,
        @Size(max = 500) String videoUrl
) {}