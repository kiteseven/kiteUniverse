package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.vo.checkin.CheckInResultVO;
import org.kiteseven.kiteuniverse.pojo.vo.checkin.CheckInStatusVO;
import org.kiteseven.kiteuniverse.pojo.vo.checkin.UserBadgeVO;

import java.util.List;

/**
 * 签到与激励服务接口。
 */
public interface CheckInService {

    /** 执行今日签到，返回奖励结果。 */
    CheckInResultVO checkIn(Long userId);

    /** 查询今日签到状态及积分概览。 */
    CheckInStatusVO getStatus(Long userId);

    /** 查询用户已获得的所有徽章。 */
    List<UserBadgeVO> getBadges(Long userId);

    /** 为用户增加积分（内部调用，用于发帖/评论/被点赞等行为奖励）。 */
    void addPoints(Long userId, int points);

    /** 授予指定徽章（如不存在则创建，已存在则忽略）。 */
    void grantBadge(Long userId, String badgeType);
}
