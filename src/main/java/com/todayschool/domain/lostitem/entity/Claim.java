package com.todayschool.domain.lostitem.entity;

import com.todayschool.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 습득물에 대한 반환 신청입니다. */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Claim extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_item_id", nullable = false)
    private LostItem lostItem;

    @Column(nullable = false, length = 50)
    private String claimerName;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status;

    @Builder
    private Claim(LostItem lostItem, String claimerName, String description) {
        this.lostItem = lostItem;
        this.claimerName = claimerName;
        this.description = description;
        this.status = ClaimStatus.PENDING;
    }
}
