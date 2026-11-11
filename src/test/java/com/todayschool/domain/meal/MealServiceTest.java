package com.todayschool.domain.meal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.todayschool.domain.meal.dto.MealCreateRequest;
import com.todayschool.domain.meal.dto.MealUpdateRequest;
import com.todayschool.domain.meal.entity.Meal;
import com.todayschool.domain.meal.entity.MealType;
import com.todayschool.domain.meal.repository.MealRepository;
import com.todayschool.domain.meal.service.MealServiceImpl;
import com.todayschool.global.exception.BusinessException;
import com.todayschool.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MealServiceTest {

    @Mock
    private MealRepository mealRepository;
    @InjectMocks
    private MealServiceImpl service;
    private final LocalDate date = LocalDate.of(2026, 11, 10);

    @Test
    @DisplayName("급식을 저장하고 응답 DTO를 반환한다")
    void createMealPersistsAndReturnsDto() {
        when(mealRepository.saveAndFlush(any(Meal.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.createMeal(new MealCreateRequest(date, MealType.LUNCH, " 쌀밥 "));
        assertThat(response.date()).isEqualTo(date);
        assertThat(response.menu()).isEqualTo("쌀밥");
        verify(mealRepository).saveAndFlush(any(Meal.class));
    }

    @Test
    @DisplayName("같은 날짜와 구분의 급식은 저장하지 않는다")
    void duplicateMealIsRejectedBeforeSave() {
        when(mealRepository.existsByDateAndMealType(date, MealType.LUNCH)).thenReturn(true);
        assertThatThrownBy(() -> service.createMeal(new MealCreateRequest(date, MealType.LUNCH, "쌀밥")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_MEAL));
        verify(mealRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("없는 급식 조회 시 예외가 발생한다")
    void missingMealThrows404BusinessCode() {
        assertThatThrownBy(() -> service.getMeal(999L)).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEAL_NOT_FOUND));
    }

    @Test
    @DisplayName("날짜와 구분을 유지하면서 메뉴를 수정할 수 있다")
    void updateChangesManagedEntityAndKeepsOwnDateAllowed() {
        var meal = new Meal(date, MealType.LUNCH, "기존 메뉴");
        when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
        var result = service.updateMeal(1L, new MealUpdateRequest(date, MealType.LUNCH, "새 메뉴"));
        assertThat(result.menu()).isEqualTo("새 메뉴");
        verify(mealRepository).existsByDateAndMealTypeAndIdNot(date, MealType.LUNCH, 1L);
        verify(mealRepository).flush();
    }

    @Test
    @DisplayName("중복되는 날짜와 구분으로 수정할 수 없다")
    void duplicateUpdateDoesNotChangeEntity() {
        var meal = new Meal(date, MealType.LUNCH, "기존 메뉴");
        when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
        when(mealRepository.existsByDateAndMealTypeAndIdNot(date, MealType.DINNER, 1L)).thenReturn(true);
        assertThatThrownBy(() -> service.updateMeal(1L, new MealUpdateRequest(date, MealType.DINNER, "새 메뉴")))
                .isInstanceOf(BusinessException.class);
        assertThat(meal.getMenu()).isEqualTo("기존 메뉴");
    }

    @Test
    @DisplayName("급식을 삭제한다")
    void deleteExistingMeal() {
        var meal = new Meal(date, MealType.LUNCH, "메뉴");
        when(mealRepository.findById(1L)).thenReturn(Optional.of(meal));
        service.deleteMeal(1L);
        verify(mealRepository).delete(meal);
    }

    @Test
    @DisplayName("없는 급식은 삭제할 수 없다")
    void deleteMissingMealDoesNotDelete() {
        assertThatThrownBy(() -> service.deleteMeal(999L)).isInstanceOf(BusinessException.class);
        verify(mealRepository, never()).delete(any());
    }

    @Test
    @DisplayName("DB 고유 제약 위반을 급식 중복 오류로 처리한다")
    void databaseUniqueConflictBecomesDuplicateMeal() {
        when(mealRepository.saveAndFlush(any(Meal.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("unique constraint"));
        assertThatThrownBy(() -> service.createMeal(new MealCreateRequest(date, MealType.LUNCH, "메뉴")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_MEAL));
    }

    @Test
    @DisplayName("잘못된 페이지 요청은 조회하지 않는다")
    void invalidPageDoesNotExecuteQuery() {
        assertThatThrownBy(() -> service.getMeals(null, -1, 20)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(mealRepository);
    }
}
