import {
  ApiError,
  fetchGameAccounts,
  bindGameAccount,
  updateGameAccount,
  unbindGameAccount,
  fetchGameCharacters,
  addGameCharacter,
  updateGameCharacter,
  deleteGameCharacter,
  fetchGameStats,
  updateGameStats,
  type GameAccountData,
  type GameCharacterData,
  type GameStatsData
} from '../services/api';
import { loadStoredToken } from '../services/session';

const CLASS_OPTIONS = ['刃影', '术士', '缚魂', '钢躯'];
const SERVER_OPTIONS = ['亚洲正式服', '欧洲正式服', '北美正式服', '测试服'];
const ACT_LABELS = ['未通关', '通关第一幕', '通关第二幕', '全通（第三幕）'];

/**
 * Game Tools page — 虚空回廊助手：账号绑定、跑图记录、游戏数据。
 */
export const GameToolsPage = {
  data() {
    return {
      activeTab: 'stats' as 'stats' | 'runs' | 'accounts',

      // 游戏数据
      stats: null as GameStatsData | null,
      statsLoading: true,
      statsError: '',
      statsSaving: false,
      statsForm: { actionPoint: 0, maxActionPoint: 240, voidShards: 0, accountLevel: 0, totalRuns: 0 },
      statsEditing: false,

      // 跑图记录
      runRecords: [] as GameCharacterData[],
      runsLoading: true,
      runsError: '',
      showAddRun: false,
      runForm: {
        classId: 'blade',
        className: '刃影',
        ascensionLevel: 0,
        actReached: 3,
        floorReached: 50,
        score: 0,
        keyRelic: ''
      },
      runSaving: false,
      editingRunId: null as number | null,

      // 游戏账号
      accounts: [] as GameAccountData[],
      accountsLoading: true,
      accountsError: '',
      showAddAccount: false,
      accountForm: { gameUid: '', serverName: '亚洲正式服', inGameName: '', accountLevel: 0 },
      accountSaving: false,
      editingAccountId: null as number | null,

      classOptions: CLASS_OPTIONS,
      serverOptions: SERVER_OPTIONS,
      actLabels: ACT_LABELS,
      token: ''
    };
  },

  computed: {
    actionPointPercent(this: any) {
      if (!this.stats || !this.stats.maxActionPoint) return 0;
      return Math.round((this.stats.actionPoint / this.stats.maxActionPoint) * 100);
    },
    sortedRuns(this: any): GameCharacterData[] {
      return [...this.runRecords].sort((a, b) => {
        if (b.ascensionLevel !== a.ascensionLevel) return b.ascensionLevel - a.ascensionLevel;
        if (b.actReached !== a.actReached) return b.actReached - a.actReached;
        return b.score - a.score;
      });
    }
  },

  methods: {
    async loadPageData(this: any) {
      this.token = loadStoredToken() || '';
      if (!this.token) {
        this.$router.push('/?auth=login');
        return;
      }
      await Promise.all([this.loadStats(), this.loadRuns(), this.loadAccounts()]);
    },

    // ── 游戏数据 ──────────────────────────────────────────────────────────────

    async loadStats(this: any) {
      this.statsLoading = true;
      this.statsError = '';
      try {
        this.stats = await fetchGameStats(this.token);
        if (this.stats) {
          this.statsForm = {
            actionPoint: this.stats.actionPoint,
            maxActionPoint: this.stats.maxActionPoint,
            voidShards: this.stats.voidShards,
            accountLevel: this.stats.accountLevel,
            totalRuns: this.stats.totalRuns
          };
        }
      } catch (e) {
        this.statsError = e instanceof ApiError ? e.message : '数据加载失败';
      } finally {
        this.statsLoading = false;
      }
    },

    async saveStats(this: any) {
      this.statsSaving = true;
      try {
        this.stats = await updateGameStats(this.token, this.statsForm);
        this.statsEditing = false;
      } catch (e) {
        alert(e instanceof ApiError ? e.message : '保存失败');
      } finally {
        this.statsSaving = false;
      }
    },

    // ── 跑图记录 ──────────────────────────────────────────────────────────────

    async loadRuns(this: any) {
      this.runsLoading = true;
      this.runsError = '';
      try {
        this.runRecords = await fetchGameCharacters(this.token);
      } catch (e) {
        this.runsError = e instanceof ApiError ? e.message : '加载失败';
      } finally {
        this.runsLoading = false;
      }
    },

    resetRunForm(this: any) {
      this.runForm = { classId: 'blade', className: '刃影', ascensionLevel: 0, actReached: 3, floorReached: 50, score: 0, keyRelic: '' };
      this.editingRunId = null;
    },

    openEditRun(this: any, run: GameCharacterData) {
      this.editingRunId = run.id;
      this.runForm = {
        classId: run.classId,
        className: run.className,
        ascensionLevel: run.ascensionLevel,
        actReached: run.actReached,
        floorReached: run.floorReached,
        score: run.score,
        keyRelic: run.keyRelic
      };
      this.showAddRun = true;
    },

    async saveRun(this: any) {
      if (!this.runForm.className.trim()) {
        alert('请选择职业');
        return;
      }
      this.runSaving = true;
      try {
        if (this.editingRunId) {
          const updated = await updateGameCharacter(this.token, this.editingRunId, this.runForm);
          const idx = this.runRecords.findIndex((r: GameCharacterData) => r.id === this.editingRunId);
          if (idx >= 0) this.runRecords[idx] = updated;
        } else {
          const added = await addGameCharacter(this.token, this.runForm);
          this.runRecords.push(added);
        }
        this.showAddRun = false;
        this.resetRunForm();
      } catch (e) {
        alert(e instanceof ApiError ? e.message : '保存失败');
      } finally {
        this.runSaving = false;
      }
    },

    async removeRun(this: any, id: number) {
      if (!confirm('确认删除该跑图记录？')) return;
      try {
        await deleteGameCharacter(this.token, id);
        this.runRecords = this.runRecords.filter((r: GameCharacterData) => r.id !== id);
      } catch (e) {
        alert(e instanceof ApiError ? e.message : '删除失败');
      }
    },

    // ── 游戏账号 ──────────────────────────────────────────────────────────────

    async loadAccounts(this: any) {
      this.accountsLoading = true;
      this.accountsError = '';
      try {
        this.accounts = await fetchGameAccounts(this.token);
      } catch (e) {
        this.accountsError = e instanceof ApiError ? e.message : '加载失败';
      } finally {
        this.accountsLoading = false;
      }
    },

    resetAccountForm(this: any) {
      this.accountForm = { gameUid: '', serverName: '亚洲正式服', inGameName: '', accountLevel: 0 };
      this.editingAccountId = null;
    },

    openEditAccount(this: any, acc: GameAccountData) {
      this.editingAccountId = acc.id;
      this.accountForm = {
        gameUid: acc.gameUid,
        serverName: acc.serverName,
        inGameName: acc.inGameName,
        accountLevel: acc.accountLevel
      };
      this.showAddAccount = true;
    },

    async saveAccount(this: any) {
      if (!this.accountForm.gameUid.trim()) {
        alert('请输入游戏UID');
        return;
      }
      this.accountSaving = true;
      try {
        if (this.editingAccountId) {
          const updated = await updateGameAccount(this.token, this.editingAccountId, this.accountForm);
          const idx = this.accounts.findIndex((a: GameAccountData) => a.id === this.editingAccountId);
          if (idx >= 0) this.accounts[idx] = updated;
        } else {
          const added = await bindGameAccount(this.token, this.accountForm);
          this.accounts.push(added);
        }
        this.showAddAccount = false;
        this.resetAccountForm();
      } catch (e) {
        alert(e instanceof ApiError ? e.message : '保存失败');
      } finally {
        this.accountSaving = false;
      }
    },

    async removeAccount(this: any, id: number) {
      if (!confirm('确认解绑该游戏账号？')) return;
      try {
        await unbindGameAccount(this.token, id);
        this.accounts = this.accounts.filter((a: GameAccountData) => a.id !== id);
      } catch (e) {
        alert(e instanceof ApiError ? e.message : '解绑失败');
      }
    },

    // ── 工具方法 ──────────────────────────────────────────────────────────────

    actLabel(actReached: number) {
      return ACT_LABELS[actReached] ?? `第${actReached}幕`;
    },

    ascensionLabel(level: number) {
      return level === 0 ? 'A0（标准）' : `A${level}`;
    },

    formatDate(dateStr: string | null) {
      if (!dateStr) return '—';
      return new Date(dateStr).toLocaleDateString('zh-CN');
    }
  },

  mounted(this: any) {
    this.loadPageData();
  },

  template: `
    <div class="game-tools-page">
      <!-- 页面标题 -->
      <div class="page-hero">
        <div class="page-hero__eyebrow">游戏工具</div>
        <h1 class="page-hero__title">虚空回廊助手</h1>
        <p class="page-hero__desc">管理账号、记录跑图历程、追踪行动力与虚空碎片</p>
      </div>

      <!-- Tab 导航 -->
      <div class="game-tools-tabs">
        <button class="game-tab" :class="{ 'game-tab--active': activeTab === 'stats' }" @click="activeTab = 'stats'">
          游戏数据
        </button>
        <button class="game-tab" :class="{ 'game-tab--active': activeTab === 'runs' }" @click="activeTab = 'runs'">
          跑图记录
          <span v-if="runRecords.length" class="game-tab__count">{{ runRecords.length }}</span>
        </button>
        <button class="game-tab" :class="{ 'game-tab--active': activeTab === 'accounts' }" @click="activeTab = 'accounts'">
          游戏账号
        </button>
      </div>

      <!-- ── 游戏数据 ── -->
      <div v-if="activeTab === 'stats'" class="game-section">
        <div v-if="statsLoading" class="loading-state">加载中...</div>
        <div v-else-if="statsError" class="error-state">{{ statsError }}</div>
        <div v-else>
          <div v-if="stats && !statsEditing" class="stats-grid">
            <div class="stat-card stat-card--sanity">
              <div class="stat-card__label">行动力</div>
              <div class="stat-card__value">{{ stats.actionPoint }}<span class="stat-card__unit"> / {{ stats.maxActionPoint }}</span></div>
              <div class="stat-progress">
                <div class="stat-progress__bar" :style="{ width: actionPointPercent + '%' }"></div>
              </div>
              <div class="stat-card__sub">{{ actionPointPercent }}%</div>
            </div>
            <div class="stat-card">
              <div class="stat-card__label">虚空碎片</div>
              <div class="stat-card__value stat-card__value--mono">{{ stats.voidShards.toLocaleString() }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-card__label">探索者等级</div>
              <div class="stat-card__value stat-card__value--mono">Lv.{{ stats.accountLevel }}</div>
            </div>
            <div class="stat-card">
              <div class="stat-card__label">总跑图次数</div>
              <div class="stat-card__value stat-card__value--mono">{{ stats.totalRuns }}</div>
            </div>
          </div>

          <div v-else-if="!stats && !statsEditing" class="empty-state">
            <div class="empty-state__icon">🌀</div>
            <div class="empty-state__text">暂无数据，点击下方按钮录入游戏数据</div>
          </div>

          <div v-if="statsEditing" class="game-form">
            <div class="game-form__row">
              <label class="game-form__label">当前行动力</label>
              <input class="game-form__input" type="number" v-model.number="statsForm.actionPoint" min="0" :max="statsForm.maxActionPoint" />
            </div>
            <div class="game-form__row">
              <label class="game-form__label">行动力上限</label>
              <input class="game-form__input" type="number" v-model.number="statsForm.maxActionPoint" min="120" max="999" />
            </div>
            <div class="game-form__row">
              <label class="game-form__label">虚空碎片</label>
              <input class="game-form__input" type="number" v-model.number="statsForm.voidShards" min="0" />
            </div>
            <div class="game-form__row">
              <label class="game-form__label">探索者等级</label>
              <input class="game-form__input" type="number" v-model.number="statsForm.accountLevel" min="0" max="999" />
            </div>
            <div class="game-form__row">
              <label class="game-form__label">总跑图次数</label>
              <input class="game-form__input" type="number" v-model.number="statsForm.totalRuns" min="0" />
            </div>
            <div class="game-form__actions">
              <button class="btn btn--primary" @click="saveStats" :disabled="statsSaving">{{ statsSaving ? '保存中…' : '保存' }}</button>
              <button class="btn btn--ghost" @click="statsEditing = false">取消</button>
            </div>
          </div>

          <div v-if="!statsEditing" class="game-section__footer">
            <button class="btn btn--primary" @click="statsEditing = true">{{ stats ? '更新数据' : '录入数据' }}</button>
            <span v-if="stats" class="game-section__hint">最后更新：{{ formatDate(stats.updateTime) }}</span>
          </div>
        </div>
      </div>

      <!-- ── 跑图记录 ── -->
      <div v-if="activeTab === 'runs'" class="game-section">
        <div class="game-section__header">
          <span>共 {{ runRecords.length }} 条记录</span>
          <button class="btn btn--primary btn--sm" @click="resetRunForm(); showAddRun = true">+ 记录跑图</button>
        </div>

        <div v-if="runsLoading" class="loading-state">加载中...</div>
        <div v-else-if="runsError" class="error-state">{{ runsError }}</div>
        <div v-else-if="runRecords.length === 0" class="empty-state">
          <div class="empty-state__icon">🗡️</div>
          <div class="empty-state__text">还没有跑图记录，开始你的虚空探索吧</div>
        </div>
        <div v-else class="character-grid">
          <div
            v-for="run in sortedRuns"
            :key="run.id"
            class="character-card"
            :class="'character-card--rarity-' + Math.min(run.ascensionLevel, 6)"
          >
            <div class="character-card__rarity">A{{ run.ascensionLevel }}</div>
            <div class="character-card__name">{{ run.className }}</div>
            <div class="character-card__meta">
              <span class="character-card__elite">{{ actLabel(run.actReached) }}</span>
              <span class="character-card__level">第 {{ run.floorReached }} 层</span>
              <span class="character-card__trust">{{ run.score.toLocaleString() }}分</span>
            </div>
            <div v-if="run.keyRelic" class="character-card__skin">遗物：{{ run.keyRelic }}</div>
            <div class="character-card__actions">
              <button class="btn btn--ghost btn--xs" @click="openEditRun(run)">编辑</button>
              <button class="btn btn--danger btn--xs" @click="removeRun(run.id)">删除</button>
            </div>
          </div>
        </div>

        <!-- 添加/编辑跑图弹层 -->
        <div v-if="showAddRun" class="modal-overlay" @click.self="showAddRun = false">
          <div class="modal-box">
            <h3 class="modal-box__title">{{ editingRunId ? '编辑跑图记录' : '记录新跑图' }}</h3>
            <div class="game-form">
              <div class="game-form__row">
                <label class="game-form__label">职业 *</label>
                <select class="game-form__input" v-model="runForm.className" @change="runForm.classId = runForm.className.toLowerCase()">
                  <option v-for="cls in classOptions" :key="cls" :value="cls">{{ cls }}</option>
                </select>
              </div>
              <div class="game-form__row">
                <label class="game-form__label">天命等级</label>
                <input class="game-form__input" type="number" v-model.number="runForm.ascensionLevel" min="0" max="20"
                  placeholder="0 = A0（标准），最高 A20" />
              </div>
              <div class="game-form__row">
                <label class="game-form__label">通关幕数</label>
                <select class="game-form__input" v-model.number="runForm.actReached">
                  <option v-for="(label, idx) in actLabels" :key="idx" :value="idx">{{ label }}</option>
                </select>
              </div>
              <div class="game-form__row">
                <label class="game-form__label">最高层数</label>
                <input class="game-form__input" type="number" v-model.number="runForm.floorReached" min="1" max="99" />
              </div>
              <div class="game-form__row">
                <label class="game-form__label">得分</label>
                <input class="game-form__input" type="number" v-model.number="runForm.score" min="0" />
              </div>
              <div class="game-form__row">
                <label class="game-form__label">核心遗物</label>
                <input class="game-form__input" type="text" v-model="runForm.keyRelic" placeholder="本局最关键的遗物（可选）" />
              </div>
              <div class="game-form__actions">
                <button class="btn btn--primary" @click="saveRun" :disabled="runSaving">{{ runSaving ? '保存中…' : '保存' }}</button>
                <button class="btn btn--ghost" @click="showAddRun = false">取消</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ── 游戏账号 ── -->
      <div v-if="activeTab === 'accounts'" class="game-section">
        <div class="game-section__header">
          <span>已绑定 {{ accounts.length }} 个账号</span>
          <button class="btn btn--primary btn--sm" @click="resetAccountForm(); showAddAccount = true">+ 绑定账号</button>
        </div>

        <div v-if="accountsLoading" class="loading-state">加载中...</div>
        <div v-else-if="accountsError" class="error-state">{{ accountsError }}</div>
        <div v-else-if="accounts.length === 0" class="empty-state">
          <div class="empty-state__icon">🎮</div>
          <div class="empty-state__text">尚未绑定游戏账号，点击上方按钮绑定</div>
        </div>
        <div v-else class="account-list">
          <div v-for="acc in accounts" :key="acc.id" class="account-card">
            <div class="account-card__uid">UID：{{ acc.gameUid }}</div>
            <div class="account-card__info">
              <span class="account-card__server">{{ acc.serverName }}</span>
              <span class="account-card__name">{{ acc.inGameName || '未设置昵称' }}</span>
              <span class="account-card__level">探索者 Lv.{{ acc.accountLevel }}</span>
            </div>
            <div class="account-card__date">绑定于 {{ formatDate(acc.bindTime) }}</div>
            <div class="account-card__actions">
              <button class="btn btn--ghost btn--xs" @click="openEditAccount(acc)">编辑</button>
              <button class="btn btn--danger btn--xs" @click="removeAccount(acc.id)">解绑</button>
            </div>
          </div>
        </div>

        <!-- 绑定/编辑账号弹层 -->
        <div v-if="showAddAccount" class="modal-overlay" @click.self="showAddAccount = false">
          <div class="modal-box">
            <h3 class="modal-box__title">{{ editingAccountId ? '编辑账号' : '绑定游戏账号' }}</h3>
            <div class="game-form">
              <div class="game-form__row">
                <label class="game-form__label">游戏 UID *</label>
                <input class="game-form__input" type="text" v-model="accountForm.gameUid" placeholder="游戏内唯一识别码" :disabled="!!editingAccountId" />
              </div>
              <div class="game-form__row">
                <label class="game-form__label">服务器</label>
                <select class="game-form__input" v-model="accountForm.serverName">
                  <option v-for="s in serverOptions" :key="s" :value="s">{{ s }}</option>
                </select>
              </div>
              <div class="game-form__row">
                <label class="game-form__label">探索者称号</label>
                <input class="game-form__input" type="text" v-model="accountForm.inGameName" placeholder="游戏内显示名称" />
              </div>
              <div class="game-form__row">
                <label class="game-form__label">探索者等级</label>
                <input class="game-form__input" type="number" v-model.number="accountForm.accountLevel" min="0" max="999" />
              </div>
              <div class="game-form__actions">
                <button class="btn btn--primary" @click="saveAccount" :disabled="accountSaving">{{ accountSaving ? '保存中…' : '保存' }}</button>
                <button class="btn btn--ghost" @click="showAddAccount = false">取消</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
};
