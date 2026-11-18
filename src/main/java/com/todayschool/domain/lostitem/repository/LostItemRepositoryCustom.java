package com.todayschool.domain.lostitem.repository;

import com.todayschool.domain.lostitem.entity.ItemCategory;
import com.todayschool.domain.lostitem.entity.ItemStatus;
import com.todayschool.domain.lostitem.entity.LostItem;
import java.util.List;

/** QueryDSL로 구현하는 습득물 조회 메서드입니다. */
public interface LostItemRepositoryCustom {

    List<LostItem> search(ItemCategory category, String place, ItemStatus status, String keyword);
}
