# Kite Universe

## Distributed Lock Protection

The backend now uses `Redisson RLock` to serialize high-contention writes that previously relied on "check first, then insert/update" logic.

- Protected operations: daily check-in, user point and badge updates, post like/unlike, comment like/unlike, post favorite/unfavorite, and follow/unfollow
- Goal: avoid duplicate relation rows, duplicate notifications, repeated counter increments, and lost user-point updates under concurrent requests
- Lock granularity:
- `user-progress:user:{userId}` for check-in, points, and badges
- `community:post-like:post:{postId}:user:{userId}` for post likes
- `community:post-favorite:post:{postId}:user:{userId}` for post favorites
- `community:comment-like:comment:{commentId}:user:{userId}` for comment likes
- `user-follow:follower:{followerId}:following:{followingId}` for follow relations
- Failure strategy: write requests fail closed when a lock cannot be acquired in time, so the system prefers retryable protection over unsafe duplicate writes

### Default lock config

```yaml
kite-universe:
  redis:
    lock:
      enabled: true
      key-prefix: lock
      wait-time-millis: 1500
      lease-time-millis: 5000
```

## Cache Penetration Protection

The backend now uses `Redisson RBloomFilter` to reduce cache penetration caused by repeated requests for illegal IDs.

- Protected objects: public `userId` lookups and public `postId` lookups
- Goal: reject obviously invalid IDs before they can keep hitting Redis cache and MySQL
- Warm-up strategy: on application startup, existing user IDs and published post IDs are loaded into Bloom filters
- Incremental updates: new users and new posts are added to the Bloom filters immediately after successful creation
- Failure strategy: if Redisson or Redis is temporarily unavailable, the guard fails open and continues to the normal DB path instead of blocking valid traffic

### Default keys

The Bloom filters are stored in Redis with the project key prefix:

- `kite-universe:bloom-filter:user-ids`
- `kite-universe:bloom-filter:post-ids`

### Config

The default configuration lives in:

- `server/src/main/resources/application.yml`
- `server/src/main/resources/application-dev.example.yml`

Relevant settings:

```yaml
kite-universe:
  redis:
    bloom-filter:
      enabled: true
      user:
        key: bloom-filter:user-ids
        expected-insertions: 200000
        false-probability: 0.001
      post:
        key: bloom-filter:post-ids
        expected-insertions: 200000
        false-probability: 0.001
```

### Verification

Backend verification command:

```powershell
.\mvnw.cmd -pl server -am test
```

## Two-Level Cache

The community content pages now use a `Caffeine + Redis` two-level cache.

- Caffeine handles hot-key local reads for millisecond-level responses inside one instance
- Redis keeps the shared distributed cache so multiple backend instances can reuse the same payloads
- Redis Pub/Sub broadcasts invalidation messages so every instance can evict its local Caffeine copy after writes
- `@TwoLevelCache` provides one unified entry point for both read-through caching and post-write eviction

### Annotation modes

- `@TwoLevelCache(key = "...")` wraps a read method with local-cache -> Redis -> database/service fallback
- `@TwoLevelCache(mode = EVICT, evictKeys = {...})` evicts both Redis and every instance's local cache after a successful write

Current usage:

- `CommunityContentServiceImpl#getHomePage()` and `getBoardsPage()` use `READ_WRITE`
- `CommunityInteractionServiceImpl` write methods such as `createPost`, `updatePost`, `deletePost`, `createComment`, `favoritePost`, `unfavoritePost`, `likePost`, and `unlikePost` use `EVICT`

### Config

Relevant settings live in:

- `server/src/main/resources/application.yml`
- `server/src/main/resources/application-dev.example.yml`

```yaml
kite-universe:
  content:
    home-cache-seconds: 300
    home-local-cache-seconds: 30
    boards-cache-seconds: 300
    boards-local-cache-seconds: 30
  cache:
    two-level:
      enabled: true
      default-local-ttl-seconds: 30
      default-redis-ttl-seconds: 300
      local-maximum-size: 1000
      invalidation-topic: cache:two-level:invalidate
```

### Verification

Backend verification command:

```powershell
.\mvnw.cmd -pl server -am clean test
```

## Async Notification Queue

The notification write path now uses `RabbitMQ` to asynchronously decouple high-frequency user interactions from notification persistence.

- Covered actions: post comment, post like, comment like, follow, and admin announcement
- Flow: business transaction commits -> publish `NotificationCommand` -> RabbitMQ queue buffers bursts -> consumer writes `notification` rows and pushes WebSocket unread-count updates
- Goal: smooth burst traffic, reduce synchronous database pressure on hot interaction APIs, and keep notification delivery eventually consistent across instances
- Safety fallback: if RabbitMQ publish fails, the backend falls back to direct post-commit processing so notifications are not silently lost
- Startup fallback: if RabbitMQ is enabled in config but the broker is unreachable during application startup, the backend skips starting the Rabbit listener and automatically falls back to direct post-commit processing
- Dead-letter support: failed consumer messages are rejected without requeue and routed to the configured DLQ for later inspection

### Config

Relevant settings live in:

- `server/src/main/resources/application.yml`
- `server/src/main/resources/application-dev.example.yml`

```yaml
spring:
  rabbitmq:
    host: ${kite-universe.rabbitmq.host:127.0.0.1}
    port: ${kite-universe.rabbitmq.port:5672}
    username: ${kite-universe.rabbitmq.username:guest}
    password: ${kite-universe.rabbitmq.password:guest}
    virtual-host: ${kite-universe.rabbitmq.virtual-host:/}

kite-universe:
  notification:
    mq:
      enabled: true
      exchange: notification.command.exchange
      queue: notification.command.queue
      routing-key: notification.command
      dead-letter-exchange: notification.command.dlx
      dead-letter-queue: notification.command.dlq
      dead-letter-routing-key: notification.command.dlq
```

### Verification

Backend verification command:

```powershell
.\mvnw.cmd -pl server -am clean test
```

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
- 后端：Spring Boot 4、MyBatis、MySQL、Redis、WebSocket/STOMP
- AI：DeepSeek（OpenAI 协议兼容，直接 HTTP 调用）
- 构建：Maven Wrapper、npm

## 环境要求

- JDK 17 或更高版本
- Maven Wrapper（项目已自带，无需单独安装 Maven）
- Node.js 18 或更高版本
- MySQL 8.0 或兼容版本
- Redis 6/7
- Elasticsearch 9.x，需安装以下两个插件：
  - `analysis-ik`（中文分词）
  - `analysis-pinyin`（拼音搜索）

安装插件命令（以 9.3.3 为例，将版本号替换为实际版本）：
```powershell
.\bin\elasticsearch-plugin.bat install https://get.infini.cloud/elasticsearch/analysis-ik/9.3.3
.\bin\elasticsearch-plugin.bat install https://get.infini.cloud/elasticsearch/analysis-pinyin/9.3.3
```

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
- 帖子点赞、评论点赞、关注 / 取关用户
- 用户主页（公开个人资料页）
- 全站关键字搜索（中文分词 + 拼音搜索 + 前缀匹配）
- 搜索自动补全下拉框（输入时实时提示）
- 相关帖子推荐（ES more_like_this）
- 搜索统计看板（热门搜索词 + 无结果率）
- Redis 登录态、验证码、页面缓存

### Elasticsearch 全文搜索

| 功能 | 说明 |
|------|------|
| 中文分词 | 索引时 `ik_max_word` 细粒度分词，搜索时 `ik_smart` 粗粒度分词，提升召回与精准度 |
| 拼音搜索 | `analysis-pinyin` 插件为 `title`、`badge` 字段添加 `.pinyin` 子字段；输入全拼（`saibo`）或首字母缩写（`sbpk`）均可匹配对应汉字内容 |
| 前缀匹配 | `match_phrase_prefix` 查询：输入"赛博"可匹配"赛博朋克"、"赛博坦"等词 |
| 模糊匹配 | `multi_match + fuzziness=AUTO`：容错拼写错误，title^3 badge^2 summary^2 content 权重递减 |
| 搜索高亮 | 匹配关键词以 `<em class="search-highlight">` 标记，前端渲染为彩色高亮 |
| 自动补全 | 输入时 200ms 防抖请求 `GET /api/posts/suggest`，下拉框展示候选标题 |
| 相关帖子 | 帖子详情页侧边栏通过 `more_like_this` 查询推荐内容相似的帖子 |
| 搜索统计 | Redis ZSet 记录搜索词频率和无结果率，`GET /api/posts/search-stats` 返回运营看板数据 |

#### 搜索相关 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/posts/search` | 全文搜索，参数 `keyword`、`limit`（默认 20） |
| GET | `/api/posts/suggest` | 自动补全，参数 `prefix`、`limit`（默认 8） |
| GET | `/api/posts/{postId}/related` | 相关帖子推荐，参数 `limit`（默认 5） |
| GET | `/api/posts/search-stats` | 搜索统计看板，参数 `topN`（默认 10） |

#### 索引配置（`server/src/main/resources/es/community-posts-settings.json`）

- `ik_pinyin_analyzer`：IK 分词 + 拼音 filter，用于**索引**阶段，同时建立中文词和拼音 token
- `pinyin_analyzer`：纯拼音 tokenizer，用于**搜索**阶段，将查询词转为拼音后匹配

索引首次创建由 Spring Data Elasticsearch 在应用启动时自动完成，数据由 `PostIndexInitializer` 启动后批量写入。

### 站内通知系统

| 功能 | 说明 |
|------|------|
| 评论通知 | 有人评论帖子时，帖子作者收到通知 |
| 帖子点赞通知 | 有人点赞帖子时，帖子作者收到通知 |
| 评论点赞通知 | 有人点赞评论时，评论作者收到通知 |
| 关注通知 | 有人关注用户时，被关注者收到通知 |
| 系统公告 | 管理员通过 `POST /api/notifications/announcement` 向全体用户发送公告 |
| 未读消息计数 | 导航栏显示实时未读数字红点，每 60 秒自动刷新 |
| 消息中心页面 | `/notifications` 页面统一展示所有通知，支持一键全部已读 |

#### 通知 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/notifications` | 查询当前用户通知列表（最多 100 条） |
| GET | `/api/notifications/unread-count` | 查询未读通知数量 |
| PUT | `/api/notifications/read-all` | 全部标为已读 |
| PUT | `/api/notifications/{id}/read` | 单条标为已读 |
| POST | `/api/notifications/announcement` | 发布全站公告（需登录，body: `{"content":"..."}`) |

### 内容发现与推荐

| 功能 | 说明 |
|------|------|
| 热门/趋势帖子 | `GET /api/posts/hot?days=7&limit=10`，按浏览×1 + 评论×5 + 收藏×3 + 点赞×2 综合热度排序 |
| 排序筛选 | 版区帖子列表支持最新 / 最热 / 精华三种排序，前端 Tab 切换即时刷新 |
| 分页/加载更多 | 版区列表和话题页支持 `offset` 分页，每页 20 条，底部「加载更多」按钮追加内容 |
| 标签/话题系统 | 帖子徽标即话题标签；点击标签跳转 `/topics/{badge}` 查看该话题下所有帖子 |
| 发现页 | `/discover` 页面展示热门榜单（可切换 3/7/30 天窗口）、个性化推荐和话题标签云 |
| 推荐算法 | 已登录用户：统计其点赞/收藏行为覆盖的版块，返回来自这些版块的最近 30 天热门帖子；无历史记录时回退全局热榜 |

#### 内容发现 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/posts/hot` | 热门帖子，参数 `days`（默认 7）、`limit`（默认 10） |
| GET | `/api/posts/board/{boardId}/paged` | 版块帖子分页，参数 `sort`（latest/hot/featured）、`limit`、`offset` |
| GET | `/api/posts/board/{boardId}/count` | 版块帖子总数，参数 `sort` |
| GET | `/api/posts/badge/{badge}` | 话题聚合帖子，参数 `limit`、`offset` |
| GET | `/api/posts/recommended` | 个性化推荐（需登录），参数 `limit` |

### 内容创作增强

| 功能 | 说明 |
|------|------|
| 富文本编辑器 | 发帖页正文改用 EasyMDE（Markdown 编辑器），支持加粗、标题、引用、代码块、表格等 |
| 帖子图片上传 | 编辑器工具栏新增「上传图片」按钮，图片上传后自动插入 Markdown 图片语法；接口 `POST /api/posts/images/upload` |
| 图集发布 | 发帖时可切换「图集模式」，上传多张图片构成图集，详情页以网格形式展示 |
| 图片预览/灯箱 | 点击正文或图集中的任意图片，弹出全屏灯箱预览，点击遮罩或关闭按钮退出 |
| AI 内容标注 | 发帖时勾选「包含 AI 生成内容」后，帖子详情页顶部显示黄色 AI 徽标，参考森空岛标注规范 |
| Markdown 渲染 | 帖子详情页正文通过 marked.js 渲染为 HTML，支持完整 Markdown 语法 |

#### 内容创作 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/posts/images/upload` | 上传帖子图片（multipart/form-data，字段名 `file`），返回 `{"url":"..."}` |
| POST | `/api/posts` | 创建帖子，body 新增 `isAiGenerated`（boolean）和 `galleryImages`（JSON 数组字符串）字段 |
| PUT | `/api/posts/{id}` | 更新帖子，同样支持以上两个新字段 |

### Spring AI + DeepSeek 大模型集成

通过 Spring AI 1.0.0 接入 DeepSeek（OpenAI 协议兼容），提供三项 AI 辅助能力。AI 功能通过 `@ConditionalOnProperty` 条件注入，未配置 API Key 时不影响其他功能正常运行。

#### AI 功能说明

| 功能 | 入口 | 说明 |
|------|------|------|
| 帖子摘要生成 | 发帖页 → 摘要输入框旁（可扩展） | 根据标题和正文自动生成不超过 100 字的中文摘要 |
| 标签智能推荐 | 发帖页徽标字段旁「AI 智能推荐」按钮 | 根据标题和正文推荐 3–5 个分类标签，点击即可应用 |
| 玩家成长报告 | 个人中心侧边栏「生成我的成长报告」按钮 | 结合积分、等级、连续签到天数、发帖量，生成 Markdown 格式的个性化成长分析 |

#### AI API 端点

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/ai/post-summary` | 无需 | body: `{title, content}`，返回 `{postId, summary}` |
| POST | `/api/ai/badge-suggest` | 无需 | body: `{title, content}`，返回 `{badges[], reason}` |
| POST | `/api/ai/growth-report` | 需要 Bearer Token | 无 body，返回 `{userId, levelName, points, report}` |

#### 配置说明（application-dev.yml）

```yaml
kite-universe:
  ai:
    openai:
      api-key: <your-deepseek-api-key>
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-chat   # 或 deepseek-reasoner
```

#### Prompt Engineering 设计

- **摘要生成**：限定 100 字、中文、只输出正文，防止模型附加额外解释
- **标签推荐**：指定输出格式 `标签1,标签2|理由`，前端按 `|` 分割解析，标签点击即填入字段
- **成长报告**：注入结构化用户数据（等级/积分/连续签到/发帖量），要求 Markdown 三段式（成就总结/优势/建议），字数限定 300 字

### WebSocket 实时通信（STOMP over WebSocket）

原有通知和私信均为 HTTP 轮询（60s / 30s / 10s），现已改造为 WebSocket 长连接实时推送，彻底替代 `setInterval` 轮询。

#### 架构设计

| 层 | 技术 |
|----|------|
| 后端代理 | Spring WebSocket + STOMP（内存 SimpleBroker） |
| 认证 | STOMP CONNECT 帧携带 `Authorization: Bearer <token>`，`WebSocketAuthChannelInterceptor` 拦截解析 JWT，设置 Principal |
| 前端 | `@stomp/stompjs@6`（CDN），无 SockJS，原生 WebSocket |
| 连接管理 | `ws.ts` 单例服务，App 登录后 `connect()`，退出后 `disconnect()` |

#### 推送目的地

| 目的地 | 方向 | 内容 |
|--------|------|------|
| `/user/queue/notifications` | 服务端 → 用户 | `{"unreadCount": N}`，评论/点赞/关注时触发 |
| `/user/queue/messages` | 服务端 → 用户 | 完整 `MessageVO`，收到私信时触发 |
| `/topic/system` | 服务端 → 所有在线用户 | `{"type":"announcement","content":"..."}` |

#### 前端改造

- **`main.ts`**：`startUnreadPoll()` 改为建立 WS 连接 + 订阅通知/消息推送，移除两个 `setInterval`
- **`messages.ts`**：移除 10s `pollTimer`，改为 `wsService.onNewMessage()` 回调——收到推送后直接追加消息、更新会话列表、标记已读
- **`ws.ts`**：封装 STOMP Client 生命周期，支持自动重连（5s delay）和监听器注册/注销

#### WebSocket 端点

| 端点 | 说明 |
|------|------|
| `ws://<host>:8081/ws` | STOMP WebSocket 入口（无 SockJS） |

### 游戏工具集成（虚空回廊主题）

| 功能 | 说明 |
|------|------|
| 游戏账号绑定 | 绑定虚空回廊游戏账号（探索者 UID、服务器、探索者称号、等级），支持多账号管理 |
| 跑图记录 | 手动录入跑图结果（职业、天命等级、通关幕数、最高层数、得分、核心遗物），按天命/层数降序展示 |
| 游戏数据查询 | 记录行动力、虚空碎片、探索者等级、总跑图次数等游戏数据快照，含行动力进度条可视化 |
| 玩家 Wiki | 静态游戏百科，收录核心机制、职业介绍、战斗机制、首领攻略、卡牌机制、进阶挑战等 40+ 条目，支持全文搜索 |
| 攻略合集 | 聚合社区中标记为攻略类话题的帖子（攻略、刃影、术士、缚魂、钢躯等），支持话题筛选和加载更多 |

#### 游戏工具 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/game/accounts` | 查询当前用户绑定的游戏账号列表 |
| POST | `/api/game/accounts` | 绑定新游戏账号，body: `{gameUid, serverName, inGameName, accountLevel}` |
| PUT | `/api/game/accounts/{id}` | 更新账号信息 |
| DELETE | `/api/game/accounts/{id}` | 解绑账号 |
| GET | `/api/game/characters` | 查询跑图记录列表（按天命/层数降序） |
| POST | `/api/game/characters` | 添加跑图记录，body: `{classId, className, ascensionLevel, actReached, floorReached, score, keyRelic}` |
| PUT | `/api/game/characters/{id}` | 更新跑图记录 |
| DELETE | `/api/game/characters/{id}` | 删除跑图记录 |
| GET | `/api/game/stats` | 查询游戏数据快照 |
| PUT | `/api/game/stats` | 更新游戏数据快照（不存在时自动创建） |

#### 前端页面

| 路由 | 页面 | 说明 |
|------|------|------|
| `/game-tools` | 游戏工具 | 登录后可用，含游戏数据 / 跑图记录 / 游戏账号三个 Tab |
| `/wiki` | 玩家百科 | 公开访问，分区导航 + 全文搜索 |
| `/strategies` | 攻略合集 | 公开访问，按话题标签筛选社区攻略帖子 |

数据库表（启动时自动创建）：
- `game_account` — 用户游戏账号绑定记录
- `game_character_record` — 用户跑图记录
- `game_stats` — 用户游戏数据快照（每用户唯一）

### 签到与激励系统

| 功能 | 说明 |
|------|------|
| 每日签到 | 每日签到基础奖励 10 积分，连续签到第 7/14/30 天额外奖励 20/40/100 积分 |
| 社区积分 | 发帖 +5、评论 +2、帖子被点赞 +3、帖子被收藏 +5（积分奖励给创作者） |
| 用户等级 | 共 6 级（探索者→初入回廊→深渊行者→虚空老兵→裂隙征服者→虚空主宰），按累计积分升级 |
| 创作者激励 | 发帖、评论、被点赞/收藏均可获得积分；发布首帖自动获得「初次创作」徽章 |
| 徽章/成就 | 初次签到、七日连签、三十日连签、初次创作、各级升级徽章，共 10 种 |

**等级阈值：**

| 等级 | 名称 | 所需累计积分 |
|------|------|------|
| Lv1 | 探索者 | 0+ |
| Lv2 | 初入回廊 | 100+ |
| Lv3 | 深渊行者 | 300+ |
| Lv4 | 虚空老兵 | 700+ |
| Lv5 | 裂隙征服者 | 1500+ |
| Lv6 | 虚空主宰 | 3000+ |

#### 签到与激励 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/checkin` | 执行今日签到，返回积分奖励和徽章 |
| GET | `/api/checkin/status` | 查询今日签到状态、当前积分和等级 |
| GET | `/api/checkin/badges` | 查询当前用户已获得的所有徽章 |

#### 前端页面

| 路由 | 页面 | 说明 |
|------|------|------|
| `/checkin` | 每日签到 | 登录后可用，含本周签到日历、签到按钮、等级进度和徽章展示 |

数据库表（启动时自动创建）：
- `user_points` — 用户积分与等级快照（每用户唯一）
- `daily_check_in` — 每日签到记录（每用户每天唯一）
- `user_badge` — 用户已获得徽章（每用户每种类型唯一）

### 私信系统

| 功能 | 说明 |
|------|------|
| 一对一私信 | 用户之间发送和接收私信，支持历史消息加载 |
| 会话列表 | 左侧展示所有会话，含对方昵称、最新消息预览、未读数红点 |
| 实时轮询 | 消息页每 10 秒自动拉取新消息；导航栏未读私信数每 30 秒刷新 |
| 未读徽章 | 导航栏「私信」链接右上角显示未读数红点 |
| 发起私信 | 从用户主页点击「发私信」，携带 `?userId=` 参数跳转至私信页直接开启会话 |

#### 私信 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/messages` | 发送私信，body: `{"recipientId": 1, "content": "..."}` |
| GET | `/api/messages/{otherId}` | 获取与指定用户的聊天记录，参数 `limit`（默认 50）、`offset`（默认 0） |
| PUT | `/api/messages/{senderId}/read` | 将指定用户发来的消息全部标为已读 |
| GET | `/api/messages/unread-count` | 查询当前用户未读私信总数 |
| GET | `/api/messages/conversations` | 查询会话列表（按最后消息时间降序） |

#### 前端页面

| 路由 | 页面 | 说明 |
|------|------|------|
| `/messages` | 私信 | 登录后可用，左侧会话列表 + 右侧聊天区，Enter 发送，Shift+Enter 换行 |

数据库表（启动时自动创建）：
- `private_message` — 私信记录（含发件人、收件人、内容、已读状态、时间戳）

### 体验优化功能

| 功能 | 说明 |
|------|------|
| 楼层回复/嵌套评论 | 评论区每条评论下方有「回复」按钮，点击展开内联回复表单；子评论以缩进样式展示并标注「回复 xxx」；后端通过 `parent_id` 字段维护评论树结构，排序按 `COALESCE(parent_id, id) ASC, id ASC` |
| 帖子置顶/精华管理 | 管理后台帖子列表新增「置顶/取消置顶」「精华/取消精华」操作按钮；后端字段 `is_pinned`/`is_featured` 通过 4 个独立 API 管理 |
| 深色模式 | 顶栏新增主题切换按钮（`☾`/`☀`），切换后在 `<html data-theme="dark">` 生效；偏好存储至 `localStorage`；首次访问自动读取 `prefers-color-scheme` |
| 分享帖子 | 帖子详情页新增「分享」按钮；优先调用 Web Share API，不支持时自动复制链接至剪贴板并提示 |
| 草稿自动保存 | 发帖页使用 `localStorage` 每 30 秒自动保存草稿，刷新后自动恢复；发帖成功后自动清除草稿；也可手动保存或清除 |
| 第三方登录入口 | 登录/注册弹窗底部展示微信、QQ、微博三个 OAuth 入口（当前为 disabled 占位，后续接入 OAuth 流程时启用） |
| 移动端适配 | `≤760px` 断点下：侧边栏隐藏、内容网格单列、认证弹窗全屏展示、嵌套评论缩进减小、hero 操作按钮自动换行 |

#### 嵌套评论 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/posts/{postId}/comments` | 创建评论，body 新增可选字段 `parentId`（填写父评论 ID 即为回复，null 或不填为顶层评论） |

数据库新增列（启动时通过 ALTER TABLE 自动添加）：
- `community_comment.parent_id` — 父评论 ID（BIGINT，可为 NULL）

#### 置顶/精华 API

| 方法 | 路径 | 说明 |
|------|------|------|
| PUT | `/api/admin/posts/{id}/pin` | 置顶帖子 |
| PUT | `/api/admin/posts/{id}/unpin` | 取消置顶 |
| PUT | `/api/admin/posts/{id}/feature` | 设为精华 |
| PUT | `/api/admin/posts/{id}/unfeature` | 取消精华 |

### 管理后台系统

| 功能 | 说明 |
|------|------|
| 数据统计面板 | Dashboard 展示注册用户数、今日新增、今日活跃、帖子总数、今日发帖、评论总数、待处理举报数、封禁用户数 |
| 内容审核 | 管理员可查看所有帖子，支持关键字/状态筛选，可隐藏（status=0）、恢复（status=1）或永久删除帖子 |
| 评论管理 | 管理员可查看所有评论，支持关键字搜索，可直接删除违规评论 |
| 举报处理 | 用户可对帖子/评论/用户提交举报；管理员可采纳举报（同时删除被举报内容）或驳回举报 |
| 用户管理 | 封禁/解封用户（status=0/1）、禁言用户（设置禁言截止时间）、修改角色（user/admin） |

**角色说明：**
- `role = 'user'`（默认）：普通用户，无管理权限
- `role = 'admin'`：管理员，导航栏显示「管理后台」入口，所有 `/api/admin/*` 接口均可访问

**设置管理员（首次需直接操作数据库）：**
```sql
UPDATE user_account SET role = 'admin' WHERE id = 1;
```

#### 管理后台 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/stats` | 数据统计面板 |
| GET | `/api/admin/users` | 用户列表，参数 `keyword`、`status`、`page`、`limit` |
| PUT | `/api/admin/users/{id}/ban` | 封禁用户 |
| PUT | `/api/admin/users/{id}/unban` | 解封用户 |
| PUT | `/api/admin/users/{id}/mute` | 禁言用户，body: `{"minutes": 60}` |
| PUT | `/api/admin/users/{id}/role` | 修改角色，body: `{"role": "admin"}` |
| GET | `/api/admin/posts` | 帖子列表（管理视角），参数 `keyword`、`status`、`page`、`limit` |
| PUT | `/api/admin/posts/{id}/hide` | 隐藏帖子 |
| PUT | `/api/admin/posts/{id}/restore` | 恢复帖子 |
| DELETE | `/api/admin/posts/{id}` | 永久删除帖子 |
| GET | `/api/admin/comments` | 评论列表，参数 `keyword`、`page`、`limit` |
| DELETE | `/api/admin/comments/{id}` | 删除评论 |
| GET | `/api/admin/reports` | 举报列表，参数 `status`（0=待处理）、`page`、`limit` |
| PUT | `/api/admin/reports/{id}/handle` | 处理举报，body: `{"action": "approve/dismiss", "note": "…"}` |
| POST | `/api/reports` | 提交举报（需登录），body: `{"targetType":"POST","targetId":1,"reason":"spam","description":"…"}` |

#### 前端页面

| 路由 | 页面 | 说明 |
|------|------|------|
| `/admin` | 管理后台 | 仅管理员可访问（需登录），含数据概览 / 帖子审核 / 评论管理 / 举报处理 / 用户管理五个 Tab |

数据库表（启动时自动创建）：
- `content_report` — 用户举报记录（含目标类型、原因、处理状态）

数据库新增列（启动时通过 ALTER TABLE 自动添加）：
- `user_account.role` — 用户角色（VARCHAR 20，默认 'user'）
- `user_account.mute_until` — 禁言截止时间（DATETIME，可为 NULL）

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
