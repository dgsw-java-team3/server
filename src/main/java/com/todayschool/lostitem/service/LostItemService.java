package com.todayschool.lostitem.service;

import com.todayschool.lostitem.dto.LostItemCreateRequest;
import com.todayschool.lostitem.dto.LostItemResponse;
import com.todayschool.lostitem.dto.LostItemUpdateRequest;

public interface LostItemService {
    LostItemResponse createLostItem(LostItemCreateRequest request);
    LostItemResponse getLostItem(Long itemId);
    LostItemResponse updateLostItem(Long itemId, LostItemUpdateRequest request);
    void deleteLostItem(Long itemId);
}
