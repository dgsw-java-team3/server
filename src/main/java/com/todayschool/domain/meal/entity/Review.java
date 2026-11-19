package com.todayschool.domain.meal.entity;

import com.todayschool.global.entity.BaseTimeEntity;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(name = "meal_reviews",
        indexes = @Index(name = "idx_review_meal_created", columnList = "meal_id, created_at, id"),
        check = @CheckConstraint(name = "ck_review_rating", constraint = "rating between 1 and 5"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_id", nullable = false)
    // FK의 ON DELETE CASCADE로 급식 삭제 시 연결된 리뷰도 함께 삭제합니다.
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Meal meal;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 200)
    private String comment;

    @Column(nullable = false, length = 30)
    private String nickname;

    public Review(Meal meal, int rating, String comment, String nickname) {
        this.meal = meal;
        this.rating = rating;
        this.comment = comment.strip();
        this.nickname = nickname.strip();
    }
}
