package com.todayschool.domain.meal.repository;

import com.todayschool.domain.meal.entity.Meal;
import com.todayschool.domain.meal.entity.MealType;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<Meal, Long> {
    Page<Meal> findByDate(LocalDate date, Pageable pageable);
    boolean existsByDateAndMealType(LocalDate date, MealType mealType);
    boolean existsByDateAndMealTypeAndIdNot(LocalDate date, MealType mealType, Long id);
}
