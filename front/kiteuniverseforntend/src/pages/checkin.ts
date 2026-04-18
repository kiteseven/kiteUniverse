import {
  getCheckInStatus,
  doCheckIn,
  getMyBadges,
  type CheckInStatusData,
  type CheckInResultData,
  type UserBadgeData
} from '../services/api';
import { loadStoredToken } from '../services/session';

// ── helpers ───────────────────────────────────────────────────────────────────

const LEVEL_THRESHOLDS = [0, 100, 300, 700, 1500, 3000];

function levelProgress(points: number, level: number): number {
  if (level >= 6) return 100;
  const lo = LEVEL_THRESHOLDS[level - 1];
  const hi = LEVEL_THRESHOLDS[level];
  return Math.min(100, Math.round(((points - lo) / (hi - lo)) * 100));
}

function nextCheckInPoints(consecutiveDays: number): number {
  const next = consecutiveDays + 1;
  let bonus = 0;
  if (next % 30 === 0) bonus = 100;
  else if (next % 14 === 0) bonus = 40;
  else if (next % 7 === 0) bonus = 20;
  return 10 + bonus;
}

// ── component ─────────────────────────────────────────────────────────────────

export const CheckInPage = {
  data() {
    return {
      token: '' as string,
      status: null as CheckInStatusData | null,
      badges: [] as UserBadgeData[],
      loading: true,
      checkingIn: false,
      result: null as CheckInResultData | null,
      errorMsg: '' as string,
      calendarDays: [] as { label: string; done: boolean }[]
    };
  },

  async created(this: any) {
    this.token = loadStoredToken() || '';
    if (!this.token) { this.loading = false; return; }
    await this.loadData();
  },

  methods: {
    async loadData(this: any) {
      this.loading = true;
      this.errorMsg = '';
      try {
        const [statusRes, badgesRes] = await Promise.all([
          getCheckInStatus(this.token),
          getMyBadges(this.token)
        ]);
        this.status = statusRes;
        this.badges = badgesRes;
        this.buildCalendar();
      } catch (e: any) {
        this.errorMsg = e?.message || '加载失败，请刷新页面重试';
      } finally {
        this.loading = false;
      }
    },

    buildCalendar(this: any) {
      const days: { label: string; done: boolean }[] = [];
      const streak: number = this.status?.consecutiveDays ?? 0;
      const checked: boolean = this.status?.checkedInToday ?? false;
      const doneDays = checked ? Math.min(streak, 7) : Math.min(streak, 6);
      const weekDayLabels = ['日', '一', '二', '三', '四', '五', '六'];
      const today = new Date();
      for (let i = 6; i >= 0; i--) {
        const d = new Date(today);
        d.setDate(today.getDate() - i);
        const dayIndex = 6 - i;
        const isDone = dayIndex >= (7 - doneDays);
        days.push({ label: weekDayLabels[d.getDay()], done: isDone });
      }
      this.calendarDays = days;
    },

    progressPercent(this: any): number {
      if (!this.status) return 0;
      return levelProgress(this.status.points, this.status.level);
    },

    nextPoints(this: any): number {
      if (!this.status) return 10;
      return nextCheckInPoints(this.status.consecutiveDays);
    },

    async handleCheckIn(this: any) {
      if (!this.token || this.checkingIn) return;
      this.checkingIn = true;
      this.errorMsg = '';
      try {
        this.result = await doCheckIn(this.token);
        await this.loadData();
      } catch (e: any) {
        this.errorMsg = e?.message || '签到失败，请稍后重试';
      } finally {
        this.checkingIn = false;
      }
    },

    dismissResult(this: any) {
      this.result = null;
    }
  },

  template: `
    <div class="checkin-page page-container">
      <div class="page-hero">
        <div class="page-hero__eyebrow">激励系统</div>
        <h1 class="page-hero__title">每日签到</h1>
        <p class="page-hero__description">坚持签到积累积分，解锁等级与徽章成就</p>
      </div>

      <div v-if="!token" class="empty-state">
        <div class="empty-state__icon">🔒</div>
        <div class="empty-state__title">请先登录</div>
        <div class="empty-state__description">登录后即可参与每日签到和积分系统</div>
      </div>

      <div v-else-if="loading" class="loading-spinner">
        <div class="spinner"></div>
        <span>加载中…</span>
      </div>

      <template v-else>
        <!-- success result overlay -->
        <div v-if="result" class="checkin-result-banner">
          <div class="checkin-result-banner__inner">
            <div class="checkin-result-banner__close" @click="dismissResult">✕</div>
            <div class="checkin-result-banner__icon">✅</div>
            <div class="checkin-result-banner__title">签到成功！</div>
            <div class="checkin-result-banner__points">+{{ result.pointsEarned }} 积分</div>
            <div class="checkin-result-banner__meta">
              连续签到 {{ result.consecutiveDays }} 天 · 当前积分 {{ result.totalPoints }} · {{ result.levelName }}
            </div>
            <div v-if="result.leveledUp" class="checkin-result-banner__levelup">
              🎉 恭喜升级！当前等级 Lv{{ result.level }} {{ result.levelName }}
            </div>
            <div v-if="result.newBadges && result.newBadges.length" class="checkin-result-banner__badges">
              <div v-for="b in result.newBadges" :key="b.badgeType" class="new-badge-chip">
                <span class="new-badge-chip__icon">{{ b.icon }}</span>
                <span class="new-badge-chip__name">{{ b.name }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="checkin-layout">
          <!-- left: check-in card -->
          <div class="checkin-main">
            <div class="checkin-calendar panel">
              <div class="panel__header">
                <span class="panel__title">本周签到</span>
                <span class="checkin-streak-badge" v-if="status">
                  🔥 已连续 {{ status.consecutiveDays }} 天
                </span>
              </div>
              <div class="checkin-calendar__days">
                <div
                  v-for="(day, i) in calendarDays"
                  :key="i"
                  :class="['checkin-day', day.done ? 'checkin-day--done' : '']"
                >
                  <div class="checkin-day__dot">
                    <span v-if="day.done">✓</span>
                  </div>
                  <div class="checkin-day__label">{{ day.label }}</div>
                </div>
              </div>
            </div>

            <div class="panel checkin-action-panel">
              <div v-if="status && status.checkedInToday" class="checkin-done-msg">
                <div class="checkin-done-msg__icon">✅</div>
                <div class="checkin-done-msg__text">今日已签到，明天再来！</div>
                <div class="checkin-done-msg__sub">明日签到预计可获得 +{{ nextPoints() }} 积分</div>
              </div>
              <div v-else class="checkin-todo">
                <div class="checkin-todo__preview">今日签到可获得 <strong>+{{ nextPoints() }}</strong> 积分</div>
                <button
                  class="button button--primary checkin-btn"
                  :disabled="checkingIn"
                  @click="handleCheckIn"
                >
                  <span v-if="checkingIn">签到中…</span>
                  <span v-else>立即签到</span>
                </button>
              </div>
              <div v-if="errorMsg" class="form-error">{{ errorMsg }}</div>
            </div>

            <div class="panel checkin-rules-panel">
              <div class="panel__header">
                <span class="panel__title">积分获取途径</span>
              </div>
              <div class="checkin-rules">
                <div class="checkin-rule-item"><span class="checkin-rule-item__icon">📅</span><span class="checkin-rule-item__label">每日签到</span><span class="checkin-rule-item__points">+10</span></div>
                <div class="checkin-rule-item"><span class="checkin-rule-item__icon">🔥</span><span class="checkin-rule-item__label">连签 7 天额外奖励</span><span class="checkin-rule-item__points">+20</span></div>
                <div class="checkin-rule-item"><span class="checkin-rule-item__icon">🌕</span><span class="checkin-rule-item__label">连签 14 天额外奖励</span><span class="checkin-rule-item__points">+40</span></div>
                <div class="checkin-rule-item"><span class="checkin-rule-item__icon">🌕</span><span class="checkin-rule-item__label">连签 30 天额外奖励</span><span class="checkin-rule-item__points">+100</span></div>
                <div class="checkin-rule-item"><span class="checkin-rule-item__icon">✍️</span><span class="checkin-rule-item__label">发布帖子</span><span class="checkin-rule-item__points">+5</span></div>
                <div class="checkin-rule-item"><span class="checkin-rule-item__icon">💬</span><span class="checkin-rule-item__label">发表评论</span><span class="checkin-rule-item__points">+2</span></div>
                <div class="checkin-rule-item"><span class="checkin-rule-item__icon">👍</span><span class="checkin-rule-item__label">帖子被点赞（创作者）</span><span class="checkin-rule-item__points">+3</span></div>
                <div class="checkin-rule-item"><span class="checkin-rule-item__icon">⭐</span><span class="checkin-rule-item__label">帖子被收藏（创作者）</span><span class="checkin-rule-item__points">+5</span></div>
              </div>
            </div>
          </div>

          <!-- right: level & badges -->
          <div class="checkin-sidebar">
            <div class="panel level-panel" v-if="status">
              <div class="panel__header">
                <span class="panel__title">我的等级</span>
              </div>
              <div class="level-display">
                <div class="level-display__badge">Lv{{ status.level }}</div>
                <div class="level-display__name">{{ status.levelName }}</div>
                <div class="level-display__points">{{ status.points }} 积分</div>
              </div>
              <div class="level-progress">
                <div class="level-progress__bar">
                  <div class="level-progress__fill" :style="{ width: progressPercent() + '%' }"></div>
                </div>
                <div class="level-progress__label">
                  <span v-if="status.level < 6">距下一级还需 {{ status.nextLevelPoints - status.points }} 积分</span>
                  <span v-else>已达最高等级</span>
                </div>
              </div>
              <div class="level-tiers">
                <div
                  v-for="(entry, i) in [
                    {name:'探索者',pts:0},{name:'初入回廊',pts:100},{name:'深渊行者',pts:300},
                    {name:'虚空老兵',pts:700},{name:'裂隙征服者',pts:1500},{name:'虚空主宰',pts:3000}
                  ]"
                  :key="i"
                  :class="['level-tier', status.level === i+1 ? 'level-tier--active' : (status.level > i+1 ? 'level-tier--done' : '')]"
                >
                  <span class="level-tier__lv">Lv{{ i+1 }}</span>
                  <span class="level-tier__name">{{ entry.name }}</span>
                  <span class="level-tier__pts">{{ entry.pts }}+</span>
                </div>
              </div>
            </div>

            <div class="panel badges-panel">
              <div class="panel__header">
                <span class="panel__title">我的徽章</span>
                <span class="panel__count">{{ badges.length }}</span>
              </div>
              <div v-if="badges.length === 0" class="empty-state empty-state--compact">
                <div class="empty-state__icon">🏅</div>
                <div class="empty-state__title">暂无徽章</div>
                <div class="empty-state__description">签到、发帖、升级即可解锁</div>
              </div>
              <div v-else class="badges-grid">
                <div v-for="b in badges" :key="b.badgeType" class="badge-card" :title="b.description">
                  <div class="badge-card__icon">{{ b.icon }}</div>
                  <div class="badge-card__name">{{ b.name }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  `
};
