import {
  ApiError,
  fetchCurrentUserFavoritePosts,
  fetchCurrentUserPosts,
  fetchCurrentUserProfile,
  logoutCurrentSession,
  resolveAssetUrl,
  updateCurrentUserProfile,
  uploadCurrentUserAvatar,
  type PostSummaryData,
  type UserDetailVO,
  type UserInfoUpdatePayload
} from '../services/api';
import {
  clearStoredSession,
  loadStoredToken,
  syncStoredUserFromDetail
} from '../services/session';

interface ProfileFormState {
  nickname: string;
  gender: string;
  realName: string;
  birthday: string;
  signature: string;
  profile: string;
  country: string;
  province: string;
  city: string;
  website: string;
  backgroundImage: string;
}

const MAX_AVATAR_SIZE = 5 * 1024 * 1024;

/**
 * Creates the default profile form state.
 */
function createProfileForm(): ProfileFormState {
  return {
    nickname: '',
    gender: '0',
    realName: '',
    birthday: '',
    signature: '',
    profile: '',
    country: '',
    province: '',
    city: '',
    website: '',
    backgroundImage: ''
  };
}

/**
 * Maps backend profile data to the editable form model.
 */
function createProfileFormFromDetail(detail: UserDetailVO): ProfileFormState {
  return {
    nickname: detail.nickname || '',
    gender: String(detail.gender ?? 0),
    realName: detail.realName || '',
    birthday: detail.birthday || '',
    signature: detail.signature || '',
    profile: detail.profile || '',
    country: detail.country || '',
    province: detail.province || '',
    city: detail.city || '',
    website: detail.website || '',
    backgroundImage: detail.backgroundImage || ''
  };
}

/**
 * Normalizes user-input text fields before they are sent to the backend.
 */
function normalizeNullableText(value: string) {
  const normalizedValue = value.trim();
  return normalizedValue ? normalizedValue : null;
}

/**
 * Personal center page component.
 */
export const ProfilePage = {
  data() {
    return {
      detail: null as UserDetailVO | null,
      form: createProfileForm(),
      ownPosts: [] as PostSummaryData[],
      favoritePosts: [] as PostSummaryData[],
      loading: true,
      saving: false,
      avatarUploading: false,
      ownPostsLoading: true,
      favoritePostsLoading: true,
      profileError: '',
      ownPostsError: '',
      favoritePostsError: '',
      profileMessage: ''
    };
  },
  methods: {
    /**
     * Loads the profile detail and personal post collections together.
     */
    async loadPageData(this: any) {
      const token = loadStoredToken();
      if (!token) {
        this.redirectToLogin('请先登录后再查看个人中心。');
        return;
      }

      this.loading = true;
      this.ownPostsLoading = true;
      this.favoritePostsLoading = true;
      this.profileError = '';
      this.ownPostsError = '';
      this.favoritePostsError = '';

      const [detailResult, ownPostsResult, favoritePostsResult] = await Promise.allSettled([
        fetchCurrentUserProfile(token),
        fetchCurrentUserPosts(token, 20),
        fetchCurrentUserFavoritePosts(token, 20)
      ]);

      if (detailResult.status === 'fulfilled') {
        this.applyDetail(detailResult.value);
      } else if (this.handleAuthFailure(detailResult.reason)) {
        return;
      } else {
        this.profileError = detailResult.reason instanceof Error
          ? detailResult.reason.message
          : '个人资料加载失败，请稍后重试。';
      }

      if (ownPostsResult.status === 'fulfilled') {
        this.ownPosts = ownPostsResult.value;
      } else if (this.handleAuthFailure(ownPostsResult.reason)) {
        return;
      } else {
        this.ownPostsError = ownPostsResult.reason instanceof Error
          ? ownPostsResult.reason.message
          : '我的帖子加载失败，请稍后重试。';
      }

      if (favoritePostsResult.status === 'fulfilled') {
        this.favoritePosts = favoritePostsResult.value;
      } else if (this.handleAuthFailure(favoritePostsResult.reason)) {
        return;
      } else {
        this.favoritePostsError = favoritePostsResult.reason instanceof Error
          ? favoritePostsResult.reason.message
          : '我的收藏加载失败，请稍后重试。';
      }

      this.loading = false;
      this.ownPostsLoading = false;
      this.favoritePostsLoading = false;
    },

    /**
     * Applies the latest profile detail to the page state and shared nav summary.
     */
    applyDetail(this: any, detail: UserDetailVO) {
      this.detail = detail;
      this.form = createProfileFormFromDetail(detail);
      syncStoredUserFromDetail(detail);
    },

    /**
     * Saves the editable profile fields back to the backend.
     */
    async submitProfile(this: any) {
      const token = loadStoredToken();
      if (!token) {
        this.redirectToLogin('登录状态已失效，请重新登录。');
        return;
      }

      if (this.form.nickname.trim().length > 20) {
        this.profileError = '昵称长度请控制在 20 个字符以内。';
        return;
      }
      if (this.form.signature.trim().length > 60) {
        this.profileError = '个性签名长度请控制在 60 个字符以内。';
        return;
      }

      this.saving = true;
      this.profileError = '';
      this.profileMessage = '';

      const payload: UserInfoUpdatePayload = {
        nickname: normalizeNullableText(this.form.nickname),
        gender: Number(this.form.gender || 0),
        realName: normalizeNullableText(this.form.realName),
        birthday: normalizeNullableText(this.form.birthday),
        signature: normalizeNullableText(this.form.signature),
        profile: normalizeNullableText(this.form.profile),
        country: normalizeNullableText(this.form.country),
        province: normalizeNullableText(this.form.province),
        city: normalizeNullableText(this.form.city),
        website: normalizeNullableText(this.form.website),
        backgroundImage: normalizeNullableText(this.form.backgroundImage)
      };

      try {
        const detail = await updateCurrentUserProfile(token, payload);
        this.applyDetail(detail);
        this.profileMessage = '个人资料已经更新。';
      } catch (error) {
        this.handleProfileError(error, '个人资料保存失败，请稍后重试。');
      } finally {
        this.saving = false;
      }
    },

    /**
     * Uploads a new avatar image selected by the user.
     */
    async handleAvatarChange(this: any, event: Event) {
      const inputElement = event.target as HTMLInputElement;
      const file = inputElement.files?.[0];
      if (!file) {
        return;
      }

      if (file.size > MAX_AVATAR_SIZE) {
        this.profileError = '头像大小不能超过 5MB。';
        inputElement.value = '';
        return;
      }

      const token = loadStoredToken();
      if (!token) {
        inputElement.value = '';
        this.redirectToLogin('登录状态已失效，请重新登录。');
        return;
      }

      this.avatarUploading = true;
      this.profileError = '';
      this.profileMessage = '';

      try {
        const detail = await uploadCurrentUserAvatar(token, file);
        this.applyDetail(detail);
        this.profileMessage = '头像已经更新。';
      } catch (error) {
        this.handleProfileError(error, '头像上传失败，请稍后重试。');
      } finally {
        this.avatarUploading = false;
        inputElement.value = '';
      }
    },

    /**
     * Logs the current user out from the personal center.
     */
    async logout(this: any) {
      const token = loadStoredToken();

      try {
        if (token) {
          await logoutCurrentSession(token);
        }
      } catch {
        // The frontend still clears the local session even if the network request fails.
      } finally {
        clearStoredSession();
        this.$router.push({
          path: '/',
          query: {
            auth: 'login'
          }
        });
      }
    },

    /**
     * Converts backend time strings into readable browser-local text.
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
    },

    /**
     * Resolves avatar URLs that may come from the backend as relative paths.
     */
    resolveAvatarUrl(this: any, path: string | null | undefined) {
      return resolveAssetUrl(path);
    },

    /**
     * Returns the display text used by avatar placeholders.
     */
    getUserInitial(this: any) {
      const baseText = this.form.nickname || this.detail?.nickname || this.detail?.username || 'K';
      return baseText.slice(0, 1).toUpperCase();
    },

    /**
     * Builds a quick profile-completion percentage from the editable fields.
     */
    getProfileCompletion(this: any) {
      const fields = [
        this.detail?.avatar,
        this.form.nickname,
        this.form.signature,
        this.form.profile,
        this.form.realName,
        this.form.birthday,
        this.form.country,
        this.form.city,
        this.form.website
      ];

      const filledCount = fields.filter((item) => normalizeNullableText(String(item || ''))).length;
      return Math.round((filledCount / fields.length) * 100);
    },

    /**
     * Handles API errors and redirects to the login modal when the session expired.
     */
    handleProfileError(this: any, error: unknown, fallbackMessage: string) {
      if (this.handleAuthFailure(error)) {
        return;
      }

      this.profileError = error instanceof Error ? error.message : fallbackMessage;
    },

    /**
     * Clears the current session and redirects the user back to the login modal.
     */
    redirectToLogin(this: any, message: string) {
      clearStoredSession();
      this.profileError = message;
      this.$router.push({
        path: '/',
        query: {
          auth: 'login',
          redirect: '/profile'
        }
      });
    },

    /**
     * Checks whether a request failed because the current session expired.
     */
    handleAuthFailure(this: any, error: unknown) {
      if (error instanceof ApiError && error.code === 401) {
        this.loading = false;
        this.ownPostsLoading = false;
        this.favoritePostsLoading = false;
        this.redirectToLogin('登录状态已失效，请重新登录。');
        return true;
      }
      return false;
    }
  },
  mounted(this: any) {
    void this.loadPageData();
  },
  template: `
    <main class="page-shell profile-shell">
      <section class="profile-hero">
        <div class="profile-hero__identity">
          <div class="profile-avatar profile-avatar--large">
            <img
              v-if="detail?.avatar"
              :src="resolveAvatarUrl(detail.avatar)"
              :alt="(detail?.nickname || detail?.username || 'Kite User') + ' avatar'"
            />
            <span v-else>{{ getUserInitial() }}</span>
          </div>
          <div class="profile-hero__text">
            <span class="eyebrow">个人中心</span>
            <h1>{{ form.nickname || detail?.nickname || detail?.username || '社区成员' }}</h1>
            <p>{{ detail?.phone || '暂未绑定手机号' }} · {{ detail?.username || '未登录用户' }}</p>
            <div class="profile-meta">
              <span>最近登录：{{ formatDateTime(detail?.lastLoginTime) }}</span>
              <span>加入时间：{{ formatDateTime(detail?.createTime) }}</span>
              <span>已发帖子：{{ ownPosts.length }}</span>
              <span>收藏内容：{{ favoritePosts.length }}</span>
            </div>
          </div>
        </div>

        <div class="profile-hero__actions">
          <label class="button button--ghost profile-upload-trigger">
            <input
              class="profile-upload-input"
              type="file"
              accept="image/png,image/jpeg,image/webp,image/gif"
              @change="handleAvatarChange"
              :disabled="avatarUploading"
            />
            {{ avatarUploading ? '头像上传中...' : '上传头像' }}
          </label>
          <router-link class="button button--primary" to="/compose">发布帖子</router-link>
          <button class="button button--ghost" type="button" @click="logout">退出登录</button>
        </div>
      </section>

      <section class="content-grid profile-grid">
        <div class="content-main profile-main-stack">
          <section class="panel profile-panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">资料设置</span>
                <h2>编辑个人资料</h2>
              </div>
              <span class="panel__link profile-panel__status">资料完整度 {{ getProfileCompletion() }}%</span>
            </div>

            <div v-if="loading" class="profile-loading">正在加载你的个人资料...</div>

            <form v-else class="profile-form" @submit.prevent="submitProfile">
              <div class="profile-form__grid">
                <label class="profile-field">
                  <span>社区昵称</span>
                  <input v-model="form.nickname" type="text" maxlength="20" placeholder="用于导航和社区展示" />
                </label>

                <label class="profile-field">
                  <span>性别</span>
                  <select v-model="form.gender">
                    <option value="0">保密</option>
                    <option value="1">男</option>
                    <option value="2">女</option>
                  </select>
                </label>

                <label class="profile-field">
                  <span>真实姓名</span>
                  <input v-model="form.realName" type="text" maxlength="30" placeholder="选填" />
                </label>

                <label class="profile-field">
                  <span>生日</span>
                  <input v-model="form.birthday" type="date" />
                </label>

                <label class="profile-field profile-field--wide">
                  <span>个性签名</span>
                  <input v-model="form.signature" type="text" maxlength="60" placeholder="用一句话介绍自己" />
                </label>

                <label class="profile-field profile-field--wide">
                  <span>个人简介</span>
                  <textarea v-model="form.profile" rows="5" placeholder="写一点你的兴趣、常驻版区或者创作方向"></textarea>
                </label>

                <label class="profile-field">
                  <span>国家 / 地区</span>
                  <input v-model="form.country" type="text" maxlength="30" placeholder="例如：中国" />
                </label>

                <label class="profile-field">
                  <span>省份</span>
                  <input v-model="form.province" type="text" maxlength="30" placeholder="例如：江苏" />
                </label>

                <label class="profile-field">
                  <span>城市</span>
                  <input v-model="form.city" type="text" maxlength="30" placeholder="例如：南京" />
                </label>

                <label class="profile-field">
                  <span>个人网站</span>
                  <input v-model="form.website" type="url" maxlength="255" placeholder="https://example.com" />
                </label>

                <label class="profile-field profile-field--wide">
                  <span>背景图链接</span>
                  <input v-model="form.backgroundImage" type="url" maxlength="255" placeholder="可填写个人主页背景图 URL" />
                </label>
              </div>

              <p v-if="profileError" class="auth-feedback auth-feedback--error">{{ profileError }}</p>
              <p v-if="!profileError && profileMessage" class="auth-feedback auth-feedback--success">{{ profileMessage }}</p>

              <div class="profile-form__actions">
                <button class="button button--primary" type="submit" :disabled="saving">
                  {{ saving ? '保存中...' : '保存资料' }}
                </button>
              </div>
            </form>
          </section>

          <section class="panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">我的创作</span>
                <h2>我的帖子</h2>
              </div>
              <router-link class="button button--ghost button--small" to="/compose">继续发帖</router-link>
            </div>

            <div v-if="ownPostsLoading" class="profile-loading">正在整理你发过的帖子...</div>
            <p v-else-if="ownPostsError" class="auth-feedback auth-feedback--error">{{ ownPostsError }}</p>
            <div v-else-if="!ownPosts.length" class="empty-state">
              <h3>还没有发布过帖子</h3>
              <p>从一篇攻略、一条讨论或一段创作开始，让个人主页慢慢丰富起来。</p>
            </div>
            <div v-else class="post-stack">
              <article v-for="post in ownPosts" :key="post.id" class="post-summary-card post-summary-card--static">
                <div class="post-summary-card__head">
                  <span class="capsule">{{ post.badge || post.boardTagName }}</span>
                  <span class="post-summary-card__time">{{ formatDateTime(post.publishedAt) }}</span>
                </div>
                <h3>{{ post.title }}</h3>
                <p>{{ post.summary }}</p>
                <div class="post-summary-card__foot">
                  <div class="topic-card__stats">
                    <span class="topic-stat">{{ post.boardName }}</span>
                    <span class="topic-stat">浏览 {{ post.viewCount }}</span>
                    <span class="topic-stat">评论 {{ post.commentCount }}</span>
                    <span class="topic-stat">收藏 {{ post.favoriteCount }}</span>
                  </div>
                  <div class="post-summary-card__actions">
                    <router-link class="button button--ghost button--small" :to="'/posts/' + post.id">查看</router-link>
                    <router-link
                      class="button button--primary button--small"
                      :to="{ path: '/compose', query: { postId: String(post.id), boardId: String(post.boardId) } }"
                    >
                      编辑
                    </router-link>
                  </div>
                </div>
              </article>
            </div>
          </section>

          <section class="panel">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">收藏夹</span>
                <h2>我的收藏</h2>
              </div>
              <router-link class="button button--ghost button--small" to="/boards">逛逛版区</router-link>
            </div>

            <div v-if="favoritePostsLoading" class="profile-loading">正在加载你收藏的内容...</div>
            <p v-else-if="favoritePostsError" class="auth-feedback auth-feedback--error">{{ favoritePostsError }}</p>
            <div v-else-if="!favoritePosts.length" class="empty-state">
              <h3>收藏夹还是空的</h3>
              <p>看到值得回看的帖子时点一下收藏，后面会更方便继续追更。</p>
            </div>
            <div v-else class="post-stack">
              <article v-for="post in favoritePosts" :key="post.id" class="post-summary-card post-summary-card--static">
                <div class="post-summary-card__head">
                  <span class="capsule">{{ post.badge || post.boardTagName }}</span>
                  <span class="post-summary-card__time">{{ formatDateTime(post.publishedAt) }}</span>
                </div>
                <h3>{{ post.title }}</h3>
                <p>{{ post.summary }}</p>
                <div class="post-summary-card__foot">
                  <div class="topic-card__stats">
                    <span class="topic-stat">{{ post.boardName }}</span>
                    <span class="topic-stat">作者 {{ post.authorName }}</span>
                    <span class="topic-stat">评论 {{ post.commentCount }}</span>
                  </div>
                  <div class="post-summary-card__actions">
                    <router-link class="button button--ghost button--small" :to="'/posts/' + post.id">查看详情</router-link>
                  </div>
                </div>
              </article>
            </div>
          </section>
        </div>

        <aside class="content-side">
          <section class="panel soft-panel profile-side-card">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">账号概览</span>
                <h2>账号信息</h2>
              </div>
            </div>

            <div class="profile-side-card__rows">
              <div class="profile-side-card__row">
                <span>账号状态</span>
                <strong>{{ detail?.status === 1 ? '正常' : '受限' }}</strong>
              </div>
              <div class="profile-side-card__row">
                <span>登录方式</span>
                <strong>手机号验证码</strong>
              </div>
              <div class="profile-side-card__row">
                <span>当前手机号</span>
                <strong>{{ detail?.phone || '未绑定' }}</strong>
              </div>
              <div class="profile-side-card__row">
                <span>常驻城市</span>
                <strong>{{ form.city || '暂未填写' }}</strong>
              </div>
            </div>
          </section>

          <section class="panel soft-panel profile-side-card">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">社区足迹</span>
                <h2>内容概览</h2>
              </div>
            </div>

            <div class="profile-side-card__rows">
              <div class="profile-side-card__row">
                <span>我发布的帖子</span>
                <strong>{{ ownPosts.length }}</strong>
              </div>
              <div class="profile-side-card__row">
                <span>我收藏的帖子</span>
                <strong>{{ favoritePosts.length }}</strong>
              </div>
              <div class="profile-side-card__row">
                <span>资料完整度</span>
                <strong>{{ getProfileCompletion() }}%</strong>
              </div>
            </div>
          </section>

          <section class="panel soft-panel profile-side-card">
            <div class="panel__header">
              <div>
                <span class="panel__kicker">编辑提示</span>
                <h2>资料建议</h2>
              </div>
            </div>

            <div class="notice-list">
              <p>头像支持 JPG、PNG、GIF 和 WebP，大小不超过 5MB。</p>
              <p>昵称和签名会同步显示在顶部导航，以及你发布的帖子和评论里。</p>
              <p>收藏和发帖都会沉淀在个人中心，方便你随时回看自己的内容轨迹。</p>
            </div>
          </section>
        </aside>
      </section>
    </main>
  `
};
