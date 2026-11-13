package com.todayschool.domain.lostitem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClaimCreateRequest(
        @NotBlank @Size(max = 50) @Schema(example = "학생1") String claimerName,
        @NotBlank @Size(max = 500) @Schema(example = "안쪽에 학생증이 있습니다") String description) {}
