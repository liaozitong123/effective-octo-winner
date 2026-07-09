# 纸箱ERP系统 — Vue3 + SpringBoot + MySQL 部署说明

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + ECharts + Vite |
| 后端 | Spring Boot 3.2 + Spring Data JPA + Spring Security |
| 数据库 | MySQL 8.0 |
| 部署 | Docker Compose |

---

## 方式一：Docker Compose 一键部署（推荐）

### 环境要求
- Docker & Docker Compose

```bash
# 在项目根目录 carton_erp_v2 下执行
docker-compose up -d
```

访问 **http://localhost**

默认账号: `admin` / `admin123`

---

## 方式二：手动部署

### 1. 启动 MySQL

```bash
docker run -d --name carton_mysql \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=carton_erp \
  -p 3306:3306 \
  mysql:8.0
```

### 2. 启动后端

```bash
cd backend

# 编译
mvn clean package -DskipTests

# 运行
java -jar target/carton-erp-1.0.0.jar

# 或通过环境变量指定数据库
MYSQL_HOST=localhost MYSQL_PORT=3306 MYSQL_USER=root MYSQL_PASSWORD=root123 java -jar target/carton-erp-1.0.0.jar
```

后端运行在 **http://localhost:8080**

### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install --registry=https://registry.npmmirror.com

# 开发模式
npm run dev

# 生产构建
npm run build
```

前端开发模式运行在 **http://localhost:3000**

---

## 数据库配置

后端 `application.yml` 中数据库相关配置通过环境变量注入：

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| MYSQL_HOST | localhost | 数据库地址 |
| MYSQL_PORT | 3306 | 数据库端口 |
| MYSQL_USER | root | 用户名 |
| MYSQL_PASSWORD | root123 | 密码 |

---

## 系统默认数据

首次启动自动插入：
- 管理员账号: admin / admin123
- 3个示例客户
- 3个示例供应商
- 6种示例库存物品

## 项目结构

```
carton_erp_v2/
├── backend/          # Spring Boot 后端
├── frontend/         # Vue 3 前端
├── docker-compose.yml
├── Dockerfile.backend
├── Dockerfile.frontend
├── nginx.conf
└── deploy.md
```
