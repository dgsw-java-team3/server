package com.todayschool.lostitem.dto;

import com.todayschool.lostitem.domain.ItemCategory;
import com.todayschool.lostitem.domain.ItemStatus;
import java.time.LocalDate;

public record LostItemResponse(Long id, String title, String description, ItemCategory category,
                               String foundPlace, LocalDate foundDate, ItemStatus status, int claimCount) {}
