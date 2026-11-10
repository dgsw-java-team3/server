package com.todayschool.domain.meal.service;

import com.todayschool.domain.meal.dto.*;
import java.time.LocalDate;

public interface MealService {
    MealResponse createMeal(MealCreateRequest request);
    MealPageResponse getMeals(LocalDate date, int page, int size);
    MealResponse getMeal(Long mealId);
    MealResponse updateMeal(Long mealId, MealUpdateRequest request);
    void deleteMeal(Long mealId);
}
