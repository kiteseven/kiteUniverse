package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.mapper.CommunityBoardMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.vo.community.BoardSummaryVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostSummaryVO;
import org.kiteseven.kiteuniverse.pojo.vo.content.BoardsPageVO;
import org.kiteseven.kiteuniverse.pojo.vo.content.HomePageVO;
import org.kiteseven.kiteuniverse.service.CommunityContentService;
import org.kiteseven.kiteuniverse.service.CommunityQueryService;
import org.kiteseven.kiteuniverse.support.cache.TwoLevelCache;
import org.kiteseven.kiteuniverse.support.community.CommunityContentCacheKeys;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the homepage and boards-page payloads from live community data.
 */
@Service
public class CommunityContentServiceImpl implements CommunityContentService {

    /**
     * Formatter used by the frontend cards for day-level timestamps.
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M月d日");

    private final CommunityQueryService communityQueryService;
    private final CommunityBoardMapper communityBoardMapper;
    private final UserMapper userMapper;

    public CommunityContentServiceImpl(CommunityQueryService communityQueryService,
                                       CommunityBoardMapper communityBoardMapper,
                                       UserMapper userMapper) {
        this.communityQueryService = communityQueryService;
        this.communityBoardMapper = communityBoardMapper;
        this.userMapper = userMapper;
    }

    /**
     * Loads the homepage payload through the two-level cache layer.
     *
     * @return homepage payload
     */
    @Override
    @TwoLevelCache(
            key = CommunityContentCacheKeys.HOME_PAGE,
            localTtlExpression = "@communityContentProperties.homeLocalCacheSeconds",
            redisTtlExpression = "@communityContentProperties.homeCacheSeconds"
    )
    public HomePageVO getHomePage() {
        return buildHomePage();
    }

    /**
     * Loads the boards-page payload through the two-level cache layer.
     *
     * @return boards-page payload
     */
    @Override
    @TwoLevelCache(
            key = CommunityContentCacheKeys.BOARDS_PAGE,
            localTtlExpression = "@communityContentProperties.boardsLocalCacheSeconds",
            redisTtlExpression = "@communityContentProperties.boardsCacheSeconds"
    )
    public BoardsPageVO getBoardsPage() {
        return buildBoardsPage();
    }

    private HomePageVO buildHomePage() {
        CommunitySnapshot snapshot = buildSnapshot();
        List<PostSummaryVO> featuredPosts = communityQueryService.listFeaturedPosts(3);
        List<PostSummaryVO> latestPosts = communityQueryService.listLatestPosts(3);
        List<BoardSummaryVO> boardSummaries = communityQueryService.listBoardSummaries();

        HomePageVO homePage = new HomePageVO();
        homePage.setHero(buildHomeHero(snapshot, featuredPosts));
        homePage.setHeroMetrics(List.of(
                createHomeMetricCard("社区成员", formatCount(snapshot.totalUsers()), "越来越多的同好在这里交流阵容、剧情和创作灵感"),
                createHomeMetricCard("已发主题", formatCount(snapshot.publishedPosts()), "从版本速递到经验整理，内容正在慢慢沉淀下来"),
                createHomeMetricCard("今日新增", formatCount(snapshot.todayPosts()), "今天刚刚冒出来的新帖子和新话题")
        ));
        homePage.setFeaturedTopics(mapFeaturedTopics(featuredPosts));
        homePage.setQuickSections(buildQuickSections(boardSummaries));
        homePage.setTimeline(mapTimeline(latestPosts));
        homePage.setMoments(List.of(
                createHomeMetricCard("开放版区", formatCount(snapshot.boardCount()), "可以按兴趣直接进入不同方向的讨论分区"),
                createHomeMetricCard("7日活跃", formatCount(snapshot.activeUsersLast7Days()), "最近七天仍在回访和参与讨论的成员"),
                createHomeMetricCard("24小时新帖", formatCount(snapshot.postsLast24Hours()), "近一天里刚刚更新出来的话题节奏")
        ));
        return homePage;
    }

    private BoardsPageVO buildBoardsPage() {
        CommunitySnapshot snapshot = buildSnapshot();
        List<BoardSummaryVO> boardSummaries = communityQueryService.listBoardSummaries();

        BoardsPageVO boardsPage = new BoardsPageVO();
        boardsPage.setHero(buildBoardsHero(snapshot));
        boardsPage.setBoardGroups(mapBoardGroups(boardSummaries));
        boardsPage.setNotices(List.of(
                "发帖前先看看对应分区的讨论方向，标题和摘要写清楚会更容易被看到。",
                "攻略类内容建议带上适用角色、版本或实测条件，后来查阅也会轻松很多。",
                "创作帖欢迎补充灵感来源、制作过程或配图说明，评论区通常更容易聊起来。",
                "如果只是想先抛出问题，也可以从综合讨论区开贴，往往会更快收到回应。"
        ));
        boardsPage.setOverviewCards(List.of(
                createBoardsMetricCard("版块总数", formatCount(snapshot.boardCount()), "目前已经开放浏览的讨论分区"),
                createBoardsMetricCard("帖子总数", formatCount(snapshot.publishedPosts()), "社区里慢慢沉淀下来的内容规模"),
                createBoardsMetricCard("今日新帖", formatCount(snapshot.todayPosts()), "今天刚刚更新出来的新话题"),
                createBoardsMetricCard("活跃用户", formatCount(snapshot.activeUsersLast7Days()), "最近常来回访和参与讨论的成员")
        ));
        return boardsPage;
    }

    private HomePageVO.Hero buildHomeHero(CommunitySnapshot snapshot, List<PostSummaryVO> featuredPosts) {
        PostSummaryVO leadPost = featuredPosts.isEmpty() ? null : featuredPosts.get(0);

        HomePageVO.Hero hero = new HomePageVO.Hero();
        hero.setEyebrow("社区大厅");
        hero.setTitle("把版本情报、玩家创作和互动讨论都收进同一个入口。");
        hero.setDescription("从活动速递到攻略整理，再到玩家创作与闲聊，常逛的内容都能在这里一眼找到。");
        hero.setPrimaryActionLabel("进入版区");
        hero.setPrimaryActionLink("/boards");
        hero.setSecondaryActionLabel("开始创作");
        hero.setSecondaryActionLink("/compose");
        hero.setVisualAlt("社区大厅主视觉");
        hero.setFloatingTitle("本周重点");
        hero.setFloatingValue(leadPost == null ? "本周推荐整理中" : leadPost.getTitle());
        hero.setFloatingDescription(leadPost == null
                ? "热门内容正在陆续整理，稍后再来看看新的推荐。"
                : "本周讨论热度很高的一篇内容，适合先从这里逛起。");
        hero.setSecondaryFloatingTitle("社区规模");
        hero.setSecondaryFloatingValue(formatCount(snapshot.publishedPosts()) + " 帖子");
        hero.setSecondaryFloatingDescription("最近正在更新的帖子、版区和活跃讨论都收在这里。");
        return hero;
    }

    private BoardsPageVO.Hero buildBoardsHero(CommunitySnapshot snapshot) {
        BoardsPageVO.Hero hero = new BoardsPageVO.Hero();
        hero.setEyebrow("版区导航");
        hero.setTitle("从常逛分区开始，慢慢找到属于你的讨论角落。");
        hero.setDescription("这里按内容方向整理了情报、攻略、闲聊和创作版区，方便你快速找到正在升温的话题。");
        hero.setMetrics(List.of(
                createBoardsMetricCard("推荐版区", formatCount(snapshot.boardCount()), "当前开放浏览的讨论分区"),
                createBoardsMetricCard("今日新帖", formatCount(snapshot.todayPosts()), "今天刚刚冒出来的新内容")
        ));
        return hero;
    }

    private List<HomePageVO.TopicCard> mapFeaturedTopics(List<PostSummaryVO> featuredPosts) {
        List<HomePageVO.TopicCard> topicCards = new ArrayList<>();
        for (PostSummaryVO featuredPost : featuredPosts) {
            HomePageVO.TopicCard topicCard = new HomePageVO.TopicCard();
            topicCard.setBadge(StringUtils.hasText(featuredPost.getBadge())
                    ? featuredPost.getBadge()
                    : featuredPost.getBoardTagName());
            topicCard.setTitle(featuredPost.getTitle());
            topicCard.setExcerpt(featuredPost.getSummary());
            topicCard.setAuthor(featuredPost.getAuthorName());
            topicCard.setMeta(formatRelativeDate(featuredPost.getPublishedAt()));
            topicCard.setStats(List.of(
                    "版区 " + featuredPost.getBoardName(),
                    "评论 " + safeCount(featuredPost.getCommentCount()),
                    "热度 " + formatCount(safeCount(featuredPost.getViewCount()))
            ));
            topicCard.setLink(buildPostLink(featuredPost.getId()));
            topicCards.add(topicCard);
        }
        return topicCards;
    }

    private List<HomePageVO.TimelineItem> mapTimeline(List<PostSummaryVO> latestPosts) {
        List<HomePageVO.TimelineItem> timelineItems = new ArrayList<>();
        for (PostSummaryVO latestPost : latestPosts) {
            HomePageVO.TimelineItem timelineItem = new HomePageVO.TimelineItem();
            timelineItem.setTime(formatRelativeDate(latestPost.getPublishedAt()));
            timelineItem.setTitle(latestPost.getTitle());
            timelineItem.setDescription(latestPost.getSummary());
            timelineItem.setLink(buildPostLink(latestPost.getId()));
            timelineItems.add(timelineItem);
        }
        return timelineItems;
    }

    private List<HomePageVO.QuickSection> buildQuickSections(List<BoardSummaryVO> boardSummaries) {
        List<HomePageVO.QuickLink> boardLinks = new ArrayList<>();
        for (BoardSummaryVO boardSummary : boardSummaries) {
            boardLinks.add(createQuickLink(boardSummary.getName(), buildBoardLink(boardSummary.getId())));
        }

        List<HomePageVO.QuickSection> quickSections = new ArrayList<>();
        quickSections.add(createQuickSection("热门版区", boardLinks));
        quickSections.add(createQuickSection("常用入口", List.of(
                createQuickLink("首页精选", "/"),
                createQuickLink("版区总览", "/boards"),
                createQuickLink("个人中心", "/profile"),
                createQuickLink("发布帖子", "/compose")
        )));
        return quickSections;
    }

    private List<BoardsPageVO.BoardCard> mapBoardGroups(List<BoardSummaryVO> boardSummaries) {
        List<BoardsPageVO.BoardCard> boardCards = new ArrayList<>();
        for (BoardSummaryVO boardSummary : boardSummaries) {
            BoardsPageVO.BoardCard boardCard = new BoardsPageVO.BoardCard();
            boardCard.setTag(boardSummary.getTagName());
            boardCard.setTitle(boardSummary.getName());
            boardCard.setDescription(boardSummary.getDescription());
            boardCard.setStats(List.of(
                    "主题 " + formatCount(boardSummary.getTopicCount()),
                    "今日 " + formatCount(boardSummary.getTodayPostCount()),
                    "更新 " + formatRelativeDate(boardSummary.getLatestPublishedAt())
            ));
            boardCard.setUpdate("最新帖子：" + boardSummary.getLatestPostTitle());
            boardCard.setActionLabel("进入版区");
            boardCard.setActionLink(buildBoardLink(boardSummary.getId()));
            boardCards.add(boardCard);
        }
        return boardCards;
    }

    private CommunitySnapshot buildSnapshot() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime last24Hours = now.minusHours(24L);
        LocalDateTime last7Days = now.minusDays(7L);

        return new CommunitySnapshot(
                userMapper.countAll(),
                userMapper.countLastLoginSince(last7Days),
                communityBoardMapper.countAll(),
                communityQueryService.countPublishedPosts(),
                communityQueryService.countPublishedPostsSince(startOfToday),
                communityQueryService.countPublishedPostsSince(last24Hours)
        );
    }

    private HomePageVO.MetricCard createHomeMetricCard(String title, String value, String description) {
        HomePageVO.MetricCard metricCard = new HomePageVO.MetricCard();
        metricCard.setTitle(title);
        metricCard.setValue(value);
        metricCard.setDescription(description);
        return metricCard;
    }

    private BoardsPageVO.MetricCard createBoardsMetricCard(String title, String value, String description) {
        BoardsPageVO.MetricCard metricCard = new BoardsPageVO.MetricCard();
        metricCard.setTitle(title);
        metricCard.setValue(value);
        metricCard.setDescription(description);
        return metricCard;
    }

    private HomePageVO.QuickSection createQuickSection(String title, List<HomePageVO.QuickLink> items) {
        HomePageVO.QuickSection quickSection = new HomePageVO.QuickSection();
        quickSection.setTitle(title);
        quickSection.setItems(items);
        return quickSection;
    }

    private HomePageVO.QuickLink createQuickLink(String label, String link) {
        HomePageVO.QuickLink quickLink = new HomePageVO.QuickLink();
        quickLink.setLabel(label);
        quickLink.setLink(link);
        return quickLink;
    }

    private String buildBoardLink(Long boardId) {
        return boardId == null ? "/boards" : "/boards/" + boardId;
    }

    private String buildPostLink(Long postId) {
        return postId == null ? "/boards" : "/posts/" + postId;
    }

    private String formatCount(long value) {
        if (value >= 10000L) {
            double formattedValue = value / 1000D;
            return String.format("%.1fk", formattedValue);
        }
        return String.valueOf(value);
    }

    private String formatRelativeDate(LocalDateTime time) {
        if (time == null) {
            return "暂无更新";
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        if (time.toLocalDate().isEqual(now.toLocalDate())) {
            return "今天 " + time.toLocalTime().withSecond(0).withNano(0);
        }
        if (time.toLocalDate().isEqual(now.toLocalDate().minusDays(1L))) {
            return "昨天 " + time.toLocalTime().withSecond(0).withNano(0);
        }
        return DATE_FORMATTER.format(time);
    }

    private long safeCount(Integer value) {
        return value == null ? 0L : value.longValue();
    }

    private record CommunitySnapshot(long totalUsers,
                                     long activeUsersLast7Days,
                                     long boardCount,
                                     long publishedPosts,
                                     long todayPosts,
                                     long postsLast24Hours) {
    }
}
