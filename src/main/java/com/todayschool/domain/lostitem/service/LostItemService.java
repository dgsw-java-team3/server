package com.todayschool.domain.lostitem.service;

import com.todayschool.domain.lostitem.dto.ClaimCreateRequest;
import com.todayschool.domain.lostitem.dto.ClaimResponse;
import com.todayschool.domain.lostitem.dto.LostItemCreateRequest;
import com.todayschool.domain.lostitem.dto.LostItemResponse;
import com.todayschool.domain.lostitem.dto.LostItemUpdateRequest;
import java.util.List;

public interface LostItemService {
    LostItemResponse createLostItem(LostItemCreateRequest request);
    LostItemResponse getLostItem(Long itemId);
    LostItemResponse updateLostItem(Long itemId, LostItemUpdateRequest request);
    void deleteLostItem(Long itemId);
    ClaimResponse createClaim(Long itemId, ClaimCreateRequest request);
    List<ClaimResponse> getClaims(Long itemId);
}
