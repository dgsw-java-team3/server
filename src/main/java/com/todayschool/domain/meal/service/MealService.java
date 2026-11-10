package com.todayschool.domain.meal.service;

import com.todayschool.domain.meal.dto.MealCreateRequest;
import com.todayschool.domain.meal.dto.MealPageResponse;
import com.todayschool.domain.meal.dto.MealResponse;
import com.todayschool.domain.meal.dto.MealUpdateRequest;
import java.time.LocalDate;

public interface MealService {
    MealResponse createMeal(MealCreateRequest request);
    MealPageResponse getMeals(LocalDate date, int page, int size);
    MealResponse getMeal(Long mealId);
    MealResponse updateMeal(Long mealId, MealUpdateRequest request);
    void deleteMeal(Long mealId);
}
