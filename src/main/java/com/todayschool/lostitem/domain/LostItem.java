package com.todayschool.lostitem.domain;

import com.todayschool.global.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 습득물입니다. 반환 신청(Claim)과 1:N 관계입니다. */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LostItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemCategory category;

    @Column(nullable = false, length = 100)
    private String foundPlace;

    @Column(nullable = false)
    private LocalDate foundDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemStatus status;

    /** 습득물을 삭제하면 반환 신청도 함께 삭제됩니다. */
    @OneToMany(mappedBy = "lostItem", cascade = CascadeType.REMOVE)
    private List<Claim> claims = new ArrayList<>();

    @Builder
    private LostItem(String title, String description, ItemCategory category,
                     String foundPlace, LocalDate foundDate) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.foundPlace = foundPlace;
        this.foundDate = foundDate;
        this.status = ItemStatus.STORED;
    }
}
