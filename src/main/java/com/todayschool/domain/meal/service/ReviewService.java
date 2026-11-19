package com.todayschool.domain.meal.service;

import com.todayschool.domain.meal.dto.ReviewCreateRequest;
import com.todayschool.domain.meal.dto.ReviewResponse;
import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(Long mealId, ReviewCreateRequest request);
    List<ReviewResponse> getReviews(Long mealId);
    void deleteReview(Long reviewId);
}
