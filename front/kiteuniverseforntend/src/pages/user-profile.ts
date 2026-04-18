import {
  ApiError,
  fetchFollowState,
  fetchUserDetail,
  fetchUserPosts,
  followUser,
  resolveAssetUrl,
  unfollowUser,
  type PostSummaryData,
  type UserDetailVO,
  type UserFollowStateData
} from '../services/api';
import { loadStoredToken, loadStoredUser } from '../services/session';

/**
 * Public user profile page.
 *
 * Displays a read-only view of another user's profile including avatar,
 * nickname, signature, join date, follower/following counts, a follow/unfollow
 * button, and a listing of that user's published posts.
 */
export const UserProfilePage = {
  data() {
    return {
      userDetail: null as UserDetailVO | null,
      followState: null as UserFollowStateData | null,
      userPosts: [] as PostSummaryData[],
      loading: true,
      errorMessage: '',
      followSubmitting: false
    };
  },
  watch: {
    '$route.params.userId': {
      immediate: false,
      handler(this: any) {
        void this.loadUserPage();
      }
    }
  },
  methods: {
    /**
     * Loads the public user detail, follow state, and user posts in parallel.
     */
    async loadUserPage(this: any) {
      const userId = this.resolveUserId();
      if (!userId) {
        this.errorMessage = '目标用户编号无效。';
        this.loading = false;
        return;
      }

      this.loading = true;
      this.errorMessage = '';

      try {
        const token = loadStoredToken();
        const [userDetail, followState, userPosts] = await Promise.all([
          fetchUserDetail(userId),
          token ? this.loadFollowStateOrNull(token, userId) : Promise.resolve(null),
          fetchUserPosts(userId)
        ]);
        this.userDetail = userDetail;
        this.followState = followState;
        this.userPosts = userPosts;
      } catch (error) {
        this.errorMessage = error instanceof Error ? error.message : '用户资料加载失败，请稍后重试。';
      } finally {
        this.loading = false;
      }
    },

    /**
     * Loads the follow state without breaking the page when the session expired.
     */
    async loadFollowStateOrNull(this: any, token: string, userId: number) {
      try {
        return await fetchFollowState(token, userId);
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          return null;
        }
        throw error;
      }
    },

    /**
     * Follows or unfollows the displayed user.
     */
    async toggleFollow(this: any) {
      const userId = this.resolveUserId();
      const token = loadStoredToken();

      if (!token) {
        this.redirectToLogin();
        return;
      }

      this.followSubmitting = true;

      try {
        const nextState = this.followState?.followed
          ? await unfollowUser(token, userId)
          : await followUser(token, userId);
        this.followState = nextState;
        if (this.userDetail) {
          this.userDetail.followerCount = nextState.followerCount;
          this.userDetail.followingCount = nextState.followingCount;
        }
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          this.redirectToLogin();
          return;
        }
        this.errorMessage = error instanceof Error ? error.message : '关注状态更新失败，请稍后重试。';
      } finally {
        this.followSubmitting = false;
      }
    },

    /**
     * Returns whether the displayed profile belongs to the currently logged-in user.
     */
    isOwnProfile(this: any) {
      const currentUser = loadStoredUser();
      const userId = this.resolveUserId();
      return Boolean(currentUser && userId && currentUser.id === userId);
    },

    /**
     * Returns whether a token is currently stored.
     */
    hasToken(this: any) {
      return Boolean(loadStoredToken());
    },

    /**
     * Parses the current route user id safely.
     */
    resolveUserId(this: any) {
      const rawUserId = Number(this.$route.params.userId);
      return Number.isFinite(rawUserId) && rawUserId > 0 ? rawUserId : 0;
    },

    /**
     * Parses a post id from a post object safely.
     */
    resolvePostId(this: any, postId: number | string | undefined) {
      const parsed = Number(postId);
      return Number.isFinite(parsed) && parsed > 0 ? parsed : 0;
    },

    /**
     * Resolves relative avatar paths returned by the backend.
     */
    resolveAvatarUrl(this: any, path: string | null | undefined) {
      return resolveAssetUrl(path);
    },

    /**
     * Returns the leading character used by avatar placeholders.
     */
    getInitial(this: any, value: string | null | undefined) {
      const fallback = value || 'K';
      return fallback.slice(0, 1).toUpperCase();
    },

    /**
     * Returns the display label for the follow/unfollow button.
     */
    getFollowActionLabel(this: any) {
      if (!this.hasToken()) {
        return '登录后关注';
      }
      return this.followState?.followed ? '已关注' : '关注';
    },

    /**
     * Formats timestamps into user-friendly text.
     */
    formatDateTime(this: any, value: string | null) {
      if (!value) {
        return '暂无记录';
      }

      const date = new Date(value);
      if (Number.isNaN(date.getTime())) {
        return value.replace('T', ' ');
      }

      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    /**
     * Redirects anonymous users to the login modal and preserves the current route.
     */
    redirectToLogin(this: any) {
      this.$router.push({
        path: '/',
        query: {
          auth: 'login',
          redirect: this.$route.fullPath
        }
      });
    }
  },
  mounted(this: any) {
    void this.loadUserPage();
  },
  template: `
    <main class="page-shell profile-shell">
      <section v-if="loading" class="panel page-state">
        <div class="page-state__content">
          <span class="eyebrow">正在展开</span>
          <h2>正在加载用户资料...</h2>
          <p>个人主页内容正在加载中，稍等一下就好。</p>
        </div>
      </section>

      <section v-else-if="errorMessage" class="panel page-state page-state--error">
        <div class="page-state__content">
          <span class="eyebrow">加载失败</span>
          <h2>用户资料暂时没有加载出来</h2>
          <p>{{ errorMessage }}</p>
          <button class="button button--primary" type="button" @click="loadUserPage">重新加载</button>
        </div>
      </section>

      <template v-else-if="userDetail">
        <section class="profile-hero">
          <div class="profile-hero__identity">
            <div class="profile-avatar profile-avatar--large">
              <img
                v-if="userDetail.avatar"
                :src="resolveAvatarUrl(userDetail.avatar)"
                :alt="(userDetail.nickname || userDetail.username || 'Kite User') + ' avatar'"
              />
              <span v-else>{{ getInitial(userDetail.nickname || userDetail.username) }}</span>
            </div>
            <div class="profile-hero__text">
              <span class="eyebrow">用户主页</span>
              <h1>{{ userDetail.nickname || userDetail.username || '社区成员' }}</h1>
              <p>{{ userDetail.signature || '这位用户还没有留下签名。' }}</p>
              <div class="profile-meta">
                <span>加入时间：{{ formatDateTime(userDetail.createTime) }}</span>
                <span>粉丝：{{ userDetail.followerCount ?? 0 }}</span>
                <span>关注：{{ userDetail.followingCount ?? 0 }}</span>
                <span>帖子：{{ userPosts.length }}</span>
              </div>
            </div>
          </div>

          <div class="profile-hero__actions">
            <button
              v-if="!isOwnProfile()"
              class="button"
              :class="followState?.followed ? 'button--followed' : 'button--follow'"
              type="button"
              :disabled="followSubmitting"
              @click="toggleFollow"
            >
              {{ followSubmitting ? '处理中...' : getFollowActionLabel() }}
            </button>
            <router-link
              v-if="!isOwnProfile() && hasToken()"
              class="button button--ghost"
              :to="'/messages?userId=' + resolveUserId()"
            >发私信</router-link>
            <router-link v-if="isOwnProfile()" class="button button--primary" to="/profile">编辑资料</router-link>
          </div>
        </section>

        <section class="content-grid profile-grid">
          <div class="content-main profile-main-stack">
            <section class="panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">用户动态</span>
                  <h2>发布的帖子</h2>
                </div>
                <span class="panel__link">{{ userPosts.length }} 篇帖子</span>
              </div>

              <div v-if="!userPosts.length" class="empty-state">
                <h3>还没有发布过帖子</h3>
                <p>这位用户暂时还没有发布任何内容。</p>
              </div>
              <div v-else class="post-stack">
                <article v-for="post in userPosts" :key="post.id" class="post-summary-card post-summary-card--static">
                  <div class="post-summary-card__head">
                    <span class="capsule">{{ post.badge || post.boardTagName }}</span>
                    <span class="post-summary-card__time">{{ formatDateTime(post.publishedAt) }}</span>
                  </div>
                  <h3>{{ post.title }}</h3>
                  <p>{{ post.summary }}</p>
                  <div class="post-summary-card__foot">
                    <div class="topic-card__stats">
                      <span class="topic-stat">{{ post.boardName }}</span>
                      <span class="topic-stat icon-eye">浏览 {{ post.viewCount }}</span>
                      <span class="topic-stat icon-comment">评论 {{ post.commentCount }}</span>
                      <span class="topic-stat icon-bookmark">收藏 {{ post.favoriteCount }}</span>
                      <span class="topic-stat icon-heart">点赞 {{ post.likeCount }}</span>
                    </div>
                    <div class="post-summary-card__actions">
                      <router-link class="button button--ghost button--small" :to="'/posts/' + post.id">查看详情</router-link>
                    </div>
                  </div>
                </article>
              </div>
            </section>
          </div>

          <aside class="content-side">
            <section class="panel soft-panel profile-side-card">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">用户信息</span>
                  <h2>基本资料</h2>
                </div>
              </div>

              <div class="profile-side-card__rows">
                <div class="profile-side-card__row">
                  <span>昵称</span>
                  <strong>{{ userDetail.nickname || userDetail.username || '未设置' }}</strong>
                </div>
                <div class="profile-side-card__row">
                  <span>个性签名</span>
                  <strong>{{ userDetail.signature || '暂未填写' }}</strong>
                </div>
                <div class="profile-side-card__row">
                  <span>所在城市</span>
                  <strong>{{ userDetail.city || '暂未填写' }}</strong>
                </div>
                <div class="profile-side-card__row">
                  <span>个人网站</span>
                  <strong>{{ userDetail.website || '暂未填写' }}</strong>
                </div>
                <div class="profile-side-card__row">
                  <span>加入时间</span>
                  <strong>{{ formatDateTime(userDetail.createTime) }}</strong>
                </div>
              </div>
            </section>

            <section class="panel soft-panel profile-side-card">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">社区数据</span>
                  <h2>互动概览</h2>
                </div>
              </div>

              <div class="profile-side-card__rows">
                <div class="profile-side-card__row">
                  <span>粉丝数</span>
                  <strong>{{ userDetail.followerCount ?? 0 }}</strong>
                </div>
                <div class="profile-side-card__row">
                  <span>关注数</span>
                  <strong>{{ userDetail.followingCount ?? 0 }}</strong>
                </div>
                <div class="profile-side-card__row">
                  <span>发布帖子</span>
                  <strong>{{ userPosts.length }}</strong>
                </div>
              </div>
            </section>

            <section v-if="userDetail.profile" class="panel soft-panel profile-side-card">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">自我介绍</span>
                  <h2>个人简介</h2>
                </div>
              </div>

              <div class="notice-list">
                <p>{{ userDetail.profile }}</p>
              </div>
            </section>
          </aside>
        </section>
      </template>
    </main>
  `
};
