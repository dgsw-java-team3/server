package com.todayschool.domain.meal.controller;

import com.todayschool.domain.meal.dto.ReviewCreateRequest;
import com.todayschool.domain.meal.dto.ReviewResponse;
import com.todayschool.domain.meal.service.ReviewService;
import com.todayschool.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "급식 리뷰")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "별점 1~5, 한줄평, 닉네임을 입력합니다.")
    @PostMapping("/meals/{mealId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponse> createReview(@PathVariable @Positive Long mealId,
                                                    @Valid @RequestBody ReviewCreateRequest request) {
        return ApiResponse.ok(reviewService.createReview(mealId, request));
    }

    @Operation(summary = "리뷰 목록 조회", description = "작성 시각 내림차순이며, 시각이 같으면 ID 내림차순입니다.")
    @GetMapping("/meals/{mealId}/reviews")
    public ApiResponse<List<ReviewResponse>> getReviews(@PathVariable @Positive Long mealId) {
        return ApiResponse.ok(reviewService.getReviews(mealId));
    }

    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("/reviews/{reviewId}")
    public ApiResponse<Void> deleteReview(@PathVariable @Positive Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ApiResponse.ok(null, "리뷰가 삭제되었습니다.");
    }
}
