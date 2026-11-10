package com.example.lms.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TakeQuizRequest(
        @NotEmpty @Valid List<AnswerItem> answers
) {
    public record AnswerItem(
            @NotNull Long questionId,
            @NotNull List<Long> selectedOptionIds
    ) {}
}