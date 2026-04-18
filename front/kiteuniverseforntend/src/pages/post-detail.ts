import {
  ApiError,
  createCommunityPostComment,
  deleteCommunityPost,
  favoriteCommunityPost,
  fetchPostComments,
  fetchPostDetail,
  fetchPostFavoriteState,
  fetchPostLikeState,
  fetchRelatedPosts,
  likeCommunityComment,
  likeCommunityPost,
  resolveAssetUrl,
  unfavoriteCommunityPost,
  unlikeCommunityComment,
  unlikeCommunityPost,
  type PostCommentData,
  type PostDetailData,
  type PostFavoriteStateData,
  type PostLikeStateData,
  type PostSummaryData
} from '../services/api';
import { loadStoredToken, loadStoredUser } from '../services/session';

declare const marked: any;

/**
 * Post-detail page backed by the real post, favorite, delete, and comment APIs.
 */
export const PostDetailPage = {
  data() {
    return {
      loading: true,
      errorMessage: '',
      submitError: '',
      submitMessage: '',
      actionError: '',
      actionMessage: '',
      commentSubmitting: false,
      favoriteSubmitting: false,
      deleting: false,
      detail: null as PostDetailData | null,
      favoriteState: null as PostFavoriteStateData | null,
      likeState: null as PostLikeStateData | null,
      comments: [] as PostCommentData[],
      commentContent: '',
      likeSubmitting: false,
      commentLikeSubmitting: {} as Record<number, boolean>,
      lightboxUrl: null as string | null,
      replyToCommentId: null as number | null,
      replyToName: '' as string,
      replyContent: '' as string,
      replySubmitting: false,
      replyError: '' as string,
      relatedPosts: [] as PostSummaryData[]
    };
  },
  watch: {
    '$route.params.postId': {
      immediate: false,
      handler(this: any) {
        void this.loadPostPage();
      }
    }
  },
  methods: {
    /**
     * Loads the post detail, comments, and the current user's favorite state together.
     */
    async loadPostPage(this: any) {
      const postId = this.resolvePostId();
      if (!postId) {
        this.errorMessage = '目标帖子编号无效。';
        this.loading = false;
        return;
      }

      this.loading = true;
      this.errorMessage = '';
      this.actionError = '';
      this.actionMessage = '';

      try {
        const token = loadStoredToken();
        const [detail, comments, favoriteState, likeState] = await Promise.all([
          fetchPostDetail(postId),
          fetchPostComments(postId, token || ''),
          token ? this.loadFavoriteStateOrNull(token, postId) : Promise.resolve(null),
          token ? this.loadLikeStateOrNull(token, postId) : Promise.resolve(null)
        ]);
        this.detail = detail;
        this.comments = comments;
        this.favoriteState = favoriteState;
        this.likeState = likeState;
        if (favoriteState) {
          this.detail.favoriteCount = favoriteState.favoriteCount;
        }
        if (likeState) {
          this.detail.likeCount = likeState.likeCount;
        }
        // load related posts in background — non-blocking
        void this.loadRelatedPosts(postId);
      } catch (error) {
        this.errorMessage = error instanceof Error ? error.message : '帖子内容加载失败，请稍后重试。';
      } finally {
        this.loading = false;
      }
    },

    /**
     * Loads ES more_like_this related posts (non-critical, fails silently).
     */
    async loadRelatedPosts(this: any, postId: number) {
      try {
        this.relatedPosts = await fetchRelatedPosts(postId, 5);
      } catch {
        this.relatedPosts = [];
      }
    },

    /**
     * Loads the favorite state without breaking the page when the session expired.
     */
    async loadFavoriteStateOrNull(this: any, token: string, postId: number) {
      try {
        return await fetchPostFavoriteState(token, postId);
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          return null;
        }
        throw error;
      }
    },

    /**
     * Loads the like state without breaking the page when the session expired.
     */
    async loadLikeStateOrNull(this: any, token: string, postId: number) {
      try {
        return await fetchPostLikeState(token, postId);
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          return null;
        }
        throw error;
      }
    },

    /**
     * Submits a new comment to the backend.
     */
    async submitComment(this: any) {
      const postId = this.resolvePostId();
      const token = loadStoredToken();
      const normalizedContent = this.commentContent.trim();

      if (!token) {
        this.redirectToLogin();
        return;
      }
      if (!normalizedContent) {
        this.submitError = '评论内容不能为空。';
        return;
      }
      if (normalizedContent.length > 1000) {
        this.submitError = '评论内容请控制在 1000 个字以内。';
        return;
      }

      this.commentSubmitting = true;
      this.submitError = '';
      this.submitMessage = '';

      try {
        const createdComment = await createCommunityPostComment(token, postId, {
          content: normalizedContent
        });
        this.comments = [...this.comments, createdComment];
        this.commentContent = '';
        this.submitMessage = '评论已经发布。';
        if (this.detail) {
          this.detail.commentCount = Number(this.detail.commentCount || 0) + 1;
        }
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          this.redirectToLogin();
          return;
        }
        this.submitError = error instanceof Error ? error.message : '评论发布失败，请稍后重试。';
      } finally {
        this.commentSubmitting = false;
      }
    },

    /**
     * Favorites or unfavorites the current post.
     */
    async toggleFavorite(this: any) {
      const postId = this.resolvePostId();
      const token = loadStoredToken();

      if (!token) {
        this.redirectToLogin();
        return;
      }

      this.favoriteSubmitting = true;
      this.actionError = '';
      this.actionMessage = '';

      try {
        const nextState = this.favoriteState?.favorited
          ? await unfavoriteCommunityPost(token, postId)
          : await favoriteCommunityPost(token, postId);
        this.favoriteState = nextState;
        if (this.detail) {
          this.detail.favoriteCount = nextState.favoriteCount;
        }
        this.actionMessage = nextState.favorited ? '帖子已加入收藏。' : '已从收藏中移除。';
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          this.redirectToLogin();
          return;
        }
        this.actionError = error instanceof Error ? error.message : '收藏状态更新失败，请稍后重试。';
      } finally {
        this.favoriteSubmitting = false;
      }
    },

    /**
     * Likes or unlikes the current post.
     */
    async toggleLike(this: any) {
      const postId = this.resolvePostId();
      const token = loadStoredToken();

      if (!token) {
        this.redirectToLogin();
        return;
      }

      this.likeSubmitting = true;
      this.actionError = '';
      this.actionMessage = '';

      try {
        const nextState = this.likeState?.liked
          ? await unlikeCommunityPost(token, postId)
          : await likeCommunityPost(token, postId);
        this.likeState = nextState;
        if (this.detail) {
          this.detail.likeCount = nextState.likeCount;
        }
        this.actionMessage = nextState.liked ? '已点赞。' : '已取消点赞。';
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          this.redirectToLogin();
          return;
        }
        this.actionError = error instanceof Error ? error.message : '点赞状态更新失败，请稍后重试。';
      } finally {
        this.likeSubmitting = false;
      }
    },

    /**
     * Likes or unlikes a comment.
     */
    async toggleCommentLike(this: any, commentId: number) {
      const postId = this.resolvePostId();
      const token = loadStoredToken();

      if (!token) {
        this.redirectToLogin();
        return;
      }

      const targetComment = this.comments.find((c: PostCommentData) => c.id === commentId);
      const wasLiked = Boolean(targetComment?.liked);
      const prevLikeCount = Number(targetComment?.likeCount || 0);

      // Optimistic update for immediate feedback
      this.comments = this.comments.map((c: PostCommentData) =>
        c.id === commentId
          ? { ...c, liked: !wasLiked, likeCount: wasLiked ? Math.max(0, prevLikeCount - 1) : prevLikeCount + 1 }
          : c
      );

      this.commentLikeSubmitting = { ...this.commentLikeSubmitting, [commentId]: true };

      try {
        const result = wasLiked
          ? await unlikeCommunityComment(token, postId, commentId)
          : await likeCommunityComment(token, postId, commentId);
        // Confirm liked state from backend; keep optimistic likeCount
        this.comments = this.comments.map((c: PostCommentData) =>
          c.id === commentId ? { ...c, liked: result.liked } : c
        );
      } catch (error) {
        // Revert optimistic update on failure
        this.comments = this.comments.map((c: PostCommentData) =>
          c.id === commentId ? { ...c, liked: wasLiked, likeCount: prevLikeCount } : c
        );
        if (error instanceof ApiError && error.code === 401) {
          this.redirectToLogin();
          return;
        }
        this.actionError = error instanceof Error ? error.message : '评论点赞失败，请稍后重试。';
      } finally {
        this.commentLikeSubmitting = { ...this.commentLikeSubmitting, [commentId]: false };
      }
    },

    /**
     * Deletes the current post after a confirmation dialog.
     */
    async deletePost(this: any) {
      const postId = this.resolvePostId();
      const token = loadStoredToken();

      if (!token) {
        this.redirectToLogin();
        return;
      }
      if (!this.detail || !this.isAuthor()) {
        this.actionError = '只有帖子作者可以删除这篇内容。';
        return;
      }
      if (typeof window !== 'undefined' && !window.confirm('确认删除这篇帖子吗？删除后将无法恢复。')) {
        return;
      }

      this.deleting = true;
      this.actionError = '';
      this.actionMessage = '';

      try {
        await deleteCommunityPost(token, postId);
        this.$router.replace(`/boards/${this.detail.boardId}`);
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          this.redirectToLogin();
          return;
        }
        this.actionError = error instanceof Error ? error.message : '帖子删除失败，请稍后重试。';
      } finally {
        this.deleting = false;
      }
    },

    /**
     * Navigates the author to the compose page in edit mode.
     */
    editPost(this: any) {
      if (!this.detail) {
        return;
      }
      this.$router.push({
        path: '/compose',
        query: {
          postId: String(this.detail.id),
          boardId: String(this.detail.boardId)
        }
      });
    },

    /**
     * Shares the current post using the Web Share API if available, else copies the URL.
     */
    async sharePost(this: any) {
      const url = window.location.href;
      const title = this.detail?.title || 'Kite Universe';
      if (navigator.share) {
        try {
          await navigator.share({ title, url });
          return;
        } catch {
          // User cancelled or API failed — fall through to copy
        }
      }
      try {
        await navigator.clipboard.writeText(url);
        this.actionMessage = '链接已复制到剪贴板';
      } catch {
        this.actionError = '链接复制失败，请手动复制地址栏链接';
      }
    },

    /**
     * Opens the inline reply form targeting a specific comment.
     */
    startReply(this: any, comment: PostCommentData) {
      this.replyToCommentId = comment.id;
      this.replyToName = comment.authorName;
      this.replyContent = '';
      this.replyError = '';
    },

    /**
     * Cancels the active inline reply.
     */
    cancelReply(this: any) {
      this.replyToCommentId = null;
      this.replyToName = '';
      this.replyContent = '';
      this.replyError = '';
    },

    /**
     * Submits a nested reply comment to the backend.
     */
    async submitReply(this: any) {
      const postId = this.resolvePostId();
      const token = loadStoredToken();
      const normalizedContent = this.replyContent.trim();

      if (!token) {
        this.redirectToLogin();
        return;
      }
      if (!normalizedContent) {
        this.replyError = '回复内容不能为空。';
        return;
      }
      if (normalizedContent.length > 1000) {
        this.replyError = '回复内容请控制在 1000 个字以内。';
        return;
      }

      this.replySubmitting = true;
      this.replyError = '';

      try {
        const created = await createCommunityPostComment(token, postId, {
          content: normalizedContent,
          parentId: this.replyToCommentId
        });
        this.comments = [...this.comments, created];
        this.cancelReply();
        if (this.detail) {
          this.detail.commentCount = Number(this.detail.commentCount || 0) + 1;
        }
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          this.redirectToLogin();
          return;
        }
        this.replyError = error instanceof Error ? error.message : '回复发布失败，请稍后重试。';
      } finally {
        this.replySubmitting = false;
      }
    },

    /**
     * Parses the current route post id safely.
     */
    resolvePostId(this: any) {
      const rawPostId = Number(this.$route.params.postId);
      return Number.isFinite(rawPostId) && rawPostId > 0 ? rawPostId : 0;
    },

    /**
     * Returns whether the current browser session already has a token.
     */
    hasToken(this: any) {
      return Boolean(loadStoredToken());
    },

    /**
     * Returns whether the current logged-in user is the author of the post.
     */
    isAuthor(this: any) {
      const currentUser = loadStoredUser();
      return Boolean(currentUser && this.detail?.authorId && currentUser.id === this.detail.authorId);
    },

    /**
     * Returns the favorite count displayed in the hero statistics.
     */
    getFavoriteCount(this: any) {
      return this.favoriteState?.favoriteCount ?? this.detail?.favoriteCount ?? 0;
    },

    /**
     * Returns the label displayed on the favorite button.
     */
    getFavoriteActionLabel(this: any) {
      if (!this.hasToken()) {
        return '登录后收藏';
      }
      return this.favoriteState?.favorited ? '已收藏' : '收藏帖子';
    },

    /**
     * Redirects anonymous users to the login modal and preserves the current route.
     */
    redirectToLogin(this: any) {
      this.$router.push({
        path: '/',
        query: {
          auth: 'login',
          redirect: this.$route.fullPath
        }
      });
    },

    /**
     * Resolves relative avatar paths returned by the backend.
     */
    resolveAvatarUrl(this: any, path: string | null | undefined) {
      return resolveAssetUrl(path);
    },

    /**
     * Returns the leading character used by avatar placeholders.
     */
    getInitial(this: any, value: string | null | undefined) {
      const fallback = value || 'K';
      return fallback.slice(0, 1).toUpperCase();
    },

    /**
     * Renders Markdown content to HTML for display in the post body.
     */
    renderMarkdown(this: any, content: string | null) {
      if (!content) {
        return '';
      }
      if (typeof marked === 'undefined') {
        return content.replace(/\n/g, '<br>');
      }
      return marked.parse(content);
    },

    /**
     * Parses the gallery images JSON string into a URL array.
     */
    getGalleryImages(this: any): string[] {
      const raw = this.detail?.galleryImages;
      if (!raw) {
        return [];
      }
      try {
        const parsed = JSON.parse(raw);
        return Array.isArray(parsed) ? parsed : [];
      } catch {
        return [];
      }
    },

    /**
     * Opens the lightbox with the specified image URL.
     */
    openLightbox(this: any, url: string) {
      this.lightboxUrl = url;
    },

    /**
     * Closes the lightbox.
     */
    closeLightbox(this: any) {
      this.lightboxUrl = null;
    },

    /**
     * Handles click events on the markdown body to open image lightbox.
     */
    onPostBodyClick(this: any, event: MouseEvent) {
      const target = event.target as HTMLElement;
      if (target.tagName === 'IMG') {
        const src = (target as HTMLImageElement).src;
        if (src) {
          this.openLightbox(src);
        }
      }
    },

    /**
     * Formats timestamps into user-friendly text.
     */
    formatDateTime(this: any, value: string | null) {
      if (!value) {
        return '暂无记录';
      }

      const date = new Date(value);
      if (Number.isNaN(date.getTime())) {
        return value.replace('T', ' ');
      }

      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  },
  mounted(this: any) {
    void this.loadPostPage();
  },
  template: `
    <main class="page-shell detail-shell">
      <section v-if="loading" class="panel page-state">
        <div class="page-state__content">
          <span class="eyebrow">正在展开</span>
          <h2>正在加载帖子详情...</h2>
          <p>正文和评论正在展开，稍等一下就好。</p>
        </div>
      </section>

      <section v-else-if="errorMessage" class="panel page-state page-state--error">
        <div class="page-state__content">
          <span class="eyebrow">加载失败</span>
          <h2>帖子内容暂时没有加载出来</h2>
          <p>{{ errorMessage }}</p>
          <button class="button button--primary" type="button" @click="loadPostPage">重新加载</button>
        </div>
      </section>

      <template v-else-if="detail">
        <section class="detail-hero detail-hero--post">
          <div class="detail-hero__main">
            <div class="detail-hero__badges">
              <span class="eyebrow">{{ detail.badge || detail.boardTagName }}</span>
              <span v-if="detail.pinned" class="post-badge post-badge--pinned">置顶</span>
              <span v-if="detail.featured" class="post-badge post-badge--featured">精华</span>
              <span v-if="detail.isAiGenerated" class="ai-badge" title="本文包含 AI 生成内容">AI</span>
            </div>
            <h1>{{ detail.title }}</h1>
            <p>{{ detail.summary }}</p>
            <div class="detail-meta">
              <span>版区：{{ detail.boardName }}</span>
              <span>作者：<router-link v-if="detail.authorId" :to="'/users/' + detail.authorId">{{ detail.authorName }}</router-link><template v-else>{{ detail.authorName }}</template></span>
              <span>发布时间：{{ formatDateTime(detail.publishedAt) }}</span>
            </div>
            <div class="hero__actions">
              <router-link class="button button--ghost" :to="'/boards/' + detail.boardId">回到版区</router-link>
              <button
                class="button"
                :class="likeState?.liked ? 'button--primary' : 'button--ghost'"
                type="button"
                :disabled="likeSubmitting || !hasToken()"
                @click="toggleLike"
              >
                {{ likeSubmitting ? '处理中...' : (likeState?.liked ? '已赞' : '点赞') }}
              </button>
              <button
                class="button"
                :class="favoriteState?.favorited ? 'button--primary' : 'button--ghost'"
                type="button"
                :disabled="favoriteSubmitting"
                @click="toggleFavorite"
              >
                {{ favoriteSubmitting ? '处理中...' : getFavoriteActionLabel() }}
              </button>
              <button
                class="button button--ghost"
                type="button"
                @click="sharePost"
              >
                分享
              </button>
              <router-link
                class="button button--ghost"
                :to="{ path: '/compose', query: { boardId: String(detail.boardId) } }"
              >
                发新帖
              </router-link>
              <button
                v-if="isAuthor()"
                class="button button--ghost"
                type="button"
                @click="editPost"
              >
                编辑帖子
              </button>
              <button
                v-if="isAuthor()"
                class="button button--danger"
                type="button"
                :disabled="deleting"
                @click="deletePost"
              >
                {{ deleting ? '删除中...' : '删除帖子' }}
              </button>
            </div>
            <p v-if="actionError" class="auth-feedback auth-feedback--error detail-feedback">{{ actionError }}</p>
            <p v-if="!actionError && actionMessage" class="auth-feedback auth-feedback--success detail-feedback">{{ actionMessage }}</p>
          </div>

          <div class="detail-hero__stats">
            <article class="detail-stat-card">
              <span>浏览</span>
              <strong>{{ detail.viewCount }}</strong>
              <p>每一次进入详情页，都会慢慢把这篇内容变成更热的话题。</p>
            </article>
            <article class="detail-stat-card">
              <span>评论</span>
              <strong>{{ detail.commentCount }}</strong>
              <p>评论提交成功后，这里的计数会立刻同步。</p>
            </article>
            <article class="detail-stat-card">
              <span>收藏</span>
              <strong>{{ getFavoriteCount() }}</strong>
              <p>值得回看的内容先留个标记，后面再翻也方便。</p>
            </article>
            <article class="detail-stat-card">
              <span>点赞</span>
              <strong>{{ likeState ? likeState.likeCount : (detail?.likeCount ?? 0) }}</strong>
              <p>觉得不错就点个赞，让更多人看到优质内容。</p>
            </article>
          </div>
        </section>

        <section class="content-grid detail-grid">
          <div class="content-main">
            <article class="panel post-article">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">帖子正文</span>
                  <h2>正文内容</h2>
                </div>
              </div>
              <div
                class="post-article__body post-markdown-body"
                v-html="renderMarkdown(detail.content)"
                @click="onPostBodyClick"
              ></div>

              <div v-if="getGalleryImages().length" class="post-gallery">
                <h3 class="post-gallery__title">图集</h3>
                <div class="post-gallery__grid">
                  <img
                    v-for="(img, idx) in getGalleryImages()"
                    :key="idx"
                    :src="img"
                    :alt="'图集图片 ' + (idx + 1)"
                    class="post-gallery__img"
                    @click="openLightbox(img)"
                  />
                </div>
              </div>
            </article>

            <section class="panel comment-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">楼层讨论</span>
                  <h2>评论区</h2>
                </div>
                <span class="panel__link">{{ comments.length }} 条评论</span>
              </div>

              <form class="comment-form" @submit.prevent="submitComment">
                <label class="profile-field profile-field--wide">
                  <span>参与讨论</span>
                  <textarea
                    v-model="commentContent"
                    rows="5"
                    placeholder="写下你的想法、补充或者反馈内容。"
                  ></textarea>
                </label>

                <p v-if="submitError" class="auth-feedback auth-feedback--error">{{ submitError }}</p>
                <p v-if="!submitError && submitMessage" class="auth-feedback auth-feedback--success">{{ submitMessage }}</p>

                <div class="comment-form__actions">
                  <button class="button button--primary" type="submit" :disabled="commentSubmitting">
                    {{ commentSubmitting ? '发布中...' : '发布评论' }}
                  </button>
                  <button
                    v-if="!hasToken()"
                    class="button button--ghost"
                    type="button"
                    @click="redirectToLogin"
                  >
                    登录后参与讨论
                  </button>
                </div>
              </form>

              <div v-if="!comments.length" class="empty-state">
                <h3>还没有评论</h3>
                <p>先留下第一条回复，让这篇帖子慢慢热起来。</p>
              </div>

              <div v-else class="comment-list">
                <article
                  v-for="comment in comments"
                  :key="comment.id"
                  class="comment-card"
                  :class="comment.parentId ? 'comment-card--reply' : ''"
                >
                  <div class="comment-card__avatar">
                    <img
                      v-if="comment.authorAvatar"
                      :src="resolveAvatarUrl(comment.authorAvatar)"
                      :alt="comment.authorName + ' avatar'"
                    />
                    <span v-else>{{ getInitial(comment.authorName) }}</span>
                  </div>
                  <div class="comment-card__body">
                    <div class="comment-card__meta">
                      <strong><router-link v-if="comment.authorId" :to="'/users/' + comment.authorId" class="comment-author-link">{{ comment.authorName }}</router-link><template v-else>{{ comment.authorName }}</template></strong>
                      <span v-if="comment.replyToName" class="comment-reply-to">回复 {{ comment.replyToName }}</span>
                      <span>{{ formatDateTime(comment.createTime) }}</span>
                    </div>
                    <p>{{ comment.content }}</p>
                    <div class="comment-card__actions">
                      <button class="button button--small" :class="comment.liked ? 'button--primary' : 'button--ghost'" type="button" :disabled="commentLikeSubmitting[comment.id]" @click="toggleCommentLike(comment.id)">{{ comment.liked ? '已赞' : '赞' }} {{ comment.likeCount || 0 }}</button>
                      <button v-if="hasToken()" class="button button--small button--ghost" type="button" @click="startReply(comment)">回复</button>
                    </div>
                    <div v-if="replyToCommentId === comment.id" class="comment-reply-form">
                      <textarea
                        v-model="replyContent"
                        rows="3"
                        :placeholder="'回复 ' + replyToName + '...'"
                        class="comment-reply-form__input"
                      ></textarea>
                      <p v-if="replyError" class="auth-feedback auth-feedback--error">{{ replyError }}</p>
                      <div class="comment-reply-form__actions">
                        <button class="button button--primary button--small" type="button" :disabled="replySubmitting" @click="submitReply">{{ replySubmitting ? '发布中...' : '发布回复' }}</button>
                        <button class="button button--ghost button--small" type="button" @click="cancelReply">取消</button>
                      </div>
                    </div>
                  </div>
                </article>
              </div>
            </section>
          </div>

          <aside class="content-side">
            <section class="panel soft-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">内容信息</span>
                  <h2>帖子信息</h2>
                </div>
              </div>
              <div class="notice-list">
                <p>所属版区：{{ detail.boardName }}</p>
                <p>创建时间：{{ formatDateTime(detail.createTime) }}</p>
                <p>最近更新：{{ formatDateTime(detail.updateTime) }}</p>
              </div>
            </section>

            <section v-if="isAuthor()" class="panel soft-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">作者操作</span>
                  <h2>管理这篇帖子</h2>
                </div>
              </div>
              <div class="notice-list">
                <p>如果有新补充或修正，可以继续编辑完善正文内容。</p>
                <p>不再展示的内容可以直接删除，版区列表和个人中心会同步更新。</p>
              </div>
              <div class="comment-form__actions detail-side-actions">
                <button class="button button--ghost button--small" type="button" @click="editPost">继续编辑</button>
                <button class="button button--danger button--small" type="button" :disabled="deleting" @click="deletePost">
                  {{ deleting ? '删除中...' : '删除' }}
                </button>
              </div>
            </section>

            <section class="panel soft-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">互动提示</span>
                  <h2>互动说明</h2>
                </div>
              </div>
              <div class="notice-list">
                <p>评论尽量围绕主题展开，补充实测、疑问或个人看法都会很有帮助。</p>
                <p>登录后可以收藏这篇帖子，稍后会在个人中心的收藏列表里看到它。</p>
                <p>如果你也是作者，可以继续编辑内容，让这篇帖子保持最新状态。</p>
              </div>
            </section>

            <section v-if="relatedPosts.length" class="panel soft-panel">
              <div class="panel__header">
                <div>
                  <span class="panel__kicker">相关推荐</span>
                  <h2>你可能也感兴趣</h2>
                </div>
              </div>
              <div class="notice-list" style="gap:8px;">
                <router-link
                  v-for="rp in relatedPosts"
                  :key="rp.id"
                  :to="'/posts/' + rp.id"
                  class="sidebar-link-card"
                >
                  <strong>{{ rp.title }}</strong>
                  <span>{{ rp.boardName }} · {{ rp.authorName }}</span>
                </router-link>
              </div>
            </section>
          </aside>
        </section>
      </template>

      <div v-if="lightboxUrl" class="lightbox" @click.self="closeLightbox">
        <button class="lightbox__close" type="button" @click="closeLightbox" aria-label="关闭图片预览">✕</button>
        <img class="lightbox__img" :src="lightboxUrl" alt="图片预览" />
      </div>
    </main>
  `
};
