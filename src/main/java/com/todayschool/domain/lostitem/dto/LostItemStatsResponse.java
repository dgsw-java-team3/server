package com.todayschool.domain.lostitem.dto;

import com.todayschool.domain.lostitem.entity.ItemCategory;

/** 반환율은 반환 완료 수 / 전체 수이며 0~1 사이 값입니다. */
public record LostItemStatsResponse(ItemCategory category, long totalCount, long returnedCount, double returnRate) {

    /** JPQL 생성자 표현식(select new ...)에서 부르는 생성자입니다. 전체 수가 0이면 반환율을 0으로 둡니다. */
    public LostItemStatsResponse(ItemCategory category, Long totalCount, Long returnedCount) {
        this(category, totalCount, returnedCount,
                totalCount == 0 ? 0.0 : (double) returnedCount / totalCount);
    }
}
