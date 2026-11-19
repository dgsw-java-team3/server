package com.todayschool.domain.meal.service;

import com.todayschool.domain.meal.dto.ReviewCreateRequest;
import com.todayschool.domain.meal.dto.ReviewResponse;
import com.todayschool.domain.meal.entity.Meal;
import com.todayschool.domain.meal.entity.Review;
import com.todayschool.domain.meal.repository.MealRepository;
import com.todayschool.domain.meal.repository.ReviewRepository;
import com.todayschool.global.exception.BusinessException;
import com.todayschool.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final MealRepository mealRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(Long mealId, ReviewCreateRequest request) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEAL_NOT_FOUND));
        return toResponse(reviewRepository.saveAndFlush(
                new Review(meal, request.rating(), request.comment(), request.nickname())));
    }

    @Override
    public List<ReviewResponse> getReviews(Long mealId) {
        if (!mealRepository.existsById(mealId)) {
            throw new BusinessException(ErrorCode.MEAL_NOT_FOUND);
        }
        return reviewRepository.findByMealIdOrderByCreatedAtDescIdDesc(mealId).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        reviewRepository.delete(review);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(review.getId(), review.getMeal().getId(), review.getRating(),
                review.getComment(), review.getNickname(), review.getCreatedAt());
    }
}
