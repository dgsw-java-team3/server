package com.todayschool.domain.meal.dto;

import com.todayschool.domain.meal.entity.MealType;
import java.time.LocalDateTime;
import java.time.LocalDate;

public record MealResponse(Long id, LocalDate date, MealType mealType, String menu,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {}
