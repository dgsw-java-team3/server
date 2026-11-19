package com.todayschool.domain.meal.dto;

import java.time.LocalDateTime;

public record ReviewResponse(Long id, Long mealId, int rating, String comment, String nickname,
                             LocalDateTime createdAt) {}
