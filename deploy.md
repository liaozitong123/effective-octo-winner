# 纸箱ERP系统 — Vue3 + SpringBoot + MySQL 部署说明

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + ECharts + Vite |
| 后端 | Spring Boot 3.2 + Spring Data JPA + Spring Security |
| 数据库 | MySQL 8.0 |
| 部署 | 当前服务器使用 Jar 直接部署；Docker Compose 为备用方案 |

---

## 当前服务器部署方式：Jar 直接部署

当前线上服务器：`120.27.236.4`

当前访问地址：`http://120.27.236.4:8080`

当前运行方式：

```bash
java -jar /root/carton-erp-1.0.0.jar --spring.profiles.active=mysql
```

### 每次更新部署步骤

先在本地提交并推送代码：

```powershell
cd D:\ai\AIAgent\carton_erp_v2
git add .
git commit -m "说明这次修改了什么"
git push origin main
```

然后登录服务器，在服务器终端执行。确认提示符类似：

```text
root@iZbp17u111z4okq8v0uxhyZ:~#
```

服务器部署命令：

```bash
cd /root/carton_erp_v2
git pull origin main

cd backend
mvn clean package -DskipTests

cd /root
cp carton-erp-1.0.0.jar carton-erp-1.0.0.jar.bak.$(date +%Y%m%d%H%M%S)

pkill -f 'carton-erp-1.0.0.jar' || true
sleep 3

cp /root/carton_erp_v2/backend/target/carton-erp-1.0.0.jar /root/carton-erp-1.0.0.jar

nohup java -jar /root/carton-erp-1.0.0.jar --spring.profiles.active=mysql > /root/app.log 2>&1 &

sleep 8
tail -n 80 /root/app.log
```

### 判断是否部署成功

日志中看到下面两行表示启动成功：

```text
Tomcat started on port 8080
Started CartonErpApplication
```

也可以检查端口：

```bash
ss -lntp | grep 8080
```

如果看到 `java` 监听 `*:8080`，说明服务正在运行。

### 注意事项

- 服务器目前不是 Docker 部署，不要在服务器上执行 `docker-compose up`。
- 如果前台运行 `java -jar ...` 成功，按 `Ctrl + C` 停掉后，再用 `nohup ... &` 后台启动。
- 如果新服务启动失败，先看日志：`tail -n 150 /root/app.log`。
- 如果日志里只有旧进程关闭时报错，先备份或清空旧日志，再前台启动一次看真实错误。
- 旧进程停止后再复制新 jar，避免替换 jar 时旧进程还在读取文件。

---

## 备用方案：Docker Compose 一键部署

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
