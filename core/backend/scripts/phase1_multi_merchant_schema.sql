-- Phase 1 multi-merchant schema migration for campus delivery platform

CREATE TABLE campus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    address VARCHAR(255) DEFAULT NULL,
    service_phone VARCHAR(20) DEFAULT NULL,
    status INT NOT NULL DEFAULT 1,
    delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estimated_delivery_minutes INT NOT NULL DEFAULT 30,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT DEFAULT NULL,
    update_user BIGINT DEFAULT NULL,
    UNIQUE KEY uk_campus_code (code)
);

CREATE TABLE merchant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    campus_id BIGINT NOT NULL,
    merchant_code VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    logo VARCHAR(255) DEFAULT NULL,
    cover_image VARCHAR(255) DEFAULT NULL,
    description VARCHAR(255) DEFAULT NULL,
    announcement VARCHAR(255) DEFAULT NULL,
    contact_name VARCHAR(32) DEFAULT NULL,
    contact_phone VARCHAR(20) DEFAULT NULL,
    address_detail VARCHAR(255) DEFAULT NULL,
    sort INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    business_status INT NOT NULL DEFAULT 1,
    business_begin_time TIME DEFAULT NULL,
    business_end_time TIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user BIGINT DEFAULT NULL,
    update_user BIGINT DEFAULT NULL,
    UNIQUE KEY uk_merchant_code (merchant_code),
    KEY idx_merchant_campus_status_sort (campus_id, status, sort)
);

ALTER TABLE employee
    ADD COLUMN merchant_id BIGINT NULL AFTER id,
    ADD COLUMN account_type INT NOT NULL DEFAULT 1 AFTER merchant_id;

ALTER TABLE employee
    ADD KEY idx_employee_merchant_type_status (merchant_id, account_type, status);

ALTER TABLE category
    DROP INDEX idx_category_name;

ALTER TABLE category
    ADD COLUMN merchant_id BIGINT NOT NULL DEFAULT 1 AFTER id,
    ADD KEY idx_category_merchant_type_sort (merchant_id, type, status, sort);

ALTER TABLE dish
    DROP INDEX idx_dish_name;

ALTER TABLE dish
    ADD COLUMN merchant_id BIGINT NOT NULL DEFAULT 1 AFTER id,
    ADD KEY idx_dish_merchant_category_status (merchant_id, category_id, status),
    ADD KEY idx_dish_merchant_name (merchant_id, name);

ALTER TABLE setmeal
    DROP INDEX idx_setmeal_name;

ALTER TABLE setmeal
    ADD COLUMN merchant_id BIGINT NOT NULL DEFAULT 1 AFTER id,
    ADD KEY idx_setmeal_merchant_category_status (merchant_id, category_id, status),
    ADD KEY idx_setmeal_merchant_name (merchant_id, name);

ALTER TABLE shopping_cart
    ADD COLUMN merchant_id BIGINT NOT NULL DEFAULT 1 AFTER id,
    ADD KEY idx_cart_user_merchant_time (user_id, merchant_id, create_time),
    ADD KEY idx_cart_user_merchant_dish (user_id, merchant_id, dish_id),
    ADD KEY idx_cart_user_merchant_setmeal (user_id, merchant_id, setmeal_id);

ALTER TABLE orders
    ADD COLUMN campus_id BIGINT NOT NULL DEFAULT 1 AFTER id,
    ADD COLUMN merchant_id BIGINT NOT NULL DEFAULT 1 AFTER campus_id,
    ADD COLUMN merchant_name VARCHAR(64) DEFAULT NULL AFTER merchant_id,
    ADD COLUMN goods_amount DECIMAL(10,2) DEFAULT NULL AFTER amount,
    ADD COLUMN delivery_fee DECIMAL(10,2) DEFAULT NULL AFTER goods_amount,
    ADD COLUMN item_count INT DEFAULT NULL AFTER pack_amount,
    MODIFY COLUMN pack_amount DECIMAL(10,2) DEFAULT NULL,
    ADD KEY idx_orders_user_time (user_id, order_time),
    ADD KEY idx_orders_merchant_status_time (merchant_id, status, order_time),
    ADD KEY idx_orders_merchant_pay_time (merchant_id, pay_status, order_time);

ALTER TABLE order_detail
    ADD KEY idx_order_detail_order (order_id);

ALTER TABLE dish_flavor
    ADD KEY idx_dish_flavor_dish (dish_id);

ALTER TABLE setmeal_dish
    ADD KEY idx_setmeal_dish_setmeal (setmeal_id),
    ADD KEY idx_setmeal_dish_dish (dish_id);

ALTER TABLE address_book
    ADD KEY idx_address_user_default (user_id, is_default);

INSERT INTO campus (
    id, code, name, address, service_phone, status, delivery_fee,
    estimated_delivery_minutes, create_time, update_time, create_user, update_user
) VALUES (
    1, 'campus-001', 'Campus Delivery', 'Default Campus Address', '4008001234', 1, 3.00,
    30, NOW(), NOW(), NULL, NULL
);

INSERT INTO merchant (
    id, campus_id, merchant_code, name, description, announcement, contact_name,
    contact_phone, address_detail, sort, status, business_status, create_time, update_time
) VALUES (
    1, 1, 'merchant-001', 'Default Merchant', 'Migrated single-store merchant',
    'Migrated from single-store mode', 'Platform Admin', '4008001234',
    'Default Merchant Address', 0, 1, 1, NOW(), NOW()
);

UPDATE category SET merchant_id = 1;
UPDATE dish SET merchant_id = 1;
UPDATE setmeal SET merchant_id = 1;
UPDATE shopping_cart SET merchant_id = 1;
UPDATE orders
SET campus_id = 1,
    merchant_id = 1,
    merchant_name = 'Default Merchant',
    item_count = CAST(pack_amount AS SIGNED),
    pack_amount = 0.00;
UPDATE employee
SET merchant_id = NULL,
    account_type = 1;
