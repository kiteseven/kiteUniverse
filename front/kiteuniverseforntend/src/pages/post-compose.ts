import {
  ApiError,
  createCommunityPost,
  fetchBoardList,
  fetchManagePostDetail,
  suggestAiBadges,
  updateCommunityPost,
  uploadPostImage,
  type AiBadgeSuggestResult,
  type BoardSummaryData,
  type PostDetailData
} from '../services/api';
import { loadStoredToken } from '../services/session';

declare const EasyMDE: any;

interface PostComposeForm {
  boardId: string;
  title: string;
  summary: string;
  badge: string;
}

/**
 * Creates the default compose-form state.
 */
function createComposeForm(): PostComposeForm {
  return {
    boardId: '',
    title: '',
    summary: '',
    badge: ''
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
    badge: detail.badge || ''
  };
}

/**
 * Normalizes optional text before it is sent to the backend.
 */
function normalizeNullableText(value: string) {
  const normalizedValue = value.trim();
  return normalizedValue ? normalizedValue : null;
}

const DRAFT_KEY = 'ku-post-draft';

interface DraftData {
  form: PostComposeForm;
  content: string;
  isAiGenerated: boolean;
  galleryMode: boolean;
  galleryImages: string[];
  savedAt: string;
}

/**
 * Post-compose page backed by the board, create-post, and update-post APIs.
 * Supports Markdown editing via EasyMDE, image upload, gallery mode, and AI label.
 */
export const PostComposePage = {
  data() {
    return {
      loading: true,
      saving: false,
      imageUploading: false,
      editPostId: 0,
      errorMessage: '',
      successMessage: '',
      draftMessage: '',
      boards: [] as BoardSummaryData[],
      form: createComposeForm(),
      isAiGenerated: false,
      galleryMode: false,
      galleryImages: [] as string[],
      initialContent: '',
      easyMde: null as any,
      draftTimer: 0,
      hasDraft: false,
      badgeSuggesting: false,
      badgeSuggestResult: null as AiBadgeSuggestResult | null,
      badgeSuggestError: ''
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
          this.editPostId ? fetchManagePostDetail(token, this.editPostId) : Promise.resolve(null as PostDetailData | null)
        ]);

        this.boards = boards;
        if (editablePost) {
          this.form = createComposeFormFromDetail(editablePost);
          this.isAiGenerated = editablePost.isAiGenerated || false;
          if (editablePost.galleryImages) {
            try {
              const parsed = JSON.parse(editablePost.galleryImages);
              if (Array.isArray(parsed)) {
                this.galleryImages = parsed;
                this.galleryMode = true;
              }
            } catch {
              this.galleryImages = [];
            }
          }
          this.initialContent = editablePost.content || '';
        } else {
          this.form = createComposeForm();
          this.isAiGenerated = false;
          this.galleryMode = false;
          this.galleryImages = [];
          this.initialContent = '';
        }
        this.applyPreferredBoard();
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          this.redirectToLogin();
          return;
        }
        this.errorMessage = error instanceof Error ? error.message : '页面内容加载失败，请稍后重试。';
      } finally {
        this.loading = false;
        await this.$nextTick();
        this.initEditor();
      }
    },

    /**
     * Initializes the EasyMDE editor on the content textarea.
     */
    initEditor(this: any) {
      if (typeof EasyMDE === 'undefined') {
        return;
      }
      const el = document.getElementById('compose-content');
      if (!el) {
        return;
      }
      if (this.easyMde) {
        this.easyMde.toTextArea();
        this.easyMde = null;
      }

      this.easyMde = new EasyMDE({
        element: el,
        initialValue: this.initialContent,
        placeholder: '先把背景、过程和结论写清楚，实测记录、时间线和补充说明也可以整理进正文。支持 Markdown 格式。',
        spellChecker: false,
        autofocus: false,
        toolbar: [
          'bold', 'italic', 'heading', '|',
          'quote', 'unordered-list', 'ordered-list', '|',
          'link', 'table', '|',
          {
            name: 'upload-image',
            action: (editor: any) => {
              void this.triggerImageUpload(editor);
            },
            className: 'fa fa-picture-o',
            title: '上传图片'
          },
          '|', 'preview', 'fullscreen', 'guide'
        ]
      });
    },

    /**
     * Triggers the hidden file input to pick an image, uploads it, and inserts the URL into the editor.
     */
    async triggerImageUpload(this: any, editor: any) {
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = 'image/*';
      input.onchange = async () => {
        const file = input.files?.[0];
        if (!file) {
          return;
        }
        const token = loadStoredToken();
        if (!token) {
          this.redirectToLogin();
          return;
        }
        this.imageUploading = true;
        try {
          const result = await uploadPostImage(token, file);
          const markdown = `![图片](${result.url})`;
          editor.codemirror.replaceSelection(markdown);
        } catch (error) {
          this.errorMessage = error instanceof Error ? error.message : '图片上传失败，请稍后重试。';
        } finally {
          this.imageUploading = false;
        }
      };
      input.click();
    },

    /**
     * Uploads a gallery image and appends it to the gallery list.
     */
    async uploadGalleryImage(this: any) {
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = 'image/*';
      input.onchange = async () => {
        const file = input.files?.[0];
        if (!file) {
          return;
        }
        const token = loadStoredToken();
        if (!token) {
          this.redirectToLogin();
          return;
        }
        this.imageUploading = true;
        try {
          const result = await uploadPostImage(token, file);
          this.galleryImages.push(result.url);
        } catch (error) {
          this.errorMessage = error instanceof Error ? error.message : '图片上传失败，请稍后重试。';
        } finally {
          this.imageUploading = false;
        }
      };
      input.click();
    },

    /**
     * Removes a gallery image by index.
     */
    removeGalleryImage(this: any, index: number) {
      this.galleryImages.splice(index, 1);
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
      const content = this.easyMde ? this.easyMde.value().trim() : '';

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
        const galleryImages = this.galleryMode && this.galleryImages.length > 0
          ? JSON.stringify(this.galleryImages)
          : null;

        const payload = {
          boardId,
          title,
          summary,
          content,
          badge: normalizeNullableText(this.form.badge),
          isAiGenerated: this.isAiGenerated,
          galleryImages
        };

        const savedPost = this.isEditMode()
          ? await updateCommunityPost(token, this.editPostId, payload)
          : await createCommunityPost(token, payload);

        this.successMessage = this.isEditMode()
          ? '帖子内容已经更新，正在跳转详情页。'
          : '帖子已经发布，正在跳转详情页。';
        this.clearDraft();
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
     * Saves the current form state as a draft to localStorage.
     */
    saveDraft(this: any) {
      if (this.isEditMode()) return;
      const content = this.easyMde ? this.easyMde.value() : '';
      const draft: DraftData = {
        form: { ...this.form },
        content,
        isAiGenerated: this.isAiGenerated,
        galleryMode: this.galleryMode,
        galleryImages: [...this.galleryImages],
        savedAt: new Date().toLocaleString('zh-CN')
      };
      localStorage.setItem(DRAFT_KEY, JSON.stringify(draft));
      this.hasDraft = true;
      this.draftMessage = `草稿已保存 ${draft.savedAt}`;
    },

    /**
     * Loads a previously saved draft from localStorage.
     */
    loadDraft(this: any) {
      if (this.isEditMode()) return;
      const raw = localStorage.getItem(DRAFT_KEY);
      if (!raw) return;
      try {
        const draft = JSON.parse(raw) as DraftData;
        this.form = { ...this.form, ...draft.form };
        this.isAiGenerated = draft.isAiGenerated ?? false;
        this.galleryMode = draft.galleryMode ?? false;
        this.galleryImages = draft.galleryImages ?? [];
        this.initialContent = draft.content || '';
        this.hasDraft = true;
        this.draftMessage = `已恢复草稿（${draft.savedAt}）`;
        if (this.easyMde) {
          this.easyMde.value(this.initialContent);
        }
      } catch {
        localStorage.removeItem(DRAFT_KEY);
      }
    },

    /**
     * Clears the saved draft from localStorage.
     */
    clearDraft(this: any) {
      localStorage.removeItem(DRAFT_KEY);
      this.hasDraft = false;
      this.draftMessage = '';
    },

    /**
     * Starts the 30-second auto-save timer.
     */
    startDraftTimer(this: any) {
      if (this.isEditMode()) return;
      this.draftTimer = window.setInterval(() => { this.saveDraft(); }, 30000);
    },

    /**
     * Calls the AI badge suggestion endpoint and pre-fills the badge field with the top suggestion.
     */
    async suggestBadge(this: any) {
      const title = this.form.title.trim();
      const content = this.easyMde ? this.easyMde.value().trim() : '';
      if (!title && !content) {
        this.badgeSuggestError = '请先填写标题或正文，再生成标签建议。';
        return;
      }
      this.badgeSuggesting = true;
      this.badgeSuggestError = '';
      this.badgeSuggestResult = null;
      try {
        this.badgeSuggestResult = await suggestAiBadges(title, content);
        if (this.badgeSuggestResult && this.badgeSuggestResult.badges.length > 0 && !this.form.badge) {
          this.form.badge = this.badgeSuggestResult.badges[0];
        }
      } catch (error) {
        this.badgeSuggestError = error instanceof Error ? error.message : 'AI 标签建议失败，请稍后重试。';
      } finally {
        this.badgeSuggesting = false;
      }
    },

    /**
     * Applies a suggested badge to the badge form field.
     */
    applyBadge(this: any, badge: string) {
      this.form.badge = badge;
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
    void (async () => {
      await this.loadPage();
      this.loadDraft();
      this.startDraftTimer();
    })();
  },
  beforeUnmount(this: any) {
    if (this.draftTimer) window.clearInterval(this.draftTimer);
    if (this.easyMde) {
      this.easyMde.toTextArea();
      this.easyMde = null;
    }
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

                <div class="profile-field">
                  <span>徽标文案</span>
                  <div class="compose-badge-row">
                    <input
                      v-model="form.badge"
                      type="text"
                      maxlength="32"
                      placeholder="可选，例如：版本情报 / 实战复盘"
                    />
                    <button
                      type="button"
                      class="button button--ghost button--small compose-ai-btn"
                      @click="suggestBadge"
                      :disabled="badgeSuggesting"
                      title="AI 智能推荐标签"
                    >
                      <span class="ai-badge ai-badge--inline">AI</span>
                      {{ badgeSuggesting ? '推荐中...' : '智能推荐' }}
                    </button>
                  </div>
                  <div v-if="badgeSuggestResult" class="compose-badge-suggest">
                    <span class="compose-badge-suggest__label">推荐标签：</span>
                    <button
                      v-for="badge in badgeSuggestResult.badges"
                      :key="badge"
                      type="button"
                      class="capsule"
                      @click="applyBadge(badge)"
                    >{{ badge }}</button>
                    <span v-if="badgeSuggestResult.reason" class="compose-badge-suggest__reason">{{ badgeSuggestResult.reason }}</span>
                  </div>
                  <p v-if="badgeSuggestError" class="compose-badge-suggest__error">{{ badgeSuggestError }}</p>
                </div>

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

                <div class="profile-field profile-field--wide compose-options-row">
                  <label class="compose-checkbox-label">
                    <input type="checkbox" v-model="isAiGenerated" />
                    <span class="compose-checkbox-text">包含 AI 生成内容</span>
                    <span class="ai-badge ai-badge--inline">AI</span>
                  </label>
                  <button
                    type="button"
                    class="button button--ghost compose-toggle-gallery"
                    @click="galleryMode = !galleryMode"
                  >
                    {{ galleryMode ? '切换为普通模式' : '切换为图集模式' }}
                  </button>
                </div>

                <div class="profile-field profile-field--wide">
                  <span>正文内容</span>
                  <textarea id="compose-content"></textarea>
                  <p v-if="imageUploading" class="compose-upload-hint">正在上传图片...</p>
                </div>

                <div v-if="galleryMode" class="profile-field profile-field--wide">
                  <span>图集图片</span>
                  <div class="gallery-editor">
                    <div class="gallery-editor__grid">
                      <div
                        v-for="(img, idx) in galleryImages"
                        :key="idx"
                        class="gallery-editor__item"
                      >
                        <img :src="img" :alt="'图集图片 ' + (idx + 1)" />
                        <button
                          type="button"
                          class="gallery-editor__remove"
                          @click="removeGalleryImage(idx)"
                          title="移除此图"
                        >✕</button>
                      </div>
                      <button
                        type="button"
                        class="gallery-editor__add"
                        @click="uploadGalleryImage"
                        :disabled="imageUploading"
                      >
                        {{ imageUploading ? '上传中...' : '+ 添加图片' }}
                      </button>
                    </div>
                    <p class="compose-gallery-hint">图集图片将以网格形式展示在帖子详情页。</p>
                  </div>
                </div>
              </div>

              <p v-if="errorMessage" class="auth-feedback auth-feedback--error">{{ errorMessage }}</p>
              <p v-if="!errorMessage && successMessage" class="auth-feedback auth-feedback--success">{{ successMessage }}</p>
              <p v-if="!errorMessage && !successMessage && draftMessage" class="draft-status">{{ draftMessage }}</p>

              <div class="comment-form__actions">
                <button class="button button--primary" type="submit" :disabled="saving || imageUploading">
                  {{ saving ? (isEditMode() ? '保存中...' : '发布中...') : (isEditMode() ? '保存修改' : '确认发布') }}
                </button>
                <button
                  v-if="!isEditMode() && hasDraft"
                  class="button button--ghost button--small"
                  type="button"
                  @click="saveDraft"
                >
                  手动保存草稿
                </button>
                <button
                  v-if="!isEditMode() && hasDraft"
                  class="button button--ghost button--small"
                  type="button"
                  @click="clearDraft"
                >
                  清除草稿
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
              <p>正文支持 Markdown 格式，工具栏中的图片按钮可上传图片。</p>
              <p>建议先写清背景、结论和适用场景，读者会更容易快速读懂你的重点。</p>
              <p>如果是攻略类内容，别忘了补上阵容、练度条件和替代方案。</p>
            </div>
          </section>
        </aside>
      </section>
    </main>
  `
};
