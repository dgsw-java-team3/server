package com.todayschool.lostitem.service;

import static com.todayschool.global.exception.ErrorCode.LOST_ITEM_ALREADY_RETURNED;
import static com.todayschool.global.exception.ErrorCode.LOST_ITEM_NOT_FOUND;

import com.todayschool.global.exception.BusinessException;
import com.todayschool.lostitem.domain.LostItem;
import com.todayschool.lostitem.dto.LostItemCreateRequest;
import com.todayschool.lostitem.dto.LostItemResponse;
import com.todayschool.lostitem.dto.LostItemUpdateRequest;
import com.todayschool.lostitem.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LostItemServiceImpl implements LostItemService {

    private final LostItemRepository lostItemRepository;

    @Override
    @Transactional
    public LostItemResponse createLostItem(LostItemCreateRequest request) {
        LostItem lostItem = LostItem.builder()
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .foundPlace(request.foundPlace())
                .foundDate(request.foundDate())
                .build();
        return toResponse(lostItemRepository.save(lostItem));
    }

    @Override
    public LostItemResponse getLostItem(Long itemId) {
        return toResponse(findLostItem(itemId));
    }

    /** 반환 완료된 습득물은 주인에게 돌아갔으므로 정보를 바꿀 수 없습니다. */
    @Override
    @Transactional
    public LostItemResponse updateLostItem(Long itemId, LostItemUpdateRequest request) {
        LostItem lostItem = findLostItem(itemId);
        if (lostItem.isReturned()) {
            throw new BusinessException(LOST_ITEM_ALREADY_RETURNED);
        }
        lostItem.update(request.title(), request.description(), request.category(),
                request.foundPlace(), request.foundDate());
        return toResponse(lostItem);
    }

    /** 반환 신청은 LostItem의 cascade 설정으로 함께 삭제됩니다. */
    @Override
    @Transactional
    public void deleteLostItem(Long itemId) {
        lostItemRepository.delete(findLostItem(itemId));
    }

    private LostItem findLostItem(Long itemId) {
        return lostItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(LOST_ITEM_NOT_FOUND));
    }

    private LostItemResponse toResponse(LostItem lostItem) {
        return new LostItemResponse(lostItem.getId(), lostItem.getTitle(), lostItem.getDescription(),
                lostItem.getCategory(), lostItem.getFoundPlace(), lostItem.getFoundDate(),
                lostItem.getStatus(), lostItem.getClaims().size());
    }
}
