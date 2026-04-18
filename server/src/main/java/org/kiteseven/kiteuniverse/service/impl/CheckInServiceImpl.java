package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.DailyCheckInMapper;
import org.kiteseven.kiteuniverse.mapper.UserBadgeMapper;
import org.kiteseven.kiteuniverse.mapper.UserPointsMapper;
import org.kiteseven.kiteuniverse.pojo.entity.DailyCheckIn;
import org.kiteseven.kiteuniverse.pojo.entity.UserBadge;
import org.kiteseven.kiteuniverse.pojo.entity.UserPoints;
import org.kiteseven.kiteuniverse.pojo.vo.checkin.CheckInResultVO;
import org.kiteseven.kiteuniverse.pojo.vo.checkin.CheckInStatusVO;
import org.kiteseven.kiteuniverse.pojo.vo.checkin.UserBadgeVO;
import org.kiteseven.kiteuniverse.service.CheckInService;
import org.kiteseven.kiteuniverse.support.redis.DistributedLockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 签到与激励服务实现。
 *
 * <p>积分规则：
 * <ul>
 *   <li>每日签到基础奖励：10 积分</li>
 *   <li>连续签到第 7 天：额外 +20</li>
 *   <li>连续签到第 14 天：额外 +40</li>
 *   <li>连续签到第 30 天：额外 +100</li>
 *   <li>发帖：+5 积分（外部调用 addPoints）</li>
 *   <li>评论：+2 积分（外部调用 addPoints）</li>
 *   <li>帖子被点赞：+3 积分（外部调用 addPoints）</li>
 *   <li>帖子被收藏：+5 积分（外部调用 addPoints）</li>
 * </ul>
 *
 * <p>等级阈值：
 * <ul>
 *   <li>Lv1 探索者：0–99</li>
 *   <li>Lv2 初入回廊：100–299</li>
 *   <li>Lv3 深渊行者：300–699</li>
 *   <li>Lv4 虚空老兵：700–1499</li>
 *   <li>Lv5 裂隙征服者：1500–2999</li>
 *   <li>Lv6 虚空主宰：3000+</li>
 * </ul>
 */
@Service
public class CheckInServiceImpl implements CheckInService {

    private static final int BASE_POINTS = 10;

    /** 等级阈值，索引 0 对应 Lv1 起点（0），索引 5 对应 Lv6 起点（3000）。 */
    private static final int[] LEVEL_THRESHOLDS = {0, 100, 300, 700, 1500, 3000};
    private static final String[] LEVEL_NAMES = {"探索者", "初入回廊", "深渊行者", "虚空老兵", "裂隙征服者", "虚空主宰"};

    private final UserPointsMapper userPointsMapper;
    private final DailyCheckInMapper dailyCheckInMapper;
    private final UserBadgeMapper userBadgeMapper;
    private final DistributedLockService distributedLockService;

    public CheckInServiceImpl(UserPointsMapper userPointsMapper,
                              DailyCheckInMapper dailyCheckInMapper,
                              UserBadgeMapper userBadgeMapper,
                              DistributedLockService distributedLockService) {
        this.userPointsMapper = userPointsMapper;
        this.dailyCheckInMapper = dailyCheckInMapper;
        this.userBadgeMapper = userBadgeMapper;
        this.distributedLockService = distributedLockService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckInResultVO checkIn(Long userId) {
        return distributedLockService.executeWithLock(buildUserProgressLockKey(userId), () -> {
        LocalDate today = LocalDate.now();
        if (dailyCheckInMapper.selectByUserIdAndDate(userId, today) != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "今日已签到，明天再来吧");
        }

        UserPoints userPoints = getOrCreateUserPoints(userId);
        int oldLevel = userPoints.getLevel();

        // 计算连续天数
        LocalDate lastDate = userPoints.getLastCheckInDate();
        int consecutive;
        if (lastDate != null && lastDate.equals(today.minusDays(1))) {
            consecutive = userPoints.getConsecutiveDays() + 1;
        } else {
            consecutive = 1;
        }

        // 计算本次积分
        int earned = BASE_POINTS + streakBonus(consecutive);

        // 更新积分快照
        int newPoints = userPoints.getPoints() + earned;
        int newTotal = userPoints.getTotalPointsEarned() + earned;
        int newLevel = calcLevel(newTotal);

        userPoints.setPoints(newPoints);
        userPoints.setLevel(newLevel);
        userPoints.setTotalPointsEarned(newTotal);
        userPoints.setConsecutiveDays(consecutive);
        userPoints.setLastCheckInDate(today);
        userPointsMapper.update(userPoints);

        // 写入签到记录
        DailyCheckIn record = new DailyCheckIn();
        record.setUserId(userId);
        record.setCheckInDate(today);
        record.setPointsEarned(earned);
        record.setConsecutiveDays(consecutive);
        dailyCheckInMapper.insert(record);

        // 发放徽章
        List<UserBadgeVO> newBadges = new ArrayList<>();
        newBadges.addAll(grantCheckInBadges(userId, consecutive));
        if (newLevel > oldLevel) {
            newBadges.addAll(grantLevelBadges(userId, oldLevel + 1, newLevel));
        }

        CheckInResultVO vo = new CheckInResultVO();
        vo.setPointsEarned(earned);
        vo.setConsecutiveDays(consecutive);
        vo.setTotalPoints(newPoints);
        vo.setLevel(newLevel);
        vo.setLevelName(LEVEL_NAMES[newLevel - 1]);
        vo.setLeveledUp(newLevel > oldLevel);
        vo.setNewBadges(newBadges);
        return vo;
        });
    }

    @Override
    public CheckInStatusVO getStatus(Long userId) {
        UserPoints userPoints = getOrCreateUserPoints(userId);
        LocalDate today = LocalDate.now();
        boolean checkedInToday = dailyCheckInMapper.selectByUserIdAndDate(userId, today) != null;

        int level = userPoints.getLevel();
        CheckInStatusVO vo = new CheckInStatusVO();
        vo.setCheckedInToday(checkedInToday);
        vo.setConsecutiveDays(userPoints.getConsecutiveDays());
        vo.setPoints(userPoints.getPoints());
        vo.setLevel(level);
        vo.setLevelName(LEVEL_NAMES[level - 1]);
        vo.setNextLevelPoints(level < 6 ? LEVEL_THRESHOLDS[level] : -1);
        return vo;
    }

    @Override
    public List<UserBadgeVO> getBadges(Long userId) {
        return userBadgeMapper.selectByUserId(userId)
                .stream()
                .map(this::toBadgeVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, int points) {
        if (points <= 0) {
            return;
        }
        distributedLockService.runWithLock(buildUserProgressLockKey(userId), () -> {
            UserPoints userPoints = getOrCreateUserPoints(userId);
            int oldLevel = userPoints.getLevel();
            int newTotal = userPoints.getTotalPointsEarned() + points;
            int newLevel = calcLevel(newTotal);
            userPoints.setPoints(userPoints.getPoints() + points);
            userPoints.setTotalPointsEarned(newTotal);
            userPoints.setLevel(newLevel);
            userPointsMapper.update(userPoints);
            if (newLevel > oldLevel) {
                grantLevelBadges(userId, oldLevel + 1, newLevel);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantBadge(Long userId, String badgeType) {
        distributedLockService.runWithLock(buildUserProgressLockKey(userId),
                () -> grantBadgeIfAbsent(userId, badgeType));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private UserPoints getOrCreateUserPoints(Long userId) {
        UserPoints up = userPointsMapper.selectByUserId(userId);
        if (up == null) {
            up = new UserPoints();
            up.setUserId(userId);
            up.setPoints(0);
            up.setLevel(1);
            up.setTotalPointsEarned(0);
            up.setConsecutiveDays(0);
            up.setLastCheckInDate(null);
            userPointsMapper.insert(up);
        }
        return up;
    }

    private int streakBonus(int consecutive) {
        if (consecutive % 30 == 0) return 100;
        if (consecutive % 14 == 0) return 40;
        if (consecutive % 7 == 0) return 20;
        return 0;
    }

    private int calcLevel(int totalPoints) {
        for (int i = LEVEL_THRESHOLDS.length - 1; i >= 0; i--) {
            if (totalPoints >= LEVEL_THRESHOLDS[i]) return i + 1;
        }
        return 1;
    }

    private List<UserBadgeVO> grantCheckInBadges(Long userId, int consecutive) {
        List<UserBadgeVO> granted = new ArrayList<>();
        if (consecutive == 1) granted.addAll(grantBadgeIfAbsent(userId, "FIRST_CHECKIN"));
        if (consecutive >= 7) granted.addAll(grantBadgeIfAbsent(userId, "WEEK_STREAK"));
        if (consecutive >= 30) granted.addAll(grantBadgeIfAbsent(userId, "MONTH_STREAK"));
        return granted;
    }

    private List<UserBadgeVO> grantLevelBadges(Long userId, int fromLevel, int toLevel) {
        List<UserBadgeVO> granted = new ArrayList<>();
        for (int lv = fromLevel; lv <= toLevel; lv++) {
            granted.addAll(grantBadgeIfAbsent(userId, "LEVEL_" + lv));
        }
        return granted;
    }

    private List<UserBadgeVO> grantBadgeIfAbsent(Long userId, String badgeType) {
        if (userBadgeMapper.selectByUserIdAndType(userId, badgeType) != null) {
            return List.of();
        }
        UserBadge badge = new UserBadge();
        badge.setUserId(userId);
        badge.setBadgeType(badgeType);
        userBadgeMapper.insert(badge);
        UserBadge saved = userBadgeMapper.selectByUserIdAndType(userId, badgeType);
        return List.of(toBadgeVO(saved));
    }

    private UserBadgeVO toBadgeVO(UserBadge badge) {
        UserBadgeVO vo = new UserBadgeVO();
        vo.setBadgeType(badge.getBadgeType());
        vo.setEarnedAt(badge.getEarnedAt());
        String[] meta = badgeMeta(badge.getBadgeType());
        vo.setName(meta[0]);
        vo.setDescription(meta[1]);
        vo.setIcon(meta[2]);
        return vo;
    }

    /**
     * 返回 [name, description, icon] 三元素数组。
     */
    private String[] badgeMeta(String badgeType) {
        return switch (badgeType) {
            case "FIRST_CHECKIN"  -> new String[]{"初次签到",    "第一次踏入虚空回廊",             "🗓️"};
            case "WEEK_STREAK"    -> new String[]{"七日连签",    "连续签到满 7 天",                "🔥"};
            case "MONTH_STREAK"   -> new String[]{"三十日连签",  "连续签到满 30 天",               "🌕"};
            case "FIRST_POST"     -> new String[]{"初次创作",    "发布了第一篇社区帖子",           "✍️"};
            case "POPULAR_POST"   -> new String[]{"内容热门",    "帖子累计获得 50 个点赞",         "⭐"};
            case "LEVEL_2"        -> new String[]{"初入回廊",    "达到 Lv2 — 初入回廊",           "🚪"};
            case "LEVEL_3"        -> new String[]{"深渊行者",    "达到 Lv3 — 深渊行者",           "🌑"};
            case "LEVEL_4"        -> new String[]{"虚空老兵",    "达到 Lv4 — 虚空老兵",           "⚔️"};
            case "LEVEL_5"        -> new String[]{"裂隙征服者",  "达到 Lv5 — 裂隙征服者",         "💎"};
            case "LEVEL_6"        -> new String[]{"虚空主宰",    "达到 Lv6 — 虚空主宰（最高级）", "👑"};
            default               -> new String[]{badgeType,     "",                              "🏅"};
        };
    }

    private String buildUserProgressLockKey(Long userId) {
        return "user-progress:user:" + userId;
    }
}
