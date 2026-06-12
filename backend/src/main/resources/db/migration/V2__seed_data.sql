-- ============================================================
-- V2__seed_data.sql
-- 初始数据：管理员账号 + 示例分类 + 敏感词词库
-- ============================================================

-- 管理员账号 (学号: admin, 密码: admin123)
-- BCrypt加密: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iDu6Z.Ee
INSERT INTO users (student_id, username, password_hash, email, role, status, reputation_score)
VALUES ('admin', '系统管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iDu6Z.Ee', 'admin@campus.example.com', 'ADMIN', 'ACTIVE', 100);

-- 示例商品分类（树形结构）
INSERT INTO categories (id, name, icon, parent_id) VALUES
(1, '教材教辅', 'book', NULL),
(2, '电子数码', 'device', NULL),
(3, '生活用品', 'living', NULL),
(4, '服饰美妆', 'clothes', NULL),
(5, '运动户外', 'sport', NULL),
(6, '其他', 'other', NULL),
(7, '计算机类', 'code', 1),
(8, '外语类', 'language', 1),
(9, '数学类', 'math', 1),
(10, '手机平板', 'mobile', 2),
(11, '电脑配件', 'pc', 2);

-- 敏感词词库
INSERT INTO sensitive_words (word) VALUES
('违禁品'), ('枪支'), ('弹药'), ('毒品'), ('赌博'),
('色情'), ('裸聊'), ('诈骗'), ('传销'), ('刷单');
