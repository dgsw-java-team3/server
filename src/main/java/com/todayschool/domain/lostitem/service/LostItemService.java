package com.todayschool.domain.lostitem.service;

import com.todayschool.domain.lostitem.dto.ClaimCreateRequest;
import com.todayschool.domain.lostitem.dto.ClaimResponse;
import com.todayschool.domain.lostitem.dto.ClaimStatusUpdateRequest;
import com.todayschool.domain.lostitem.dto.LostItemCreateRequest;
import com.todayschool.domain.lostitem.dto.LostItemResponse;
import com.todayschool.domain.lostitem.dto.LostItemStatsResponse;
import com.todayschool.domain.lostitem.dto.LostItemUpdateRequest;
import com.todayschool.domain.lostitem.entity.ItemCategory;
import com.todayschool.domain.lostitem.entity.ItemStatus;
import java.util.List;

public interface LostItemService {
    LostItemResponse createLostItem(LostItemCreateRequest request);
    List<LostItemResponse> searchLostItems(ItemCategory category, String place, ItemStatus status, String keyword);
    LostItemResponse getLostItem(Long itemId);
    LostItemResponse updateLostItem(Long itemId, LostItemUpdateRequest request);
    void deleteLostItem(Long itemId);
    List<LostItemStatsResponse> getStats();
    ClaimResponse createClaim(Long itemId, ClaimCreateRequest request);
    List<ClaimResponse> getClaims(Long itemId);
    ClaimResponse updateClaimStatus(Long claimId, ClaimStatusUpdateRequest request);
}
