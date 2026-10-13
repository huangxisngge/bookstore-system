CREATE DATABASE IF NOT EXISTS `bookstore` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `bookstore`;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` TINYINT DEFAULT 0 COMMENT '角色: 0-普通用户 1-管理员',
    `member_level` TINYINT DEFAULT 1 COMMENT '会员等级: 1-普通 2-银卡 3-金卡',
    `member_discount` DECIMAL(3,2) DEFAULT 1.00 COMMENT '会员折扣',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='用户表';

-- 图书分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='图书分类表';

-- 图书表
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '书名',
    `author` VARCHAR(100) DEFAULT NULL COMMENT '作者',
    `isbn` VARCHAR(20) DEFAULT NULL COMMENT 'ISBN',
    `publisher` VARCHAR(100) DEFAULT NULL COMMENT '出版社',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `price` DECIMAL(10,2) NOT NULL COMMENT '原价',
    `discount` DECIMAL(3,2) DEFAULT 1.00 COMMENT '固定折扣',
    `promo_start_time` DATETIME DEFAULT NULL COMMENT '促销开始时间',
    `promo_end_time` DATETIME DEFAULT NULL COMMENT '促销结束时间',
    `promo_price` DECIMAL(10,2) DEFAULT NULL COMMENT '促销价',
    `grab_start_time` DATETIME DEFAULT NULL COMMENT '新书开抢时间(未来时间=未开抢倒计时)',
    `stock` INT DEFAULT 0 COMMENT '库存',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
    `description` TEXT DEFAULT NULL COMMENT '图书简介',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB COMMENT='图书表';

-- 购物车表
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `book_id` BIGINT NOT NULL COMMENT '图书ID',
    `quantity` INT DEFAULT 1 COMMENT '数量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_book` (`user_id`, `book_id`)
) ENGINE=InnoDB COMMENT='购物车表';

-- 订单表
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额(优惠前)',
    `discount_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '总优惠金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `member_discount` DECIMAL(3,2) DEFAULT 1.00 COMMENT '会员折扣',
    `full_reduction_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '满减金额',
    `status` TINYINT DEFAULT 0 COMMENT '0-待支付 1-已支付 2-已发货 3-已完成 4-已退货',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
    `return_time` DATETIME DEFAULT NULL COMMENT '退货时间',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
    `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收件人姓名',
    `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收件人电话',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='订单表';

-- 订单明细表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `book_id` BIGINT NOT NULL COMMENT '图书ID',
    `book_title` VARCHAR(200) DEFAULT NULL COMMENT '书名(快照)',
    `book_cover` VARCHAR(255) DEFAULT NULL COMMENT '封面(快照)',
    `price` DECIMAL(10,2) NOT NULL COMMENT '下单时单价',
    `discount` DECIMAL(3,2) DEFAULT 1.00 COMMENT '下单时折扣',
    `promo_price` DECIMAL(10,2) DEFAULT NULL COMMENT '下单时促销价',
    `quantity` INT NOT NULL COMMENT '数量',
    `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB COMMENT='订单明细表';

-- 退货记录表（支持部分退货：按订单明细 + 数量）
DROP TABLE IF EXISTS `return_record`;
CREATE TABLE `return_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_item_id` BIGINT DEFAULT NULL COMMENT '订单明细ID（支持按单品部分退货）',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT '退货数量',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `reason` VARCHAR(255) DEFAULT NULL COMMENT '退货原因',
    `status` TINYINT DEFAULT 0 COMMENT '0-待审核 1-审核通过 2-审核拒绝',
    `admin_remark` VARCHAR(255) DEFAULT NULL COMMENT '管理员备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB COMMENT='退货记录表';

-- 初始数据
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `member_level`, `member_discount`)
VALUES ('admin', '$2b$10$ulr94WreQbeiaey9L09Fy.wMolaQEoRMtrkhfjZuZ3VPj2vhvo4BC', '系统管理员', 1, 3, 1.00);

INSERT INTO `category` (`name`, `description`) VALUES
('文学', '小说、散文、诗歌等文学作品'),
('计算机', '编程、计算机科学相关书籍'),
('经济管理', '经济、金融、管理类书籍'),
('教育', '教材、教辅、考试用书'),
('科技', '自然科学、工程技术类书籍'),
('历史', '历史传记、考古研究等'),
('哲学', '哲学思想、逻辑学等'),
('艺术', '绘画、音乐、设计等艺术类');
