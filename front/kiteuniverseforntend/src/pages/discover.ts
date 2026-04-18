import {
  fetchBoardList,
  fetchHotPosts,
  fetchRecommendedPosts,
  type BoardSummaryData,
  type PostSummaryData
} from '../services/api';
import { loadStoredToken } from '../services/session';

const COMMON_BADGES = ['特别企划', '玩家热议', '资料精选', '创作精选', '维护公告', '剧情讨论', '版本情报', '攻略', '官方内容', '玩家交流', '资料共建', '创作社区'];

/**
 * Discover page — shows hot posts, personalized recommendations, and topic tags.
 */
export const DiscoverPage = {
  data() {
    return {
      loading: true,
      errorMessage: '',
      hotPosts: [] as PostSummaryData[],
      hotDays: 7,
      recommendedPosts: [] as PostSummaryData[],
      boards: [] as BoardSummaryData[],
      hasToken: false
    };
  },
  methods: {
    /**
     * Loads hot posts, recommendations, and board list.
     */
    async loadPage(this: any) {
      this.loading = true;
      this.errorMessage = '';

      const token = loadStoredToken();
      this.hasToken = Boolean(token);

      try {
        const [hotPosts, boards] = await Promise.all([
          fetchHotPosts(20, this.hotDays),
          fetchBoardList()
        ]);
        this.hotPosts = hotPosts;
        this.boards = boards;

        if (token) {
          try {
            this.recommendedPosts = await fetchRecommendedPosts(token, 10);
          } catch {
            this.recommendedPosts = [];
          }
        }
      } catch (error) {
        this.errorMessage = error instanceof Error ? error.message : '内容加载失败，请稍后重试。';
      } finally {
        this.loading = false;
      }
    },

    /**
     * Changes the hot-posts time window and reloads.
     */
    async changeHotDays(this: any, days: number) {
      this.hotDays = days;
      try {
        this.hotPosts = await fetchHotPosts(20, days);
      } catch (error) {
        this.errorMessage = error instanceof Error ? error.message : '加载失败，请稍后重试。';
      }
    },

    /**
     * Returns the unique set of badge tags from hot posts for quick browsing.
     */
    getPopularBadges(this: any): string[] {
      const seen = new Set<string>();
      const result: string[] = [];
      for (const post of this.hotPosts) {
        const badge = post.badge || post.boardTagName;
        if (badge && !seen.has(badge)) {
          seen.add(badge);
          result.push(badge);
        }
      }
      // Fill with common badges not already shown
      for (const badge of COMMON_BADGES) {
        if (!seen.has(badge) && result.length < 16) {
          result.push(badge);
        }
      }
      return result.slice(0, 16);
    },

    /**
     * Navigates to the topics page for a badge.
     */
    goToBadge(this: any, badge: string) {
      this.$router.push('/topics/' + encodeURIComponent(badge));
    },

    /**
     * Formats timestamps.
     */
    formatTime(this: any, value: string | null) {
      if (!value) {
        return '';
      }
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) {
        return value.replace('T', ' ');
      }
      return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    /**
     * Formats raw counts for UI cards.
     */
    formatCount(this: any, value: number | null | undefined) {
      const safeValue = Number(value || 0);
      if (safeValue >= 10000) {
        return `${(safeValue / 1000).toFixed(1)}k`;
      }
      return String(safeValue);
    }
  },
  mounted(this: any) {
    void this.loadPage();
  },
  template: `
    <main class="page-shell discover-shell">
      <section class="detail-hero discover-hero">
        <div class="detail-hero__main">
          <span class="eyebrow">内容发现</span>
          <h1>发现热门内容</h1>
          <p>基于社区热度和你的互动记录，把近期最值得看的内容聚合到这里。</p>
          <div class="hero__actions">
            <router-link class="button button--primary" to="/boards">浏览版区</router-link>
            <router-link class="button button--ghost" to="/search">搜索内容</router-link>
          </div>
        </div>
      </section>

      <section v-if="loading" class="panel page-state">
        <div class="page-state__content">
          <span class="eyebrow">正在整理</span>
          <h2>正在加载热门内容...</h2>
          <p>马上就好。</p>
        </div>
      </section>

      <section v-else-if="errorMessage" class="panel page-state page-state--error">
        <div class="page-state__content">
          <span class="eyebrow">加载失败</span>
          <h2>内容加载失败</h2>
          <p>{{ errorMessage }}</p>
          <button class="button button--primary" type="button" @click="loadPage">重新加载</button>
        </div>
      </section>

      <template v-else>
        <section class="content-grid discover-grid">
          <div class="content-main">

            <!-- Hot posts -->
            <section class="panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">社区热度</span>
                  <h2>热门帖子</h2>
                </div>
                <div class="discover-time-tabs">
                  <button type="button" class="discover-time-tab" :class="{ 'discover-time-tab--active': hotDays === 3 }" @click="changeHotDays(3)">3 天</button>
                  <button type="button" class="discover-time-tab" :class="{ 'discover-time-tab--active': hotDays === 7 }" @click="changeHotDays(7)">7 天</button>
                  <button type="button" class="discover-time-tab" :class="{ 'discover-time-tab--active': hotDays === 30 }" @click="changeHotDays(30)">30 天</button>
                </div>
              </div>

              <div v-if="!hotPosts.length" class="empty-state">
                <h3>暂无热门帖子</h3>
                <p>社区最近活跃度还不够高，先去发几篇帖子吧。</p>
              </div>

              <div v-else class="post-stack">
                <router-link
                  v-for="(post, idx) in hotPosts"
                  :key="post.id"
                  :to="'/posts/' + post.id"
                  class="post-summary-card post-summary-card--ranked"
                >
                  <div class="post-summary-card__rank">{{ idx + 1 }}</div>
                  <div class="post-summary-card__content">
                    <div class="post-summary-card__head">
                      <span class="capsule capsule--clickable" @click.prevent.stop="goToBadge(post.badge || post.boardTagName)">{{ post.badge || post.boardTagName }}</span>
                      <span v-if="post.isAiGenerated" class="ai-badge ai-badge--inline">AI</span>
                      <span class="topic-card__meta">{{ formatTime(post.publishedAt) }}</span>
                    </div>
                    <h3>{{ post.title }}</h3>
                    <p>{{ post.summary }}</p>
                    <div class="post-summary-card__foot">
                      <span class="topic-card__author">
                        <a v-if="post.authorId" href="javascript:void(0)" @click.prevent.stop="$router.push('/users/' + post.authorId)">{{ post.authorName }}</a>
                        <template v-else>{{ post.authorName }}</template>
                      </span>
                      <div class="topic-card__stats">
                        <span class="topic-stat icon-eye">{{ formatCount(post.viewCount) }}</span>
                        <span class="topic-stat icon-comment">{{ formatCount(post.commentCount) }}</span>
                        <span class="topic-stat icon-bookmark">{{ formatCount(post.favoriteCount) }}</span>
                        <span class="topic-stat icon-heart">{{ formatCount(post.likeCount) }}</span>
                      </div>
                    </div>
                  </div>
                </router-link>
              </div>
            </section>

            <!-- Personalized recommendations -->
            <section v-if="hasToken" class="panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">为你推荐</span>
                  <h2>个性化推荐</h2>
                </div>
              </div>

              <div v-if="!recommendedPosts.length" class="empty-state">
                <h3>暂时没有个性化推荐</h3>
                <p>多浏览、点赞、收藏一些帖子之后，这里会出现根据你的兴趣挑选的内容。</p>
              </div>

              <div v-else class="post-stack">
                <router-link
                  v-for="post in recommendedPosts"
                  :key="post.id"
                  :to="'/posts/' + post.id"
                  class="post-summary-card"
                >
                  <div class="post-summary-card__head">
                    <span class="capsule capsule--clickable" @click.prevent.stop="goToBadge(post.badge || post.boardTagName)">{{ post.badge || post.boardTagName }}</span>
                    <span v-if="post.isAiGenerated" class="ai-badge ai-badge--inline">AI</span>
                    <span class="topic-card__meta">{{ formatTime(post.publishedAt) }}</span>
                  </div>
                  <h3>{{ post.title }}</h3>
                  <p>{{ post.summary }}</p>
                  <div class="post-summary-card__foot">
                    <span class="topic-card__author">
                      <a v-if="post.authorId" href="javascript:void(0)" @click.prevent.stop="$router.push('/users/' + post.authorId)">{{ post.authorName }}</a>
                      <template v-else>{{ post.authorName }}</template>
                    </span>
                    <div class="topic-card__stats">
                      <span class="topic-stat icon-eye">{{ formatCount(post.viewCount) }}</span>
                      <span class="topic-stat icon-comment">{{ formatCount(post.commentCount) }}</span>
                      <span class="topic-stat icon-bookmark">{{ formatCount(post.favoriteCount) }}</span>
                      <span class="topic-stat icon-heart">{{ formatCount(post.likeCount) }}</span>
                    </div>
                  </div>
                </router-link>
              </div>
            </section>

            <section v-else class="panel soft-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">为你推荐</span>
                  <h2>个性化推荐</h2>
                </div>
              </div>
              <div class="empty-state">
                <h3>登录后查看个性化推荐</h3>
                <p>根据你在社区的互动记录，推荐来自你感兴趣版区的热门内容。</p>
                <router-link class="button button--primary" to="/?auth=login">立即登录</router-link>
              </div>
            </section>

          </div>

          <aside class="content-side">
            <!-- Topic tags -->
            <section class="panel soft-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">话题标签</span>
                  <h2>热门话题</h2>
                </div>
              </div>
              <div class="topic-tag-cloud">
                <button
                  v-for="badge in getPopularBadges()"
                  :key="badge"
                  type="button"
                  class="topic-tag"
                  @click="goToBadge(badge)"
                >
                  {{ badge }}
                </button>
              </div>
            </section>

            <!-- Board links -->
            <section class="panel soft-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">版区导航</span>
                  <h2>全部版区</h2>
                </div>
              </div>
              <div class="sidebar-board-list">
                <router-link
                  v-for="board in boards"
                  :key="board.id"
                  :to="'/boards/' + board.id"
                  class="sidebar-link-card"
                >
                  <span class="capsule">{{ board.tagName }}</span>
                  <span class="sidebar-link-card__name">{{ board.name }}</span>
                </router-link>
              </div>
            </section>
          </aside>
        </section>
      </template>
    </main>
  `
};
