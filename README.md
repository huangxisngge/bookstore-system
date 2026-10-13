# 喵喵书店 —— 小型网上书店系统

基于 **B/S 架构** 的网上图书销售系统，实现图书浏览、搜索、购物车、下单、支付、发货、收货、退货审核、统计报表等完整电商闭环，包含**用户端**与**管理员端**双入口。

## 技术栈

| 端 | 技术 |
|----|------|
| 后端 | Spring Boot 3.2.5 · MyBatis-Plus 3.5.6 · MySQL 8 · JWT (jjwt 0.12.5) · Spring Security (BCrypt) · Lombok · Java 21 |
| 前端 | Vue 3.5 (`<script setup>`) · Vite 8 · Element Plus 2.14 · Pinia · vue-router 4 · axios · echarts 6 |

## 功能特性

**普通用户**
- 注册 / 登录、浏览与搜索图书、查看详情
- 加入购物车、修改数量、清空购物车
- 提交订单、在线支付、取消待支付订单
- 查看订单、确认收货、申请退货
- 维护个人资料、修改密码、查看会员等级与折扣

**管理员**
- 图书管理：增删改查、上下架、促销价、新书"开抢时间"
- 分类管理、订单管理（发货、退货审核）、用户管理（禁用/启用）
- 统计分析：总览指标与多维度图表

**核心亮点**
- 防超卖：悲观锁 `FOR UPDATE` + 原子扣减 `WHERE stock >= q` 双防线，库存永不为负
- 新书开抢倒计时、一键抢购、收货地址预填（服务器时间校准）
- JWT 无状态认证，用户被禁用即时生效
- 订单明细价格快照、逻辑删除、公共字段自动填充、统一 `Result<T>` 响应

## 目录结构

```
database_ks
├── bookStore_hou          # 后端 (Spring Boot)
│   └── src/main/resources
│       ├── application.yml
│       ├── application-local.yml.example   # 本地配置模板
│       └── sql/schema.sql                  # 建库建表 + 初始数据
├── bookStore_qian         # 前端 (Vue 3 + Vite)
├── 系统设计文档.md         # 详细设计文档
└── 网站前端页面展示        # 页面截图
```

## 环境要求

- JDK 21
- Maven 3.6+
- MySQL 8
- Node.js 18+（Vite 8 要求）

## 快速开始

### 1. 初始化数据库

执行 `bookStore_hou/src/main/resources/sql/schema.sql`，自动创建 `bookstore` 库、7 张表及初始数据。

```sql
source bookStore_hou/src/main/resources/sql/schema.sql;
```

### 2. 配置后端

数据库密码与 JWT 密钥已改为环境变量，启动前需设置：

```powershell
# Windows PowerShell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
$env:JWT_SECRET="你的JWT密钥(至少256位)"
```

> 也可复制 `application-local.yml.example` 为 `application-local.yml` 填入真实值（该文件已被 `.gitignore` 忽略）。

### 3. 启动后端

```bash
cd bookStore_hou
mvn spring-boot:run
```

后端运行于 `http://localhost:8080`。

### 4. 启动前端

```bash
cd bookStore_qian
npm install
npm run dev
```

前端运行于 `http://localhost:5173`，`/api` 已代理到后端 8080 端口。

## 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | 123456 |
| 普通用户 | zhangwei | 123456 |
| 普通用户 | liming | 123456 |

## 数据库表

| 表名 | 说明 |
|------|------|
| `user` | 用户表 |
| `category` | 图书分类表 |
| `book` | 图书表 |
| `cart` | 购物车表 |
| `order` | 订单表 |
| `order_item` | 订单明细表 |
| `return_record` | 退货记录表 |

## 相关文档

- `系统设计文档.md` —— 完整系统设计说明（业务流程、状态机、技术要点）
- `小型网上书店系统课程设计报告.pdf` —— 课程设计报告
- `小型书店管理系统ppt.pptx` —— 答辩 PPT
