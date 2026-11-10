package com.example.lms.web.dto;

import java.time.LocalDate;

public record CourseResponse(
        Long id,
        String title,
        String description,
        String duration,
        LocalDate startDate,
        Long categoryId,
        String categoryName,
        Long teacherId,
        String teacherName
) {}