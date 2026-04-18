import {
  ApiError,
  fetchPostsByBadge,
  fetchBoardList,
  type PostSummaryData,
  type BoardSummaryData
} from '../services/api';

const STRATEGY_BADGES = ['攻略', '刃影', '术士', '缚魂', '钢躯', '新手入门', '天命挑战', '无限流', '遗物', '首领'];
const PAGE_SIZE = 20;

/**
 * Strategy Guides aggregation page — filters community posts tagged as guides.
 */
export const StrategiesPage = {
  data() {
    return {
      posts: [] as PostSummaryData[],
      loading: true,
      error: '',
      activeBadge: '攻略',
      badges: STRATEGY_BADGES,
      offset: 0,
      hasMore: true,
      loadingMore: false,

      // 推荐版块
      relatedBoards: [] as BoardSummaryData[],
      boardsLoading: true
    };
  },

  methods: {
    async loadPageData(this: any) {
      this.loading = true;
      this.error = '';
      this.posts = [];
      this.offset = 0;
      this.hasMore = true;

      try {
        const [postsResult, boardsResult] = await Promise.allSettled([
          fetchPostsByBadge(this.activeBadge, PAGE_SIZE, 0),
          fetchBoardList()
        ]);

        if (postsResult.status === 'fulfilled') {
          this.posts = postsResult.value;
          if (postsResult.value.length < PAGE_SIZE) this.hasMore = false;
          this.offset = postsResult.value.length;
        } else {
          this.error = postsResult.reason instanceof ApiError
            ? postsResult.reason.message
            : '攻略加载失败，请稍后重试。';
        }

        if (boardsResult.status === 'fulfilled') {
          this.relatedBoards = boardsResult.value.slice(0, 6);
        }
      } finally {
        this.loading = false;
        this.boardsLoading = false;
      }
    },

    async switchBadge(this: any, badge: string) {
      if (this.activeBadge === badge) return;
      this.activeBadge = badge;
      await this.loadPageData();
    },

    async loadMore(this: any) {
      if (this.loadingMore || !this.hasMore) return;
      this.loadingMore = true;
      try {
        const more = await fetchPostsByBadge(this.activeBadge, PAGE_SIZE, this.offset);
        this.posts.push(...more);
        this.offset += more.length;
        if (more.length < PAGE_SIZE) this.hasMore = false;
      } catch (e) {
        // silently fail on load-more
      } finally {
        this.loadingMore = false;
      }
    },

    formatTime(this: any, dateStr: string | null): string {
      if (!dateStr) return '';
      const d = new Date(dateStr);
      const now = new Date();
      const diffMs = now.getTime() - d.getTime();
      const diffH = Math.floor(diffMs / 3600000);
      if (diffH < 1) return '刚刚';
      if (diffH < 24) return `${diffH}小时前`;
      const diffD = Math.floor(diffH / 24);
      if (diffD < 30) return `${diffD}天前`;
      return d.toLocaleDateString('zh-CN');
    },

    goToPost(this: any, postId: number) {
      this.$router.push(`/posts/${postId}`);
    },

    goToBoard(this: any, boardId: number) {
      this.$router.push(`/boards/${boardId}`);
    }
  },

  mounted(this: any) {
    this.loadPageData();
  },

  template: `
    <div class="strategies-page">
      <div class="page-hero">
        <div class="page-hero__eyebrow">攻略合集</div>
        <h1 class="page-hero__title">虚空回廊攻略</h1>
        <p class="page-hero__desc">社区玩家分享的职业构建、遗物搭配、首领攻略与天命通关指南</p>
      </div>

      <div class="strategies-layout">
        <!-- 主内容区 -->
        <div class="strategies-main">
          <!-- 话题筛选 -->
          <div class="sort-tabs" style="margin-bottom: 16px;">
            <button
              v-for="badge in badges"
              :key="badge"
              class="sort-tab"
              :class="{ 'sort-tab--active': activeBadge === badge }"
              @click="switchBadge(badge)"
            >
              {{ badge }}
            </button>
          </div>

          <div v-if="loading" class="loading-state">加载中...</div>
          <div v-else-if="error" class="error-state">{{ error }}</div>
          <div v-else-if="posts.length === 0" class="empty-state">
            <div class="empty-state__icon">🌀</div>
            <div class="empty-state__text">暂无「{{ activeBadge }}」相关攻略</div>
            <div class="empty-state__hint">成为第一个分享该主题攻略的探索者</div>
            <a href="#/compose" class="btn btn--primary" style="margin-top: 12px;">发布攻略</a>
          </div>
          <div v-else>
            <div class="post-list">
              <div
                v-for="post in posts"
                :key="post.id"
                class="post-summary-card"
                @click="goToPost(post.id)"
              >
                <div class="post-summary-card__header">
                  <span class="capsule capsule--strategy">{{ post.badge || activeBadge }}</span>
                  <span v-if="post.isAiGenerated" class="capsule capsule--ai">AI</span>
                </div>
                <h3 class="post-summary-card__title">{{ post.title }}</h3>
                <p class="post-summary-card__summary">{{ post.summary }}</p>
                <div class="post-summary-card__meta">
                  <span>{{ post.authorName }}</span>
                  <span>·</span>
                  <span>{{ formatTime(post.publishedAt) }}</span>
                  <span>·</span>
                  <span>👁 {{ post.viewCount }}</span>
                  <span>💬 {{ post.commentCount }}</span>
                  <span>❤ {{ post.likeCount }}</span>
                </div>
              </div>
            </div>

            <!-- 加载更多 -->
            <div v-if="hasMore" class="load-more-area">
              <button class="btn btn--ghost" @click="loadMore" :disabled="loadingMore">
                {{ loadingMore ? '加载中…' : '加载更多' }}
              </button>
            </div>
            <div v-else class="load-more-area" style="color: var(--text-secondary); font-size: 13px;">
              已显示全部 {{ posts.length }} 篇攻略
            </div>
          </div>
        </div>

        <!-- 侧边栏 -->
        <aside class="strategies-sidebar">
          <div class="sidebar-card">
            <div class="sidebar-card__title">发布攻略</div>
            <p class="sidebar-card__desc">分享你的构建思路、首领打法与天命通关心得，帮助更多探索者踏入虚空</p>
            <a href="#/compose" class="btn btn--primary" style="width: 100%; text-align: center; display: block;">
              + 发布攻略
            </a>
          </div>

          <div v-if="!boardsLoading && relatedBoards.length" class="sidebar-card">
            <div class="sidebar-card__title">相关版块</div>
            <div v-for="board in relatedBoards" :key="board.id" class="sidebar-link-card" @click="goToBoard(board.id)">
              <span class="sidebar-link-card__tag">{{ board.tagName }}</span>
              <span class="sidebar-link-card__name">{{ board.name }}</span>
            </div>
          </div>

          <div class="sidebar-card">
            <div class="sidebar-card__title">攻略分类</div>
            <div class="topic-tag-cloud">
              <span
                v-for="badge in badges"
                :key="badge"
                class="topic-tag capsule--clickable"
                :class="{ 'topic-tag--active': activeBadge === badge }"
                @click="switchBadge(badge)"
              >{{ badge }}</span>
            </div>
          </div>
        </aside>
      </div>
    </div>
  `
};
