package com.todayschool.domain.meal.entity;

import com.todayschool.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "meals", uniqueConstraints = @UniqueConstraint(
        name = "uk_meal_date_type", columnNames = {"meal_date", "meal_type"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;

    @Column(nullable = false, length = 1000)
    private String menu;

    public Meal(LocalDate date, MealType mealType, String menu) {
        update(date, mealType, menu);
    }

    public void update(LocalDate date, MealType mealType, String menu) {
        this.date = date;
        this.mealType = mealType;
        this.menu = menu.strip();
    }
}
