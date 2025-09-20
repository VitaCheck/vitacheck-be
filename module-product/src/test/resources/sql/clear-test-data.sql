-- 순서 중요: 자식 테이블부터 지워야 외래키 충돌이 없음

-- 영양제-성분
DELETE FROM supplement_ingredients;

-- 성분 권장량
DELETE FROM ingredient_dosages;

-- 목적-성분
DELETE FROM purpose_ingredients;

-- 영양제
DELETE FROM supplements;

-- 브랜드
DELETE FROM brands;

-- 목적
DELETE FROM purposes;

-- 성분
DELETE FROM ingredients;
