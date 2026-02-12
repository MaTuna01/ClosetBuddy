-- 테스트용 카테고리 데이터 (H2 호환)
-- MERGE INTO를 사용하여 중복 삽입 방지

-- 루트 카테고리
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (1, '상의', 'TOP', NULL);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (2, '하의', 'PANTS', NULL);

-- 상의 하위 카테고리
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (3, '티셔츠', 'TSHIRT', 1);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (4, '셔츠', 'SHIRT', 1);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (5, '맨투맨', 'SWEATSHIRT', 1);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (6, '후드티', 'HOODIE', 1);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (7, '니트', 'KNIT', 1);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (8, '가디건', 'CARDIGAN', 1);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (9, '베스트', 'VEST', 1);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (10, '폴로셔츠', 'POLO', 1);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (11, '블라우스', 'BLOUSE', 1);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (12, '롱슬리브', 'LONGSLEEVE', 1);

-- 하의 하위 카테고리
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (13, '슬랙스', 'SLACKS', 2);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (14, '청바지', 'JEANS', 2);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (15, '면바지', 'COTTON_PANTS', 2);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (16, '조거팬츠', 'JOGGERS', 2);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (17, '카고팬츠', 'CARGO', 2);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (18, '치노팬츠', 'CHINOS', 2);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (19, '반바지', 'SHORTS', 2);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (20, '레깅스', 'LEGGINGS', 2);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (21, '스커트', 'SKIRT', 2);
MERGE INTO category (category_id, name, code, parent_id) KEY (category_id) VALUES (22, '트레이닝팬츠', 'TRAINING', 2);