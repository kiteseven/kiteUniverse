import {
  ApiError,
  createCommunityPost,
  fetchBoardList,
  fetchManagePostDetail,
  updateCommunityPost,
  type BoardSummaryData,
  type PostDetailData
} from '../services/api';
import { loadStoredToken } from '../services/session';

interface PostComposeForm {
  boardId: string;
  title: string;
  summary: string;
  badge: string;
  content: string;
}

/**
 * Creates the default compose-form state.
 */
function createComposeForm(): PostComposeForm {
  return {
    boardId: '',
    title: '',
    summary: '',
    badge: '',
    content: ''
  };
}

/**
 * Maps an editable post detail into the compose form model.
 */
function createComposeFormFromDetail(detail: PostDetailData): PostComposeForm {
  return {
    boardId: String(detail.boardId),
    title: detail.title || '',
    summary: detail.summary || '',
    badge: detail.badge || '',
    content: detail.content || ''
  };
}

/**
 * Normalizes optional text before it is sent to the backend.
 */
function normalizeNullableText(value: string) {
  const normalizedValue = value.trim();
  return normalizedValue ? normalizedValue : null;
}

/**
 * Post-compose page backed by the board, create-post, and update-post APIs.
 */
export const PostComposePage = {
  data() {
    return {
      loading: true,
      saving: false,
      editPostId: 0,
      errorMessage: '',
      successMessage: '',
      boards: [] as BoardSummaryData[],
      form: createComposeForm()
    };
  },
  watch: {
    '$route.query.postId': {
      immediate: false,
      handler(this: any) {
        void this.loadPage();
      }
    },
    '$route.query.boardId': {
      immediate: false,
      handler(this: any) {
        if (this.isEditMode()) {
          return;
        }
        this.applyPreferredBoard();
      }
    }
  },
  methods: {
    /**
     * Loads boards and, when needed, the editable post detail.
     */
    async loadPage(this: any) {
      const token = loadStoredToken();
      if (!token) {
        this.redirectToLogin();
        return;
      }

      this.loading = true;
      this.errorMessage = '';
      this.successMessage = '';
      this.editPostId = this.resolveEditPostId();

      try {
        const [boards, editablePost] = await Promise.all([
          fetchBoardList(),
          this.editPostId ? fetchManagePostDetail(token, this.editPostId) : Promise.resolve(null)
        ]);

        this.boards = boards;
        this.form = editablePost ? createComposeFormFromDetail(editablePost) : createComposeForm();
        this.applyPreferredBoard();
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          this.redirectToLogin();
          return;
        }
        this.errorMessage = error instanceof Error ? error.message : '页面内容加载失败，请稍后重试。';
      } finally {
        this.loading = false;
      }
    },

    /**
     * Applies the preferred board from the route query or falls back to the first board.
     */
    applyPreferredBoard(this: any) {
      if (!this.boards.length) {
        this.form.boardId = '';
        return;
      }

      const hasCurrentBoard = this.boards.some((board: BoardSummaryData) => String(board.id) === this.form.boardId);
      if (this.isEditMode() && hasCurrentBoard) {
        return;
      }

      const preferredBoardId = Number(this.$route.query.boardId);
      const matchedBoard = this.boards.find((board: BoardSummaryData) => board.id === preferredBoardId);
      if (matchedBoard) {
        this.form.boardId = String(matchedBoard.id);
        return;
      }

      if (!hasCurrentBoard) {
        this.form.boardId = String(this.boards[0].id);
      }
    },

    /**
     * Returns the currently selected board summary.
     */
    getSelectedBoard(this: any) {
      const boardId = Number(this.form.boardId);
      return this.boards.find((board: BoardSummaryData) => board.id === boardId) || null;
    },

    /**
     * Returns whether the page is editing an existing post.
     */
    isEditMode(this: any) {
      return this.editPostId > 0;
    },

    /**
     * Safely parses the editable post id from the route query.
     */
    resolveEditPostId(this: any) {
      const rawPostId = Number(this.$route.query.postId);
      return Number.isFinite(rawPostId) && rawPostId > 0 ? rawPostId : 0;
    },

    /**
     * Submits the compose form to the backend and navigates to the post detail page.
     */
    async submitPost(this: any) {
      const token = loadStoredToken();
      if (!token) {
        this.redirectToLogin();
        return;
      }

      const boardId = Number(this.form.boardId);
      const title = this.form.title.trim();
      const summary = this.form.summary.trim();
      const content = this.form.content.trim();

      if (!boardId) {
        this.errorMessage = '请选择目标版区。';
        return;
      }
      if (!title) {
        this.errorMessage = '帖子标题不能为空。';
        return;
      }
      if (!summary) {
        this.errorMessage = '帖子摘要不能为空。';
        return;
      }
      if (!content) {
        this.errorMessage = '帖子正文不能为空。';
        return;
      }
      if (title.length > 120) {
        this.errorMessage = '帖子标题请控制在 120 个字以内。';
        return;
      }
      if (summary.length > 255) {
        this.errorMessage = '帖子摘要请控制在 255 个字以内。';
        return;
      }

      this.saving = true;
      this.errorMessage = '';
      this.successMessage = '';

      try {
        const payload = {
          boardId,
          title,
          summary,
          content,
          badge: normalizeNullableText(this.form.badge)
        };

        const savedPost = this.isEditMode()
          ? await updateCommunityPost(token, this.editPostId, payload)
          : await createCommunityPost(token, payload);

        this.successMessage = this.isEditMode()
          ? '帖子内容已经更新，正在跳转详情页。'
          : '帖子已经发布，正在跳转详情页。';
        this.$router.push(`/posts/${savedPost.id}`);
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          this.redirectToLogin();
          return;
        }
        this.errorMessage = error instanceof Error ? error.message : '帖子保存失败，请稍后重试。';
      } finally {
        this.saving = false;
      }
    },

    /**
     * Sends anonymous users back to the login modal and preserves this route.
     */
    redirectToLogin(this: any) {
      this.$router.push({
        path: '/',
        query: {
          auth: 'login',
          redirect: this.$route.fullPath
        }
      });
    }
  },
  mounted(this: any) {
    void this.loadPage();
  },
  template: `
    <main class="page-shell compose-shell">
      <section class="detail-hero compose-hero">
        <div class="detail-hero__main">
          <span class="eyebrow">{{ isEditMode() ? '编辑帖子' : '发布帖子' }}</span>
          <h1>{{ isEditMode() ? '把这篇帖子继续打磨到更清晰。' : '把你的心得、观察或创作发到合适的版区。' }}</h1>
          <p>
            {{ isEditMode()
              ? '你可以继续补充背景、修正文案和更新结论，保存后会直接回到帖子详情页。'
              : '无论是攻略整理、剧情讨论还是同人创作，都可以从这里开始发布。'
            }}
          </p>
        </div>

        <div class="detail-hero__stats">
          <article class="detail-stat-card">
            <span>适合内容</span>
            <strong>攻略 / 讨论 / 创作</strong>
            <p>把背景、过程和结论写清楚，会更容易让读者快速抓住重点。</p>
          </article>
          <article class="detail-stat-card">
            <span>当前状态</span>
            <strong>{{ isEditMode() ? '修改中' : '待发布' }}</strong>
            <p>{{ isEditMode() ? '保存后会覆盖原帖内容。' : '发布成功后会直接进入帖子详情页。' }}</p>
          </article>
        </div>
      </section>

      <section class="content-grid detail-grid">
        <div class="content-main">
          <section class="panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">{{ isEditMode() ? '帖子编辑' : '帖子编辑器' }}</span>
                <h2>{{ isEditMode() ? '修改帖子内容' : '发布新内容' }}</h2>
              </div>
            </div>

            <div v-if="loading" class="profile-loading">正在加载可用版区和帖子内容...</div>

            <form v-else class="compose-form" @submit.prevent="submitPost">
              <div class="profile-form__grid">
                <label class="profile-field">
                  <span>目标版区</span>
                  <select v-model="form.boardId">
                    <option v-for="board in boards" :key="board.id" :value="String(board.id)">
                      {{ board.name }}
                    </option>
                  </select>
                </label>

                <label class="profile-field">
                  <span>徽标文案</span>
                  <input
                    v-model="form.badge"
                    type="text"
                    maxlength="32"
                    placeholder="可选，例如：版本情报 / 实战复盘"
                  />
                </label>

                <label class="profile-field profile-field--wide">
                  <span>帖子标题</span>
                  <input
                    v-model="form.title"
                    type="text"
                    maxlength="120"
                    placeholder="用一句话概括这篇内容的核心主题"
                  />
                </label>

                <label class="profile-field profile-field--wide">
                  <span>帖子摘要</span>
                  <textarea
                    v-model="form.summary"
                    rows="4"
                    placeholder="摘要会显示在首页推荐、版区列表和帖子详情顶部。"
                  ></textarea>
                </label>

                <label class="profile-field profile-field--wide">
                  <span>正文内容</span>
                  <textarea
                    v-model="form.content"
                    rows="12"
                    class="compose-textarea"
                    placeholder="先把背景、过程和结论写清楚，实测记录、时间线和补充说明也可以整理进正文。"
                  ></textarea>
                </label>
              </div>

              <p v-if="errorMessage" class="auth-feedback auth-feedback--error">{{ errorMessage }}</p>
              <p v-if="!errorMessage && successMessage" class="auth-feedback auth-feedback--success">{{ successMessage }}</p>

              <div class="comment-form__actions">
                <button class="button button--primary" type="submit" :disabled="saving">
                  {{ saving ? (isEditMode() ? '保存中...' : '发布中...') : (isEditMode() ? '保存修改' : '确认发布') }}
                </button>
                <router-link
                  v-if="isEditMode()"
                  class="button button--ghost"
                  :to="'/posts/' + editPostId"
                >
                  返回帖子
                </router-link>
                <router-link
                  v-else-if="getSelectedBoard()"
                  class="button button--ghost"
                  :to="'/boards/' + getSelectedBoard().id"
                >
                  返回版区
                </router-link>
              </div>
            </form>
          </section>
        </div>

        <aside class="content-side">
          <section class="panel soft-panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">当前分区</span>
                <h2>当前版区</h2>
              </div>
            </div>
            <div v-if="getSelectedBoard()" class="notice-list">
              <p>版区名称：{{ getSelectedBoard().name }}</p>
              <p>版区标签：{{ getSelectedBoard().tagName }}</p>
              <p>{{ getSelectedBoard().description }}</p>
            </div>
          </section>

          <section class="panel soft-panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">发布提示</span>
                <h2>写作建议</h2>
              </div>
            </div>
            <div class="notice-list">
              <p>标题最多 120 字，摘要最多 255 字，正文不能为空。</p>
              <p>建议先写清背景、结论和适用场景，读者会更容易快速读懂你的重点。</p>
              <p>如果是攻略类内容，别忘了补上阵容、练度条件和替代方案。</p>
            </div>
          </section>
        </aside>
      </section>
    </main>
  `
};
