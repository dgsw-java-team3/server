package com.todayschool.lostitem.controller;

import com.todayschool.global.response.ApiResponse;
import com.todayschool.lostitem.dto.LostItemCreateRequest;
import com.todayschool.lostitem.dto.LostItemResponse;
import com.todayschool.lostitem.dto.LostItemUpdateRequest;
import com.todayschool.lostitem.service.LostItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "분실물")
@RestController
@RequestMapping("/api/lost-items")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemService lostItemService;

    @Operation(summary = "습득물 등록", description = "등록하면 상태는 보관 중(STORED)입니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<LostItemResponse>> createLostItem(
            @Valid @RequestBody LostItemCreateRequest request) {
        LostItemResponse lostItem = lostItemService.createLostItem(request);
        return ResponseEntity.created(URI.create("/api/lost-items/" + lostItem.id()))
                .body(ApiResponse.ok(lostItem));
    }

    @Operation(summary = "습득물 상세 조회")
    @GetMapping("/{itemId}")
    public ApiResponse<LostItemResponse> getLostItem(@PathVariable @Positive Long itemId) {
        return ApiResponse.ok(lostItemService.getLostItem(itemId));
    }

    @Operation(summary = "습득물 수정", description = "반환 완료(RETURNED)된 습득물은 수정할 수 없습니다(409).")
    @PutMapping("/{itemId}")
    public ApiResponse<LostItemResponse> updateLostItem(@PathVariable @Positive Long itemId,
                                                        @Valid @RequestBody LostItemUpdateRequest request) {
        return ApiResponse.ok(lostItemService.updateLostItem(itemId, request));
    }

    @Operation(summary = "습득물 삭제", description = "반환 신청도 함께 삭제됩니다.")
    @DeleteMapping("/{itemId}")
    public ApiResponse<Void> deleteLostItem(@PathVariable @Positive Long itemId) {
        lostItemService.deleteLostItem(itemId);
        return ApiResponse.ok(null, "습득물이 삭제되었습니다.");
    }
}
