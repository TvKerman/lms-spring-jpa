package com.example.lms.web.dto;

import com.example.lms.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateQuestionRequest(
        @NotBlank @Size(max = 5000) String text,
        @NotNull QuestionType type,
        @NotEmpty @Valid List<CreateOptionRequest> options
) {}