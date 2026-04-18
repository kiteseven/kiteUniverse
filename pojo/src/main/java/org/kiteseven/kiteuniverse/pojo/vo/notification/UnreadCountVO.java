package org.kiteseven.kiteuniverse.pojo.vo.notification;

import java.io.Serial;
import java.io.Serializable;

/**
 * 未读通知数量视图对象。
 */
public class UnreadCountVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 未读通知数量。
     */
    private int unreadCount;

    public UnreadCountVO() {
    }

    public UnreadCountVO(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
