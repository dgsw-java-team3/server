package com.todayschool.domain.meal.service;

import static com.todayschool.global.exception.ErrorCode.DUPLICATE_MEAL;
import static com.todayschool.global.exception.ErrorCode.INVALID_INPUT_VALUE;
import static com.todayschool.global.exception.ErrorCode.MEAL_NOT_FOUND;

import com.todayschool.domain.meal.dto.MealCreateRequest;
import com.todayschool.domain.meal.dto.MealPageResponse;
import com.todayschool.domain.meal.dto.MealResponse;
import com.todayschool.domain.meal.dto.MealUpdateRequest;
import com.todayschool.domain.meal.entity.Meal;
import com.todayschool.domain.meal.repository.MealRepository;
import com.todayschool.global.exception.BusinessException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealServiceImpl implements MealService {

    private final MealRepository mealRepository;

    @Override
    @Transactional
    public MealResponse createMeal(MealCreateRequest request) {
        if (mealRepository.existsByDateAndMealType(request.date(), request.mealType())) {
            throw new BusinessException(DUPLICATE_MEAL);
        }
        Meal meal = new Meal(request.date(), request.mealType(), request.menu());
        try {
            // 사전 조회를 동시에 통과해도 DB 고유 제약으로 중복을 막는다.
            return toResponse(mealRepository.saveAndFlush(meal));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(DUPLICATE_MEAL);
        }
    }

    @Override
    public MealPageResponse getMeals(LocalDate date, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(INVALID_INPUT_VALUE);
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("date"), Sort.Order.asc("id")));
        Page<Meal> meals = date == null ? mealRepository.findAll(pageable) : mealRepository.findByDate(date, pageable);
        return MealPageResponse.from(meals.map(this::toResponse));
    }

    @Override
    public MealResponse getMeal(Long mealId) {
        return toResponse(findMeal(mealId));
    }

    @Override
    @Transactional
    public MealResponse updateMeal(Long mealId, MealUpdateRequest request) {
        Meal meal = findMeal(mealId);
        if (mealRepository.existsByDateAndMealTypeAndIdNot(request.date(), request.mealType(), mealId)) {
            throw new BusinessException(DUPLICATE_MEAL);
        }
        meal.update(request.date(), request.mealType(), request.menu());
        try {
            mealRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(DUPLICATE_MEAL);
        }
        return toResponse(meal);
    }

    @Override
    @Transactional
    public void deleteMeal(Long mealId) {
        mealRepository.delete(findMeal(mealId));
    }

    private Meal findMeal(Long mealId) {
        return mealRepository.findById(mealId).orElseThrow(() -> new BusinessException(MEAL_NOT_FOUND));
    }

    private MealResponse toResponse(Meal meal) {
        return new MealResponse(meal.getId(), meal.getDate(), meal.getMealType(), meal.getMenu(),
                meal.getCreatedAt(), meal.getUpdatedAt());
    }
}
