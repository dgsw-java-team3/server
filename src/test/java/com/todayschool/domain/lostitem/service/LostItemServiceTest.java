package com.todayschool.domain.lostitem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.todayschool.domain.lostitem.dto.ClaimCreateRequest;
import com.todayschool.domain.lostitem.dto.ClaimResponse;
import com.todayschool.domain.lostitem.dto.ClaimStatusUpdateRequest;
import com.todayschool.domain.lostitem.dto.LostItemUpdateRequest;
import com.todayschool.domain.lostitem.entity.Claim;
import com.todayschool.domain.lostitem.entity.ClaimStatus;
import com.todayschool.domain.lostitem.entity.ItemCategory;
import com.todayschool.domain.lostitem.entity.ItemStatus;
import com.todayschool.domain.lostitem.entity.LostItem;
import com.todayschool.domain.lostitem.repository.ClaimRepository;
import com.todayschool.domain.lostitem.repository.LostItemRepository;
import com.todayschool.global.exception.BusinessException;
import com.todayschool.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LostItemServiceTest {

    @Mock
    private LostItemRepository lostItemRepository;

    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private LostItemServiceImpl lostItemService;

    @Test
    @DisplayName("없는 습득물을 조회하면 LOST_ITEM_NOT_FOUND 예외가 발생합니다")
    void getMissingLostItemThrows() {
        when(lostItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lostItemService.getLostItem(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.LOST_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("반환 완료된 습득물은 수정할 수 없고 값도 바뀌지 않습니다")
    void updateReturnedLostItemThrows() {
        LostItem returned = lostItem(1L, ItemStatus.RETURNED);
        when(lostItemRepository.findById(1L)).thenReturn(Optional.of(returned));
        LostItemUpdateRequest request = new LostItemUpdateRequest(
                "바뀐 제목", "바뀐 설명", ItemCategory.ETC, "바뀐 장소", LocalDate.of(2026, 11, 11));

        assertThatThrownBy(() -> lostItemService.updateLostItem(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.LOST_ITEM_ALREADY_RETURNED);
        assertThat(returned.getTitle()).isEqualTo("검은색 반지갑");
    }

    @Test
    @DisplayName("보관 중인 습득물에 반환을 신청하면 대기(PENDING) 상태로 저장됩니다")
    void createClaimSavesPendingClaim() {
        when(lostItemRepository.findById(1L)).thenReturn(Optional.of(lostItem(1L, ItemStatus.STORED)));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClaimResponse response = lostItemService.createClaim(1L, new ClaimCreateRequest("학생1", "학생증이 있습니다"));

        assertThat(response.itemId()).isEqualTo(1L);
        assertThat(response.claimerName()).isEqualTo("학생1");
        assertThat(response.status()).isEqualTo(ClaimStatus.PENDING);
    }

    @Test
    @DisplayName("반환 완료된 습득물에는 반환 신청을 저장하지 않습니다")
    void createClaimOnReturnedLostItemThrows() {
        when(lostItemRepository.findById(1L)).thenReturn(Optional.of(lostItem(1L, ItemStatus.RETURNED)));

        assertThatThrownBy(() -> lostItemService.createClaim(1L, new ClaimCreateRequest("학생1", "학생증이 있습니다")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.LOST_ITEM_ALREADY_RETURNED);
        verify(claimRepository, never()).save(any());
    }

    @Test
    @DisplayName("없는 습득물의 반환 신청 목록은 조회하지 않고 LOST_ITEM_NOT_FOUND 예외가 발생합니다")
    void getClaimsOfMissingLostItemThrows() {
        when(lostItemRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> lostItemService.getClaims(99L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.LOST_ITEM_NOT_FOUND);
        verify(claimRepository, never()).findByLostItemId(anyLong());
    }

    @Test
    @DisplayName("신청을 승인하면 같은 습득물의 다른 대기 신청은 거절되고 습득물은 반환 완료가 됩니다")
    void approveClaimRejectsOthersAndReturnsItem() {
        LostItem item = lostItem(1L, ItemStatus.STORED);
        Claim target = claim(10L, item, ClaimStatus.PENDING);
        Claim other = claim(11L, item, ClaimStatus.PENDING);
        when(claimRepository.findById(10L)).thenReturn(Optional.of(target));
        when(claimRepository.findByLostItemIdAndStatus(1L, ClaimStatus.PENDING)).thenReturn(List.of(target, other));

        ClaimResponse response = lostItemService.updateClaimStatus(10L, new ClaimStatusUpdateRequest(ClaimStatus.APPROVED));

        assertThat(response.status()).isEqualTo(ClaimStatus.APPROVED);
        assertThat(other.getStatus()).isEqualTo(ClaimStatus.REJECTED);
        assertThat(item.getStatus()).isEqualTo(ItemStatus.RETURNED);
    }

    @Test
    @DisplayName("신청을 거절하면 신청만 거절되고 습득물은 보관 중으로 남습니다")
    void rejectClaimKeepsItemStored() {
        LostItem item = lostItem(1L, ItemStatus.STORED);
        when(claimRepository.findById(10L)).thenReturn(Optional.of(claim(10L, item, ClaimStatus.PENDING)));

        ClaimResponse response = lostItemService.updateClaimStatus(10L, new ClaimStatusUpdateRequest(ClaimStatus.REJECTED));

        assertThat(response.status()).isEqualTo(ClaimStatus.REJECTED);
        assertThat(item.getStatus()).isEqualTo(ItemStatus.STORED);
    }

    @Test
    @DisplayName("이미 처리된 신청을 다시 처리하면 CLAIM_ALREADY_PROCESSED 예외가 발생합니다")
    void updateProcessedClaimThrows() {
        LostItem item = lostItem(1L, ItemStatus.STORED);
        Claim rejected = claim(10L, item, ClaimStatus.REJECTED);
        when(claimRepository.findById(10L)).thenReturn(Optional.of(rejected));

        assertThatThrownBy(() -> lostItemService.updateClaimStatus(10L, new ClaimStatusUpdateRequest(ClaimStatus.APPROVED)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CLAIM_ALREADY_PROCESSED);
        assertThat(rejected.getStatus()).isEqualTo(ClaimStatus.REJECTED);
        assertThat(item.getStatus()).isEqualTo(ItemStatus.STORED);
    }

    @Test
    @DisplayName("대기(PENDING)로 바꾸는 요청은 신청을 조회하지 않고 CLAIM_STATUS_INVALID 예외가 발생합니다")
    void updateClaimToPendingThrows() {
        assertThatThrownBy(() -> lostItemService.updateClaimStatus(10L, new ClaimStatusUpdateRequest(ClaimStatus.PENDING)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CLAIM_STATUS_INVALID);
        verify(claimRepository, never()).findById(anyLong());
    }

    /** 테스트용 습득물입니다. id와 상태는 DB 없이 지정하려고 직접 넣습니다. */
    private LostItem lostItem(Long id, ItemStatus status) {
        LostItem lostItem = LostItem.builder()
                .title("검은색 반지갑")
                .description("학생증과 교통카드가 들어 있음")
                .category(ItemCategory.WALLET)
                .foundPlace("본관 2층 복도")
                .foundDate(LocalDate.of(2026, 11, 10))
                .build();
        ReflectionTestUtils.setField(lostItem, "id", id);
        ReflectionTestUtils.setField(lostItem, "status", status);
        return lostItem;
    }

    /** 테스트용 반환 신청입니다. 습득물과 같은 이유로 id와 상태를 직접 넣습니다. */
    private Claim claim(Long id, LostItem lostItem, ClaimStatus status) {
        Claim claim = Claim.builder()
                .lostItem(lostItem)
                .claimerName("학생" + id)
                .description("학생증이 있습니다")
                .build();
        ReflectionTestUtils.setField(claim, "id", id);
        ReflectionTestUtils.setField(claim, "status", status);
        return claim;
    }
}
