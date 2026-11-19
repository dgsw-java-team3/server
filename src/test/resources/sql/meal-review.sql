DELETE FROM meal_reviews;
DELETE FROM meals;

INSERT INTO meals (id, meal_date, meal_type, menu, created_at, updated_at) VALUES
    (101, '2030-01-01', 'LUNCH', '기간 전', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (102, '2030-01-02', 'LUNCH', '시작일', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (103, '2030-01-03', 'LUNCH', '동점 급식', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (104, '2030-01-04', 'LUNCH', '종료일', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (105, '2030-01-03', 'DINNER', '리뷰 없는 급식', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (106, '2030-01-05', 'LUNCH', '기간 후', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO meal_reviews (id, meal_id, rating, comment, nickname, created_at, updated_at) VALUES
    (201, 101, 5, '기간 전 리뷰', '학생1', '2030-01-10 10:00:00', '2030-01-10 10:00:00'),
    (202, 102, 5, '먼저 쓴 리뷰', '학생2', '2030-01-10 10:00:00', '2030-01-10 10:00:00'),
    (203, 102, 3, '나중에 쓴 리뷰', '학생3', '2030-01-10 11:00:00', '2030-01-10 11:00:00'),
    (204, 103, 4, '동점 리뷰', '학생4', '2030-01-10 10:00:00', '2030-01-10 10:00:00'),
    (205, 104, 5, '종료일 리뷰', '학생5', '2030-01-10 10:00:00', '2030-01-10 10:00:00'),
    (206, 106, 5, '기간 후 리뷰', '학생6', '2030-01-10 10:00:00', '2030-01-10 10:00:00');

ALTER TABLE meal_reviews ALTER COLUMN id RESTART WITH 207;
