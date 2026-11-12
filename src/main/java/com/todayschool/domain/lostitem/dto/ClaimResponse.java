package com.todayschool.domain.lostitem.dto;

import com.todayschool.domain.lostitem.entity.ClaimStatus;
import java.time.LocalDateTime;

public record ClaimResponse(Long id, Long itemId, String claimerName, String description,
                            ClaimStatus status, LocalDateTime createdAt) {}
