package org.kiteseven.kiteuniverse.support.community;

import org.kiteseven.kiteuniverse.mapper.CommunityBoardMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityCommentMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityPostMapper;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityBoard;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityComment;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityPost;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Ensures the community tables and baseline seed data exist after startup.
 */
@Component
@Order(100)
public class CommunityDataInitializer implements ApplicationRunner {

    /**
     * Enabled status flag used by boards, posts, and comments.
     */
    private static final int STATUS_ENABLED = 1;

    private final JdbcTemplate jdbcTemplate;
    private final CommunityBoardMapper communityBoardMapper;
    private final CommunityPostMapper communityPostMapper;
    private final CommunityCommentMapper communityCommentMapper;

    public CommunityDataInitializer(JdbcTemplate jdbcTemplate,
                                    CommunityBoardMapper communityBoardMapper,
                                    CommunityPostMapper communityPostMapper,
                                    CommunityCommentMapper communityCommentMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.communityBoardMapper = communityBoardMapper;
        this.communityPostMapper = communityPostMapper;
        this.communityCommentMapper = communityCommentMapper;
    }

    /**
     * Creates tables and seeds default community content when the tables are empty.
     *
     * @param args startup arguments
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        createCommunityBoardTable();
        createCommunityPostTable();
        createCommunityCommentTable();
        createCommunityPostFavoriteTable();
        createCommunityPostLikeTable();
        createCommunityCommentLikeTable();
        createUserFollowTable();
        createNotificationTable();
        addPostLikeCountColumnIfMissing();
        addCommentLikeCountColumnIfMissing();
        addPostAiGeneratedColumnIfMissing();
        addPostGalleryImagesColumnIfMissing();
        createGameAccountTable();
        createGameCharacterRecordTable();
        createGameStatsTable();
        createUserPointsTable();
        createDailyCheckInTable();
        createUserBadgeTable();
        createPrivateMessageTable();
        createContentReportTable();
        addUserRoleColumnIfMissing();
        addUserMuteUntilColumnIfMissing();
        addCommentParentIdColumnIfMissing();
        seedBoardsIfNecessary();
        seedPostsIfNecessary();
        seedCommentsIfNecessary();
    }

    /**
     * Creates the community-board table.
     */
    private void createCommunityBoardTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_board (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(64) NOT NULL,
                    slug VARCHAR(64) NOT NULL,
                    tag_name VARCHAR(32) NOT NULL,
                    description VARCHAR(255) NOT NULL,
                    sort_order INT NOT NULL DEFAULT 0,
                    status TINYINT NOT NULL DEFAULT 1,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_community_board_slug UNIQUE (slug)
                )
                """);
    }

    /**
     * Creates the community-post table.
     */
    private void createCommunityPostTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_post (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    board_id BIGINT NOT NULL,
                    author_id BIGINT NULL,
                    title VARCHAR(120) NOT NULL,
                    summary VARCHAR(255) NOT NULL,
                    content TEXT NOT NULL,
                    badge VARCHAR(32) NULL,
                    status TINYINT NOT NULL DEFAULT 1,
                    is_featured TINYINT NOT NULL DEFAULT 0,
                    is_pinned TINYINT NOT NULL DEFAULT 0,
                    view_count INT NOT NULL DEFAULT 0,
                    comment_count INT NOT NULL DEFAULT 0,
                    favorite_count INT NOT NULL DEFAULT 0,
                    published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    /**
     * Creates the community-comment table.
     */
    private void createCommunityCommentTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_comment (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    post_id BIGINT NOT NULL,
                    author_id BIGINT NULL,
                    content VARCHAR(1000) NOT NULL,
                    status TINYINT NOT NULL DEFAULT 1,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    /**
     * Creates the community-post-like table.
     */
    private void createCommunityPostLikeTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_post_like (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    post_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    status TINYINT NOT NULL DEFAULT 1,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_community_post_like UNIQUE (post_id, user_id)
                )
                """);
    }

    /**
     * Creates the community-comment-like table.
     */
    private void createCommunityCommentLikeTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_comment_like (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    comment_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    status TINYINT NOT NULL DEFAULT 1,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_community_comment_like UNIQUE (comment_id, user_id)
                )
                """);
    }

    /**
     * Creates the user-follow table.
     */
    private void createUserFollowTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_follow (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    follower_id BIGINT NOT NULL,
                    following_id BIGINT NOT NULL,
                    status TINYINT NOT NULL DEFAULT 1,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_user_follow UNIQUE (follower_id, following_id)
                )
                """);
    }

    /**
     * Adds the is_ai_generated column to community_post if it does not yet exist.
     */
    private void addPostAiGeneratedColumnIfMissing() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE community_post ADD COLUMN is_ai_generated TINYINT NOT NULL DEFAULT 0"
            );
        } catch (Exception ignored) {
            // Column already exists.
        }
    }

    /**
     * Adds the gallery_images column to community_post if it does not yet exist.
     */
    private void addPostGalleryImagesColumnIfMissing() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE community_post ADD COLUMN gallery_images TEXT NULL"
            );
        } catch (Exception ignored) {
            // Column already exists.
        }
    }

    /**
     * Adds the like_count column to community_post if it does not yet exist.
     */
    private void addPostLikeCountColumnIfMissing() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE community_post ADD COLUMN like_count INT NOT NULL DEFAULT 0 AFTER favorite_count"
            );
        } catch (Exception ignored) {
            // Column already exists.
        }
    }

    /**
     * Adds the like_count column to community_comment if it does not yet exist.
     */
    private void addCommentLikeCountColumnIfMissing() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE community_comment ADD COLUMN like_count INT NOT NULL DEFAULT 0"
            );
        } catch (Exception ignored) {
            // Column already exists.
        }
    }

    /**
     * Creates the notification table.
     */
    private void createNotificationTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS notification (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    recipient_id BIGINT NOT NULL,
                    sender_id BIGINT NULL,
                    type VARCHAR(32) NOT NULL,
                    post_id BIGINT NULL,
                    comment_id BIGINT NULL,
                    content VARCHAR(500) NOT NULL,
                    is_read TINYINT NOT NULL DEFAULT 0,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_notification_recipient (recipient_id, is_read, create_time)
                )
                """);
    }

    /**
     * Creates the community-post-favorite table.
     */
    private void createCommunityPostFavoriteTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_post_favorite (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    post_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    status TINYINT NOT NULL DEFAULT 1,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_community_post_favorite UNIQUE (post_id, user_id)
                )
                """);
    }

    /**
     * Seeds default boards when the board table is empty.
     */
    private void seedBoardsIfNecessary() {
        if (communityBoardMapper.countAll() > 0L) {
            return;
        }

        insertBoard("虚空情报站", "void-intel", "官方动态", "汇总版本公告、热修说明、限时活动与前瞻内容。", 10);
        insertBoard("回廊讨论区", "corridor-discussion", "玩家交流", "围绕跑图体验、职业构建、遗物搭配与剧情感想展开讨论。", 20);
        insertBoard("攻略与遗物志", "guides-and-relics", "策略沉淀", "沉淀职业指南、遗物数据库、天命通关记录与长期参考内容。", 30);
        insertBoard("同人与展示", "fanworks-and-showcase", "创作社区", "展示同人画作、跑图记录截图、自制 MOD 与社区共创内容。", 40);
    }

    /**
     * Seeds default posts when the post table is empty.
     */
    private void seedPostsIfNecessary() {
        if (communityPostMapper.countAll() > 0L) {
            return;
        }

        insertPost("void-intel",
                "1.4 版本「裂隙之冬」限时活动今日开启",
                "本次限时活动带来全新精英敌人「霜凝游荡者」、专属遗物池与登录奖励，活动期间行动力上限临时提升至 280。",
                """
## 活动时间

即日起至 2026-05-10 维护前。

## 新增内容

**精英敌人：霜凝游荡者**
出没于第二幕随机节点。携带「极寒护甲」被动——每次受到物理伤害时自动叠加 1 层格挡，层数上限 4 层。建议优先使用法术或真实伤害打穿护甲层，否则后期压力极大。

**限时遗物池：冬息碎片**
活动期间所有精英战斗完成后额外掉落 1 枚冬息碎片，累计 10 枚可在商店兑换本期限定传说遗物「极夜棱镜」。效果：每打出一张消耗牌，对所有敌人附加 1 层灼烧。

**登录奖励**
连续登录 7 天可领取虚空碎片×600 与限定头像框「裂隙旅人」。

## 建议节奏

- 前三天优先打精英节点积攒冬息碎片；
- 第四天起根据手头构建判断是否兑换「极夜棱镜」，该遗物在术士和缚魂中均有强力协同；
- 行动力不要囤着，当前回廊生成概率略有提升，适合多跑几局测试新构建。

有通关「霜凝游荡者」的小技巧欢迎在评论区分享。""",
                "特别企划",
                1,
                1,
                4280,
                0,
                31,
                LocalDateTime.now().minusHours(1L));
        insertPost("corridor-discussion",
                "新手选职业指南：第一局选哪个最顺手",
                "四个职业风格差异很大，这篇从操作难度、容错空间和学习曲线三个维度做了对比，帮你找到最适合入门的起点。",
                """
## 简要对比

| 职业 | 操作难度 | 容错空间 | 推荐程度 |
|------|----------|----------|----------|
| 刃影 | ★★☆ | 高 | 新手首选 |
| 钢躯 | ★☆☆ | 极高 | 新手友好 |
| 术士 | ★★★ | 中 | 进阶推荐 |
| 缚魂 | ★★★★ | 低 | 老手挑战 |

## 各职业入门体感

**刃影**：最直觉的职业。攻击牌打出就叠连击层，达到阈值后触发爆发，核心循环一目了然。遗物池和卡牌池都很宽容，遇到的绝大多数遗物都能用上。新手期最常见的失误是过度追求连击爆发而忽略格挡，意识到这点之后上手很快。

**钢躯**：血量是四职最厚的，护甲会累积而不在回合末清零，打错一两张牌也很难立刻被打死，给了新手大量思考时间。弱点是输出偏慢，面对高装甲精英时需要一些格挡转伤的组合思路，不然会拖很长。

**术士**：爆发潜力最高，但法球机制需要理解「回合结束自动触发」的节奏，不能像刃影那样直线打。推荐在刃影或钢躯跑了 5 局以上、熟悉基础意图阅读之后再切。

**缚魂**：牺牲灵体的时机判断是全职业最难的决策树，强烈建议在已经理解精英战斗机制后再挑战。

## 结论

第一局不追求高天命的话，**刃影或钢躯都是很好的起点**，打死了多试几次找到手感比纠结选哪个更重要。""",
                "新手入门",
                1,
                0,
                2340,
                0,
                19,
                LocalDateTime.now().minusHours(3L));
        insertPost("guides-and-relics",
                "从 A0 到 A10：天命进阶节奏与遗物优先级完整指南",
                "整理了从标准难度到 A10 的核心差异、每级新增的惩罚机制，以及不同职业应对高天命的遗物取舍思路。",
                """
## 天命等级变化概览

**A0–A3**：无额外惩罚，建立对四职基础构建路线的认知。核心目标是理解「精简卡组」的意义——每层营地优先移除弱牌而非升级。

**A4–A6**：首领开始附带强化词条。裂隙先驱在 A5 后获得「每次使用裂痕标记后额外施加 1 层易伤」，需要格外注意标记层数管理。

**A7–A9**：商店价格上浮 15%，金币管理变得更关键。建议从 A7 起养成「每层记录金币收支」的习惯，优先给核心牌留预算，不要在前两层乱买。

**A10**：精英敌人全面附加随机强化词条，每次遭遇精英前无法预判词条类型。这是一个重要门槛，推荐用刃影连击流或钢躯厚血流先通过 A10，再考虑更高难度。

## 各职业遗物优先级（A5–A10 参考）

### 刃影
- 必争：「断刃环」（连击触发时额外抽 1 张牌）、「暗影刀鞘」（消耗牌触发时叠 2 层连击）
- 次选：任何力量类或额外能量类遗物
- 规避：单次大额格挡遗物（刃影不走格挡路线）

### 钢躯
- 必争：「铸铁心脏」（护甲超量部分在每回合开始时转化为格挡）、「熔岩壳」（受到攻击时有 20% 概率反弹等量伤害）
- 次选：额外行动力 / 减费遗物
- 规避：「灵魂誓约」（牺牲生命值换取增益）

### 术士
- 必争：「幻相碎珠」（每有一个法球时，所有攻击额外造成 1 点法术伤害）
- 次选：法球上限提升类遗物

### 缚魂
- 必争：任何灵体召唤速度加成 / 牺牲增效遗物
- 规避：「能量壶」（缚魂本身能量相对宽裕，浪费遗物槽）

## 结语

A10 之前最大的误区是「把所有卡牌都升级」。精简优先于升级，留几张关键牌在手牌率高的状态比把整副牌都升到普通强度有效得多。""",
                "攻略",
                1,
                0,
                3170,
                0,
                44,
                LocalDateTime.now().minusHours(7L));
        insertPost("fanworks-and-showcase",
                "第 347 次尝试终于 A15 全通——附跑图截图与遗物列表",
                "从 A12 卡关到 A15，中间无数次死在第二幕精英和虚空伶官上，这次终于把正确的遗物顺序和打牌节奏理清楚了。",
                """
## 职业：术士 / 天命 A15

本次终于把 A15 打穿了，分享一下全程关键遗物组合和几个差点翻车的节点。

## 核心遗物组合

1. **幻相碎珠**（第一幕精英掉落）：每法球额外 1 点法术伤害，是本局构建的基石
2. **虚空回响**（商店购入）：每回合开始时随机生成 1 个法球，大幅提升法球密度
3. **时间棱镜**（第二幕事件获取）：每打出第 5 张牌时重置能量，让单回合打出 8–10 张牌成为可能
4. **极夜棱镜**（1.4 限时活动兑换）：每张消耗牌触发 1 层灼烧，与我的 3 张「以太消耗牌」形成无限循环

## 差点翻车的节点

**第二幕精英·虚影傀儡**：我的伤害分散在法球触发，主体面板不高，被分身拖了太多回合。下次对付这个精英要提前备一张单目标高倍率牌。

**虚空伶官（Boss）**：幕帷机制在我这个构建里反而有利——幕帷关闭的那一回合我可以直接打掉 1/3 血量，三次幕帷周期就结束了。

## 小结

A15 的核心不是卡组有多花，而是能不能在遗物顺序乱掉的情况下保持核心循环。有兴趣讨论遗物搭配的可以在评论区继续聊。""",
                "天命挑战",
                1,
                0,
                1890,
                0,
                27,
                LocalDateTime.now().minusHours(11L));
        insertPost("void-intel",
                "1.4.1 热修说明与行动力补偿",
                "本次热修修复了「时间棱镜」与特定消耗牌组合触发的无限重置漏洞，以及第二幕地图生成偶发缺少商店节点的问题。",
                """
## 修复内容

- **遗物漏洞**：「时间棱镜」在特定条件下（以太牌 + 连续触发第 5 张计数重置）会进入无限能量状态，本次已修复计数器在以太牌触发时不重置的问题。受影响的历史成绩不会回溯处理。
- **地图生成**：第二幕极少数情况下会生成没有商店节点的路线布局，导致玩家无法补充药水或购买关键卡牌。已修复生成算法，确保每幕至少包含一个商店节点。
- **文本修正**：「极寒护甲」描述文本中「最多叠加 4 层」改为「最多叠加 4 层格挡」，与实际行为保持一致。

## 补偿

由于热修导致短暂停机，向所有受影响时段在线的玩家补偿**行动力×60 与虚空碎片×100**，将通过游戏邮件发放，有效期 7 天。

如发现其他异常欢迎在评论区反馈，附上复现步骤和天命等级有助于我们更快定位问题。""",
                "维护公告",
                0,
                0,
                1140,
                0,
                11,
                LocalDateTime.now().minusDays(1L));
        insertPost("corridor-discussion",
                "缚魂「灵体牺牲流」深度分析——上限极高但容错接近于零",
                "从灵体召唤节奏、牺牲时机到与中毒叠加的协同逻辑，整理了一套相对成熟的操作框架。",
                """
## 为什么缚魂这么难

缚魂的核心决策在每一回合都存在——「现在该不该牺牲灵体」。牺牲能触发爆发，但灵体数量决定了防御屏障和后续召唤节奏。牺牲太早，下一波攻击裸面承伤；牺牲太晚，爆发窗口丢失，高天命精英直接把你打死。

## 标准操作框架

**召唤节奏**：每回合开始时优先打出召唤牌，将场上灵体维持在 3–4 个。低于 2 个时不要主动牺牲，除非当前回合能立刻补满。

**牺牲时机**：
1. 敌人意图为「防御」或「增益」时牺牲——该回合不受攻击，可以安全爆发。
2. 敌人意图为「弱攻击（≤12 伤）」时，视当前格挡量决定，通常可以承受。
3. 敌人意图为「强攻击（≥20 伤）」时，不要牺牲，优先格挡或留手。

**与中毒协同**：缚魂有若干张「灵体消散时对所有敌人附加 X 层中毒」的牌，这是本职业最强的群体输出路线之一。配合「毒瓶」遗物（中毒每叠加 3 层触发额外 1 层灼烧），前期积累足够层数后中后期的精英几乎不需要直接攻击。

## 遗物优先级

- **必须**：「灵魂共鸣」（召唤灵体时有 30% 概率额外多召唤 1 个）
- **强力**：「消逝之证」（牺牲灵体时回复 2 点能量）
- **避开**：所有降低手牌上限的遗物——缚魂需要持续保有足够的牌来维持召唤节奏

构建思路有疑问的可以在评论区讨论，我会持续更新这篇。""",
                "缚魂",
                0,
                0,
                870,
                0,
                8,
                LocalDateTime.now().minusDays(2L));
    }

    /**
     * Seeds default comments when the comment table is empty.
     */
    private void seedCommentsIfNecessary() {
        if (communityCommentMapper.countAll() > 0L) {
            return;
        }

        insertComment(1L, "活动奖励那块整理得很直观，我这种每天只上来清体力的人也能一眼看懂先换什么。");
        insertComment(1L, "联合作战分阶段开放这点太关键了，我刚好一直在纠结首周要不要把门票留着。");
        insertComment(2L, "这套开荒思路很稳，尤其是先养一队再补功能位这点，能少走很多弯路。");
    }

    /**
     * Inserts a default board record.
     *
     * @param name board name
     * @param slug board slug
     * @param tagName board tag
     * @param description board description
     * @param sortOrder board sort order
     */
    private void insertBoard(String name, String slug, String tagName, String description, int sortOrder) {
        CommunityBoard communityBoard = new CommunityBoard();
        communityBoard.setName(name);
        communityBoard.setSlug(slug);
        communityBoard.setTagName(tagName);
        communityBoard.setDescription(description);
        communityBoard.setSortOrder(sortOrder);
        communityBoard.setStatus(STATUS_ENABLED);
        communityBoardMapper.insert(communityBoard);
    }

    /**
     * Inserts a default post record.
     *
     * @param boardSlug board slug
     * @param title post title
     * @param summary post summary
     * @param content post content
     * @param badge post badge
     * @param featured whether featured
     * @param pinned whether pinned
     * @param viewCount initial view count
     * @param commentCount initial comment count
     * @param favoriteCount initial favorite count
     * @param publishedAt publish time
     */
    private void insertPost(String boardSlug,
                            String title,
                            String summary,
                            String content,
                            String badge,
                            int featured,
                            int pinned,
                            int viewCount,
                            int commentCount,
                            int favoriteCount,
                            LocalDateTime publishedAt) {
        CommunityBoard communityBoard = communityBoardMapper.selectBySlug(boardSlug);
        if (communityBoard == null) {
            return;
        }

        CommunityPost communityPost = new CommunityPost();
        communityPost.setBoardId(communityBoard.getId());
        communityPost.setAuthorId(1L);
        communityPost.setTitle(title);
        communityPost.setSummary(summary);
        communityPost.setContent(content);
        communityPost.setBadge(badge);
        communityPost.setStatus(STATUS_ENABLED);
        communityPost.setFeatured(featured);
        communityPost.setPinned(pinned);
        communityPost.setViewCount(viewCount);
        communityPost.setCommentCount(commentCount);
        communityPost.setFavoriteCount(favoriteCount);
        communityPost.setPublishedAt(publishedAt);
        communityPostMapper.insert(communityPost);
    }

    /**
     * Inserts a default comment record and synchronizes the post comment count.
     *
     * @param postId post id
     * @param content comment content
     */
    private void createGameAccountTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS game_account (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    game_uid VARCHAR(64) NOT NULL,
                    server_name VARCHAR(32) NOT NULL DEFAULT '',
                    in_game_name VARCHAR(64) NOT NULL DEFAULT '',
                    account_level INT NOT NULL DEFAULT 0,
                    status TINYINT NOT NULL DEFAULT 1,
                    bind_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_game_account_user_id (user_id)
                )
                """);
    }

    private void createGameCharacterRecordTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS game_character_record (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    class_id VARCHAR(64) NOT NULL DEFAULT '',
                    class_name VARCHAR(64) NOT NULL,
                    ascension_level INT NOT NULL DEFAULT 0,
                    act_reached INT NOT NULL DEFAULT 0,
                    floor_reached INT NOT NULL DEFAULT 1,
                    score INT NOT NULL DEFAULT 0,
                    key_relic VARCHAR(128) NOT NULL DEFAULT '',
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_game_character_user_id (user_id)
                )
                """);
    }

    private void createGameStatsTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS game_stats (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    action_point INT NOT NULL DEFAULT 0,
                    max_action_point INT NOT NULL DEFAULT 240,
                    void_shards INT NOT NULL DEFAULT 0,
                    account_level INT NOT NULL DEFAULT 0,
                    total_runs INT NOT NULL DEFAULT 0,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_game_stats_user_id UNIQUE (user_id)
                )
                """);
    }

    /**
     * Creates the user-points table.
     */
    private void createUserPointsTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_points (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    points INT NOT NULL DEFAULT 0,
                    level INT NOT NULL DEFAULT 1,
                    total_points_earned INT NOT NULL DEFAULT 0,
                    consecutive_days INT NOT NULL DEFAULT 0,
                    last_check_in_date DATE NULL,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_user_points_user_id UNIQUE (user_id)
                )
                """);
    }

    /**
     * Creates the daily-check-in table.
     */
    private void createDailyCheckInTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS daily_check_in (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    check_in_date DATE NOT NULL,
                    points_earned INT NOT NULL DEFAULT 0,
                    consecutive_days INT NOT NULL DEFAULT 0,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_daily_check_in UNIQUE (user_id, check_in_date)
                )
                """);
    }

    /**
     * Creates the user-badge table.
     */
    private void createUserBadgeTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_badge (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    badge_type VARCHAR(32) NOT NULL,
                    earned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_user_badge UNIQUE (user_id, badge_type)
                )
                """);
    }

    /**
     * Creates the private-message table.
     */
    private void createPrivateMessageTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS private_message (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    sender_id BIGINT NOT NULL,
                    recipient_id BIGINT NOT NULL,
                    content TEXT NOT NULL,
                    is_read TINYINT NOT NULL DEFAULT 0,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_pm_recipient (recipient_id, is_read, create_time),
                    INDEX idx_pm_pair (sender_id, recipient_id, create_time)
                )
                """);
    }

    private void createContentReportTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS content_report (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    reporter_id BIGINT NOT NULL,
                    target_type VARCHAR(20) NOT NULL,
                    target_id BIGINT NOT NULL,
                    reason VARCHAR(64) NOT NULL,
                    description VARCHAR(500) NOT NULL DEFAULT '',
                    status TINYINT NOT NULL DEFAULT 0,
                    handler_id BIGINT NULL,
                    handle_note VARCHAR(500) NOT NULL DEFAULT '',
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_report_status (status, create_time),
                    INDEX idx_report_reporter (reporter_id)
                )
                """);
    }

    private void addUserRoleColumnIfMissing() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE user_account ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'user'");
        } catch (Exception ignored) {
            // Column already exists.
        }
    }

    private void addUserMuteUntilColumnIfMissing() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE user_account ADD COLUMN mute_until DATETIME NULL");
        } catch (Exception ignored) {
            // Column already exists.
        }
    }

    private void addCommentParentIdColumnIfMissing() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE community_comment ADD COLUMN parent_id BIGINT NULL AFTER post_id");
        } catch (Exception ignored) {
            // Column already exists.
        }
    }

    private void insertComment(Long postId, String content) {
        CommunityComment communityComment = new CommunityComment();
        communityComment.setPostId(postId);
        communityComment.setAuthorId(1L);
        communityComment.setContent(content);
        communityComment.setStatus(STATUS_ENABLED);
        communityCommentMapper.insert(communityComment);
        communityPostMapper.incrementCommentCount(postId);
    }
}
