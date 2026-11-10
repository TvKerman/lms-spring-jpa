package com.example.lms.web.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollRequest(@NotNull Long studentId) {}