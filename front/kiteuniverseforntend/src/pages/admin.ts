import {
  getAdminStats,
  getAdminUsers,
  banUser,
  unbanUser,
  muteUser,
  changeUserRole,
  getAdminPosts,
  hidePost,
  restorePost,
  adminDeletePost,
  pinPost,
  unpinPost,
  featurePost,
  unfeaturePost,
  getAdminComments,
  adminDeleteComment,
  getAdminReports,
  handleReport,
  resolveAssetUrl,
  type AdminStatsData,
  type AdminUserData,
  type AdminPostData,
  type AdminCommentData,
  type AdminReportData
} from '../services/api';
import { loadStoredToken } from '../services/session';

type AdminTab = 'dashboard' | 'users' | 'posts' | 'comments' | 'reports';

const REASON_LABELS: Record<string, string> = {
  spam: '垃圾信息',
  harassment: '骚扰/攻击',
  inappropriate: '不当内容',
  misinformation: '虚假信息',
  other: '其他'
};

function fmtTime(s: string | null | undefined): string {
  if (!s) return '—';
  return new Date(s).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}

export const AdminPage = {
  data() {
    return {
      token: '' as string,
      tab: 'dashboard' as AdminTab,

      // dashboard
      stats: null as AdminStatsData | null,
      statsLoading: false,
      statsError: '',

      // users
      users: [] as AdminUserData[],
      usersTotal: 0,
      usersPage: 0,
      usersKeyword: '',
      usersStatusFilter: null as number | null,
      usersLoading: false,
      usersError: '',

      // posts
      posts: [] as AdminPostData[],
      postsTotal: 0,
      postsPage: 0,
      postsKeyword: '',
      postsStatusFilter: null as number | null,
      postsLoading: false,
      postsError: '',

      // comments
      comments: [] as AdminCommentData[],
      commentsTotal: 0,
      commentsPage: 0,
      commentsKeyword: '',
      commentsLoading: false,
      commentsError: '',

      // reports
      reports: [] as AdminReportData[],
      reportsTotal: 0,
      reportsPage: 0,
      reportsStatusFilter: null as number | null,
      reportsLoading: false,
      reportsError: '',

      // mute modal
      muteModalUserId: null as number | null,
      muteMinutes: 60,

      actionError: ''
    };
  },

  async created(this: any) {
    this.token = loadStoredToken() || '';
    if (!this.token) return;
    await this.loadStats();
  },

  methods: {
    fmtTime(this: any, s: string | null) { return fmtTime(s); },

    resolveAvatar(this: any, url: string | null) { return resolveAssetUrl(url); },

    reasonLabel(this: any, r: string) { return REASON_LABELS[r] || r; },

    reportStatusLabel(this: any, s: number) {
      return s === 0 ? '待处理' : s === 1 ? '已采纳' : '已驳回';
    },

    async switchTab(this: any, t: AdminTab) {
      this.tab = t;
      this.actionError = '';
      if (t === 'dashboard') await this.loadStats();
      else if (t === 'users') { this.usersPage = 0; await this.loadUsers(); }
      else if (t === 'posts') { this.postsPage = 0; await this.loadPosts(); }
      else if (t === 'comments') { this.commentsPage = 0; await this.loadComments(); }
      else if (t === 'reports') { this.reportsPage = 0; await this.loadReports(); }
    },

    // ── Dashboard ──────────────────────────────────────────────

    async loadStats(this: any) {
      this.statsLoading = true;
      this.statsError = '';
      try {
        this.stats = await getAdminStats(this.token);
      } catch (e: any) {
        this.statsError = e?.message || '加载失败';
      } finally {
        this.statsLoading = false;
      }
    },

    // ── Users ──────────────────────────────────────────────────

    async loadUsers(this: any) {
      this.usersLoading = true;
      this.usersError = '';
      try {
        const r = await getAdminUsers(this.token, this.usersKeyword, this.usersStatusFilter, this.usersPage, 20);
        this.users = r.items;
        this.usersTotal = r.total;
      } catch (e: any) {
        this.usersError = e?.message || '加载失败';
      } finally {
        this.usersLoading = false;
      }
    },

    async doBanUser(this: any, id: number) {
      try { await banUser(this.token, id); await this.loadUsers(); } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    async doUnbanUser(this: any, id: number) {
      try { await unbanUser(this.token, id); await this.loadUsers(); } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    openMuteModal(this: any, id: number) {
      this.muteModalUserId = id;
      this.muteMinutes = 60;
    },

    closeMuteModal(this: any) { this.muteModalUserId = null; },

    async doMuteUser(this: any) {
      if (!this.muteModalUserId) return;
      try {
        await muteUser(this.token, this.muteModalUserId, this.muteMinutes);
        this.muteModalUserId = null;
        await this.loadUsers();
      } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    async doUnmuteUser(this: any, id: number) {
      try { await muteUser(this.token, id, 0); await this.loadUsers(); } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    async doSetAdmin(this: any, id: number, role: string) {
      try { await changeUserRole(this.token, id, role); await this.loadUsers(); } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    // ── Posts ──────────────────────────────────────────────────

    async loadPosts(this: any) {
      this.postsLoading = true;
      this.postsError = '';
      try {
        const r = await getAdminPosts(this.token, this.postsKeyword, this.postsStatusFilter, this.postsPage, 20);
        this.posts = r.items;
        this.postsTotal = r.total;
      } catch (e: any) {
        this.postsError = e?.message || '加载失败';
      } finally {
        this.postsLoading = false;
      }
    },

    async doHidePost(this: any, id: number) {
      try { await hidePost(this.token, id); await this.loadPosts(); } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    async doRestorePost(this: any, id: number) {
      try { await restorePost(this.token, id); await this.loadPosts(); } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    async doDeletePost(this: any, id: number) {
      if (!window.confirm('确定永久删除该帖子？')) return;
      try { await adminDeletePost(this.token, id); await this.loadPosts(); } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    async doPinPost(this: any, id: number, isPinned: boolean) {
      try {
        if (isPinned) { await unpinPost(this.token, id); } else { await pinPost(this.token, id); }
        await this.loadPosts();
      } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    async doFeaturePost(this: any, id: number, isFeatured: boolean) {
      try {
        if (isFeatured) { await unfeaturePost(this.token, id); } else { await featurePost(this.token, id); }
        await this.loadPosts();
      } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    // ── Comments ───────────────────────────────────────────────

    async loadComments(this: any) {
      this.commentsLoading = true;
      this.commentsError = '';
      try {
        const r = await getAdminComments(this.token, this.commentsKeyword, this.commentsPage, 20);
        this.comments = r.items;
        this.commentsTotal = r.total;
      } catch (e: any) {
        this.commentsError = e?.message || '加载失败';
      } finally {
        this.commentsLoading = false;
      }
    },

    async doDeleteComment(this: any, id: number) {
      if (!window.confirm('确定删除该评论？')) return;
      try { await adminDeleteComment(this.token, id); await this.loadComments(); } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    // ── Reports ────────────────────────────────────────────────

    async loadReports(this: any) {
      this.reportsLoading = true;
      this.reportsError = '';
      try {
        const r = await getAdminReports(this.token, this.reportsStatusFilter, this.reportsPage, 20);
        this.reports = r.items;
        this.reportsTotal = r.total;
      } catch (e: any) {
        this.reportsError = e?.message || '加载失败';
      } finally {
        this.reportsLoading = false;
      }
    },

    async doApproveReport(this: any, id: number) {
      try { await handleReport(this.token, id, 'approve'); await this.loadReports(); } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    async doDismissReport(this: any, id: number) {
      try { await handleReport(this.token, id, 'dismiss'); await this.loadReports(); } catch (e: any) { this.actionError = e?.message || '操作失败'; }
    },

    // ── Pagination helpers ─────────────────────────────────────

    prevPage(this: any, listName: string) {
      const pageKey = `${listName}Page` as keyof typeof this;
      if ((this[pageKey] as number) > 0) {
        (this[pageKey] as any)--;
        (this as any)[`load${listName.charAt(0).toUpperCase() + listName.slice(1)}`]();
      }
    },

    nextPage(this: any, listName: string, total: number) {
      const pageKey = `${listName}Page` as keyof typeof this;
      if (((this[pageKey] as number) + 1) * 20 < total) {
        (this[pageKey] as any)++;
        (this as any)[`load${listName.charAt(0).toUpperCase() + listName.slice(1)}`]();
      }
    }
  },

  template: `
    <div class="admin-page page-container">
      <div class="page-hero">
        <div class="page-hero__eyebrow">管理后台</div>
        <h1 class="page-hero__title">后台管理中心</h1>
        <p class="page-hero__description">内容审核、举报处理、用户管理与数据统计</p>
      </div>

      <div v-if="!token" class="empty-state">
        <div class="empty-state__icon">🔒</div>
        <div class="empty-state__title">请先登录</div>
      </div>

      <template v-else>
        <p v-if="actionError" class="form-error admin-action-error">{{ actionError }}</p>

        <!-- Tab bar -->
        <div class="admin-tabs">
          <button :class="['admin-tab-btn', tab === 'dashboard' ? 'admin-tab-btn--active' : '']" @click="switchTab('dashboard')">数据概览</button>
          <button :class="['admin-tab-btn', tab === 'posts' ? 'admin-tab-btn--active' : '']" @click="switchTab('posts')">帖子审核</button>
          <button :class="['admin-tab-btn', tab === 'comments' ? 'admin-tab-btn--active' : '']" @click="switchTab('comments')">评论管理</button>
          <button :class="['admin-tab-btn', tab === 'reports' ? 'admin-tab-btn--active' : '']" @click="switchTab('reports')">举报处理</button>
          <button :class="['admin-tab-btn', tab === 'users' ? 'admin-tab-btn--active' : '']" @click="switchTab('users')">用户管理</button>
        </div>

        <!-- ── Dashboard ── -->
        <div v-if="tab === 'dashboard'">
          <div v-if="statsLoading" class="loading-spinner"><div class="spinner"></div></div>
          <div v-else-if="statsError" class="form-error">{{ statsError }}</div>
          <div v-else-if="stats" class="admin-stats-grid">
            <div class="admin-stat-card">
              <div class="admin-stat-card__value">{{ stats.totalUsers }}</div>
              <div class="admin-stat-card__label">注册用户</div>
            </div>
            <div class="admin-stat-card">
              <div class="admin-stat-card__value admin-stat-card__value--cyan">{{ stats.newUsersToday }}</div>
              <div class="admin-stat-card__label">今日新增</div>
            </div>
            <div class="admin-stat-card">
              <div class="admin-stat-card__value admin-stat-card__value--green">{{ stats.activeUsersToday }}</div>
              <div class="admin-stat-card__label">今日活跃</div>
            </div>
            <div class="admin-stat-card">
              <div class="admin-stat-card__value">{{ stats.totalPosts }}</div>
              <div class="admin-stat-card__label">帖子总数</div>
            </div>
            <div class="admin-stat-card">
              <div class="admin-stat-card__value admin-stat-card__value--cyan">{{ stats.newPostsToday }}</div>
              <div class="admin-stat-card__label">今日发帖</div>
            </div>
            <div class="admin-stat-card">
              <div class="admin-stat-card__value">{{ stats.totalComments }}</div>
              <div class="admin-stat-card__label">评论总数</div>
            </div>
            <div class="admin-stat-card">
              <div class="admin-stat-card__value admin-stat-card__value--yellow">{{ stats.pendingReports }}</div>
              <div class="admin-stat-card__label">待处理举报</div>
            </div>
            <div class="admin-stat-card">
              <div class="admin-stat-card__value admin-stat-card__value--red">{{ stats.bannedUsers }}</div>
              <div class="admin-stat-card__label">已封禁用户</div>
            </div>
          </div>
        </div>

        <!-- ── Posts ── -->
        <div v-if="tab === 'posts'">
          <div class="admin-toolbar">
            <input class="admin-search" v-model="postsKeyword" placeholder="搜索标题或作者…" @keydown.enter="() => { postsPage = 0; loadPosts(); }" />
            <select class="admin-select" v-model="postsStatusFilter" @change="() => { postsPage = 0; loadPosts(); }">
              <option :value="null">全部状态</option>
              <option :value="1">已发布</option>
              <option :value="0">已隐藏</option>
            </select>
            <button class="button button--primary button--small" @click="() => { postsPage = 0; loadPosts(); }">搜索</button>
          </div>
          <div v-if="postsLoading" class="loading-spinner"><div class="spinner"></div></div>
          <div v-else-if="postsError" class="form-error">{{ postsError }}</div>
          <div v-else class="admin-table-wrap">
            <table class="admin-table">
              <thead>
                <tr>
                  <th>ID</th><th>标题</th><th>作者</th><th>板块</th><th>状态</th><th>浏览/评论/赞</th><th>发布时间</th><th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in posts" :key="p.id">
                  <td>{{ p.id }}</td>
                  <td class="admin-table__title">{{ p.title }}</td>
                  <td>{{ p.authorName }}</td>
                  <td>{{ p.boardName }}</td>
                  <td>
                    <span :class="['admin-badge', p.status === 1 ? 'admin-badge--green' : 'admin-badge--red']">{{ p.status === 1 ? '已发布' : '已隐藏' }}</span>
                    <span v-if="p.pinned" class="admin-badge admin-badge--yellow" style="margin-left:4px">置顶</span>
                    <span v-if="p.featured" class="admin-badge admin-badge--cyan" style="margin-left:4px">精华</span>
                  </td>
                  <td>{{ p.viewCount }} / {{ p.commentCount }} / {{ p.likeCount }}</td>
                  <td>{{ fmtTime(p.createTime) }}</td>
                  <td class="admin-table__actions">
                    <button v-if="p.status === 1" class="button button--ghost button--small" @click="doHidePost(p.id)">隐藏</button>
                    <button v-else class="button button--ghost button--small" @click="doRestorePost(p.id)">恢复</button>
                    <button class="button button--ghost button--small" @click="doPinPost(p.id, p.pinned)">{{ p.pinned ? '取消置顶' : '置顶' }}</button>
                    <button class="button button--ghost button--small" @click="doFeaturePost(p.id, p.featured)">{{ p.featured ? '取消精华' : '设精华' }}</button>
                    <button class="button button--danger button--small" @click="doDeletePost(p.id)">删除</button>
                  </td>
                </tr>
                <tr v-if="posts.length === 0"><td colspan="8" class="admin-table__empty">暂无数据</td></tr>
              </tbody>
            </table>
            <div class="admin-pagination">
              <button class="button button--ghost button--small" :disabled="postsPage === 0" @click="prevPage('posts')">上一页</button>
              <span>第 {{ postsPage + 1 }} 页 / 共 {{ postsTotal }} 条</span>
              <button class="button button--ghost button--small" :disabled="(postsPage + 1) * 20 >= postsTotal" @click="nextPage('posts', postsTotal)">下一页</button>
            </div>
          </div>
        </div>

        <!-- ── Comments ── -->
        <div v-if="tab === 'comments'">
          <div class="admin-toolbar">
            <input class="admin-search" v-model="commentsKeyword" placeholder="搜索评论内容或作者…" @keydown.enter="() => { commentsPage = 0; loadComments(); }" />
            <button class="button button--primary button--small" @click="() => { commentsPage = 0; loadComments(); }">搜索</button>
          </div>
          <div v-if="commentsLoading" class="loading-spinner"><div class="spinner"></div></div>
          <div v-else-if="commentsError" class="form-error">{{ commentsError }}</div>
          <div v-else class="admin-table-wrap">
            <table class="admin-table">
              <thead>
                <tr><th>ID</th><th>所属帖子</th><th>作者</th><th>内容</th><th>发布时间</th><th>操作</th></tr>
              </thead>
              <tbody>
                <tr v-for="c in comments" :key="c.id">
                  <td>{{ c.id }}</td>
                  <td class="admin-table__title">{{ c.postTitle }}</td>
                  <td>{{ c.authorName }}</td>
                  <td class="admin-table__content">{{ c.content }}</td>
                  <td>{{ fmtTime(c.createTime) }}</td>
                  <td><button class="button button--danger button--small" @click="doDeleteComment(c.id)">删除</button></td>
                </tr>
                <tr v-if="comments.length === 0"><td colspan="6" class="admin-table__empty">暂无数据</td></tr>
              </tbody>
            </table>
            <div class="admin-pagination">
              <button class="button button--ghost button--small" :disabled="commentsPage === 0" @click="prevPage('comments')">上一页</button>
              <span>第 {{ commentsPage + 1 }} 页 / 共 {{ commentsTotal }} 条</span>
              <button class="button button--ghost button--small" :disabled="(commentsPage + 1) * 20 >= commentsTotal" @click="nextPage('comments', commentsTotal)">下一页</button>
            </div>
          </div>
        </div>

        <!-- ── Reports ── -->
        <div v-if="tab === 'reports'">
          <div class="admin-toolbar">
            <select class="admin-select" v-model="reportsStatusFilter" @change="() => { reportsPage = 0; loadReports(); }">
              <option :value="null">全部</option>
              <option :value="0">待处理</option>
              <option :value="1">已采纳</option>
              <option :value="2">已驳回</option>
            </select>
            <button class="button button--primary button--small" @click="() => { reportsPage = 0; loadReports(); }">刷新</button>
          </div>
          <div v-if="reportsLoading" class="loading-spinner"><div class="spinner"></div></div>
          <div v-else-if="reportsError" class="form-error">{{ reportsError }}</div>
          <div v-else class="admin-table-wrap">
            <table class="admin-table">
              <thead>
                <tr><th>ID</th><th>举报人</th><th>目标类型</th><th>目标ID</th><th>原因</th><th>说明</th><th>状态</th><th>处理人</th><th>时间</th><th>操作</th></tr>
              </thead>
              <tbody>
                <tr v-for="r in reports" :key="r.id">
                  <td>{{ r.id }}</td>
                  <td>{{ r.reporterName }}</td>
                  <td>{{ r.targetType }}</td>
                  <td>{{ r.targetId }}</td>
                  <td>{{ reasonLabel(r.reason) }}</td>
                  <td class="admin-table__content">{{ r.description || '—' }}</td>
                  <td><span :class="['admin-badge', r.status === 0 ? 'admin-badge--yellow' : r.status === 1 ? 'admin-badge--green' : 'admin-badge--red']">{{ reportStatusLabel(r.status) }}</span></td>
                  <td>{{ r.handlerName || '—' }}</td>
                  <td>{{ fmtTime(r.createTime) }}</td>
                  <td class="admin-table__actions">
                    <template v-if="r.status === 0">
                      <button class="button button--danger button--small" @click="doApproveReport(r.id)">采纳并删除</button>
                      <button class="button button--ghost button--small" @click="doDismissReport(r.id)">驳回</button>
                    </template>
                    <span v-else class="admin-handled">已处理</span>
                  </td>
                </tr>
                <tr v-if="reports.length === 0"><td colspan="10" class="admin-table__empty">暂无数据</td></tr>
              </tbody>
            </table>
            <div class="admin-pagination">
              <button class="button button--ghost button--small" :disabled="reportsPage === 0" @click="prevPage('reports')">上一页</button>
              <span>第 {{ reportsPage + 1 }} 页 / 共 {{ reportsTotal }} 条</span>
              <button class="button button--ghost button--small" :disabled="(reportsPage + 1) * 20 >= reportsTotal" @click="nextPage('reports', reportsTotal)">下一页</button>
            </div>
          </div>
        </div>

        <!-- ── Users ── -->
        <div v-if="tab === 'users'">
          <div class="admin-toolbar">
            <input class="admin-search" v-model="usersKeyword" placeholder="搜索昵称、用户名或手机号…" @keydown.enter="() => { usersPage = 0; loadUsers(); }" />
            <select class="admin-select" v-model="usersStatusFilter" @change="() => { usersPage = 0; loadUsers(); }">
              <option :value="null">全部状态</option>
              <option :value="1">正常</option>
              <option :value="0">已封禁</option>
            </select>
            <button class="button button--primary button--small" @click="() => { usersPage = 0; loadUsers(); }">搜索</button>
          </div>
          <div v-if="usersLoading" class="loading-spinner"><div class="spinner"></div></div>
          <div v-else-if="usersError" class="form-error">{{ usersError }}</div>
          <div v-else class="admin-table-wrap">
            <table class="admin-table">
              <thead>
                <tr><th>ID</th><th>昵称</th><th>手机号</th><th>角色</th><th>状态</th><th>禁言至</th><th>最后登录</th><th>注册时间</th><th>操作</th></tr>
              </thead>
              <tbody>
                <tr v-for="u in users" :key="u.id">
                  <td>{{ u.id }}</td>
                  <td>{{ u.nickname || u.username }}</td>
                  <td>{{ u.phone }}</td>
                  <td><span :class="['admin-badge', u.role === 'admin' ? 'admin-badge--cyan' : '']">{{ u.role === 'admin' ? '管理员' : '普通用户' }}</span></td>
                  <td><span :class="['admin-badge', u.status === 1 ? 'admin-badge--green' : 'admin-badge--red']">{{ u.status === 1 ? '正常' : '封禁' }}</span></td>
                  <td>{{ u.muteUntil ? fmtTime(u.muteUntil) : '—' }}</td>
                  <td>{{ fmtTime(u.lastLoginTime) }}</td>
                  <td>{{ fmtTime(u.createTime) }}</td>
                  <td class="admin-table__actions">
                    <button v-if="u.status === 1" class="button button--danger button--small" @click="doBanUser(u.id)">封禁</button>
                    <button v-else class="button button--ghost button--small" @click="doUnbanUser(u.id)">解封</button>
                    <button v-if="!u.muteUntil || new Date(u.muteUntil) < new Date()" class="button button--ghost button--small" @click="openMuteModal(u.id)">禁言</button>
                    <button v-else class="button button--ghost button--small" @click="doUnmuteUser(u.id)">解除禁言</button>
                    <button v-if="u.role !== 'admin'" class="button button--ghost button--small" @click="doSetAdmin(u.id, 'admin')">设为管理员</button>
                    <button v-else class="button button--ghost button--small" @click="doSetAdmin(u.id, 'user')">取消管理员</button>
                  </td>
                </tr>
                <tr v-if="users.length === 0"><td colspan="9" class="admin-table__empty">暂无数据</td></tr>
              </tbody>
            </table>
            <div class="admin-pagination">
              <button class="button button--ghost button--small" :disabled="usersPage === 0" @click="prevPage('users')">上一页</button>
              <span>第 {{ usersPage + 1 }} 页 / 共 {{ usersTotal }} 条</span>
              <button class="button button--ghost button--small" :disabled="(usersPage + 1) * 20 >= usersTotal" @click="nextPage('users', usersTotal)">下一页</button>
            </div>
          </div>
        </div>
      </template>

      <!-- Mute Modal -->
      <div v-if="muteModalUserId" class="auth-modal-mask" @click.self="closeMuteModal">
        <div class="admin-mute-modal">
          <h3>设置禁言时长</h3>
          <div class="admin-mute-modal__row">
            <label>禁言时长（分钟）</label>
            <input type="number" class="admin-mute-input" v-model.number="muteMinutes" min="1" max="525600" />
          </div>
          <div class="admin-mute-modal__hint">常用：60分钟 / 1440分钟（1天）/ 10080分钟（1周）</div>
          <div class="admin-mute-modal__actions">
            <button class="button button--ghost" @click="closeMuteModal">取消</button>
            <button class="button button--primary" @click="doMuteUser">确认禁言</button>
          </div>
        </div>
      </div>
    </div>
  `
};
