package com.todayschool.domain.meal;

import com.todayschool.global.exception.*;
import com.todayschool.domain.meal.dto.*;
import com.todayschool.domain.meal.entity.*;
import com.todayschool.domain.meal.repository.*;
import com.todayschool.domain.meal.service.MealServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {
    @Mock MealRepository mealRepository;
    @InjectMocks MealServiceImpl service;
    private final LocalDate date = LocalDate.of(2026, 11, 10);

    @Test
    void createMealPersistsAndReturnsDto() {
        when(mealRepository.saveAndFlush(any(Meal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.createMeal(new MealCreateRequest(date, MealType.LUNCH, " 쌀밥 "));
        assertThat(response.date()).isEqualTo(date);
        assertThat(response.menu()).isEqualTo("쌀밥");
        verify(mealRepository).saveAndFlush(any(Meal.class));
    }

    @Test
    void duplicateMealIsRejectedBeforeSave() {
        when(mealRepository.existsByDateAndMealType(date, MealType.LUNCH)).thenReturn(true);
        assertThatThrownBy(() -> service.createMeal(new MealCreateRequest(date, MealType.LUNCH, "쌀밥")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_MEAL));
        verify(mealRepository, never()).saveAndFlush(any());
    }

    @Test
    void missingMealThrows404BusinessCode() {
        assertThatThrownBy(() -> service.getMeal(999L)).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEAL_NOT_FOUND));
    }

    @Test
    void updateChangesManagedEntityAndKeepsOwnDateAllowed() {
        var meal = new Meal(date, MealType.LUNCH, "기존 메뉴");
        when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
        var result = service.updateMeal(1L, new MealUpdateRequest(date, MealType.LUNCH, "새 메뉴"));
        assertThat(result.menu()).isEqualTo("새 메뉴");
        verify(mealRepository).existsByDateAndMealTypeAndIdNot(date, MealType.LUNCH, 1L);
        verify(mealRepository).flush();
    }

    @Test
    void duplicateUpdateDoesNotChangeEntity() {
        var meal = new Meal(date, MealType.LUNCH, "기존 메뉴");
        when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
        when(mealRepository.existsByDateAndMealTypeAndIdNot(date, MealType.DINNER, 1L)).thenReturn(true);
        assertThatThrownBy(() -> service.updateMeal(1L, new MealUpdateRequest(date, MealType.DINNER, "새 메뉴")))
                .isInstanceOf(BusinessException.class);
        assertThat(meal.getMenu()).isEqualTo("기존 메뉴");
    }

    @Test
    void deleteExistingMeal() {
        var meal = new Meal(date, MealType.LUNCH, "메뉴");
        when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
        service.deleteMeal(1L);
        verify(mealRepository).delete(meal);
    }

    @Test
    void deleteMissingMealDoesNotDelete() {
        assertThatThrownBy(() -> service.deleteMeal(999L)).isInstanceOf(BusinessException.class);
        verify(mealRepository, never()).delete(any());
    }

    @Test
    void databaseUniqueConflictBecomesDuplicateMeal() {
        when(mealRepository.saveAndFlush(any(Meal.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("unique constraint"));
        assertThatThrownBy(() -> service.createMeal(new MealCreateRequest(date, MealType.LUNCH, "메뉴")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_MEAL));
    }

    @Test
    void invalidPageDoesNotExecuteQuery() {
        assertThatThrownBy(() -> service.getMeals(null, -1, 20)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(mealRepository);
    }
}
