# Kite Universe

Kite Universe 是一个前后端分离的二次元社区项目，当前已经包含首页、版区、帖子、评论、登录注册、个人中心、收藏和发帖等核心流程。

## 项目结构

```text
F:\javaweb\kiteUniverse
├─ common                      # 通用常量、返回体、异常等基础能力
├─ pojo                        # 实体、DTO、VO 等数据模型
├─ server                      # Spring Boot 后端服务
├─ front\kiteuniverseforntend  # Vite + TypeScript 前端项目
├─ uploads                     # 本地上传文件目录（已加入忽略）
└─ README.md
```

## 技术栈

- 前端：Vite、TypeScript、Vue Router（运行时 CDN 方式）
- 后端：Spring Boot 4、MyBatis、MySQL、Redis
- 构建：Maven Wrapper、npm

## 环境要求

- JDK 17 或更高版本
- Maven Wrapper（项目已自带，无需单独安装 Maven）
- Node.js 18 或更高版本
- MySQL 8.0 或兼容版本
- Redis 6/7

## 本地配置

### 1. 后端配置

项目不会提交真实的开发配置。请先复制示例文件：

```powershell
Copy-Item .\server\src\main\resources\application-dev.example.yml .\server\src\main\resources\application-dev.yml
```

然后打开 `server/src/main/resources/application-dev.yml`，填写你自己的本地配置：

- MySQL：`username`、`password`
- Redis：`host`、`port`
- JWT：`user-secret-key-string`
- 阿里云 OSS：`access-key-id`、`access-key-secret`、`bucket-name`
- 百度翻译：`APP_ID`、`SECRET_KEY`

说明：

- `application.yml` 默认启用 `dev` 环境。
- 后端默认端口是 `8081`。
- 社区相关表会在启动时自动检查并初始化，空表时会自动写入基础社区数据。

### 2. 前端配置

前端默认请求 `http://127.0.0.1:8081`。如果你的后端地址不同，可以复制示例文件：

```powershell
Copy-Item .\front\kiteuniverseforntend\.env.example .\front\kiteuniverseforntend\.env.local
```

然后按需修改：

```env
VITE_API_BASE_URL=http://127.0.0.1:8081
```

## 首次启动步骤

### 1. 准备数据库

先确保 MySQL 中已经创建数据库：

```sql
CREATE DATABASE kite_universe CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 启动 Redis

请先确保本地 Redis 可以访问，例如：

- 主机：`127.0.0.1`
- 端口：`6379`

### 3. 启动后端

在项目根目录执行：

```powershell
.\mvnw.cmd -pl server -am spring-boot:run
```

启动成功后，后端地址为：

- [http://127.0.0.1:8081](http://127.0.0.1:8081)

### 4. 启动前端

进入前端目录后执行：

```powershell
cd .\front\kiteuniverseforntend
npm install
npm run dev
```

默认开发地址通常是：

- [http://127.0.0.1:5173](http://127.0.0.1:5173)

## 常用命令

### 后端测试

```powershell
.\mvnw.cmd -pl server -am test
```

### 前端打包

```powershell
cd .\front\kiteuniverseforntend
npm run build
```

## 当前已实现功能

- 手机号验证码登录 / 注册
- 登录态持久化、退出登录、路由登录守卫
- 个人中心资料编辑、头像上传
- 社区首页、版区列表、版区详情
- 发帖、编辑帖子、删除帖子
- 评论、收藏、我的帖子、我的收藏
- Redis 登录态、验证码、页面缓存

## 提交前注意

这些文件或目录不要上传到公开仓库：

- `server/src/main/resources/application-dev.yml`
- `.idea/`
- `front/kiteuniverseforntend/node_modules/`
- `front/kiteuniverseforntend/dist/`
- `uploads/`
- `target/`
- `.claude/`

如果真实密钥已经上传过远端，请尽快更换：

- 数据库密码
- OSS AccessKey
- 百度翻译密钥
- JWT 密钥
