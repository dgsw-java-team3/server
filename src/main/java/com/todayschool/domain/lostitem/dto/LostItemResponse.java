package com.todayschool.domain.lostitem.dto;

import com.todayschool.domain.lostitem.entity.ItemCategory;
import com.todayschool.domain.lostitem.entity.ItemStatus;
import java.time.LocalDate;

public record LostItemResponse(Long id, String title, String description, ItemCategory category,
                               String foundPlace, LocalDate foundDate, ItemStatus status, int claimCount) {}
