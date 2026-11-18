package com.todayschool.domain.lostitem.dto;

import com.todayschool.domain.lostitem.entity.ClaimStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ClaimStatusUpdateRequest(
        @NotNull @Schema(example = "APPROVED", allowableValues = {"APPROVED", "REJECTED"}) ClaimStatus status) {}
