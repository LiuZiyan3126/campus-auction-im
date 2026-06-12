-- ============================================================
-- V1__init_schema.sql
-- 校园二手拍卖与即时通讯平台 (CAIP) 初始数据库Schema
-- 创建11张表及所有索引、约束
-- ============================================================

-- (1) users 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NULL,
    avatar VARCHAR(500) NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    reputation_score INT DEFAULT 100,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT uk_users_student_id UNIQUE (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_role ON users (role);

-- (2) categories 分类表
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(100) NULL,
    parent_id INT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_categories_parent ON categories (parent_id);

-- (3) products 商品表
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    category_id INT NULL,
    type VARCHAR(10) NOT NULL COMMENT 'SALE/AUCTION',
    start_price DECIMAL(10,2) NULL COMMENT '起拍价',
    fixed_price DECIMAL(10,2) NULL COMMENT '一口价',
    current_price DECIMAL(10,2) NULL COMMENT '当前最高价',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'ACTIVE/SOLD/EXPIRED/BANNED/PENDING',
    auction_start DATETIME NULL,
    auction_end DATETIME NULL,
    winner_id BIGINT NULL,
    view_count INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users(id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_products_winner FOREIGN KEY (winner_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_products_status_created ON products (status, created_at DESC);
CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_seller ON products (seller_id);
CREATE FULLTEXT INDEX ft_products_search ON products (title, description) WITH PARSER ngram;

-- (4) product_images 商品图片表
CREATE TABLE product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT DEFAULT 0,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_product_images_product ON product_images (product_id);

-- (5) product_favorites 收藏表
CREATE TABLE product_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_favorites_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uk_fav_user_product UNIQUE (user_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- (6) bids 出价记录表
CREATE TABLE bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    bid_amount DECIMAL(10,2) NOT NULL,
    bid_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bids_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_bids_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bids_product_time ON bids (product_id, bid_time DESC);

-- (7) conversations 会话表
CREATE TABLE conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user1_id BIGINT NOT NULL,
    user2_id BIGINT NOT NULL,
    product_id BIGINT NULL,
    last_message_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conv_user1 FOREIGN KEY (user1_id) REFERENCES users(id),
    CONSTRAINT fk_conv_user2 FOREIGN KEY (user2_id) REFERENCES users(id),
    CONSTRAINT fk_conv_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uk_conv_users_product UNIQUE (user1_id, user2_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- (8) messages 消息表
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    content_type VARCHAR(10) DEFAULT 'TEXT' COMMENT 'TEXT/IMAGE',
    is_read TINYINT(1) DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_conv FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_messages_conv_time ON messages (conversation_id, created_at);

-- (9) orders 订单表
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/SHIPPED/COMPLETED/CANCELLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at DATETIME NULL,
    shipped_at DATETIME NULL,
    completed_at DATETIME NULL,
    CONSTRAINT fk_orders_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
    CONSTRAINT fk_orders_seller FOREIGN KEY (seller_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_orders_buyer_status ON orders (buyer_id, status);
CREATE INDEX idx_orders_seller_status ON orders (seller_id, status);
CREATE INDEX idx_orders_product ON orders (product_id);

-- (10) reviews 评价表
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_reviews_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_target FOREIGN KEY (target_user_id) REFERENCES users(id),
    CONSTRAINT uk_reviews_order UNIQUE (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_reviews_target ON reviews (target_user_id);

-- (11) notifications 通知表
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL COMMENT 'AUCTION_WON/NEW_MESSAGE/ORDER_UPDATE/SYSTEM',
    title VARCHAR(100) NOT NULL,
    content VARCHAR(500) NULL,
    is_read TINYINT(1) DEFAULT 0,
    related_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notif_user_read ON notifications (user_id, is_read, created_at DESC);

-- (12) sensitive_words 敏感词表
CREATE TABLE sensitive_words (
    id INT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sw_word UNIQUE (word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
