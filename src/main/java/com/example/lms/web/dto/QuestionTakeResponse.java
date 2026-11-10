package com.example.lms.web.dto;

import java.util.List;

public record QuestionTakeResponse(
        Long id,
        String text,
        String type,
        List<OptionResponse> options
) {}