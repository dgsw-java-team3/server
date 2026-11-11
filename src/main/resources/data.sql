INSERT INTO meals (id, meal_date, meal_type, menu, created_at, updated_at) VALUES
    (1, '2026-11-09', 'LUNCH', '쌀밥, 미역국, 제육볶음, 김치', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, '2026-11-10', 'LUNCH', '스팸마요덮밥, 우동국물, 단무지, 사과', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, '2026-11-10', 'DINNER', '현미밥, 된장국, 닭갈비, 깍두기', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, '2026-11-11', 'LUNCH', '카레라이스, 유부국, 샐러드, 요구르트', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, '2026-11-09', 'BREAKFAST', '토스트, 스크램블에그, 우유', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE meals ALTER COLUMN id RESTART WITH 6;
