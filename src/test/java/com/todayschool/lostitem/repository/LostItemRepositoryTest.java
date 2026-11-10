package com.todayschool.lostitem.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.todayschool.lostitem.domain.Claim;
import com.todayschool.lostitem.domain.ClaimStatus;
import com.todayschool.lostitem.domain.ItemCategory;
import com.todayschool.lostitem.domain.ItemStatus;
import com.todayschool.lostitem.domain.LostItem;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class LostItemRepositoryTest {

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("습득물을 저장하면 상태는 보관 중(STORED)입니다")
    void savedLostItemIsStored() {
        LostItem saved = lostItemRepository.save(lostItem("검은색 지갑"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(ItemStatus.STORED);
    }

    @Test
    @DisplayName("반환 신청을 저장하면 상태는 대기(PENDING)입니다")
    void savedClaimIsPending() {
        LostItem item = lostItemRepository.save(lostItem("검은색 지갑"));

        Claim saved = claimRepository.save(claim(item, "학생1"));

        assertThat(saved.getStatus()).isEqualTo(ClaimStatus.PENDING);
        assertThat(saved.getLostItem().getId()).isEqualTo(item.getId());
    }

    @Test
    @DisplayName("습득물을 삭제하면 반환 신청도 함께 삭제됩니다")
    void deletingLostItemDeletesClaims() {
        LostItem item = lostItemRepository.save(lostItem("검은색 지갑"));
        claimRepository.save(claim(item, "학생1"));
        claimRepository.save(claim(item, "학생2"));
        // 연관 목록(claims)을 DB에서 다시 읽도록 영속성 컨텍스트를 비웁니다.
        entityManager.flush();
        entityManager.clear();

        lostItemRepository.deleteById(item.getId());
        entityManager.flush();

        assertThat(lostItemRepository.findById(item.getId())).isEmpty();
        assertThat(claimRepository.count()).isZero();
    }

    private LostItem lostItem(String title) {
        return LostItem.builder()
                .title(title)
                .description("카드 두 장이 들어 있음")
                .category(ItemCategory.WALLET)
                .foundPlace("본관 2층 복도")
                .foundDate(LocalDate.of(2026, 11, 10))
                .build();
    }

    private Claim claim(LostItem item, String claimerName) {
        return Claim.builder()
                .lostItem(item)
                .claimerName(claimerName)
                .description("안쪽에 학생증이 있습니다")
                .build();
    }
}
