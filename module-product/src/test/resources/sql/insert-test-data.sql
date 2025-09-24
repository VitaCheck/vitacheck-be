-- 브랜드
INSERT INTO brands (id, name, image_url, created_at, updated_at)
VALUES (1, '뉴트리브랜드', 'https://example.com/brand1.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 성분
INSERT INTO ingredients (id, name, description, caution, effect, unit, created_at, updated_at)
VALUES (1, '비타민A', '시력 보호에 도움', '과다 복용 시 간 손상', '눈 건강 개선', 'mg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ingredients (id, name, description, caution, effect, unit, created_at, updated_at)
VALUES (2, '비타민C', '면역력 강화에 도움', '과다 복용 시 위장 장애', '피로 회복, 면역력 강화', 'mg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 성분 권장량 (IngredientDosage)
INSERT INTO ingredient_dosages (id, ingredient_id, gender, min_age, max_age, recommended_dosage, upper_limit, created_at, updated_at)
VALUES (1, 1, 'FEMALE', 20, 39, 500, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ingredient_dosages (id, ingredient_id, gender, min_age, max_age, recommended_dosage, upper_limit, created_at, updated_at)
VALUES (2, 2, 'MALE', 20, 39, 100, 2000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 목적
INSERT INTO purposes (id, name)
VALUES (1, '시력 보호');

INSERT INTO purposes (id, name)
VALUES (2, '면역력 강화');

-- 목적-성분 (PurposeIngredient)
INSERT INTO purpose_ingredients (id, purpose_id, ingredient_id)
VALUES (1, 1, 1); -- 시력 보호 목적에는 비타민A

INSERT INTO purpose_ingredients (id, purpose_id, ingredient_id)
VALUES (2, 2, 2); -- 면역력 강화 목적에는 비타민C

-- 영양제
INSERT INTO supplements (id, brand_id, name, coupang_url, image_url, price, description, method, caution, created_at, updated_at)
VALUES (1, 1, '루테인 비타민A', 'https://example.com/product1', 'https://example.com/product1.png', 15000,
        '시력 보호용 비타민A 보충제', '하루 1정', '임산부 복용 주의', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO supplements (id, brand_id, name, coupang_url, image_url, price, description, method, caution, created_at, updated_at)
VALUES (2, 1, '비타민C 1000mg', 'https://example.com/product2', 'https://example.com/product2.png', 12000,
        '면역력 강화를 위한 비타민C 보충제', '하루 1~2정', '과다 복용 시 위장 장애', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 영양제-성분 (SupplementIngredient)
INSERT INTO supplement_ingredients (id, supplement_id, ingredient_id, amount, created_at, updated_at)
VALUES (1, 1, 1, 500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO supplement_ingredients (id, supplement_id, ingredient_id, amount, created_at, updated_at)
VALUES (2, 2, 2, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
