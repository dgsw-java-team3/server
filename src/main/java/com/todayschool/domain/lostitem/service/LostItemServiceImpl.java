package com.todayschool.domain.lostitem.service;

import static com.todayschool.global.exception.ErrorCode.LOST_ITEM_ALREADY_RETURNED;
import static com.todayschool.global.exception.ErrorCode.LOST_ITEM_NOT_FOUND;

import com.todayschool.global.exception.BusinessException;
import com.todayschool.domain.lostitem.entity.Claim;
import com.todayschool.domain.lostitem.entity.LostItem;
import com.todayschool.domain.lostitem.dto.ClaimCreateRequest;
import com.todayschool.domain.lostitem.dto.ClaimResponse;
import com.todayschool.domain.lostitem.dto.LostItemCreateRequest;
import com.todayschool.domain.lostitem.dto.LostItemResponse;
import com.todayschool.domain.lostitem.dto.LostItemUpdateRequest;
import com.todayschool.domain.lostitem.repository.ClaimRepository;
import com.todayschool.domain.lostitem.repository.LostItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LostItemServiceImpl implements LostItemService {

    private final LostItemRepository lostItemRepository;
    private final ClaimRepository claimRepository;

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

    /** 반환 완료된 습득물은 이미 주인에게 돌아갔으므로 새 신청을 받지 않습니다. */
    @Override
    @Transactional
    public ClaimResponse createClaim(Long itemId, ClaimCreateRequest request) {
        LostItem lostItem = findLostItem(itemId);
        if (lostItem.isReturned()) {
            throw new BusinessException(LOST_ITEM_ALREADY_RETURNED);
        }
        Claim claim = Claim.builder()
                .lostItem(lostItem)
                .claimerName(request.claimerName())
                .description(request.description())
                .build();
        return toClaimResponse(claimRepository.save(claim));
    }

    @Override
    public List<ClaimResponse> getClaims(Long itemId) {
        if (!lostItemRepository.existsById(itemId)) {
            throw new BusinessException(LOST_ITEM_NOT_FOUND);
        }
        return claimRepository.findByLostItemId(itemId).stream()
                .map(this::toClaimResponse)
                .toList();
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

    private ClaimResponse toClaimResponse(Claim claim) {
        return new ClaimResponse(claim.getId(), claim.getLostItem().getId(), claim.getClaimerName(),
                claim.getDescription(), claim.getStatus(), claim.getCreatedAt());
    }
}
