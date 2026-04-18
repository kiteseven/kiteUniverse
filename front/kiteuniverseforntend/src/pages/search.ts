import { fetchSearchStats, searchPosts, type PostSummaryData, type SearchStatsData } from '../services/api';

/**
 * Search results page — queries posts by keyword from the URL query string.
 */
export const SearchPage = {
  data() {
    return {
      loading: false,
      errorMessage: '',
      keyword: '',
      posts: [] as PostSummaryData[],
      stats: null as SearchStatsData | null
    };
  },
  watch: {
    '$route.query.keyword': {
      immediate: false,
      handler(this: any) {
        void this.loadSearchResults();
      }
    }
  },
  methods: {
    /**
     * Reads keyword from route and fetches matching posts.
     */
    async loadSearchResults(this: any) {
      const kw = String(this.$route.query.keyword || '').trim();
      this.keyword = kw;

      if (!kw) {
        this.posts = [];
        this.errorMessage = '';
        return;
      }

      this.loading = true;
      this.errorMessage = '';

      try {
        this.posts = await searchPosts(kw, 20);
      } catch (error) {
        this.errorMessage = error instanceof Error ? error.message : '搜索失败，请稍后重试。';
      } finally {
        this.loading = false;
      }
    },

    /**
     * Formats timestamps into user-friendly text.
     */
    formatTime(this: any, value: string | null) {
      if (!value) return '暂无时间';
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) return value.replace('T', ' ');
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
      if (safeValue >= 10000) return `${(safeValue / 1000).toFixed(1)}k`;
      return String(safeValue);
    },

    /**
     * Loads search statistics for the dashboard panel.
     */
    async loadStats(this: any) {
      try {
        this.stats = await fetchSearchStats(10);
      } catch {
        this.stats = null;
      }
    }
  },
  mounted(this: any) {
    void this.loadSearchResults();
    void this.loadStats();
  },
  template: `
    <main class="page-shell detail-shell">
      <section class="detail-hero">
        <div class="detail-hero__main">
          <span class="eyebrow">全站搜索</span>
          <h1 v-if="keyword">「{{ keyword }}」的搜索结果</h1>
          <h1 v-else>搜索帖子</h1>
          <p v-if="!loading && !errorMessage && keyword">
            共找到 {{ posts.length }} 篇相关内容，结果按最新时间排列。
          </p>
          <p v-else-if="!keyword">请在顶部搜索框输入关键字后按回车键开始搜索。</p>
          <div class="hero__actions">
            <router-link class="button button--ghost" to="/">回到首页</router-link>
          </div>
        </div>
      </section>

      <section class="content-grid detail-grid">
        <div class="content-main">
          <section class="panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">搜索结果</span>
                <h2>{{ keyword ? '「' + keyword + '」相关帖子' : '请输入关键字' }}</h2>
              </div>
              <span v-if="!loading && keyword" class="panel__link">{{ posts.length }} 篇内容</span>
            </div>

            <section v-if="loading" class="panel page-state">
              <div class="page-state__content">
                <span class="eyebrow">正在搜索</span>
                <h2>正在查找相关帖子...</h2>
                <p>正在全站搜索「{{ keyword }}」，马上就好。</p>
              </div>
            </section>

            <section v-else-if="errorMessage" class="panel page-state page-state--error">
              <div class="page-state__content">
                <span class="eyebrow">搜索失败</span>
                <h2>搜索结果暂时没有加载出来</h2>
                <p>{{ errorMessage }}</p>
                <button class="button button--primary" type="button" @click="loadSearchResults">重新搜索</button>
              </div>
            </section>

            <div v-else-if="!keyword" class="empty-state">
              <h3>输入关键字开始搜索</h3>
              <p>你可以搜索帖子标题、摘要或分类标签中的内容。</p>
            </div>

            <div v-else-if="!posts.length" class="empty-state">
              <h3>没有找到「{{ keyword }}」相关帖子</h3>
              <p>换个关键字再试试，或者去浏览版区里的热门内容。</p>
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
                <h3 v-if="post.highlightTitle" v-html="post.highlightTitle"></h3>
                <h3 v-else>{{ post.title }}</h3>
                <p v-if="post.highlightSnippet" v-html="post.highlightSnippet"></p>
                <p v-else>{{ post.summary }}</p>
                <div class="post-summary-card__foot">
                  <span class="topic-card__author">{{ post.authorName }}</span>
                  <div class="topic-card__stats">
                    <span class="topic-stat icon-eye">浏览 {{ formatCount(post.viewCount) }}</span>
                    <span class="topic-stat icon-comment">评论 {{ formatCount(post.commentCount) }}</span>
                    <span class="topic-stat icon-bookmark">收藏 {{ formatCount(post.favoriteCount) }}</span>
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
                <span class="panel__kicker">搜索说明</span>
                <h2>搜索范围</h2>
              </div>
            </div>
            <div class="notice-list">
              <p>搜索使用 Elasticsearch 全文检索，支持中文分词，匹配帖子标题、正文和分类标签。</p>
              <p>搜索结果按相关度排序，匹配关键字将以高亮形式展示。</p>
              <p>每次最多返回 20 条匹配结果。</p>
            </div>
          </section>

          <section v-if="stats" class="panel soft-panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">运营看板</span>
                <h2>搜索统计</h2>
              </div>
            </div>
            <div class="search-stats">
              <div class="search-stats__row">
                <span class="search-stats__label">总搜索次数</span>
                <strong class="search-stats__val">{{ stats.totalSearches }}</strong>
              </div>
              <div class="search-stats__row">
                <span class="search-stats__label">无结果次数</span>
                <strong class="search-stats__val">{{ stats.zeroResultSearches }}</strong>
              </div>
              <div class="search-stats__row">
                <span class="search-stats__label">无结果率</span>
                <strong class="search-stats__val" :class="stats.zeroResultRate > 30 ? 'search-stats__val--warn' : ''">
                  {{ stats.zeroResultRate }}%
                </strong>
              </div>
              <div v-if="stats.hotKeywords && stats.hotKeywords.length" class="search-stats__hot">
                <p class="search-stats__hot-label">热门搜索词</p>
                <div class="search-stats__tags">
                  <span
                    v-for="item in stats.hotKeywords"
                    :key="item.term"
                    class="search-stats__tag"
                    @click="$router.push({ path: '/search', query: { keyword: item.term } })"
                  >{{ item.term }} <small>{{ item.count }}</small></span>
                </div>
              </div>
            </div>
          </section>

          <section class="panel soft-panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">找不到？</span>
                <h2>其他入口</h2>
              </div>
            </div>
            <div class="notice-list" style="gap:10px;">
              <router-link class="sidebar-link-card" to="/boards">
                <strong>浏览全部版区</strong>
                <span>按主题分类找到你感兴趣的内容</span>
              </router-link>
              <router-link class="sidebar-link-card" to="/">
                <strong>回到首页看精选</strong>
                <span>社区编辑推荐的热门话题和攻略</span>
              </router-link>
            </div>
          </section>
        </aside>
      </section>
    </main>
  `
};
