-- ============================================================
-- V3__sample_data.sql
-- 测试数据：普通用户 + 示例商品
-- ============================================================

-- 测试用户1 (学号: test001, 密码: test123)
-- BCrypt hash for 'test123'
INSERT INTO users (student_id, username, password_hash, email, role, status, reputation_score)
VALUES ('test001', '测试买家', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iDu6Z.Ee', 'test001@example.com', 'USER', 'ACTIVE', 100);

-- 测试用户2 (学号: test002, 密码: test123)
INSERT INTO users (student_id, username, password_hash, email, role, status, reputation_score)
VALUES ('test002', '测试卖家', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iDu6Z.Ee', 'test002@example.com', 'USER', 'ACTIVE', 100);

-- ============================================================
-- 拍卖商品（14件，起拍价+时长）
-- ============================================================
INSERT INTO products (seller_id, title, description, category_id, type, start_price, current_price, status, auction_start, auction_end, view_count) VALUES
(3, '高等数学（第七版）上册', '同济大学数学系编，九成新，少量笔记', 1, 'AUCTION', 15.00, 15.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 72 HOUR), 128),
(3, 'iPhone 14 128GB 午夜色', '国行正品，使用一年，无维修，屏幕无划痕，电池健康88%', 2, 'AUCTION', 2000.00, 2000.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 48 HOUR), 356),
(3, '罗技 G502 游戏鼠标', '使用半年，功能正常，包装盒还在，配重块齐全', 2, 'AUCTION', 80.00, 80.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 24 HOUR), 89),
(3, '机械键盘 IKBC C87  cherry轴', '青轴，87键，用了三个月，键帽无打油', 2, 'AUCTION', 120.00, 120.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 36 HOUR), 67),
(3, 'iPad Air 5 + Apple Pencil 二代', 'M1芯片，64G，屏幕完美，一直贴膜戴套使用', 2, 'AUCTION', 2500.00, 2500.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 48 HOUR), 210),
(3, '哑铃一对 可调节 10-20kg', '铸铁包胶，不伤地板，带连接杆可变杠铃', 5, 'AUCTION', 60.00, 60.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 36 HOUR), 45),
(3, '单反相机 Canon 800D 套机', '18-55mm套头，快门约8000次，附带相机包+32G卡', 2, 'AUCTION', 1800.00, 1800.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 72 HOUR), 178),
(3, 'AirPods Pro 二代 USB-C', '使用半年，耳塞齐全，降噪完美，续航正常', 2, 'AUCTION', 800.00, 800.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 24 HOUR), 134),
(3, '显示器 Dell 27寸 2K U2719D', 'IPS面板，色彩精准，接口齐全(DP+HDMI+USB Hub)', 2, 'AUCTION', 900.00, 900.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 60 HOUR), 95),
(3, '蓝牙音箱 JBL Flip 6', '防水防尘，续航12小时，音质出色，几乎全新', 2, 'AUCTION', 300.00, 300.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 36 HOUR), 76),
(3, 'Nintendo Switch OLED 白色', '日版，带塞尔达+动森卡带，使用一年，无ban机', 2, 'AUCTION', 1400.00, 1400.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 48 HOUR), 203),
(3, '羽毛球拍 YONEX 天斧99', '4U5，穿线BG80 26磅，一处轻微掉漆，手感极佳', 5, 'AUCTION', 350.00, 350.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 30 HOUR), 56),
(3, '滑板 专业长板 舞板', '长度110cm，枫木，轮子适合刷街，附赠工具包', 5, 'AUCTION', 150.00, 150.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 48 HOUR), 34),
(3, '移动硬盘 西部数据 4TB', 'USB 3.0，2.5寸便携，通电时间少于100小时', 2, 'AUCTION', 280.00, 280.00, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 24 HOUR), 61);

-- ============================================================
-- 一口价商品（16件）
-- ============================================================
INSERT INTO products (seller_id, title, description, category_id, type, fixed_price, status, view_count) VALUES
(3, '宿舍用小台灯 LED护眼', '充电款，三档亮度可调，配送充电线，触摸开关', 3, 'SALE', 29.90, 'ACTIVE', 45),
(3, '线性代数教材 同济版', '几乎全新，只用了一学期，少量铅笔笔记已擦除', 1, 'SALE', 10.00, 'ACTIVE', 67),
(3, '篮球 Nike 标准7号', '室外场地用的，七八成新，气密性好，附赠打气筒', 5, 'SALE', 50.00, 'ACTIVE', 23),
(3, '大学英语四级真题集 2025版', '只做了2套，其余全新，附赠听力光盘未拆封', 1, 'SALE', 15.00, 'ACTIVE', 88),
(3, '床上折叠桌 懒人电脑桌', '铝合金折叠腿，带杯托和iPad支架槽，承重15kg', 3, 'SALE', 35.00, 'ACTIVE', 112),
(3, '雅思词汇红宝书 俞敏洪', '全新未拆封，买重了故出', 1, 'SALE', 18.00, 'ACTIVE', 41),
(3, '宿舍小冰箱 20L 车载家用', '冷暖两用，可放6罐可乐，静音运行，功率仅45W', 3, 'SALE', 120.00, 'ACTIVE', 156),
(3, '考研数学全书 张宇36讲', '全书做了前5讲（共36讲），其余干净，赠网课笔记', 1, 'SALE', 25.00, 'ACTIVE', 73),
(3, '床上四件套 纯棉 1.2m', '100%纯棉，蓝灰条纹，洗过一次缩水后闲置', 3, 'SALE', 40.00, 'ACTIVE', 38),
(3, '路由器 Xiaomi AX3000 WiFi6', '支持Mesh组网，千兆端口，稳定不丢包', 2, 'SALE', 89.00, 'ACTIVE', 94),
(3, '三体全集 精装三册 刘慈欣', '地球往事+黑暗森林+死神永生，仅翻阅一遍，书脊完好', 1, 'SALE', 45.00, 'ACTIVE', 201),
(3, '吉他 雅马哈 F310 民谣', '41寸缺角，云杉面板，带琴包+调音器+教材', 5, 'SALE', 380.00, 'ACTIVE', 62),
(3, '香水 Dior Sauvage 60ml', '只用过5次，味道不太适合我，保真可验', 4, 'SALE', 280.00, 'ACTIVE', 47),
(3, '电煮锅 宿舍小功率 300W', '1.5L容量，不粘涂层，煮面煮粥火锅都行', 3, 'SALE', 35.00, 'ACTIVE', 83),
(3, 'PS4 Slim 500G + 手柄×2', '国行，带5款大作（战神/神海/血缘等），功能完好', 2, 'SALE', 650.00, 'ACTIVE', 109),
(3, '防晒霜 安耐晒 金瓶 60ml', '日本购入，全新未拆，保质期到2027年', 4, 'SALE', 90.00, 'ACTIVE', 55);

-- ============================================================
-- 商品图片（loremflickr 按关键词从 Flickr 匹配真实图片）
-- ============================================================
INSERT INTO product_images (product_id, image_url, sort_order) VALUES
-- 拍卖品
(1,  'https://loremflickr.com/400/400/textbook,mathematics', 0),
(2,  'https://loremflickr.com/400/400/iphone,smartphone', 0),
(3,  'https://loremflickr.com/400/400/computer-mouse,gaming', 0),
(4,  'https://loremflickr.com/400/400/mechanical-keyboard', 0),
(5,  'https://loremflickr.com/400/400/ipad,apple', 0),
(6,  'https://loremflickr.com/400/400/dumbbell,gym', 0),
(7,  'https://loremflickr.com/400/400/canon,camera', 0),
(8,  'https://loremflickr.com/400/400/airpods,earphone', 0),
(9,  'https://loremflickr.com/400/400/monitor,display', 0),
(10, 'https://loremflickr.com/400/400/bluetooth-speaker', 0),
(11, 'https://loremflickr.com/400/400/nintendo-switch', 0),
(12, 'https://loremflickr.com/400/400/badminton,racket', 0),
(13, 'https://loremflickr.com/400/400/skateboard,longboard', 0),
(14, 'https://loremflickr.com/400/400/hard-drive,storage', 0),
-- 一口价
(15, 'https://loremflickr.com/400/400/desk-lamp,led', 0),
(16, 'https://loremflickr.com/400/400/textbook,library', 0),
(17, 'https://loremflickr.com/400/400/basketball,sport', 0),
(18, 'https://loremflickr.com/400/400/exam,book', 0),
(19, 'https://loremflickr.com/400/400/laptop,desk', 0),
(20, 'https://loremflickr.com/400/400/vocabulary,dictionary', 0),
(21, 'https://loremflickr.com/400/400/mini-fridge', 0),
(22, 'https://loremflickr.com/400/400/math,textbook', 0),
(23, 'https://loremflickr.com/400/400/bedding,sheet', 0),
(24, 'https://loremflickr.com/400/400/wifi,router', 0),
(25, 'https://loremflickr.com/400/400/novel,books', 0),
(26, 'https://loremflickr.com/400/400/acoustic-guitar', 0),
(27, 'https://loremflickr.com/400/400/perfume,dior', 0),
(28, 'https://loremflickr.com/400/400/electric-cooker', 0),
(29, 'https://loremflickr.com/400/400/playstation,ps4', 0),
(30, 'https://loremflickr.com/400/400/sunscreen,skincare', 0);
