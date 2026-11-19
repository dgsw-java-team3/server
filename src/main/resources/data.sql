INSERT INTO meals (id, meal_date, meal_type, menu, created_at, updated_at) VALUES
    (1, '2026-11-09', 'LUNCH', '쌀밥, 미역국, 제육볶음, 김치', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, '2026-11-10', 'LUNCH', '스팸마요덮밥, 우동국물, 단무지, 사과', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, '2026-11-10', 'DINNER', '현미밥, 된장국, 닭갈비, 깍두기', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, '2026-11-11', 'LUNCH', '카레라이스, 유부국, 샐러드, 요구르트', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, '2026-11-09', 'BREAKFAST', '토스트, 스크램블에그, 우유', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE meals ALTER COLUMN id RESTART WITH 6;

-- ===== B. 실습실 예약 (room, reservation) =====
-- 다른 테스트가 @Sql로 이 파일을 다시 실행해도 중복되지 않도록 MERGE INTO ... KEY(id)를 사용합니다.
-- 예약이 있는 호실(1~4): 삭제 시 409(ROOM_HAS_RESERVATIONS)를 확인할 수 있습니다.
-- 예약이 없는 호실(5, 6): 삭제 시 200을 확인할 수 있습니다.
MERGE INTO room (id, name, capacity, location, created_at, updated_at) KEY (id) VALUES
    (1, '실습실 101', 30, '본관 1층', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, '실습실 102', 24, '본관 1층', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, '실습실 201', 40, '본관 2층', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, '실습실 202', 20, '본관 2층', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, '프로그래밍실', 36, '별관 3층', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, '소회의실', 10, '별관 1층', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE room ALTER COLUMN id RESTART WITH 7;

-- 겹침 테스트용: 호실 1, 2026-11-20 10:00~12:00 (id 1).
--   11:00~13:00, 10:30~11:30 예약 -> 409(RESERVATION_CONFLICT)
--   12:00~13:00, 09:00~10:00 예약 -> 201 (맞닿는 경계는 허용)
-- 호실 2는 2026-11-20 10:00~11:30, 13:00~15:00에 예약되어 있어 같은 시간대 빈 호실 조회에서 제외됩니다.
MERGE INTO reservation (id, room_id, reservation_date, start_time, end_time, reserver_name, purpose, created_at, updated_at) KEY (id) VALUES
    (1, 1, '2026-11-20', '10:00:00', '12:00:00', '학생1', '자바 스터디', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, '2026-11-20', '14:00:00', '16:00:00', '학생2', '팀 프로젝트 회의', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1, '2026-11-21', '09:00:00', '11:00:00', '학생3', '알고리즘 문제 풀이', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 2, '2026-11-20', '10:00:00', '11:30:00', '학생4', '동아리 활동', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 2, '2026-11-20', '13:00:00', '15:00:00', '학생5', '발표 연습', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 3, '2026-11-20', '09:00:00', '12:00:00', '학생6', '데이터베이스 실습', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, 3, '2026-11-23', '13:00:00', '17:00:00', '학생7', '해커톤 준비', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (8, 4, '2026-11-24', '15:00:00', '16:30:00', '학생8', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE reservation ALTER COLUMN id RESTART WITH 9;

-- ===== C. 분실물 게시판 (lost_item, claim) =====
-- 다른 테스트가 @Sql로 이 파일을 다시 실행해도 중복되지 않도록 MERGE INTO ... KEY(id)를 사용합니다.
-- id를 직접 넣었으므로 RESTART로 자동 증가 번호를 맞춰 API 등록 시 id가 충돌하지 않게 합니다.
-- 반환 완료(RETURNED) 습득물(1, 5): 수정·새 신청 시 409를 확인할 수 있습니다.
-- 신청이 없는 습득물(4, 6, 7): 신청 수 0을 확인할 수 있습니다.
MERGE INTO lost_item (id, title, description, category, found_place, found_date, status, created_at, updated_at) KEY (id) VALUES
    (1, '검은색 반지갑', '학생증과 교통카드가 들어 있음', 'WALLET', '본관 2층 복도', '2026-11-02', 'RETURNED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, '무선 이어폰 케이스', '흰색 케이스, 왼쪽 이어폰만 들어 있음', 'ELECTRONICS', '체육관', '2026-11-03', 'STORED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, '남색 후드티', '학교 로고가 있는 L 사이즈', 'CLOTHING', '운동장 벤치', '2026-11-04', 'STORED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, '파란색 필통', '샤프와 지우개가 들어 있음', 'STATIONERY', '본관 1층 로비', '2026-11-05', 'STORED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, '보조배터리', '10000mAh, 회색', 'ELECTRONICS', '기숙사 휴게실', '2026-11-06', 'RETURNED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, '검은색 장우산', '손잡이에 흰 테이프가 감겨 있음', 'ETC', '급식실 입구', '2026-11-07', 'STORED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (7, '갈색 카드지갑', '카드 1장이 들어 있음', 'WALLET', '본관 3층 실습실', '2026-11-09', 'STORED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE lost_item ALTER COLUMN id RESTART WITH 8;

-- 승인(APPROVED)·거절(REJECTED)된 신청: 다시 처리할 때 409를 확인할 수 있습니다.
MERGE INTO claim (id, lost_item_id, claimer_name, description, status, created_at, updated_at) KEY (id) VALUES
    (1, 1, '학생1', '학생증 이름이 학생1입니다', 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, '학생2', '검은색 지갑을 잃어버렸습니다', 'REJECTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 5, '학생3', '옆면에 스티커가 붙어 있습니다', 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 2, '학생4', '케이스 뒷면에 이니셜이 적혀 있습니다', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 2, '학생5', '흰색 이어폰을 체육 시간에 잃어버렸습니다', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 3, '학생6', '오른쪽 소매에 얼룩이 있습니다', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE claim ALTER COLUMN id RESTART WITH 7;
