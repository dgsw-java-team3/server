package com.todayschool.lostitem.repository;

import com.todayschool.lostitem.domain.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {
}
