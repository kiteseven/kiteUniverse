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

        insertBoard("版本情报站", "version-intel", "官方内容", "汇总公告、维护说明、前瞻内容与活动节奏。", 10);
        insertBoard("综合讨论区", "general-discussion", "玩家交流", "围绕阵容搭配、活动体验与剧情感想展开讨论。", 20);
        insertBoard("攻略与图鉴", "guides-and-wiki", "资料共建", "沉淀养成建议、数据笔记与长期收藏内容。", 30);
        insertBoard("同人与影音", "fanworks-and-media", "创作社区", "展示绘画、剪辑、MAD 与社区共创内容。", 40);
    }

    /**
     * Seeds default posts when the post table is empty.
     */
    private void seedPostsIfNecessary() {
        if (communityPostMapper.countAll() > 0L) {
            return;
        }

        insertPost("version-intel",
                "4 月星港联合作战正式开启",
                "活动分为巡航清扫、联合作战和补给兑换三段，首周建议先把每日补给和门票拿满。",
                "这次联合作战从今天维护后正式开放，活动商店会分三批解锁。想打得稳一点的话，先把体力留给每日补给和联合作战门票，第二阶段再补高收益关卡。首周奖励里最值得优先换的是限定头像框、养成材料箱和记忆碎片，如果你还在观望阵容，也欢迎把自己的通关配置聊在评论区。",
                "特别企划",
                1,
                1,
                3620,
                0,
                24,
                LocalDateTime.now().minusHours(1L));
        insertPost("general-discussion",
                "新手开荒阵容路线整理",
                "如果你刚进社区，这套路线更偏稳扎稳打：先凑一队能推图的主力，再慢慢补破盾、治疗和对群位。",
                "开荒前几天不要急着把资源铺得太散。优先养一个稳定站场位、一个破盾位，再补一个能兜底的治疗或护盾角色，推图会舒服很多。前期遇到卡关时，先检查技能等级和装备词条，再考虑换人。如果你手里缺核心角色，也可以把现有阵容贴出来，大家一起帮你看替代方案。",
                "玩家热议",
                1,
                0,
                1840,
                0,
                12,
                LocalDateTime.now().minusHours(2L));
        insertPost("guides-and-wiki",
                "新人七日养成路线图",
                "把前期资源投入节奏、角色培养顺序和常见误区做成了结构化指南。",
                "第一天先把主线和基础训练打通，尽快拿到能稳定刷材料的功能入口。第二到第四天主要处理主力队伍的等级、技能和武器突破，第五天开始再考虑补副队和工具人。商店里的通用材料不要一口气全换完，优先换限时、限量且短期真的会用到的部分。新手期最怕平均养成，稳住一队通常比拉高很多半成品更划算。",
                "资料精选",
                1,
                0,
                2680,
                0,
                36,
                LocalDateTime.now().minusHours(6L));
        insertPost("fanworks-and-media",
                "星港日常主题投稿精选",
                "这周的创作区明显被夜巡、港口灯光和雨后天台刷屏了，顺手挑了几篇氛围最好的作品。",
                "最近创作区的整体气氛特别统一，很多作品都在写星港夜色和角色之间那种安静但有温度的日常。挑了几篇个人很喜欢的投稿，有的是偏插画，有的是短视频剪辑，也有把设定和画面节奏结合得很好的短篇。如果你也在做同主题创作，欢迎把自己的稿子接在下面，后面也可以再做一期同主题合集。",
                "创作精选",
                1,
                0,
                1430,
                0,
                18,
                LocalDateTime.now().minusHours(10L));
        insertPost("version-intel",
                "维护补偿与修复说明",
                "本次维护主要处理了活动入口异常、部分文本显示问题以及个别关卡掉落说明不一致的情况。",
                "维护后我们确认修复了活动入口偶发无法跳转、任务列表刷新延迟，以及部分简体文本显示不完整的问题。补偿会通过邮件统一发放，领取期限覆盖整个活动第一周。如果你还遇到异常，可以把设备、网络环境和复现步骤写在评论区，方便大家集中反馈和排查。",
                "维护公告",
                0,
                0,
                920,
                0,
                9,
                LocalDateTime.now().minusDays(1L));
        insertPost("general-discussion",
                "剧情支线讨论串",
                "新支线把几位角色之间的关系写得更近了一步，尤其是最后那段对话，很适合单开一串慢慢聊。",
                "这一段支线最有意思的地方，不是单纯补设定，而是把人物之间原本有点含糊的情绪推得更清楚了。尤其尾声那段对话，信息量不大，却把角色的立场和默契都写出来了。欢迎大家在不剧透主线关键节点的前提下聊聊自己最在意的片段，也可以说说你觉得后续还有哪些伏笔会继续回收。",
                "剧情讨论",
                0,
                0,
                710,
                0,
                5,
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
