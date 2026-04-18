import {
  fetchBoardsPageData,
  type BoardGroup,
  type BoardsPageData,
  type ContentMetricCard
} from '../services/api';

/**
 * Boards page component backed by the new content API.
 */
export const BoardsPage = {
  data() {
    return {
      loading: true,
      errorMessage: '',
      pageData: null as BoardsPageData | null
    };
  },
  methods: {
    /**
     * Loads boards-page content from the backend cache endpoint.
     */
    async loadBoardsPage(this: any) {
      this.loading = true;
      this.errorMessage = '';

      try {
        this.pageData = await fetchBoardsPageData();
      } catch (error) {
        this.errorMessage = error instanceof Error
          ? error.message
          : '版区内容加载失败，请稍后重试。';
      } finally {
        this.loading = false;
      }
    },

    /**
     * Returns the hero metrics currently available on the page.
     */
    getHeroMetrics(this: any): ContentMetricCard[] {
      return this.pageData?.hero.metrics || [];
    },

    /**
     * Returns the board groups currently available on the page.
     */
    getBoardGroups(this: any): BoardGroup[] {
      return this.pageData?.boardGroups || [];
    },

    /**
     * Returns the side-panel overview cards currently available on the page.
     */
    getOverviewCards(this: any): ContentMetricCard[] {
      return this.pageData?.overviewCards || [];
    }
  },
  mounted(this: any) {
    void this.loadBoardsPage();
  },
  template: `
    <main class="page-shell boards-shell">
      <section v-if="loading" class="panel page-state">
        <div class="page-state__content">
          <span class="eyebrow">正在整理</span>
          <h2>正在加载版区导航...</h2>
          <p>正在汇总各个版区的最新动态，稍等一下就能进入常逛分区。</p>
        </div>
      </section>

      <section v-else-if="errorMessage" class="panel page-state page-state--error">
        <div class="page-state__content">
          <span class="eyebrow">加载失败</span>
          <h2>版区内容暂时没有加载出来</h2>
          <p>{{ errorMessage }}</p>
          <button class="button button--primary" type="button" @click="loadBoardsPage">重新加载</button>
        </div>
      </section>

      <template v-else-if="pageData">
        <section class="sub-hero">
          <div>
            <span class="eyebrow">{{ pageData.hero.eyebrow }}</span>
            <h1>{{ pageData.hero.title }}</h1>
            <p>{{ pageData.hero.description }}</p>
          </div>
          <div class="sub-hero__meta">
            <div v-for="metric in getHeroMetrics()" :key="metric.title">
              <span>{{ metric.title }}</span>
              <strong>{{ metric.value }}</strong>
            </div>
          </div>
        </section>

        <section class="content-grid boards-grid">
          <div class="content-main">
            <section class="panel boards-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">内容分区</span>
                  <h2>推荐版区</h2>
                </div>
                <router-link to="/compose" class="panel__link">发布帖子</router-link>
              </div>
              <div class="board-list">
                <router-link
                  v-for="group in getBoardGroups()"
                  :key="group.title + group.actionLink"
                  :to="group.actionLink"
                  class="board-card board-card--link"
                >
                  <div class="board-card__top">
                    <span class="capsule">{{ group.tag }}</span>
                  </div>
                  <div class="board-card__body">
                    <div>
                      <h3>{{ group.title }}</h3>
                      <p>{{ group.description }}</p>
                    </div>
                    <div class="board-card__stats">
                      <span v-for="stat in group.stats" :key="stat" class="board-chip">{{ stat }}</span>
                    </div>
                  </div>
                  <div class="board-card__footer">
                    <span>{{ group.update }}</span>
                    <span class="board-card__action">{{ group.actionLabel }}</span>
                  </div>
                </router-link>
              </div>
            </section>
          </div>

          <aside class="content-side">
            <section class="panel soft-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">使用提示</span>
                  <h2>发帖前先看</h2>
                </div>
              </div>
              <div class="notice-list">
                <p v-for="notice in pageData.notices" :key="notice">{{ notice }}</p>
              </div>
            </section>

            <section class="panel soft-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">版区数据</span>
                  <h2>社区概览</h2>
                </div>
              </div>
              <div class="overview-grid">
                <article v-for="metric in getOverviewCards()" :key="metric.title" class="overview-card">
                  <span>{{ metric.title }}</span>
                  <strong>{{ metric.value }}</strong>
                </article>
              </div>
            </section>
          </aside>
        </section>
      </template>
    </main>
  `
};
