package com.todayschool.domain.lostitem.repository;

import com.todayschool.domain.lostitem.entity.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {
}
