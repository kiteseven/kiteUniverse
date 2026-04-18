package org.kiteseven.kiteuniverse.support.community;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds extra community members and interactions so the default dataset
 * feels closer to a naturally active site.
 */
@Component
@Order(200)
public class CommunityActivitySeeder implements ApplicationRunner {

    /**
     * Shared enabled status used by the sample rows.
     */
    private static final int STATUS_ENABLED = 1;

    /**
     * Placeholder password for SMS-first sample accounts.
     */
    private static final String SEEDED_PASSWORD = "sms-demo-account";

    private final JdbcTemplate jdbcTemplate;

    public CommunityActivitySeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Seeds extra users, posts, and comments after baseline community data exists.
     *
     * @param args startup arguments
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        createUserAccountTableIfNecessary();
        createUserInfoTableIfNecessary();
        seedCommunityMembers();
        normalizeLegacyDemoUsers();
        if (!hasTable("community_board") || !hasTable("community_post") || !hasTable("community_comment")) {
            return;
        }
        if (countRows("community_board") <= 0L) {
            return;
        }
        normalizeLegacyDemoPosts();
        diversifyBaselineAuthors();
        seedAdditionalPosts();
        seedAdditionalComments();
        synchronizeCommentCounts();
    }

    private void createUserAccountTableIfNecessary() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_account (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(64) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    nickname VARCHAR(64),
                    email VARCHAR(128),
                    phone VARCHAR(20),
                    avatar VARCHAR(255),
                    gender TINYINT DEFAULT 0,
                    status TINYINT NOT NULL DEFAULT 1,
                    last_login_time DATETIME,
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_user_account_username UNIQUE (username),
                    CONSTRAINT uk_user_account_email UNIQUE (email),
                    CONSTRAINT uk_user_account_phone UNIQUE (phone)
                )
                """);
    }

    private void createUserInfoTableIfNecessary() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_info (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    real_name VARCHAR(64),
                    birthday DATE,
                    signature VARCHAR(255),
                    profile VARCHAR(500),
                    country VARCHAR(64),
                    province VARCHAR(64),
                    city VARCHAR(64),
                    website VARCHAR(255),
                    background_image VARCHAR(255),
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_user_info_user_id UNIQUE (user_id)
                )
                """);
    }

    private void seedCommunityMembers() {
        ensureMember("starport_editor", "星港编辑部", "13890010001", 0, "上海",
                "负责整理版本情报和维护提醒", "常驻版本情报站，喜欢把零散公告整理成一页看懂的清单。", LocalDateTime.now().minusHours(1L));
        ensureMember("rain_dock_log", "雨港值夜人", "13890010002", 1, "杭州",
                "夜巡路上顺手记一点活动体感", "更爱聊实战手感和活动节奏，看到合适的话题就会留下长评。", LocalDateTime.now().minusHours(3L));
        ensureMember("northwind_plan", "北风计划局", "13890010003", 0, "南京",
                "只做能真正省资源的养成笔记", "习惯把材料、体力和成长节奏拆成表格，方便后来人照着走。", LocalDateTime.now().minusHours(5L));
        ensureMember("tea_break_runner", "茶歇补给员", "13890010004", 2, "苏州",
                "会先替你踩一遍活动兑换路线", "看到商店和活动代币就会手痒，最爱研究哪些材料先换最赚。", LocalDateTime.now().minusHours(9L));
        ensureMember("signal_gull", "海鸥信标", "13890010005", 0, "青岛",
                "喜欢把语音、剧情和角色印象串在一起聊", "常驻综合讨论区，发言不快，但每次都会把想法说完整。", LocalDateTime.now().minusDays(1L).minusHours(2L));
        ensureMember("afterglow_lab", "余晖试验田", "13890010006", 1, "深圳",
                "低练度测试和替代方案爱好者", "比起毕业面板，更关心普通玩家手里的配置能不能照着抄。", LocalDateTime.now().minusDays(1L).minusHours(6L));
        ensureMember("paper_crane_radio", "纸鹤投递员", "13890010007", 2, "成都",
                "看到好看的二创就想顺手做个合集", "经常在创作区搬运灵感，也会记录壁纸、色卡和剪辑推荐。", LocalDateTime.now().minusDays(2L).minusHours(1L));
        ensureMember("dockside_pixel", "港口像素屋", "13890010008", 0, "广州",
                "会把灵感做成壁纸、头像和小动图", "创作速度不快，但很爱折腾画面氛围和角色小表情。", LocalDateTime.now().minusDays(2L).minusHours(4L));
        ensureMember("moonwake_note", "月潮轻声", "13890010009", 2, "武汉",
                "喜欢聊支线剧情里的情绪流向", "更关注角色关系和台词背后的留白，发帖常常会写成长文。", LocalDateTime.now().minusDays(3L).minusHours(2L));
        ensureMember("drift_compass", "漂流罗盘", "13890010010", 1, "重庆",
                "会把主线推进和资源规划一起记下来", "属于边玩边记的那类玩家，擅长把碎片体验整理成路线图。", LocalDateTime.now().minusDays(4L).minusHours(5L));
        ensureMember("slow_wave_archive", "缓潮档案室", "13890010011", 0, "天津",
                "喜欢把容易遗漏的细节整理成短备忘", "经常在攻略和剧情区之间来回跑，看到有用的信息就会顺手记下来。", LocalDateTime.now().minusDays(2L).minusHours(7L));
        ensureMember("porchlight_signal", "岸灯回声", "13890010012", 2, "厦门",
                "截图、壁纸和角色情绪碎片爱好者", "发言不算多，但会认真把自己留意到的画面和台词都收集起来。", LocalDateTime.now().minusDays(5L).minusHours(3L));
    }

    private void normalizeLegacyDemoUsers() {
        refreshExistingUser(3L, "night_shift_log", "夜航巡查笔记", 1, LocalDateTime.now().minusHours(2L),
                "南京", "把活动体验和阵容感受写成巡查记录", "比起追热点，更喜欢把亲手打过的内容慢慢记下来。");
        refreshExistingUser(4L, "dockside_radio", "港口小广播", 2, LocalDateTime.now().minusHours(1L),
                "上海", "早晚都会上来看看社区有没有新话题", "热衷于把零散的配队体验和趣味发现汇总成短帖。");
    }

    private void normalizeLegacyDemoPosts() {
        updateLegacyPost(7L, "今天商店兑换别忘了领巡航票",
                "如果你今晚才上线，先去活动页把巡航票和限时补给领完，再决定体力怎么花。",
                "很多人容易先把体力清掉，结果回头才发现今天的巡航票还没领。保险一点的顺序是先去活动页收掉每日巡航票、补给箱和限时任务，再看自己剩下的体力要投在哪个阶段。这样至少不会出现代币花完了、票却还没拿的情况。",
                1L, 3L, 218, 7, LocalDateTime.now().minusMinutes(52L));
        updateLegacyPost(8L, "早班车配队实测：双破盾比我想象里稳",
                "今早拿两套常见低配阵容跑了一轮，结论是双破盾的容错确实比纯输出更舒服。",
                "本来只是想试试低练度能不能吃满早班车那几段伤害，没想到双破盾体系比想象里稳定很多。虽然结算时间会慢一点，但对手速和轴要求低，失误了也能拉回来。如果你手里正好缺一名主C，不妨先用稳一点的思路过渡，等后面再慢慢补输出位。",
                2L, 4L, 246, 9, LocalDateTime.now().minusMinutes(35L));
        updateLegacyComment(1L, 1L, resolveUserIdByUsername("tea_break_runner"),
                "活动奖励那块整理得很直观，我这种每天只上来清体力的人也能一眼看懂先换什么。", LocalDateTime.now().minusMinutes(56L));
        updateLegacyComment(2L, 1L, resolveUserIdByUsername("rain_dock_log"),
                "联合作战分阶段开放这点太关键了，我刚好一直在纠结首周要不要把门票留着。", LocalDateTime.now().minusMinutes(49L));
        updateLegacyComment(3L, 2L, resolveUserIdByUsername("afterglow_lab"),
                "这套开荒思路很稳，尤其是先养一队再补功能位这点，能少走很多弯路。", LocalDateTime.now().minusHours(1L).minusMinutes(18L));
        updateLegacyComment(4L, 7L, 3L,
                "我刚刚就是先把体力打空了才想起巡航票，多亏这条提醒，不然今天又要漏。", LocalDateTime.now().minusMinutes(41L));
        updateLegacyComment(5L, 8L, 4L,
                "双破盾这套我也试了，手感确实稳很多，尤其适合上班前那种没时间反复凹的场景。", LocalDateTime.now().minusMinutes(28L));
    }

    private void diversifyBaselineAuthors() {
        assignAuthorToPost("4 月星港联合作战正式开启", "starport_editor");
        assignAuthorToPost("新手开荒阵容路线整理", "night_shift_log");
        assignAuthorToPost("新人七日养成路线图", "northwind_plan");
        assignAuthorToPost("星港日常主题投稿精选", "dockside_pixel");
        assignAuthorToPost("维护补偿与修复说明", "starport_editor");
        assignAuthorToPost("剧情支线讨论串", "moonwake_note");
    }

    private void seedAdditionalPosts() {
        ensurePost("周末补给站开放提醒：体力别急着全花", "void-intel", "starport_editor",
                "本周补给站会在周五晚间开启，先把每日代币和限时兑换看清楚，体力才不容易浪费。",
                "这周补给站的节奏和之前不太一样，第一晚最值得先拿的是限时票券和能补主力练度的两档材料。体力如果一上来就全投进常驻本，后面很容易发现自己缺的是活动代币。保险一点的做法是先清每日，再看自己缺的是突破素材还是活动兑换。要是你已经排好了本周体力分配，也欢迎把自己的取舍顺手贴出来。",
                "版本速递", 1, 0, 768, 22, LocalDateTime.now().minusMinutes(45L));
        ensurePost("夜巡活动里最好用的通用配队是哪几套？", "corridor-discussion", "night_shift_log",
                "我先抛砖引玉列了三套常见低配思路，大家也可以把自己顺手的组合继续接在下面。",
                "这次夜巡活动我来回试了不少配置，最后留下来的还是那几套容错够高、换人也方便的组合。一个是双破盾加站场主C，一个是单核输出配治疗和挂件，另一个是专门给低练度准备的均衡队。如果你已经打完活动，可以聊聊哪一队最顺手；如果还在卡关，也可以直接贴角色池，大家一起看有没有现成能抄的配置。",
                "玩家热议", 1, 0, 652, 18, LocalDateTime.now().minusMinutes(82L));
        ensurePost("低练度也能抄的资源规划表，按周拆给大家", "guides-and-relics", "northwind_plan",
                "把一周体力、商店兑换和角色培养拆成了更容易照抄的顺序，适合想稳一点推进的新玩家。",
                "这份表最大的目标不是追求毕业速度，而是尽量减少浪费。前两天优先保证主线推进和主力练度，第三到第五天开始补武器和核心技能，到了周末再看活动商店有没有必须先换的东西。如果你本周正好在补副队，也可以把体力从常驻素材里挪一部分给活动代币，但前提还是主力队伍得先站稳。",
                "资料整理", 1, 0, 834, 29, LocalDateTime.now().minusMinutes(118L));
        ensurePost("把港口夜景做成了动态壁纸，放一个预览", "fanworks-and-showcase", "dockside_radio",
                "昨晚把角色待机画面和港口灯带重新拼了一遍，做了一版偏安静氛围的动态壁纸。",
                "这版壁纸主要想保留港口夜色那种风吹过去会轻轻晃动的感觉，所以没有堆太多特效。角色动作也故意压得很轻，只让发梢和外套有一点点呼吸感。先放个预览版，如果大家觉得这个方向还不错，后面我再补一版手机尺寸和更亮一点的版本。",
                "创作分享", 0, 0, 541, 31, LocalDateTime.now().minusHours(3L));
        ensurePost("把支线里的港口广播词单独抄出来了，越看越有味道", "corridor-discussion", "moonwake_note",
                "支线结尾那段广播词我单独整理了一遍，发现很多情绪其实都埋在停顿和措辞里。",
                "回头重看这段广播词的时候，最打动我的不是信息量，而是它故意留出来的那些空白。很多句子看起来像在说港口夜班和值守安排，实际上又轻轻碰到了人物当下的心境。我把原文里的几句重点都摘出来了，想看看大家有没有在别处也注意到同样的写法。",
                "剧情摘录", 0, 0, 428, 16, LocalDateTime.now().minusHours(4L).minusMinutes(15L));
        ensurePost("做了一套夜航主题头像框配色，先放三张预览", "fanworks-and-showcase", "paper_crane_radio",
                "用夜港灯带和角色衣装里的蓝灰色重新拼了几版头像框，先挑了三张顺眼的预览图。",
                "这一套主要想做出那种夜航时窗边有冷光掠过去的感觉，所以整体配色压得比较低，只把边缘和小装饰提亮了一点。现在先放三张预览，如果大家觉得方向对，后面我再把透明底和手机裁切版一起整理出来，方便直接拿去做社交头像或者社区签名图。",
                "创作草稿", 0, 0, 367, 14, LocalDateTime.now().minusHours(6L).minusMinutes(40L));
        // --- additional diverse posts ---
        ensurePost("刃影满命和半命的实战差距有多大？我测了一轮", "corridor-discussion", "afterglow_lab",
                "专门拿了两套练度相近的刃影对比跑了几段内容，把数字和手感差异都整理出来了。",
                "很多人问半命刃影值不值得继续氪。我的结论是：对于大多数常规关卡，半命到满命的提升感知没有那么明显，决定输出天花板的往往是遗物词条和技能优先级。真正拉开差距的是第三命和第五命，因为这两个节点改变了技能循环的逻辑。如果你现在是两命或四命，先把遗物补齐比继续抽命座性价比高很多。实测数据我整理在正文表格里，可以直接对号入座看自己的阶段适合哪种策略。",
                "刃影", 0, 0, 912, 34, LocalDateTime.now().minusDays(1L).minusHours(3L));
        ensurePost("活动代币兑换优先级速查表（本期更新）", "void-intel", "tea_break_runner",
                "每期都有人问先换什么，我把这期商店拆成必换、看情况和可以跳过三档，一眼看懂。",
                "必换档：限时突破素材、技能书×2、遗物选择箱；这三类不换就是亏。看情况档：通用材料箱和体力药水，取决于你本周是否有大体力计划。可以跳过档：装扮碎片和收藏品，这类等下期折扣或者攒代币更稳。补充一句：头像框不要急着换，上周有人反馈下期会出更好看的限定款，等等比较好。",
                "活动速查", 1, 0, 1043, 47, LocalDateTime.now().minusDays(1L).minusHours(7L));
        ensurePost("回廊三层以上遗物搭配思路汇总", "guides-and-relics", "drift_compass",
                "整理了目前玩家反馈最多的高层遗物组合，按职业分开列了每套的优先词条和常见替代方案。",
                "高层遗物没有完美答案，但有几套组合在社区内反复被提起。术士系优先爆伤和元素强度，遗物主词条上暴击率保持在 60% 以上再堆输出。刃影系更看重破防触发效率，所以攻速和暴击触发比单纯堆面板更重要。钢躯系的核心是减伤阈值，推荐配一件固定生命上限词条的饰品槽遗物。每套搭配我都列了三件套和两件套的过渡方案，新进高层的朋友可以先照着过渡方案走。",
                "遗物指南", 1, 0, 758, 41, LocalDateTime.now().minusDays(2L).minusHours(1L));
        ensurePost("新版本剧情主线通关感受，不含剧透", "corridor-discussion", "signal_gull",
                "刚打完主线新章节，整体节奏和上一章差异挺大，这里聊聊体感，故事内容不透。",
                "这一章的叙事节奏明显慢下来了，前半段几乎没有战斗高潮，更多在铺人物关系。我个人很喜欢这种处理方式，因为这一章要讲的核心矛盾必须靠积累才能爆发。中段有一个场景是我在整个游戏里印象最深的，建议那个地方把音量调高一点，原声配乐在那里的设计很细。Boss 战整体难度适中，机制比上一章清晰，第一次打大概率能过。",
                "主线讨论", 0, 0, 634, 28, LocalDateTime.now().minusDays(2L).minusHours(5L));
        ensurePost("用游戏截图拼了一张角色关系图，连线附注释", "fanworks-and-showcase", "porchlight_signal",
                "把现有主线和支线里登场过的角色单独截了图，按照剧情关系连线，附上我自己的解读注释。",
                "做这张图主要是因为我自己玩到后来有点混淆支线里的人物关系，于是边整理边把思路写下来。连线分三类：实线是有直接对话的已知关系，虚线是推测或者间接提及，波浪线是目前存疑或者可能有多重解读的部分。注释我写得比较主观，欢迎大家在评论里指出我没注意到的细节，或者提不同的解读角度。",
                "角色情报", 0, 0, 489, 22, LocalDateTime.now().minusDays(3L).minusHours(2L));
        ensurePost("缚魂职业入门指南：选择缚魂的理由和基础打法", "guides-and-relics", "slow_wave_archive",
                "缚魂很容易被误解成纯辅助，这篇说说它在什么阵容里最不可替代，以及基本技能循环。",
                "很多新手看到缚魂的输出数字就觉得不够硬，但缚魂的价值在于它同时处于两个层面：控场和增益叠加。标准打法是先挂上限制效果锁住目标行动顺序，再进入增益循环放大队友核心技能的收益。缚魂独自存在的时候价值有限，但只要队里有一名依赖技能触发的主C，缚魂的贡献就会被放大两到三倍。入门推荐先练习技能释放节奏，找到增益窗口的感觉比死记打法公式更重要。",
                "缚魂", 1, 0, 671, 38, LocalDateTime.now().minusDays(3L).minusHours(8L));
        ensurePost("周年庆前瞻情报汇总，官方已确认的部分", "void-intel", "starport_editor",
                "官方渠道目前已经放出来的周年庆信息我整理了一遍，没有猜测内容，只写已确认的部分。",
                "已确认：周年庆期间限定签到奖励池开启，包含材料礼包和一次性选择器；限定活动关卡共三期，每期持续两周；官方直播定档在周年庆首日，预计有新角色情报和版本路线图公布。未确认但官方暗示过：据开发者日志透露下半年有一次较大的遗物系统调整，具体内容留到直播说。现在只适合把体力和资源留着，别在周年庆前两周把积蓄花空。",
                "周年前瞻", 1, 0, 1287, 63, LocalDateTime.now().minusDays(4L).minusHours(2L));
        ensurePost("跑图五连败之后我想清楚了一件事", "corridor-discussion", "rain_dock_log",
                "连续翻车之后重新复盘，发现自己一直在用错误的方式评估遗物词条优先级。",
                "连输五局的时候我很沮丧，以为是运气问题。复盘之后才发现问题出在选遗物的思路上：我一直优先选面板数字最高的词条，但在当前职业体系下，触发型词条的实际收益远超静态属性。换了一套选遗物的逻辑之后，同样的起手包我开始能稳到中后期。总结一句：先想清楚自己的胜利条件，再往那个方向找词条，比拿到什么就堆什么更有效。",
                "心得随笔", 0, 0, 523, 19, LocalDateTime.now().minusDays(1L).minusHours(9L));
        ensurePost("给即将进高层的朋友：我踩过的几个坑", "guides-and-relics", "afterglow_lab",
                "进高层之前我犯了好几个本来可以避开的错误，整理出来希望能给后来者省点弯路。",
                "坑一：以为低层稳定就代表高层也能照搬配队，实际上高层对职业定位的要求更严格。坑二：体力花太多在补命座上，忽略了遗物才是高层门槛的核心。坑三：低估了理解 Boss 机制的重要性，前期用硬打代替理解，到高层之后这个习惯会直接导致翻车。建议进高层之前把目标职业的技能循环吃透，再看看高层常见 Boss 的伤害来源，这两步做好之后带中等遗物进去也能走得不错。",
                "进阶攻略", 1, 0, 845, 52, LocalDateTime.now().minusDays(4L).minusHours(6L));
    }

    private void seedAdditionalComments() {
        ensureComment("4 月星港联合作战正式开启", "tea_break_runner",
                "我刚把首周商店算了一遍，确实是材料箱和碎片最值得优先拿，头像框反而可以等第二轮代币再补。", LocalDateTime.now().minusMinutes(44L));
        ensureComment("4 月星港联合作战正式开启", "slow_wave_archive",
                "补一个细节，周末如果时间不多，记得先把每日门票拿掉，晚点再回头刷本也来得及。", LocalDateTime.now().minusMinutes(32L));
        ensureComment("新手开荒阵容路线整理", "afterglow_lab",
                "补一句，如果手里没有稳定治疗，护盾位其实能顶掉不少前期压力，尤其适合练度还没跟上的阶段。", LocalDateTime.now().minusHours(1L).minusMinutes(9L));
        ensureComment("新手开荒阵容路线整理", "drift_compass",
                "我自己前两天就是先拉站场和破盾，明显比平均养成舒服很多，这条路线对新号真的实用。", LocalDateTime.now().minusHours(2L).minusMinutes(6L));
        ensureComment("新人七日养成路线图", "tea_break_runner",
                "商店别一口气清空这句太重要了，我上次就是把通用材料全换了，后面活动票券差点不够。", LocalDateTime.now().minusHours(4L).minusMinutes(18L));
        ensureComment("新人七日养成路线图", "starport_editor",
                "如果是刚进活动周的新号，周末那两天可以适当把体力从常驻素材里挪一点给限时兑换。", LocalDateTime.now().minusHours(3L).minusMinutes(25L));
        ensureComment("星港日常主题投稿精选", "paper_crane_radio",
                "求一个同主题合集二期，我这两天也攒了几张很适合放进来的稿子。", LocalDateTime.now().minusHours(6L).minusMinutes(12L));
        ensureComment("星港日常主题投稿精选", "porchlight_signal",
                "这期挑的几张夜港氛围都好舒服，看完又想回去翻创作区里那些雨夜主题了。", LocalDateTime.now().minusHours(5L).minusMinutes(40L));
        ensureComment("剧情支线讨论串", "signal_gull",
                "同感，而且这一段几乎没靠大段设定补充，纯靠语气和停顿就把关系写出来了。", LocalDateTime.now().minusDays(1L).minusHours(2L));
        ensureComment("剧情支线讨论串", "moonwake_note",
                "我最喜欢的是那句看似轻描淡写的告别，越往回想越觉得很多情绪都压在没说出口的地方。", LocalDateTime.now().minusDays(1L).minusHours(1L).minusMinutes(10L));
        ensureComment("今天商店兑换别忘了领巡航票", "tea_break_runner",
                "刚刚帮朋友看号的时候也漏了这一步，果然这种提醒帖最适合白天一直挂着。", LocalDateTime.now().minusMinutes(26L));
        ensureComment("今天商店兑换别忘了领巡航票", "slow_wave_archive",
                "我已经把这条转给公会群了，今天这种容易漏的小提醒真的很救命。", LocalDateTime.now().minusMinutes(18L));
        ensureComment("早班车配队实测：双破盾比我想象里稳", "afterglow_lab",
                "我补测了一轮，低练度情况下双破盾确实稳，尤其适合需要多容错一点的手残玩家。", LocalDateTime.now().minusMinutes(21L));
        ensureComment("早班车配队实测：双破盾比我想象里稳", "night_shift_log",
                "如果主C练度差一点，就先接受通关时间慢一点，稳稳打完比临时翻车舒服太多。", LocalDateTime.now().minusMinutes(14L));
        ensureComment("周末补给站开放提醒：体力别急着全花", "rain_dock_log",
                "我准备先把活动代币和武器材料错开刷，不然周末一上头就很容易把体力全砸到一个本里。", LocalDateTime.now().minusMinutes(37L));
        ensureComment("周末补给站开放提醒：体力别急着全花", "signal_gull",
                "这个提醒来得正好，我差点把明后天预留给补给站的体力先拿去补常驻本了。", LocalDateTime.now().minusMinutes(24L));
        ensureComment("夜巡活动里最好用的通用配队是哪几套？", "moonwake_note",
                "我自己更喜欢单核加治疗那套，虽然慢一点，但容错和节奏都比较舒服。", LocalDateTime.now().minusHours(1L).minusMinutes(3L));
        ensureComment("夜巡活动里最好用的通用配队是哪几套？", "afterglow_lab",
                "双破盾那套更适合角色池不齐的时候，少一个关键输出也不至于直接打不动。", LocalDateTime.now().minusMinutes(53L));
        ensureComment("低练度也能抄的资源规划表，按周拆给大家", "drift_compass",
                "这种按周拆开的表太适合新手了，不然很多人一看到大表格就会直接放弃。", LocalDateTime.now().minusHours(1L).minusMinutes(46L));
        ensureComment("低练度也能抄的资源规划表，按周拆给大家", "slow_wave_archive",
                "我把这张表又细分成了每日待办，发现照着走的时候焦虑感真的少很多。", LocalDateTime.now().minusHours(1L).minusMinutes(11L));
        ensureComment("把港口夜景做成了动态壁纸，放一个预览", "dockside_pixel",
                "这个风吹灯带的细节好舒服，如果后面有手机版我一定收。", LocalDateTime.now().minusHours(2L).minusMinutes(21L));
        ensureComment("把港口夜景做成了动态壁纸，放一个预览", "paper_crane_radio",
                "安静氛围真的拿捏住了，我特别喜欢角色动作压得很轻这一点。", LocalDateTime.now().minusHours(2L).minusMinutes(8L));
        ensureComment("把港口夜景做成了动态壁纸，放一个预览", "porchlight_signal",
                "预览里那一段灯光往水面上滑过去的效果太漂亮了，做成桌面应该会很耐看。", LocalDateTime.now().minusHours(1L).minusMinutes(42L));
        ensureComment("把支线里的港口广播词单独抄出来了，越看越有味道", "signal_gull",
                "你把那几句摘出来之后味道一下子更明显了，广播词和人物情绪之间确实是互相照着的。", LocalDateTime.now().minusHours(3L).minusMinutes(26L));
        ensureComment("把支线里的港口广播词单独抄出来了，越看越有味道", "porchlight_signal",
                "我还特意回去听了一遍配音，停顿的位置和你说的一样，真的很会留白。", LocalDateTime.now().minusHours(2L).minusMinutes(17L));
        ensureComment("做了一套夜航主题头像框配色，先放三张预览", "dockside_pixel",
                "蓝灰这一版好适合夜景截图，等透明底出来我想直接拿去换头像。", LocalDateTime.now().minusHours(5L).minusMinutes(8L));
        ensureComment("做了一套夜航主题头像框配色，先放三张预览", "paper_crane_radio",
                "第三张边缘那一点冷光特别妙，我觉得这个系列很适合继续做成签名条。", LocalDateTime.now().minusHours(4L).minusMinutes(2L));
        // --- comments for new posts ---
        ensureComment("刃影满命和半命的实战差距有多大？我测了一轮", "drift_compass",
                "三命和五命的节点说到点子上了，我卡在三命之前也明显感觉循环不顺畅。", LocalDateTime.now().minusDays(1L).minusHours(2L).minusMinutes(30L));
        ensureComment("刃影满命和半命的实战差距有多大？我测了一轮", "slow_wave_archive",
                "表格对比这个形式很好，新手容易只看描述看晕，有数字对比一眼就能判断自己在哪个阶段。", LocalDateTime.now().minusDays(1L).minusHours(1L).minusMinutes(50L));
        ensureComment("刃影满命和半命的实战差距有多大？我测了一轮", "northwind_plan",
                "遗物优先于命座这个结论我这边测试结果一致，补完遗物之后同样命座的提升感非常明显。", LocalDateTime.now().minusDays(1L).minusMinutes(40L));
        ensureComment("活动代币兑换优先级速查表（本期更新）", "starport_editor",
                "必换档整理得很准，技能书这期消耗量大，确实不能省，建议放在每天清日常之后第一个换。", LocalDateTime.now().minusDays(1L).minusHours(6L).minusMinutes(10L));
        ensureComment("活动代币兑换优先级速查表（本期更新）", "rain_dock_log",
                "头像框那条补充太及时了，我上期就没忍住，结果下期出了个更好看的，悔得不行。", LocalDateTime.now().minusDays(1L).minusHours(5L).minusMinutes(33L));
        ensureComment("活动代币兑换优先级速查表（本期更新）", "night_shift_log",
                "收藏品可以跳过这个建议对刚入坑的人很友好，前期资源紧，先把实战材料补齐比摆好看重要。", LocalDateTime.now().minusDays(1L).minusHours(4L).minusMinutes(15L));
        ensureComment("回廊三层以上遗物搭配思路汇总", "afterglow_lab",
                "术士那套的暴击率门槛说得很准，60% 以下在高层暴击收益会有很明显的波动感。", LocalDateTime.now().minusDays(1L).minusHours(22L).minusMinutes(14L));
        ensureComment("回廊三层以上遗物搭配思路汇总", "tea_break_runner",
                "过渡方案这部分是这篇的精华，很多攻略只写毕业配置，对刚进高层的人参考意义不大。", LocalDateTime.now().minusDays(1L).minusHours(18L).minusMinutes(42L));
        ensureComment("回廊三层以上遗物搭配思路汇总", "drift_compass",
                "钢躯减伤阈值那段我之前没仔细研究过，照着改了词条之后感觉硬度提升比堆生命更明显。", LocalDateTime.now().minusDays(1L).minusHours(14L).minusMinutes(7L));
        ensureComment("新版本剧情主线通关感受，不含剧透", "moonwake_note",
                "你说的中段那个场景我知道是哪里，音乐进来的时机把氛围全托住了，配音也是整章最好的一段。", LocalDateTime.now().minusDays(2L).minusHours(4L).minusMinutes(20L));
        ensureComment("新版本剧情主线通关感受，不含剧透", "porchlight_signal",
                "前半段节奏慢我一开始也有点不适应，但打完之后回头看感觉铺垫是必要的，结局那一段才有力量。", LocalDateTime.now().minusDays(2L).minusHours(3L).minusMinutes(5L));
        ensureComment("新版本剧情主线通关感受，不含剧透", "signal_gull",
                "Boss 机制比上一章清晰这个感受一样，第一次进去没看攻略也打得比较顺，机制提示做得好。", LocalDateTime.now().minusDays(1L).minusHours(20L).minusMinutes(38L));
        ensureComment("用游戏截图拼了一张角色关系图，连线附注释", "moonwake_note",
                "波浪线的设计很聪明，支线里有几处我也觉得可以有两种解读，你标的几条和我想法差不多。", LocalDateTime.now().minusDays(3L).minusHours(1L).minusMinutes(10L));
        ensureComment("用游戏截图拼了一张角色关系图，连线附注释", "signal_gull",
                "有一个细节补充：支线第三章有一段台词里间接提到了两个角色之间还有更早的接触，可以在虚线那里再加一条。", LocalDateTime.now().minusDays(2L).minusHours(22L).minusMinutes(45L));
        ensureComment("缚魂职业入门指南：选择缚魂的理由和基础打法", "afterglow_lab",
                "增益窗口那块写得最实用，很多缚魂玩家前期容易抢着输出，反而错过最佳叠加时机。", LocalDateTime.now().minusDays(3L).minusHours(6L).minusMinutes(20L));
        ensureComment("缚魂职业入门指南：选择缚魂的理由和基础打法", "drift_compass",
                "我带缚魂进高层踩过的最大坑就是以为控制越多越好，实际上技能循环节奏不对反而拖队友后腿。", LocalDateTime.now().minusDays(3L).minusHours(5L).minusMinutes(10L));
        ensureComment("缚魂职业入门指南：选择缚魂的理由和基础打法", "northwind_plan",
                "能不能补一节遗物选择方向？缚魂的遗物词条优先级我一直没找到比较系统的说明。", LocalDateTime.now().minusDays(2L).minusHours(15L).minusMinutes(30L));
        ensureComment("周年庆前瞻情报汇总，官方已确认的部分", "rain_dock_log",
                "直播定档消息帮大忙了，我差点周年庆那天排了出行计划，现在可以提前把时间留出来。", LocalDateTime.now().minusDays(4L).minusHours(1L).minusMinutes(15L));
        ensureComment("周年庆前瞻情报汇总，官方已确认的部分", "tea_break_runner",
                "遗物系统调整那条让我稍微放下了一点囤货焦虑，如果词条逻辑改了，现在满斗的遗物不一定要急着花体力。", LocalDateTime.now().minusDays(3L).minusHours(23L).minusMinutes(40L));
        ensureComment("周年庆前瞻情报汇总，官方已确认的部分", "slow_wave_archive",
                "建议周年庆两周前的帖子可以置顶一下，方便大家随时回来对照，不然帖子沉下去了不好找。", LocalDateTime.now().minusDays(3L).minusHours(18L).minusMinutes(5L));
        ensureComment("跑图五连败之后我想清楚了一件事", "signal_gull",
                "触发型词条 vs 静态属性这个思路换了之后我整个遗物规划逻辑都变了，这条值得单独出一篇详解。", LocalDateTime.now().minusDays(1L).minusHours(8L).minusMinutes(20L));
        ensureComment("跑图五连败之后我想清楚了一件事", "porchlight_signal",
                "先想清楚胜利条件这句话，我觉得对选职业和选遗物都适用，基本是高层跑图的核心思路。", LocalDateTime.now().minusDays(1L).minusHours(7L).minusMinutes(10L));
        ensureComment("给即将进高层的朋友：我踩过的几个坑", "northwind_plan",
                "坑二太真实了，我之前把大量体力投在命座上，进了高层才发现遗物才是真正的门槛。", LocalDateTime.now().minusDays(4L).minusHours(5L).minusMinutes(8L));
        ensureComment("给即将进高层的朋友：我踩过的几个坑", "rain_dock_log",
                "低层用硬打代替理解机制这个习惯我也有，高层 Boss 的一套连招能直接把没做功课的玩家秒掉。", LocalDateTime.now().minusDays(4L).minusHours(4L).minusMinutes(25L));
        ensureComment("给即将进高层的朋友：我踩过的几个坑", "tea_break_runner",
                "技能循环吃透这个建议对缚魂和术士玩家尤其重要，这两个职业的增益窗口非常吃熟练度。", LocalDateTime.now().minusDays(3L).minusHours(21L).minusMinutes(30L));
    }

    private void synchronizeCommentCounts() {
        List<Long> postIds = jdbcTemplate.query(
                "SELECT id FROM community_post WHERE status = ?",
                (resultSet, rowNum) -> resultSet.getLong(1),
                STATUS_ENABLED
        );
        for (Long postId : postIds) {
            Long commentCount = queryForLong(
                    "SELECT COUNT(1) FROM community_comment WHERE post_id = ? AND status = ?",
                    postId,
                    STATUS_ENABLED
            );
            jdbcTemplate.update(
                    "UPDATE community_post SET comment_count = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?",
                    commentCount == null ? 0L : commentCount,
                    postId
            );
        }
    }

    private boolean hasTable(String tableName) {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(1) FROM " + tableName, Long.class);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private long countRows(String tableName) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM " + tableName, Long.class);
        return count == null ? 0L : count;
    }

    private Long queryForLong(String sql, Object... args) {
        List<Long> result = jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getLong(1), args);
        return result.isEmpty() ? null : result.get(0);
    }

    private void ensureMember(String username,
                              String nickname,
                              String phone,
                              int gender,
                              String city,
                              String signature,
                              String profile,
                              LocalDateTime lastLoginTime) {
        Long userId = queryForLong("SELECT id FROM user_account WHERE phone = ?", phone);
        if (userId == null) {
            jdbcTemplate.update(
                    """
                            INSERT INTO user_account (
                                username, password, nickname, email, phone, avatar, gender, status, last_login_time
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    username,
                    SEEDED_PASSWORD,
                    nickname,
                    null,
                    phone,
                    null,
                    gender,
                    STATUS_ENABLED,
                    Timestamp.valueOf(lastLoginTime)
            );
            userId = queryForLong("SELECT id FROM user_account WHERE phone = ?", phone);
        } else {
            jdbcTemplate.update(
                    """
                            UPDATE user_account
                            SET username = ?, nickname = ?, gender = ?, status = ?, last_login_time = ?, update_time = CURRENT_TIMESTAMP
                            WHERE id = ?
                            """,
                    username,
                    nickname,
                    gender,
                    STATUS_ENABLED,
                    Timestamp.valueOf(lastLoginTime),
                    userId
            );
        }
        if (userId == null) {
            return;
        }
        Long infoCount = queryForLong("SELECT COUNT(1) FROM user_info WHERE user_id = ?", userId);
        if (infoCount == null || infoCount == 0L) {
            jdbcTemplate.update(
                    """
                            INSERT INTO user_info (
                                user_id, real_name, birthday, signature, profile, country, province, city, website, background_image
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    userId,
                    nickname,
                    LocalDate.of(2000, 1, 1),
                    signature,
                    profile,
                    "中国",
                    city,
                    city,
                    null,
                    null
            );
        } else {
            jdbcTemplate.update(
                    """
                            UPDATE user_info
                            SET real_name = ?, signature = ?, profile = ?, country = ?, province = ?, city = ?, update_time = CURRENT_TIMESTAMP
                            WHERE user_id = ?
                            """,
                    nickname,
                    signature,
                    profile,
                    "中国",
                    city,
                    city,
                    userId
            );
        }
    }

    private void refreshExistingUser(Long userId,
                                     String username,
                                     String nickname,
                                     int gender,
                                     LocalDateTime lastLoginTime,
                                     String city,
                                     String signature,
                                     String profile) {
        Long count = queryForLong("SELECT COUNT(1) FROM user_account WHERE id = ?", userId);
        if (count == null || count == 0L) {
            return;
        }
        jdbcTemplate.update(
                """
                        UPDATE user_account
                        SET username = ?, nickname = ?, gender = ?, status = ?, last_login_time = ?, update_time = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                username,
                nickname,
                gender,
                STATUS_ENABLED,
                Timestamp.valueOf(lastLoginTime),
                userId
        );
        Long infoCount = queryForLong("SELECT COUNT(1) FROM user_info WHERE user_id = ?", userId);
        if (infoCount == null || infoCount == 0L) {
            jdbcTemplate.update(
                    """
                            INSERT INTO user_info (
                                user_id, real_name, birthday, signature, profile, country, province, city, website, background_image
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    userId,
                    nickname,
                    LocalDate.of(2001, 1, 1),
                    signature,
                    profile,
                    "中国",
                    city,
                    city,
                    null,
                    null
            );
        } else {
            jdbcTemplate.update(
                    """
                            UPDATE user_info
                            SET real_name = ?, signature = ?, profile = ?, country = ?, province = ?, city = ?, update_time = CURRENT_TIMESTAMP
                            WHERE user_id = ?
                            """,
                    nickname,
                    signature,
                    profile,
                    "中国",
                    city,
                    city,
                    userId
            );
        }
    }

    private void updateLegacyPost(Long postId,
                                  String title,
                                  String summary,
                                  String content,
                                  Long boardId,
                                  Long authorId,
                                  int viewCount,
                                  int favoriteCount,
                                  LocalDateTime publishedAt) {
        Long count = queryForLong("SELECT COUNT(1) FROM community_post WHERE id = ?", postId);
        if (count == null || count == 0L) {
            return;
        }
        jdbcTemplate.update(
                """
                        UPDATE community_post
                        SET board_id = ?, author_id = ?, title = ?, summary = ?, content = ?, view_count = ?, favorite_count = ?,
                            status = ?, published_at = ?, update_time = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                boardId,
                authorId,
                title,
                summary,
                content,
                viewCount,
                favoriteCount,
                STATUS_ENABLED,
                Timestamp.valueOf(publishedAt),
                postId
        );
    }

    /**
     * Updates an existing legacy comment with a default recent timestamp.
     *
     * @param commentId comment id
     * @param postId target post id
     * @param authorId target author id
     * @param content normalized comment content
     */
    private void updateLegacyComment(Long commentId, Long postId, Long authorId, String content) {
        Long count = queryForLong("SELECT COUNT(1) FROM community_comment WHERE id = ?", commentId);
        if (count == null || count == 0L) {
            return;
        }
        updateLegacyComment(commentId, postId, authorId, content, LocalDateTime.now().minusMinutes(20L));
    }

    /**
     * Updates an existing legacy comment and rewrites its timeline placement.
     *
     * @param commentId comment id
     * @param postId target post id
     * @param authorId target author id
     * @param content normalized comment content
     * @param commentedAt normalized comment time
     */
    private void updateLegacyComment(Long commentId,
                                     Long postId,
                                     Long authorId,
                                     String content,
                                     LocalDateTime commentedAt) {
        if (authorId == null) {
            return;
        }
        Long count = queryForLong("SELECT COUNT(1) FROM community_comment WHERE id = ?", commentId);
        if (count == null || count == 0L) {
            return;
        }
        jdbcTemplate.update(
                """
                        UPDATE community_comment
                        SET post_id = ?, author_id = ?, content = ?, status = ?, create_time = ?, update_time = ?
                        WHERE id = ?
                        """,
                postId,
                authorId,
                content,
                STATUS_ENABLED,
                Timestamp.valueOf(commentedAt),
                Timestamp.valueOf(commentedAt),
                commentId
        );
    }

    private void assignAuthorToPost(String title, String username) {
        Long postId = queryForLong("SELECT id FROM community_post WHERE title = ? LIMIT 1", title);
        Long authorId = queryForLong("SELECT id FROM user_account WHERE username = ? LIMIT 1", username);
        if (postId == null || authorId == null) {
            return;
        }
        jdbcTemplate.update("UPDATE community_post SET author_id = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?", authorId, postId);
    }

    private void ensurePost(String title,
                            String boardSlug,
                            String authorUsername,
                            String summary,
                            String content,
                            String badge,
                            int featured,
                            int pinned,
                            int viewCount,
                            int favoriteCount,
                            LocalDateTime publishedAt) {
        Long existingPostId = queryForLong("SELECT id FROM community_post WHERE title = ? LIMIT 1", title);
        Long boardId = queryForLong("SELECT id FROM community_board WHERE slug = ? LIMIT 1", boardSlug);
        Long authorId = queryForLong("SELECT id FROM user_account WHERE username = ? LIMIT 1", authorUsername);
        if (boardId == null || authorId == null) {
            return;
        }
        if (existingPostId != null) {
            jdbcTemplate.update(
                    """
                            UPDATE community_post
                            SET board_id = ?, author_id = ?, summary = ?, content = ?, badge = ?, status = ?,
                                is_featured = ?, is_pinned = ?, view_count = ?, favorite_count = ?, published_at = ?,
                                update_time = CURRENT_TIMESTAMP
                            WHERE id = ?
                            """,
                    boardId, authorId, summary, content, badge, STATUS_ENABLED,
                    featured, pinned, viewCount, favoriteCount, Timestamp.valueOf(publishedAt), existingPostId
            );
            return;
        }
        jdbcTemplate.update(
                """
                        INSERT INTO community_post (
                            board_id, author_id, title, summary, content, badge, status,
                            is_featured, is_pinned, view_count, comment_count, favorite_count, published_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                boardId, authorId, title, summary, content, badge, STATUS_ENABLED,
                featured, pinned, viewCount, 0, favoriteCount, Timestamp.valueOf(publishedAt)
        );
    }

    /**
     * Inserts a sample comment when the same text is not already present.
     *
     * @param postTitle target post title
     * @param authorUsername author username
     * @param content comment content
     * @param commentedAt comment timestamp
     */
    private void ensureComment(String postTitle, String authorUsername, String content, LocalDateTime commentedAt) {
        Long existingCommentId = queryForLong("SELECT id FROM community_comment WHERE content = ? LIMIT 1", content);
        if (existingCommentId != null) {
            return;
        }
        Long postId = queryForLong("SELECT id FROM community_post WHERE title = ? LIMIT 1", postTitle);
        Long authorId = queryForLong("SELECT id FROM user_account WHERE username = ? LIMIT 1", authorUsername);
        if (postId == null || authorId == null) {
            return;
        }
        jdbcTemplate.update(
                """
                        INSERT INTO community_comment (
                            post_id, author_id, content, status, create_time, update_time
                        ) VALUES (?, ?, ?, ?, ?, ?)
                        """,
                postId, authorId, content, STATUS_ENABLED,
                Timestamp.valueOf(commentedAt),
                Timestamp.valueOf(commentedAt)
        );
    }

    /**
     * Resolves a user id from the seeded username.
     *
     * @param username unique username
     * @return user id or null
     */
    private Long resolveUserIdByUsername(String username) {
        return queryForLong("SELECT id FROM user_account WHERE username = ? LIMIT 1", username);
    }
}
