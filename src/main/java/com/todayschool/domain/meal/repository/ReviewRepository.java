package com.todayschool.domain.meal.repository;

import com.todayschool.domain.meal.dto.MealRankingResponse;
import com.todayschool.domain.meal.entity.Review;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMealIdOrderByCreatedAtDescIdDesc(Long mealId);

    interface ReviewSummary {
        Double getAverageRating();
        long getReviewCount();
    }

    @Query("select avg(r.rating) as averageRating, count(r) as reviewCount from Review r where r.meal.id = :mealId")
    ReviewSummary summarizeByMealId(@Param("mealId") Long mealId);

    @Query("""
            select new com.todayschool.domain.meal.dto.MealRankingResponse(
                m.id, m.date, m.mealType, m.menu, avg(r.rating), count(r))
            from Review r join r.meal m
            where m.date between :startDate and :endDate
            group by m.id, m.date, m.mealType, m.menu
            order by avg(r.rating) desc, count(r) desc, m.id asc
            """)
    List<MealRankingResponse> findMealRanking(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
}
