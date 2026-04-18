import {
  fetchNotifications,
  markAllNotificationsRead,
  markNotificationRead,
  resolveAssetUrl,
  type NotificationData
} from '../services/api';
import { loadStoredToken } from '../services/session';

const TYPE_LABELS: Record<string, string> = {
  COMMENT: '评论',
  POST_LIKE: '帖子点赞',
  COMMENT_LIKE: '评论点赞',
  FOLLOW: '新关注者',
  ANNOUNCEMENT: '系统公告'
};

/**
 * Notification center page — lists all incoming notifications with read/unread state.
 */
export const NotificationsPage = {
  data() {
    return {
      loading: false,
      errorMessage: '',
      notifications: [] as NotificationData[],
      markingRead: false
    };
  },
  computed: {
    unreadCount(this: any): number {
      return this.notifications.filter((n: NotificationData) => !n.isRead).length;
    }
  },
  methods: {
    async loadNotifications(this: any) {
      const token = loadStoredToken();
      if (!token) {
        this.$router.push({ path: '/', query: { auth: 'login', redirect: '/notifications' } });
        return;
      }

      this.loading = true;
      this.errorMessage = '';

      try {
        this.notifications = await fetchNotifications(token, 100);
      } catch (error) {
        this.errorMessage = error instanceof Error ? error.message : '通知加载失败，请稍后重试。';
      } finally {
        this.loading = false;
      }
    },

    async markAllRead(this: any) {
      const token = loadStoredToken();
      if (!token || this.markingRead) return;

      this.markingRead = true;
      try {
        await markAllNotificationsRead(token);
        this.notifications = this.notifications.map((n: NotificationData) => ({ ...n, isRead: true }));
        window.dispatchEvent(new CustomEvent('notification-read-all'));
      } catch (error) {
        this.errorMessage = error instanceof Error ? error.message : '标记已读失败';
      } finally {
        this.markingRead = false;
      }
    },

    async markOneRead(this: any, notification: NotificationData) {
      if (notification.isRead) return;
      const token = loadStoredToken();
      if (!token) return;

      try {
        await markNotificationRead(token, notification.id);
        const idx = this.notifications.findIndex((n: NotificationData) => n.id === notification.id);
        if (idx !== -1) {
          this.notifications[idx] = { ...this.notifications[idx], isRead: true };
        }
        window.dispatchEvent(new CustomEvent('notification-read-one'));
      } catch {
        // Silently ignore single-read errors
      }
    },

    navigateToTarget(this: any, notification: NotificationData) {
      void this.markOneRead(notification);
      if (notification.postId) {
        void this.$router.push(`/posts/${notification.postId}`);
      } else if (notification.senderId && notification.type === 'FOLLOW') {
        void this.$router.push(`/users/${notification.senderId}`);
      }
    },

    getTypeLabel(type: string): string {
      return TYPE_LABELS[type] || type;
    },

    getTypeClass(type: string): string {
      const map: Record<string, string> = {
        COMMENT: 'notif-type--comment',
        POST_LIKE: 'notif-type--like',
        COMMENT_LIKE: 'notif-type--like',
        FOLLOW: 'notif-type--follow',
        ANNOUNCEMENT: 'notif-type--announce'
      };
      return map[type] || '';
    },

    resolveAvatarUrl(this: any, path: string | null | undefined) {
      return resolveAssetUrl(path);
    },

    getInitial(value: string | null | undefined): string {
      return (value || 'K').slice(0, 1).toUpperCase();
    },

    formatTime(this: any, value: string | null) {
      if (!value) return '';
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) return value.replace('T', ' ');
      return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  },
  mounted(this: any) {
    void this.loadNotifications();
  },
  template: `
    <main class="page-shell detail-shell">
      <section class="detail-hero">
        <div class="detail-hero__main">
          <span class="eyebrow">消息中心</span>
          <h1>我的通知</h1>
          <p v-if="!loading && !errorMessage">
            共 {{ notifications.length }} 条通知，其中
            <strong>{{ unreadCount }}</strong> 条未读。
          </p>
          <div class="hero__actions">
            <button
              v-if="unreadCount > 0"
              class="button button--primary"
              type="button"
              :disabled="markingRead"
              @click="markAllRead"
            >{{ markingRead ? '处理中...' : '全部标为已读' }}</button>
            <router-link class="button button--ghost" to="/">回到首页</router-link>
          </div>
        </div>
      </section>

      <section class="content-grid detail-grid">
        <div class="content-main">
          <section class="panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">通知列表</span>
                <h2>全部消息</h2>
              </div>
              <span v-if="!loading" class="panel__link">{{ notifications.length }} 条</span>
            </div>

            <section v-if="loading" class="panel page-state">
              <div class="page-state__content">
                <span class="eyebrow">正在加载</span>
                <h2>正在获取通知...</h2>
                <p>消息中心内容加载中，稍等一下。</p>
              </div>
            </section>

            <section v-else-if="errorMessage" class="panel page-state page-state--error">
              <div class="page-state__content">
                <span class="eyebrow">加载失败</span>
                <h2>通知暂时没有加载出来</h2>
                <p>{{ errorMessage }}</p>
                <button class="button button--primary" type="button" @click="loadNotifications">重新加载</button>
              </div>
            </section>

            <div v-else-if="!notifications.length" class="empty-state">
              <h3>暂时没有通知</h3>
              <p>当有人评论你的帖子、点赞你的内容或关注你时，通知会出现在这里。</p>
            </div>

            <div v-else class="notif-list">
              <div
                v-for="notif in notifications"
                :key="notif.id"
                class="notif-item"
                :class="{ 'notif-item--unread': !notif.isRead }"
                @click="navigateToTarget(notif)"
              >
                <div class="notif-item__avatar">
                  <img
                    v-if="notif.senderAvatar"
                    :src="resolveAvatarUrl(notif.senderAvatar)"
                    :alt="notif.senderName || '用户头像'"
                  />
                  <span v-else>{{ getInitial(notif.senderName) }}</span>
                </div>
                <div class="notif-item__body">
                  <div class="notif-item__head">
                    <span class="capsule" :class="getTypeClass(notif.type)">{{ getTypeLabel(notif.type) }}</span>
                    <span class="notif-item__time">{{ formatTime(notif.createTime) }}</span>
                  </div>
                  <p class="notif-item__content">{{ notif.content }}</p>
                  <p v-if="notif.postTitle" class="notif-item__post">帖子：{{ notif.postTitle }}</p>
                </div>
                <div class="notif-item__dot" v-if="!notif.isRead" title="未读"></div>
              </div>
            </div>
          </section>
        </div>

        <aside class="content-side">
          <section class="panel soft-panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">通知说明</span>
                <h2>消息类型</h2>
              </div>
            </div>
            <div class="notice-list">
              <p>📝 <strong>评论</strong>：有人评论了你的帖子</p>
              <p>👍 <strong>点赞</strong>：有人点赞了你的帖子或评论</p>
              <p>➕ <strong>关注</strong>：有新用户关注了你</p>
              <p>📢 <strong>公告</strong>：管理员发布的全站公告</p>
            </div>
          </section>

          <section class="panel soft-panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">快捷操作</span>
                <h2>其他入口</h2>
              </div>
            </div>
            <div class="notice-list" style="gap:10px;">
              <router-link class="sidebar-link-card" to="/profile">
                <strong>个人中心</strong>
                <span>查看和编辑个人资料</span>
              </router-link>
              <router-link class="sidebar-link-card" to="/">
                <strong>社区首页</strong>
                <span>浏览精选帖子和热门话题</span>
              </router-link>
            </div>
          </section>
        </aside>
      </section>
    </main>
  `
};
