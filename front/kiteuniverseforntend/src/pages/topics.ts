import {
  fetchPostsByBadge,
  type PostSummaryData
} from '../services/api';

const PAGE_SIZE = 20;

/**
 * Topics page — shows posts for a specific badge/tag with load-more pagination.
 */
export const TopicsPage = {
  data() {
    return {
      loading: true,
      loadingMore: false,
      errorMessage: '',
      badge: '',
      posts: [] as PostSummaryData[],
      offset: 0,
      hasMore: false
    };
  },
  watch: {
    '$route.params.badge': {
      immediate: false,
      handler(this: any) {
        void this.loadTopicPage();
      }
    }
  },
  methods: {
    /**
     * Loads the first page of posts for the current badge.
     */
    async loadTopicPage(this: any) {
      const badge = this.resolveBadge();
      if (!badge) {
        this.errorMessage = '话题标签无效。';
        this.loading = false;
        return;
      }

      this.badge = badge;
      this.loading = true;
      this.errorMessage = '';
      this.offset = 0;
      this.posts = [];

      try {
        const posts = await fetchPostsByBadge(badge, PAGE_SIZE, 0);
        this.posts = posts;
        this.offset = posts.length;
        this.hasMore = posts.length === PAGE_SIZE;
      } catch (error) {
        this.errorMessage = error instanceof Error ? error.message : '话题内容加载失败，请稍后重试。';
      } finally {
        this.loading = false;
      }
    },

    /**
     * Loads more posts for this badge.
     */
    async loadMore(this: any) {
      if (this.loadingMore || !this.hasMore) {
        return;
      }
      const badge = this.resolveBadge();
      if (!badge) {
        return;
      }

      this.loadingMore = true;
      try {
        const morePosts = await fetchPostsByBadge(badge, PAGE_SIZE, this.offset);
        this.posts = [...this.posts, ...morePosts];
        this.offset += morePosts.length;
        this.hasMore = morePosts.length === PAGE_SIZE;
      } catch (error) {
        this.errorMessage = error instanceof Error ? error.message : '加载更多内容失败，请稍后重试。';
      } finally {
        this.loadingMore = false;
      }
    },

    /**
     * Parses the badge from the route params.
     */
    resolveBadge(this: any) {
      const raw = this.$route.params.badge;
      if (typeof raw !== 'string' || !raw.trim()) {
        return '';
      }
      return decodeURIComponent(raw).trim();
    },

    /**
     * Navigates to the topics page for another badge.
     */
    goToBadge(this: any, badge: string) {
      if (badge !== this.badge) {
        this.$router.push('/topics/' + encodeURIComponent(badge));
      }
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
    void this.loadTopicPage();
  },
  template: `
    <main class="page-shell detail-shell">
      <section class="detail-hero">
        <div class="detail-hero__main">
          <span class="eyebrow">话题标签</span>
          <h1># {{ badge }}</h1>
          <p>聚合所有包含「{{ badge }}」标签的帖子内容。</p>
          <div class="hero__actions">
            <router-link class="button button--ghost" to="/discover">回到发现</router-link>
            <router-link class="button button--ghost" to="/boards">浏览版区</router-link>
          </div>
        </div>
      </section>

      <section class="content-grid detail-grid">
        <div class="content-main">
          <section class="panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">话题聚合</span>
                <h2># {{ badge }}</h2>
              </div>
              <span class="panel__link">{{ posts.length }}{{ hasMore ? '+' : '' }} 篇内容</span>
            </div>

            <section v-if="loading" class="panel page-state">
              <div class="page-state__content">
                <span class="eyebrow">正在整理</span>
                <h2>正在加载话题内容...</h2>
              </div>
            </section>

            <section v-else-if="errorMessage" class="panel page-state page-state--error">
              <div class="page-state__content">
                <span class="eyebrow">加载失败</span>
                <h2>{{ errorMessage }}</h2>
                <button class="button button--primary" type="button" @click="loadTopicPage">重新加载</button>
              </div>
            </section>

            <template v-else>
              <div v-if="!posts.length" class="empty-state">
                <h3>没有找到标签为「{{ badge }}」的帖子</h3>
                <p>可以去版区发一篇相关内容，或者浏览其他热门话题。</p>
              </div>

              <div v-else class="post-stack">
                <router-link
                  v-for="post in posts"
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
                      <span class="topic-stat icon-eye">浏览 {{ formatCount(post.viewCount) }}</span>
                      <span class="topic-stat icon-comment">评论 {{ formatCount(post.commentCount) }}</span>
                      <span class="topic-stat icon-bookmark">收藏 {{ formatCount(post.favoriteCount) }}</span>
                      <span class="topic-stat icon-heart">点赞 {{ formatCount(post.likeCount) }}</span>
                    </div>
                  </div>
                </router-link>
              </div>

              <div v-if="hasMore || loadingMore" class="load-more-area">
                <button
                  class="button button--ghost load-more-btn"
                  type="button"
                  :disabled="loadingMore"
                  @click="loadMore"
                >
                  {{ loadingMore ? '加载中...' : '加载更多' }}
                </button>
              </div>
              <div v-else-if="posts.length > 0 && !hasMore" class="load-more-area load-more-area--end">
                <span>已显示全部相关帖子</span>
              </div>
            </template>
          </section>
        </div>

        <aside class="content-side">
          <section class="panel soft-panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">话题说明</span>
                <h2>关于话题</h2>
              </div>
            </div>
            <div class="notice-list">
              <p>话题标签来自帖子的徽标字段，由发帖者在创作时设置。</p>
              <p>发帖时可以设置徽标来让你的内容出现在对应的话题聚合页。</p>
              <p>点击帖子卡片上的标签可以快速跳转到该话题页面。</p>
            </div>
          </section>
        </aside>
      </section>
    </main>
  `
};
