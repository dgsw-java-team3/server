package com.todayschool.domain.meal.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record MealPageResponse(List<MealResponse> content, int page, int size,
                               long totalElements, int totalPages) {
    public static MealPageResponse from(Page<MealResponse> page) {
        return new MealPageResponse(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
