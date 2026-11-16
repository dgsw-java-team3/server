package com.todayschool.domain.lostitem.repository;

import static com.todayschool.domain.lostitem.entity.QLostItem.lostItem;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todayschool.domain.lostitem.entity.ItemCategory;
import com.todayschool.domain.lostitem.entity.ItemStatus;
import com.todayschool.domain.lostitem.entity.LostItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

/** Spring Data가 이름 규칙(~Impl)으로 찾아 LostItemRepository에 합쳐 줍니다. */
@RequiredArgsConstructor
public class LostItemRepositoryImpl implements LostItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /** 값이 없는 조건은 null을 돌려주고, where()는 null 조건을 빼고 AND로 묶습니다. */
    @Override
    public List<LostItem> search(ItemCategory category, String place, ItemStatus status, String keyword) {
        return queryFactory
                .selectFrom(lostItem)
                .where(categoryEq(category), placeContains(place), statusEq(status), keywordContains(keyword))
                .orderBy(lostItem.foundDate.desc())
                .fetch();
    }

    private BooleanExpression categoryEq(ItemCategory category) {
        return category != null ? lostItem.category.eq(category) : null;
    }

    private BooleanExpression placeContains(String place) {
        return StringUtils.hasText(place) ? lostItem.foundPlace.contains(place) : null;
    }

    private BooleanExpression statusEq(ItemStatus status) {
        return status != null ? lostItem.status.eq(status) : null;
    }

    /** 키워드는 제목이나 설명 중 한 곳에만 있어도 찾습니다. */
    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword)
                ? lostItem.title.contains(keyword).or(lostItem.description.contains(keyword))
                : null;
    }
}
