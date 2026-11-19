package com.todayschool.domain.meal.dto;

import com.todayschool.domain.meal.entity.MealType;
import java.time.LocalDate;

public record MealRankingResponse(Long mealId, LocalDate date, MealType mealType, String menu,
                                  double averageRating, long reviewCount) {}
