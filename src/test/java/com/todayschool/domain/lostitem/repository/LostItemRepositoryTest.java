package com.todayschool.domain.lostitem.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.todayschool.domain.lostitem.dto.LostItemStatsResponse;
import com.todayschool.domain.lostitem.entity.Claim;
import com.todayschool.domain.lostitem.entity.ClaimStatus;
import com.todayschool.domain.lostitem.entity.ItemCategory;
import com.todayschool.domain.lostitem.entity.ItemStatus;
import com.todayschool.domain.lostitem.entity.LostItem;
import com.todayschool.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

/** @DataJpaTest는 JPA 빈만 올리므로 검색에 쓰는 JPAQueryFactory 설정을 직접 불러옵니다. */
@DataJpaTest
@Import(QuerydslConfig.class)
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
        // data.sql 더미 신청이 있으므로 전체 개수 대신 삭제 전후 차이로 확인합니다.
        long claimCountBefore = claimRepository.count();

        lostItemRepository.deleteById(item.getId());
        entityManager.flush();

        assertThat(lostItemRepository.findById(item.getId())).isEmpty();
        assertThat(claimRepository.count()).isEqualTo(claimCountBefore - 2);
    }

    @Test
    @DisplayName("카테고리와 장소를 함께 주면 둘 다 맞는 습득물만 최신 발견일 순으로 찾습니다")
    void searchByCategoryAndPlace() {
        // data.sql 더미와 겹치지 않도록 테스트에서만 쓰는 장소 이름을 씁니다.
        LostItem older = lostItemRepository.save(lostItem("검은색 지갑", ItemCategory.WALLET, "테스트동 1층 매점", 3));
        LostItem newer = lostItemRepository.save(lostItem("갈색 지갑", ItemCategory.WALLET, "테스트동 1층 매점", 5));
        lostItemRepository.save(lostItem("흰색 이어폰", ItemCategory.ELECTRONICS, "테스트동 1층 매점", 4));
        lostItemRepository.save(lostItem("빨간 지갑", ItemCategory.WALLET, "테스트동 2층 복도", 6));

        List<LostItem> result = lostItemRepository.search(ItemCategory.WALLET, "1층 매점", null, null);

        assertThat(result).extracting(LostItem::getId).containsExactly(newer.getId(), older.getId());
    }

    @Test
    @DisplayName("키워드는 제목이나 설명 중 한 곳에만 있어도 찾습니다")
    void searchByKeywordInTitleOrDescription() {
        LostItem inTitle = lostItemRepository.save(lostItem("테스트키워드 필통", ItemCategory.STATIONERY, "테스트동 3층", 1));
        LostItem inDescription = lostItemRepository.save(LostItem.builder()
                .title("파란 필통")
                .description("테스트키워드 스티커가 붙어 있음")
                .category(ItemCategory.STATIONERY)
                .foundPlace("테스트동 3층")
                .foundDate(LocalDate.of(2026, 11, 2))
                .build());

        List<LostItem> result = lostItemRepository.search(null, null, null, "테스트키워드");

        assertThat(result).extracting(LostItem::getId).containsExactly(inDescription.getId(), inTitle.getId());
    }

    @Test
    @DisplayName("조건에 맞는 습득물이 없으면 빈 목록을 반환합니다")
    void searchWithNoMatchReturnsEmpty() {
        lostItemRepository.save(lostItem("검은색 지갑", ItemCategory.WALLET, "테스트동 1층 매점", 3));

        List<LostItem> result = lostItemRepository.search(ItemCategory.CLOTHING, "테스트동 1층 매점", null, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("통계는 카테고리별 전체 수와 반환 완료 수를 세고 반환율을 계산합니다")
    void countByCategory() {
        // data.sql 더미가 있으므로 저장 전후 차이로 확인합니다.
        LostItemStatsResponse before = clothingStats();
        lostItemRepository.save(lostItem("남색 후드티", ItemCategory.CLOTHING, "테스트동 4층", 1));
        LostItem returned = lostItem("검은 점퍼", ItemCategory.CLOTHING, "테스트동 4층", 2);
        ReflectionTestUtils.setField(returned, "status", ItemStatus.RETURNED);
        lostItemRepository.save(returned);

        LostItemStatsResponse after = clothingStats();

        assertThat(after.totalCount()).isEqualTo(before.totalCount() + 2);
        assertThat(after.returnedCount()).isEqualTo(before.returnedCount() + 1);
        assertThat(after.returnRate()).isEqualTo((double) after.returnedCount() / after.totalCount());
    }

    private LostItemStatsResponse clothingStats() {
        return lostItemRepository.countByCategory().stream()
                .filter(stats -> stats.category() == ItemCategory.CLOTHING)
                .findFirst()
                .orElse(new LostItemStatsResponse(ItemCategory.CLOTHING, 0L, 0L));
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

    private LostItem lostItem(String title, ItemCategory category, String foundPlace, int foundDay) {
        return LostItem.builder()
                .title(title)
                .description("검색 테스트용 습득물")
                .category(category)
                .foundPlace(foundPlace)
                .foundDate(LocalDate.of(2026, 11, foundDay))
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
