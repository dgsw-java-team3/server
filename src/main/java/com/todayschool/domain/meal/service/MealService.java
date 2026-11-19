package com.todayschool.domain.meal.service;

import com.todayschool.domain.meal.dto.MealCreateRequest;
import com.todayschool.domain.meal.dto.MealDetailResponse;
import com.todayschool.domain.meal.dto.MealRankingResponse;
import com.todayschool.domain.meal.dto.MealPageResponse;
import com.todayschool.domain.meal.dto.MealResponse;
import com.todayschool.domain.meal.dto.MealUpdateRequest;
import java.time.LocalDate;
import java.util.List;

public interface MealService {
    MealResponse createMeal(MealCreateRequest request);
    MealPageResponse getMeals(LocalDate date, int page, int size);
    MealDetailResponse getMeal(Long mealId);
    List<MealRankingResponse> getMealRanking(LocalDate startDate, LocalDate endDate);
    MealResponse updateMeal(Long mealId, MealUpdateRequest request);
    void deleteMeal(Long mealId);
}
