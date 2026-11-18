package com.todayschool.domain.lostitem.repository;

import com.todayschool.domain.lostitem.dto.LostItemStatsResponse;
import com.todayschool.domain.lostitem.entity.LostItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LostItemRepository extends JpaRepository<LostItem, Long>, LostItemRepositoryCustom {

    /** 카테고리별 전체 수와 반환 완료 수를 한 번의 GROUP BY 쿼리로 셉니다. */
    @Query("""
            select new com.todayschool.domain.lostitem.dto.LostItemStatsResponse(
                l.category,
                count(l),
                sum(case when l.status = com.todayschool.domain.lostitem.entity.ItemStatus.RETURNED
                         then 1L else 0L end))
            from LostItem l
            group by l.category
            """)
    List<LostItemStatsResponse> countByCategory();
}
