# 校园二手拍卖与即时通讯平台 (CAIP)

> 软件工程实验课（2026）第9组项目
>
> Spring Boot 3.4 + Vue 3 + TypeScript + MySQL 8.0 + Redis 7

---

## 快速启动

### 前置条件

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | `java -version` |
| Node.js | 18+ | `node -v` |
| MySQL | 8.0 | 需创建数据库 `campus_auction` |
| Redis | 7.x | 默认端口 6379 |

### 1. 准备数据库

```sql
CREATE DATABASE IF NOT EXISTS campus_auction CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 配置环境变量

复制 `.env.example` 为 `.env` 并填入实际值：

```bash
cp .env.example .env
```

编辑 `.env` 文件：

```bash
DB_USERNAME=root
DB_PASSWORD=<你的数据库密码>
REDIS_HOST=localhost
JWT_SECRET=<生成一个随机密钥，例如: openssl rand -base64 32>
FILE_UPLOAD_PATH=./uploads/images/
```

> 不加 `.env` 文件，后端和 Docker Compose 将因缺少必需变量而拒绝启动。

### 3. 启动后端（Flyway 首次启动自动建表）

**方式一：IntelliJ IDEA（推荐）**

1. 用 IDEA 打开 `backend/` 目录（File → Open → 选择 `D:\lab\SElab\backend`）
2. 等待 Maven 依赖下载完成（右下角进度条消失）

**IDEA 必要配置（首次打开后检查）：**

| 配置项 | 路径 | 设置值 |
|--------|------|--------|
| JDK | File → Project Structure → SDK | **JDK 17+** |
| Lombok 插件 | File → Settings → Plugins | 搜索安装 **Lombok** |
| 注解处理器 | File → Settings → Build → Compiler → Annotation Processors | 勾选 **Enable annotation processing** |
| 数据库密码 | `application.yml` 第 12 行 | 改为你的 MySQL 密码 |

3. 确保 MySQL 和 Redis 已启动
4. 找到 `src/main/java/com/ca/CaApplication.java`
5. 右键 → Run 'CaApplication.main()'，或点击 `main` 方法左侧的绿色三角
6. 控制台出现 `Started CaApplication` 即启动成功

**方式二：命令行**

```bash
cd backend
./mvnw spring-boot:run        # Linux/Mac
.\mvnw.cmd spring-boot:run    # Windows
```

启动后访问：
- API 文档（Swagger）：http://localhost:8080/swagger-ui.html
- 健康检查：http://localhost:8080/actuator/health

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问：http://localhost:5173

### Docker 一键启动（可选）

```bash
# 同样需要先在项目根目录创建 .env 文件（同上一步）
cd deploy
docker-compose up -d
# 启动顺序：MySQL → Redis → Spring Boot → Nginx
# 访问 http://localhost
```

---

## 测试账号

| 账号 | 密码 | 角色 |
|------|------|:--:|
| `admin` | `admin123` | 管理员 |
| `test001` | `test123` | 普通用户 |
| `test002` | `test123` | 普通用户 |

> 首次启动时 `DataInitializer` 会自动更新密码哈希。

---

## 项目结构

```
SElab/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/ca/
│   │   ├── common/                   # 公共：config/security/exception/util
│   │   └── module/
│   │       ├── user/                 # M1 用户认证
│   │       ├── product/              # M2 商品搜索
│   │       ├── auction/              # M3 拍卖引擎
│   │       ├── chat/                 # M4 即时通讯
│   │       ├── order/                # M5 订单交易
│   │       └── admin/                # M6 管理后台
│   ├── src/main/resources/
│   │   └── db/migration/             # Flyway 迁移脚本
│   └── pom.xml
├── frontend/                         # Vue 3 前端
│   ├── src/
│   │   ├── api/                      # API 封装
│   │   ├── components/               # 公共组件
│   │   ├── composables/              # 组合式函数
│   │   ├── router/                   # 路由
│   │   ├── stores/                   # Pinia 状态
│   │   ├── types/                    # TypeScript 类型
│   │   └── views/                    # 页面组件
│   └── package.json
├── deploy/                           # 部署配置
│   ├── docker-compose.yml
│   ├── nginx.conf
│   └── Dockerfile.backend
├── docs/                             # 项目文档
│   ├── 02-设计说明书-完整版.md
│   ├── 03-完成计划与进度.md
│   ├── 04-测试设计及结果报告书.md
│   └── 05-项目总结报告书.md
└── CLAUDE.md                         # 全局约束（AI 辅助开发用）
```

---

## 功能模块

| 模块 | 功能 | 负责人 |
|------|------|:--:|
| M1 | 注册/登录/JWT/RBAC/个人信息 | 顾思豪 |
| M2 | 商品发布/搜索/筛选/收藏/图片上传 | 刘子彦 |
| M3 | 英式拍卖/Redis Lua出价/STOMP推送/成交判定 | 段睿智 |
| M4 | 一对一私聊/WebSocket实时收发/敏感词过滤 | 罗圣杰 |
| M5 | 订单状态机/模拟支付/发货收货 | 罗涵 |
| M6 | 用户审核/商品管理/分类管理/数据统计 | 欧阳熠 |

---

## 技术栈

- **后端**：Spring Boot 3.4 + MyBatis-Plus 3.5 + Flyway + Spring Security + JWT
- **前端**：Vue 3 + TypeScript + Element Plus + Pinia + ECharts
- **数据库**：MySQL 8.0 + Redis 7
- **通信**：REST API + WebSocket (STOMP)
- **部署**：Docker Compose + Nginx

---

## 运行测试

```bash
# 后端单元测试
cd backend
./mvnw test

# 前端 TypeScript 检查
cd frontend
npx vue-tsc --noEmit

# 前端构建
npm run build
```
