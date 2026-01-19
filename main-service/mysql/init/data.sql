CREATE DATABASE IF NOT EXISTS closetBuddy_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE closetBuddy_db;

CREATE TABLE IF NOT EXISTS `member` (
                                        `id`	BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                        member_name     VARCHAR(50)  NOT NULL,
    `member_id`	VARCHAR(255)	NULL,
    `password`	VARCHAR(255)	NULL,
    `member_email`	VARCHAR(255)	NULL,
    `member_address`	VARCHAR(255)	NULL,
    `phone_number`	VARCHAR(255)	NULL,
    `role`	Enum	('admin', 'member', 'guest') NOT NULL DEFAULT 'MEMBER',
    primary key (id),
    UNIQUE KEY uk_member_name (member_name),
    UNIQUE KEY uk_member_email (member_email),
    UNIQUE KEY uk_member_phone (phone_number)
    );

CREATE TABLE IF NOT EXISTS `seller` (
                                        seller_id  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                        member_id  BIGINT UNSIGNED NOT NULL,
                                        `seller_name`	VARCHAR(255)	NULL,
    PRIMARY KEY (seller_id),
    UNIQUE KEY uk_seller_member (member_id)
    );

CREATE TABLE IF NOT EXISTS `store` (
                                       store_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                       store_name  VARCHAR(100) NOT NULL,
    seller_id   BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (store_id),
    KEY idx_store_seller (seller_id),
    CONSTRAINT fk_store_seller
    FOREIGN KEY (seller_id) REFERENCES seller(seller_id)
    );

CREATE TABLE IF NOT EXISTS `product` (
                                         `product_id`	BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                         `product_name`	VARCHAR(200) NOT NULL,
    `product_price`	BIGINT NOT NULL,
    `product_stock`	INT NOT NULL,
    `store_id`	BIGINT UNSIGNED NOT NULL,
    `image_url`	VARCHAR(255)	NULL,
    `is_like`	Boolean	NULL,
    `category`	Enum ('상의','하의'),
    `Field`	VARCHAR(255)	NULL,
    PRIMARY KEY (product_id),
    KEY idx_product_store (store_id),
    CONSTRAINT fk_product_store
    FOREIGN KEY (store_id) REFERENCES store(store_id)
    );

CREATE TABLE IF NOT EXISTS `orders` (
                                        `order_id`	BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                        `created_at`	DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at`	DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `order_status`	ENUM('CREATED','PAID','CANCELED','COMPLETED') NOT NULL DEFAULT 'CREATED',
    order_amount  BIGINT NOT NULL,
    member_id     BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (order_id),
    KEY idx_orders_member_created (member_id, created_at)
    );

CREATE TABLE IF NOT EXISTS `order_item` (
                                            order_item_id  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                            product_id     BIGINT UNSIGNED NOT NULL, -- catalog_db.product.product_id (논리 참조)
                                            order_id       BIGINT UNSIGNED NOT NULL,
                                            order_count    INT NOT NULL,
                                            order_price    BIGINT NOT NULL,
                                            PRIMARY KEY (order_item_id),
    KEY idx_order_item_order (order_id),
    KEY idx_order_item_product (product_id),
    CONSTRAINT fk_order_item_order
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS `cart_item` (
                                           cart_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                           product_id   BIGINT UNSIGNED NOT NULL,
                                           member_id    BIGINT UNSIGNED NOT NULL,
                                           cart_stock   INT NOT NULL,
                                           PRIMARY KEY (cart_id),
    UNIQUE KEY uk_cart_member_product (member_id, product_id),
    KEY idx_cart_member (member_id),
    KEY idx_cart_product (product_id)
    );

CREATE TABLE IF NOT EXISTS `account` (
                                         account_id  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                         balance     BIGINT NOT NULL DEFAULT 0,
                                         member_id   BIGINT UNSIGNED NOT NULL,
                                         PRIMARY KEY (account_id),
    UNIQUE KEY uk_account_member (member_id)
    );

CREATE TABLE IF NOT EXISTS `account_history` (
                                                 account_history_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                 account_id         BIGINT UNSIGNED NOT NULL,
                                                 transaction_type ENUM('USE', 'CHARGE', 'REFUND', 'CANCEL') NOT NULL,
    account_amount     BIGINT NOT NULL,
    created_at       DATETIME NOT NULL,
    updated_at       DATETIME NOT NULL,
    balance_snapshot BIGINT NOT NULL,
    ref_id          BIGINT UNSIGNED NULL,
    PRIMARY KEY (account_history_id),
    KEY idx_history_account (account_id)
    );

CREATE TABLE IF NOT EXISTS `deposit_charge` (
                                                charge_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                member_id        BIGINT UNSIGNED NOT NULL,
                                                pg_payment_key   VARCHAR(255) NOT NULL,
    pg_order_id      VARCHAR(255) NOT NULL, -- PG사에 보낸 우리측 고유 ID
    charge_amount    BIGINT NOT NULL,       -- 충전 금액
    charge_status    ENUM('READY', 'DONE', 'CANCELED') NOT NULL DEFAULT 'READY',
    approved_at      DATETIME(6) NULL,      -- PG 승인 시간
    created_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (charge_id),
    UNIQUE KEY uk_pg_payment_key (pg_payment_key),
    KEY idx_charge_member (member_id)
    );

CREATE TABLE IF NOT EXISTS `payment` (
                                         payment_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                         order_id        BIGINT UNSIGNED NOT NULL,
                                         member_id       BIGINT UNSIGNED NOT NULL,
                                         payment_amount  BIGINT NOT NULL,
                                         payment_status  ENUM('PENDING', 'APPROVED', 'CANCELED') NOT NULL DEFAULT 'PENDING',
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    approved_at         DATETIME(6) DEFAULT NULL,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (payment_id),
    UNIQUE KEY uk_payment_order (order_id),
    KEY idx_payment_member (member_id)
    );


CREATE TABLE IF NOT EXISTS `settlement` (
                                            `settle_id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    -- [관계 정보]
                                            `store_id`           BIGINT UNSIGNED NOT NULL,
                                            `seller_id`          BIGINT UNSIGNED NOT NULL, -- 나중에 예치금 입금해줄 대상(Seller) 식별용

    -- [집계 정보]
                                            `total_sales_amount` BIGINT NOT NULL DEFAULT 0, -- 총 매출액
                                            `fee_amount`         BIGINT NOT NULL DEFAULT 0, -- 수수료 총액
                                            `payout_amount`      BIGINT NOT NULL DEFAULT 0, -- 최종 지급액 (매출 - 수수료)

    -- [상태 및 기준]
                                            `settle_status`      ENUM('SCHEDULED', 'CALCULATING', 'SETTLED', 'FAILED') NOT NULL DEFAULT 'SCHEDULED',
    `settlement_date`    DATE NOT NULL,        -- 정산 기준일

    `created_at`         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `completed_at`       DATETIME(6) NULL,     -- 예치금 지급 완료 시간

    PRIMARY KEY (`settle_id`),
    KEY `idx_settlement_store_date` (`store_id`, `settlement_date`),
    KEY `idx_settlement_status` (`settle_status`)
    );

CREATE TABLE IF NOT EXISTS `settlement_detail` (
                                                   `settle_detail_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                                   `settle_id`        BIGINT UNSIGNED NOT NULL, -- settlement 테이블의 settle_id 참조

    -- [원천 데이터 추적용]
                                                   `order_id`         BIGINT UNSIGNED NOT NULL,
                                                   `order_item_id`    BIGINT UNSIGNED NOT NULL,
                                                   `product_id`       BIGINT UNSIGNED NOT NULL,
                                                   `payment_id`       BIGINT UNSIGNED NOT NULL, -- 결제(예치금 사용) 내역 연결

    -- [스냅샷 데이터]
                                                   `product_name`     VARCHAR(255) NOT NULL,
    `product_price`    BIGINT NOT NULL,      -- 판매 당시 가격
    `quantity`         INT NOT NULL,         -- 판매 수량

-- [금액 계산]
    `total_amount`     BIGINT NOT NULL,      -- 상품별 총 매출 (price * quantity)
    `fee_rate`         DECIMAL(5, 2) NOT NULL, -- 적용된 수수료율
    `fee_amount`       BIGINT NOT NULL,      -- 수수료 금액
    `payout_amount`    BIGINT NOT NULL,      -- 지급 예정 금액

    `created_at`       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (`settle_detail_id`),
    KEY `idx_detail_settle_id` (`settle_id`),
    KEY `idx_detail_order_item` (`order_item_id`) -- 중복 정산 방지용
    );

