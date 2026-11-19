package com.todayschool.domain.meal.dto;

import com.todayschool.domain.meal.entity.MealType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MealDetailResponse(Long id, LocalDate date, MealType mealType, String menu,
                                 LocalDateTime createdAt, LocalDateTime updatedAt,
                                 double averageRating, long reviewCount) {}
