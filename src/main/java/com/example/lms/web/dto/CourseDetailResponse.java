package com.example.lms.web.dto;

import java.time.LocalDate;
import java.util.List;

public record CourseDetailResponse(
        Long id,
        String title,
        String description,
        String duration,
        LocalDate startDate,
        Long categoryId,
        String categoryName,
        Long teacherId,
        String teacherName,
        List<ModuleResponse> modules,
        List<String> tags,
        Double avgRating,
        long studentCount
) {}