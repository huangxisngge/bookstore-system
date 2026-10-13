-- ============================================
-- 网上书店测试数据初始化脚本
-- 适用于 bookstore 数据库
-- ============================================

USE `bookstore`;

-- ============================================
-- 1. 清空原有数据
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `return_record`;
TRUNCATE TABLE `order_item`;
TRUNCATE TABLE `order`;
TRUNCATE TABLE `cart`;
TRUNCATE TABLE `book`;
TRUNCATE TABLE `category`;
TRUNCATE TABLE `user`;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 2. 插入用户数据 (1个管理员 + 10个测试用户)
--    密码均为 123456 (BCrypt加密)
--    role: 0-普通用户 1-管理员
--    member_level: 1-普通 2-银卡 3-金卡
--    member_discount: 普通1.00 银卡0.95 金卡0.90
-- ============================================
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `member_level`, `member_discount`, `status`) VALUES
('admin',     '$2b$10$ulr94WreQbeiaey9L09Fy.wMolaQEoRMtrkhfjZuZ3VPj2vhvo4BC', '系统管理员', NULL, NULL, 1, 3, 1.00, 1),
('zhangwei',  '$2b$10$ulr94WreQbeiaey9L09Fy.wMolaQEoRMtrkhfjZuZ3VPj2vhvo4BC', '张伟',   '13800000001', 'zhangwei@email.com',  0, 3, 0.90, 1),
('liming',    '$2b$10$ulr94WreQbeiaey9L09Fy.wMolaQEoRMtrkhfjZuZ3VPj2vhvo4BC', '李明',   '13800000002', 'liming@email.com',    0, 2, 0.95, 1),
('wangfang',  '$2b$10$ulr94WreQbeiaey9L09Fy.wMolaQEoRMtrkhfjZuZ3VPj2vhvo4BC', '王芳',   '13800000003', 'wangfang@email.com',  0, 3, 0.90, 1),
('liuyang',   '$2b$10$ulr94WreQbeiaey9L09Fy.wMolaQEoRMtrkhfjZuZ3VPj2vhvo4BC', '刘洋',   '13800000004', 'liuyang@email.com',   0, 1, 1.00, 1),
('chenjing',  '$2b$10$ulr94WreQbeiaey9L09Fy.wMolaQEoRMtrkhfjZuZ3VPj2vhvo4BC', '陈静',   '13800000005', 'chenjing@email.com',  0, 2, 0.95, 1),
('huanglei',  '$2b$10$ulr94WreQbeiaey9L09Fy.wMolaQEoRMtrkhfjZuZ3VPj2vhvo4BC', '黄磊',   '13800000006', 'huanglei@email.com',  0, 1, 1.00, 1),
('zhaoming',  '$2b$10$ulr94WreQbeiaey9L09Fy.wMolaQEoRMtrkhfjZuZ3VPj2vhvo4BC', '赵明',   '13800000007', 'zhaoming@email.com',  0, 3, 0.90, 1),
('sunli',     '$2b$10$ulr94WreQbeiaey9L09Fy.wMolaQEoRMtrkhfjZuZ3VPj2vhvo4BC', '孙丽',   '13800000008', 'sunli@email.com',     0, 2, 0.95, 1),
('zhoujie',   '$2b$10$ulr94WreQbeiaey9L09Fy.wMolaQEoRMtrkhfjZuZ3VPj2vhvo4BC', '周杰',   '13800000009', 'zhoujie@email.com',   0, 1, 1.00, 1),
('wuqian',    '$2b$10$ulr94WreQbeiaey9L09Fy.wMolaQEoRMtrkhfjZuZ3VPj2vhvo4BC', '吴倩',   '13800000010', 'wuqian@email.com',    0, 3, 0.90, 1);

-- ============================================
-- 3. 插入图书分类
-- ============================================
INSERT INTO `category` (`name`, `description`) VALUES
('文学',     '小说、散文、诗歌等文学作品'),
('计算机',   '编程、计算机科学相关书籍'),
('历史',     '历史传记、考古研究等'),
('哲学',     '哲学思想、逻辑学等'),
('经济管理', '经济、金融、管理类书籍');

-- ============================================
-- 4. 插入30本图书
--    status: 0-下架 1-上架
-- ============================================
INSERT INTO `book` (`title`, `author`, `isbn`, `publisher`, `category_id`, `price`, `discount`, `promo_price`, `promo_start_time`, `promo_end_time`, `stock`, `sales`, `description`, `status`) VALUES
-- 文学类 (category_id=1)
('三体',               '刘慈欣',       '9787536692930', '重庆出版社',     1, 68.00,  0.80, 55.00,  '2026-07-01 00:00:00', '2026-08-01 00:00:00', 100, 85,  '中国科幻巅峰之作',                 1),
('三体II：黑暗森林',    '刘慈欣',       '9787536693820', '重庆出版社',     1, 72.00,  0.80, 58.00,  '2026-07-01 00:00:00', '2026-08-01 00:00:00',  90, 72,  '三体系列第二部',                   1),
('三体III：死神永生',   '刘慈欣',       '9787536694700', '重庆出版社',     1, 78.00,  0.80, NULL,   NULL, NULL,  85, 68,  '三体系列大结局',                   1),
('活着',               '余华',         '9787506365437', '作家出版社',     1, 45.00,  0.75, 35.00,  '2026-07-10 00:00:00', '2026-07-31 00:00:00', 120, 156, '余华代表作',                       1),
('许三观卖血记',        '余华',         '9787506365673', '作家出版社',     1, 42.00,  0.75, NULL,   NULL, NULL, 110, 98,  '余华经典作品',                     1),
('百年孤独',           '马尔克斯',      '9787544253994', '南海出版社',     1, 88.00,  0.80, NULL,   NULL, NULL,  60, 45,  '魔幻现实主义经典',                 1),
('霍乱时期的爱情',      '马尔克斯',      '9787544258203', '南海出版社',     1, 85.00,  0.80, NULL,   NULL, NULL,  55, 38,  '爱情小说巅峰',                     1),
('围城',               '钱钟书',       '9787020024759', '人民文学出版社', 1, 45.00,  0.70, NULL,   NULL, NULL,  90, 78,  '中国现代文学经典',                 1),
('边城',               '沈从文',       '9787020042524', '人民文学出版社', 1, 38.00,  0.70, NULL,   NULL, NULL,  85, 65,  '沈从文代表作',                     1),
('西游记',             '吴承恩',       '9787020002207', '人民文学出版社', 1, 88.00,  0.75, NULL,   NULL, NULL,  50, 42,  '中国古典四大名著',                 1),
('红楼梦',             '曹雪芹',       '9787020002208', '人民文学出版社', 1, 98.00,  0.75, NULL,   NULL, NULL,  45, 55,  '中国古典文学巅峰',                 1),
('天龙八部',           '金庸',         '9787538718379', '广州出版社',     1, 108.00, 0.70, NULL,   NULL, NULL,  60, 48,  '金庸武侠经典',                     1),
('射雕英雄传',         '金庸',         '9787538718386', '广州出版社',     1, 98.00,  0.70, NULL,   NULL, NULL,  55, 52,  '金庸代表作',                       1),
-- 计算机类 (category_id=2)
('Java核心技术',        'Cay S. Horstmann', '9787111561279', '机械工业出版社', 2, 168.00, 0.85, 140.00, '2026-07-01 00:00:00', '2026-08-15 00:00:00', 50, 42,  'Java程序员必读',                   1),
('Spring实战',          'Craig Walls',     '9787115417305', '人民邮电出版社', 2, 128.00, 0.80, NULL,   NULL, NULL, 45, 56,  'Spring框架权威指南',               1),
('MySQL必知必会',       'Ben Forta',       '9787115275967', '人民邮电出版社', 2, 69.00,  0.70, 49.00,  '2026-07-05 00:00:00', '2026-07-25 00:00:00', 80, 120, 'MySQL入门经典',                   1),
('算法导论',            'Thomas H. Cormen','9787111407010', '机械工业出版社', 2, 198.00, 0.85, NULL,   NULL, NULL, 30, 25,  '算法领域圣经',                     1),
('数据结构与算法分析',   'Mark Allen Weiss','9787111528302', '机械工业出版社', 2, 128.00, 0.80, NULL,   NULL, NULL, 40, 32,  '数据结构经典教材',                 1),
('深入理解Java虚拟机',   '周志明',         '9787111641247', '机械工业出版社', 2, 128.00, 0.85, 108.00, '2026-07-15 00:00:00', '2026-08-15 00:00:00', 30, 25,  'JVM必读书',                       1),
('设计模式',            'Erich Gamma',     '9787111211266', '机械工业出版社', 2, 88.00,  0.80, NULL,   NULL, NULL, 40, 35,  '设计模式经典',                     1),
-- 历史类 (category_id=3)
('明朝那些事',         '当年明月',       '9787213041440', '浙江人民出版社', 3, 120.00, 0.75, NULL,   NULL, NULL, 70, 88,  '通俗历史读物',                     1),
('万历十五年',         '黄仁宇',         '9787108009820', '中华书局',       3, 48.00,  0.80, NULL,   NULL, NULL, 60, 52,  '历史学经典',                       1),
('中国通史',           '吕思勉',         '9787108009821', '中华书局',       3, 88.00,  0.75, NULL,   NULL, NULL, 45, 30,  '中国历史入门',                     1),
('人类简史',           '尤瓦尔·赫拉利',  '9787508647357', '中信出版社',     3, 108.00, 0.75, 88.00,  '2026-07-01 00:00:00', '2026-08-01 00:00:00', 50, 42,  '全球畅销历史书',                   1),
('未来简史',           '尤瓦尔·赫拉利',  '9787508682211', '中信出版社',     3, 98.00,  0.75, NULL,   NULL, NULL, 45, 38,  '未来趋势预测',                     1),
-- 哲学类 (category_id=4)
('苏菲的世界',         '乔斯坦·贾德',    '9787506394864', '作家出版社',     4, 56.00,  0.70, NULL,   NULL, NULL, 50, 40,  '哲学启蒙书',                       1),
('中国哲学简史',       '冯友兰',         '9787301256120', '北京大学出版社', 4, 68.00,  0.75, NULL,   NULL, NULL, 55, 35,  '中国哲学入门',                     1),
('西方哲学史',         '罗素',           '9787100004572', '商务印书馆',     4, 128.00, 0.80, NULL,   NULL, NULL, 40, 28,  '西方哲学经典',                     1),
-- 经济管理类 (category_id=5)
('经济学原理',         '曼昆',           '9787301256580', '北京大学出版社', 5, 148.00, 0.80, 125.00, '2026-07-01 00:00:00', '2026-08-01 00:00:00', 35, 30,  '经济学入门经典',                   1),
('国富论',             '亚当·斯密',      '9787100023887', '商务印书馆',     5, 98.00,  0.75, NULL,   NULL, NULL, 40, 22,  '经济学奠基之作',                   1);

-- ============================================
-- 4.1 新书开抢倒计时演示数据
--    给个别图书设置"未来开抢时间"：开抢前展示倒计时且不可购买，
--    到点自动开抢、恢复可购，销量计入畅销榜。
-- ============================================
UPDATE `book` SET `grab_start_time` = DATE_ADD(NOW(), INTERVAL 20 MINUTE)
WHERE `title` IN ('三体III：死神永生', '算法导论');

-- ============================================
-- 5. 生成50个订单及订单明细 (存储过程)
--    订单号格式: ORD20260721xxxx
--    status: 0-待支付 1-已支付 2-已发货 3-已完成 4-已退货
-- ============================================
DELIMITER $$

DROP PROCEDURE IF EXISTS generate_orders$$

CREATE PROCEDURE generate_orders(IN order_count INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE user_id_val BIGINT;
    DECLARE order_id_val BIGINT;
    DECLARE item_count INT;
    DECLARE j INT;
    DECLARE book_id_val BIGINT;
    DECLARE quantity_val INT;
    DECLARE price_val DECIMAL(10,2);
    DECLARE discount_val DECIMAL(3,2);
    DECLARE final_price DECIMAL(10,2);
    DECLARE total DECIMAL(10,2);
    DECLARE order_date DATETIME;
    DECLARE order_status TINYINT;
    DECLARE order_no_val VARCHAR(50);
    DECLARE book_title_val VARCHAR(200);
    DECLARE day_offset INT;
    DECLARE rand_idx INT;

    WHILE i < order_count DO
        -- 随机选一个用户(2~11，跳过admin)
        SET user_id_val = 2 + FLOOR(RAND() * 10);

        -- 随机生成1~30天前的订单日期
        SET day_offset = FLOOR(RAND() * 30);
        SET order_date = DATE_SUB(NOW(), INTERVAL day_offset DAY);

        -- 随机订单状态(主要生成已完成的，少量其他状态)
        SET order_status = CASE
            WHEN RAND() < 0.70 THEN 3   -- 70% 已完成
            WHEN RAND() < 0.50 THEN 2   -- 15% 已发货
            WHEN RAND() < 0.50 THEN 1   -- 10% 已支付
            ELSE 0                       -- 5%  待支付
        END;

        -- 生成唯一订单号
        SET order_no_val = CONCAT('ORD', DATE_FORMAT(order_date, '%Y%m%d%H%i%s'),
                                  LPAD(FLOOR(RAND() * 10000), 4, '0'));

        -- 随机收件地址 (6项, index 0~5)
        SET rand_idx = FLOOR(RAND() * 6);
        SET @addr_list = '北京市朝阳区建国路88号,上海市浦东新区陆家嘴路100号,广州市天河区体育西路200号,深圳市南山区科技园路300号,杭州市西湖区文三路150号,成都市武侯区人民南路4段1号';
        SET @rand_addr = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(@addr_list, ',', rand_idx + 1), ',', -1));

        -- 随机姓名 (10项, index 0~9)
        SET rand_idx = FLOOR(RAND() * 10);
        SET @name_list = '张伟,李明,王芳,刘洋,陈静,黄磊,赵明,孙丽,周杰,吴倩';
        SET @rand_name = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(@name_list, ',', rand_idx + 1), ',', -1));

        -- 随机手机号 (10项, index 0~9)
        SET rand_idx = FLOOR(RAND() * 10);
        SET @phone_list = '13800000001,13800000002,13800000003,13800000004,13800000005,13800000006,13800000007,13800000008,13800000009,13800000010';
        SET @rand_phone = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(@phone_list, ',', rand_idx + 1), ',', -1));

        -- 插入订单
        INSERT INTO `order` (`order_no`, `user_id`, `total_amount`, `discount_amount`, `pay_amount`,
                             `member_discount`, `full_reduction_amount`, `status`, `address`,
                             `receiver_name`, `receiver_phone`, `remark`,
                             `pay_time`, `ship_time`, `receive_time`, `create_time`)
        VALUES (order_no_val, user_id_val, 0, 0, 0, 1.00, 0, order_status,
                @rand_addr, @rand_name, @rand_phone,
                IF(RAND() < 0.3, '请尽快发货', NULL),
                IF(order_status >= 1, DATE_ADD(order_date, INTERVAL 10 MINUTE), NULL),
                IF(order_status >= 2, DATE_ADD(order_date, INTERVAL 1 HOUR), NULL),
                IF(order_status >= 3, DATE_ADD(order_date, INTERVAL 2 HOUR), NULL),
                order_date);

        SET order_id_val = LAST_INSERT_ID();
        SET total = 0;
        SET item_count = 1 + FLOOR(RAND() * 4); -- 每个订单1~4本书

        SET j = 0;
        WHILE j < item_count DO
            -- 随机选一本上架的书
            SELECT b.id, b.price, b.discount, b.title
            INTO book_id_val, price_val, discount_val, book_title_val
            FROM book b WHERE b.status = 1 AND b.deleted = 0
            ORDER BY RAND() LIMIT 1;

            SET quantity_val = 1 + FLOOR(RAND() * 2); -- 每本1~2本
            SET final_price = price_val * discount_val * quantity_val;
            SET total = total + final_price;

            -- 插入订单明细(快照)
            INSERT INTO `order_item` (`order_id`, `book_id`, `book_title`, `price`, `discount`, `quantity`, `subtotal`)
            VALUES (order_id_val, book_id_val, book_title_val, price_val, discount_val, quantity_val, final_price);

            -- 更新图书销量
            UPDATE `book` SET `sales` = `sales` + quantity_val WHERE `id` = book_id_val;

            SET j = j + 1;
        END WHILE;

        -- 更新订单总金额和实付金额
        UPDATE `order`
        SET `total_amount` = total,
            `discount_amount` = GREATEST(total - total, 0),
            `pay_amount` = total
        WHERE `id` = order_id_val;

        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

-- 执行存储过程生成50个订单
CALL generate_orders(50);

-- 清理存储过程
DROP PROCEDURE IF EXISTS generate_orders;

-- ============================================
-- 6. 验证数据
-- ============================================
SELECT '用户数' AS item, COUNT(*) AS cnt FROM `user`
UNION ALL
SELECT '图书数', COUNT(*) FROM `book`
UNION ALL
SELECT '分类数', COUNT(*) FROM `category`
UNION ALL
SELECT '订单数', COUNT(*) FROM `order`
UNION ALL
SELECT '订单明细数', COUNT(*) FROM `order_item`;
