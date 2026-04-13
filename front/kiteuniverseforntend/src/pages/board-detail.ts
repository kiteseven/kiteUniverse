import {
  fetchBoardDetail,
  fetchPostsByBoard,
  type BoardSummaryData,
  type PostSummaryData
} from '../services/api';

/**
 * Board-detail page backed by the real board and post APIs.
 */
export const BoardDetailPage = {
  data() {
    return {
      loading: true,
      errorMessage: '',
      board: null as BoardSummaryData | null,
      posts: [] as PostSummaryData[]
    };
  },
  watch: {
    '$route.params.boardId': {
      immediate: false,
      handler(this: any) {
        void this.loadBoardPage();
      }
    }
  },
  methods: {
    /**
     * Loads the board summary and its latest posts together.
     */
    async loadBoardPage(this: any) {
      const boardId = this.resolveBoardId();
      if (!boardId) {
        this.errorMessage = '目标版区编号无效。';
        this.loading = false;
        return;
      }

      this.loading = true;
      this.errorMessage = '';

      try {
        const [board, posts] = await Promise.all([
          fetchBoardDetail(boardId),
          fetchPostsByBoard(boardId, 20)
        ]);
        this.board = board;
        this.posts = posts;
      } catch (error) {
        this.errorMessage = error instanceof Error ? error.message : '版区内容加载失败，请稍后重试。';
      } finally {
        this.loading = false;
      }
    },

    /**
     * Parses the current route board id safely.
     */
    resolveBoardId(this: any) {
      const rawBoardId = Number(this.$route.params.boardId);
      return Number.isFinite(rawBoardId) && rawBoardId > 0 ? rawBoardId : 0;
    },

    /**
     * Formats timestamps into user-friendly text.
     */
    formatTime(this: any, value: string | null) {
      if (!value) {
        return '暂无更新';
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
    void this.loadBoardPage();
  },
  template: `
    <main class="page-shell detail-shell">
      <section v-if="loading" class="panel page-state">
        <div class="page-state__content">
          <span class="eyebrow">正在整理</span>
          <h2>正在加载版区内容...</h2>
          <p>版区简介和最新帖子正在整理中，马上就好。</p>
        </div>
      </section>

      <section v-else-if="errorMessage" class="panel page-state page-state--error">
        <div class="page-state__content">
          <span class="eyebrow">加载失败</span>
          <h2>版区内容暂时没有加载出来</h2>
          <p>{{ errorMessage }}</p>
          <button class="button button--primary" type="button" @click="loadBoardPage">重新加载</button>
        </div>
      </section>

      <template v-else-if="board">
        <section class="detail-hero">
          <div class="detail-hero__main">
            <span class="eyebrow">{{ board.tagName }}</span>
            <h1>{{ board.name }}</h1>
            <p>{{ board.description }}</p>
            <div class="hero__actions">
              <router-link
                class="button button--primary"
                :to="{ path: '/compose', query: { boardId: String(board.id) } }"
              >
                发布帖子
              </router-link>
              <router-link class="button button--ghost" to="/boards">返回版区</router-link>
            </div>
          </div>

          <div class="detail-hero__stats">
            <article class="detail-stat-card">
              <span>主题总数</span>
              <strong>{{ formatCount(board.topicCount) }}</strong>
              <p>这个版区里已经沉淀下来的主题讨论。</p>
            </article>
            <article class="detail-stat-card">
              <span>今日新增</span>
              <strong>{{ formatCount(board.todayPostCount) }}</strong>
              <p>当天在这个版区新发布的帖子数量。</p>
            </article>
            <article class="detail-stat-card">
              <span>最近更新</span>
              <strong>{{ formatTime(board.latestPublishedAt) }}</strong>
              <p>{{ board.latestPostTitle || '暂无帖子' }}</p>
            </article>
          </div>
        </section>

        <section class="content-grid detail-grid">
          <div class="content-main">
            <section class="panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">版区动态</span>
                  <h2>最新帖子</h2>
                </div>
                <span class="panel__link">{{ posts.length }} 篇内容</span>
              </div>

              <div v-if="!posts.length" class="empty-state">
                <h3>这个版区还没有帖子</h3>
                <p>先开一个话题，把这片版区慢慢聊热起来。</p>
              </div>

              <div v-else class="post-stack">
                <router-link
                  v-for="post in posts"
                  :key="post.id"
                  :to="'/posts/' + post.id"
                  class="post-summary-card"
                >
                  <div class="post-summary-card__head">
                    <span class="capsule">{{ post.badge || post.boardTagName }}</span>
                    <span class="topic-card__meta">{{ formatTime(post.publishedAt) }}</span>
                  </div>
                  <h3>{{ post.title }}</h3>
                  <p>{{ post.summary }}</p>
                  <div class="post-summary-card__foot">
                    <span class="topic-card__author">{{ post.authorName }}</span>
                    <div class="topic-card__stats">
                      <span class="topic-stat">浏览 {{ formatCount(post.viewCount) }}</span>
                      <span class="topic-stat">评论 {{ formatCount(post.commentCount) }}</span>
                      <span class="topic-stat">收藏 {{ formatCount(post.favoriteCount) }}</span>
                    </div>
                  </div>
                </router-link>
              </div>
            </section>
          </div>

          <aside class="content-side">
            <section class="panel soft-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">版区速览</span>
                  <h2>版区概览</h2>
                </div>
              </div>
              <div class="notice-list">
                <p>版区别名：{{ board.slug }}</p>
                <p>最近帖子：{{ board.latestPostTitle || '暂无帖子' }}</p>
                <p>点进帖子卡片就能继续查看正文和评论。</p>
              </div>
            </section>

            <section class="panel soft-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">发布建议</span>
                  <h2>发帖建议</h2>
                </div>
              </div>
              <div class="notice-list">
                <p>标题建议控制在 120 字以内，摘要控制在 255 字以内。</p>
                <p>攻略类内容尽量写清适用版本和适用阶段。</p>
                <p>发帖后记得回来看看回复，补充更新通常也很受欢迎。</p>
              </div>
            </section>
          </aside>
        </section>
      </template>
    </main>
  `
};
