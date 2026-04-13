import {
  fetchHomePageData,
  type ContentMetricCard,
  type HomePageData,
  type HomeQuickSection,
  type HomeTimelineItem,
  type HomeTopicCard
} from '../services/api';
import heroLogo from '../assets/community-hall-logo.gif';

/**
 * Community homepage component backed by the content API.
 */
export const HomePage = {
  data() {
    return {
      loading: true,
      errorMessage: '',
      pageData: null as HomePageData | null
    };
  },
  methods: {
    /**
     * Loads homepage content from the backend cache endpoint.
     */
    async loadHomePage(this: any) {
      this.loading = true;
      this.errorMessage = '';

      try {
        this.pageData = await fetchHomePageData();
      } catch (error) {
        this.errorMessage = error instanceof Error
          ? error.message
          : '首页内容加载失败，请稍后重试。';
      } finally {
        this.loading = false;
      }
    },

    /**
     * Returns the metric list currently available on the page.
     */
    getHeroMetrics(this: any): ContentMetricCard[] {
      return this.pageData?.heroMetrics || [];
    },

    /**
     * Returns the featured topic cards currently available on the page.
     */
    getFeaturedTopics(this: any): HomeTopicCard[] {
      return this.pageData?.featuredTopics || [];
    },

    /**
     * Returns the shortcut sections currently available on the page.
     */
    getQuickSections(this: any): HomeQuickSection[] {
      return this.pageData?.quickSections || [];
    },

    /**
     * Returns the timeline items currently available on the page.
     */
    getTimeline(this: any): HomeTimelineItem[] {
      return this.pageData?.timeline || [];
    },

    /**
     * Returns the spotlight metrics currently available on the page.
     */
    getMoments(this: any): ContentMetricCard[] {
      return this.pageData?.moments || [];
    }
  },
  mounted(this: any) {
    void this.loadHomePage();
  },
  template: `
    <main class="page-shell home-shell">
      <section v-if="loading" class="panel page-state">
        <div class="page-state__content">
          <span class="eyebrow">正在整理</span>
          <h2>正在加载社区首页内容...</h2>
          <p>热门内容、社区快报和常用入口正在整理中，马上就好。</p>
        </div>
      </section>

      <section v-else-if="errorMessage" class="panel page-state page-state--error">
        <div class="page-state__content">
          <span class="eyebrow">加载失败</span>
          <h2>首页内容暂时没有加载出来</h2>
          <p>{{ errorMessage }}</p>
          <button class="button button--primary" type="button" @click="loadHomePage">重新加载</button>
        </div>
      </section>

      <template v-else-if="pageData">
        <section class="hero">
          <div class="hero__content">
            <span class="eyebrow">{{ pageData.hero.eyebrow }}</span>
            <h1>{{ pageData.hero.title }}</h1>
            <p>{{ pageData.hero.description }}</p>
            <div class="hero__actions">
              <router-link :to="pageData.hero.primaryActionLink" class="button button--primary">
                {{ pageData.hero.primaryActionLabel }}
              </router-link>
              <router-link :to="pageData.hero.secondaryActionLink" class="button button--ghost">
                {{ pageData.hero.secondaryActionLabel }}
              </router-link>
            </div>
            <div class="hero__metrics">
              <div v-for="metric in getHeroMetrics()" :key="metric.title">
                <span>{{ metric.title }}</span>
                <strong>{{ metric.value }}</strong>
              </div>
            </div>
          </div>

          <div class="hero__visual">
            <div class="hero__art">
              <img :src="'${heroLogo}'" :alt="pageData.hero.visualAlt" />
            </div>
            <div class="floating-card floating-card--top">
              <span>{{ pageData.hero.floatingTitle }}</span>
              <strong>{{ pageData.hero.floatingValue }}</strong>
              <p>{{ pageData.hero.floatingDescription }}</p>
            </div>
            <div class="floating-card floating-card--bottom">
              <span>{{ pageData.hero.secondaryFloatingTitle }}</span>
              <strong>{{ pageData.hero.secondaryFloatingValue }}</strong>
              <p>{{ pageData.hero.secondaryFloatingDescription }}</p>
            </div>
          </div>
        </section>

        <section class="content-grid">
          <div class="content-main">
            <section class="panel feature-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">精选内容</span>
                  <h2>首页推荐</h2>
                </div>
                <router-link to="/boards" class="panel__link">查看全部</router-link>
              </div>
              <div class="topic-grid">
                <router-link
                  v-for="topic in getFeaturedTopics()"
                  :key="topic.title + topic.link"
                  :to="topic.link"
                  class="topic-card topic-card--link"
                >
                  <div class="topic-card__head">
                    <span class="capsule">{{ topic.badge }}</span>
                    <span class="topic-card__meta">{{ topic.meta }}</span>
                  </div>
                  <h3>{{ topic.title }}</h3>
                  <p>{{ topic.excerpt }}</p>
                  <div class="topic-card__foot">
                    <span class="topic-card__author">{{ topic.author }}</span>
                    <div class="topic-card__stats">
                      <span v-for="stat in topic.stats" :key="stat" class="topic-stat">{{ stat }}</span>
                    </div>
                  </div>
                </router-link>
              </div>
            </section>

            <section class="panel timeline-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">最新动态</span>
                  <h2>社区快报</h2>
                </div>
                <router-link to="/boards" class="panel__link">查看更多</router-link>
              </div>
              <div class="timeline">
                <router-link
                  v-for="item in getTimeline()"
                  :key="item.time + item.title"
                  :to="item.link"
                  class="timeline-item timeline-link"
                >
                  <div class="timeline-item__time">{{ item.time }}</div>
                  <div class="timeline-item__content">
                    <h4>{{ item.title }}</h4>
                    <p>{{ item.description }}</p>
                  </div>
                </router-link>
              </div>
            </section>
          </div>

          <aside class="content-side">
            <section class="panel spotlight-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">数据看板</span>
                  <h2>今日现场</h2>
                </div>
              </div>
              <div class="stat-card-list">
                <article v-for="metric in getMoments()" :key="metric.title" class="stat-card">
                  <span class="stat-card__label">{{ metric.title }}</span>
                  <strong>{{ metric.value }}</strong>
                  <p>{{ metric.description }}</p>
                </article>
              </div>
            </section>

            <section v-for="section in getQuickSections()" :key="section.title" class="panel soft-panel">
              <div class="panel__header">
                <h3>{{ section.title }}</h3>
              </div>
              <div class="pill-list">
                <router-link
                  v-for="item in section.items"
                  :key="section.title + item.label"
                  :to="item.link"
                  class="pill"
                >
                  {{ item.label }}
                </router-link>
              </div>
            </section>
          </aside>
        </section>
      </template>
    </main>
  `
};
