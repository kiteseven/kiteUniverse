declare const Vue: any;
declare const VueRouter: any;

import './style.css';
import brandLogo from './assets/kite-universe-logo-v4.svg';
import {
  ApiError,
  fetchCurrentAuthUser,
  loginByPhone,
  logoutCurrentSession,
  registerByPhone,
  resolveAssetUrl,
  sendSmsCode,
  type AuthResultVO,
  type AuthUserVO
} from './services/api';
import {
  clearStoredSession,
  loadStoredToken,
  loadStoredUser,
  saveAuthSession,
  subscribeSessionChange,
  updateStoredUser,
  type SessionSnapshot
} from './services/session';
import { createRouterInstance, RouteNames, RoutePaths, routerHelper } from './router';

type AuthMode = 'login' | 'register';

interface AuthFormState {
  phone: string;
  code: string;
  nickname: string;
}

const { createApp } = Vue;
const router = createRouterInstance(VueRouter);

const PHONE_PATTERN = /^1\d{10}$/;
const CODE_PATTERN = /^\d{6}$/;

/**
 * Creates the default auth form state for the modal.
 */
function createAuthForm(): AuthFormState {
  return {
    phone: '',
    code: '',
    nickname: ''
  };
}

const App = {
  data() {
    return {
      RouteNames,
      RoutePaths,
      isAuthModalVisible: false,
      routeDrivenAuth: false,
      authMode: 'login' as AuthMode,
      authForm: createAuthForm(),
      authToken: loadStoredToken(),
      currentUser: loadStoredUser() as AuthUserVO | null,
      authError: '',
      authMessage: '',
      devCodeHint: '',
      authLoading: false,
      smsSending: false,
      smsCooldown: 0,
      smsTimerId: 0,
      unsubscribeSessionChange: null as null | (() => void),
      searchKeyword: ''
    };
  },
  watch: {
    '$route.fullPath': {
      immediate: true,
      handler(this: any) {
        this.syncAuthModalWithRoute();
      }
    }
  },
  methods: {
    /**
     * Navigates to the search results page with the current keyword.
     */
    doSearch(this: any) {
      const kw = this.searchKeyword.trim();
      if (!kw) return;
      void router.push({ path: '/search', query: { keyword: kw } });
    },

    /**
     * Highlights the current navigation item.
     */
    isRouteActive(this: any, name: string) {
      const currentPath = router.currentRoute.value.path as string;

      if (name === RouteNames.HOME) {
        return currentPath === RoutePaths.HOME;
      }
      if (name === RouteNames.BOARDS) {
        return currentPath === RoutePaths.BOARDS || currentPath.startsWith('/boards/');
      }
      if (name === RouteNames.COMPOSE) {
        return currentPath === RoutePaths.COMPOSE;
      }
      if (name === RouteNames.PROFILE) {
        return currentPath === RoutePaths.PROFILE;
      }

      return routerHelper.isActiveRoute(router, name);
    },

    /**
     * Opens the login or registration modal manually.
     */
    openAuthModal(this: any, mode: AuthMode) {
      this.routeDrivenAuth = false;
      this.isAuthModalVisible = true;
      this.switchAuthMode(mode);
    },

    /**
     * Closes the auth modal and clears transient messages.
     */
    async closeAuthModal(this: any) {
      this.isAuthModalVisible = false;
      this.authError = '';
      this.authMessage = '';
      this.devCodeHint = '';
      this.resetSmsCooldown();

      if (this.routeDrivenAuth) {
        this.routeDrivenAuth = false;
        await this.clearRouteAuthIntent();
      }
    },

    /**
     * Switches the modal between login and registration.
     */
    switchAuthMode(this: any, mode: AuthMode) {
      this.authMode = mode;
      this.authForm.code = '';
      this.authForm.nickname = '';
      this.authError = '';
      this.authMessage = '';
      this.devCodeHint = '';
      this.resetSmsCooldown();
    },

    /**
     * Returns the correct label for the submit button.
     */
    getSubmitButtonText(this: any) {
      return this.authMode === 'login' ? '登录并进入社区' : '注册并加入社区';
    },

    /**
     * Returns the correct label for the verification button.
     */
    getSmsButtonText(this: any) {
      if (this.smsSending) {
        return '发送中...';
      }
      if (this.smsCooldown > 0) {
        return `${this.smsCooldown}s 后重试`;
      }
      return '获取验证码';
    },

    /**
     * Starts the countdown after a code is sent.
     */
    startSmsCooldown(this: any, seconds: number) {
      this.resetSmsCooldown();
      this.smsCooldown = seconds;
      this.smsTimerId = window.setInterval(() => {
        if (this.smsCooldown <= 1) {
          this.resetSmsCooldown();
          return;
        }
        this.smsCooldown -= 1;
      }, 1000);
    },

    /**
     * Clears the countdown timer.
     */
    resetSmsCooldown(this: any) {
      if (this.smsTimerId) {
        window.clearInterval(this.smsTimerId);
      }
      this.smsTimerId = 0;
      this.smsCooldown = 0;
    },

    /**
     * Sends a verification code for the current modal mode.
     */
    async sendSmsCode(this: any) {
      if (this.smsSending || this.smsCooldown > 0) {
        return;
      }

      const phone = this.authForm.phone.trim();
      if (!PHONE_PATTERN.test(phone)) {
        this.authError = '请输入正确的 11 位手机号。';
        return;
      }

      this.authError = '';
      this.authMessage = '';
      this.devCodeHint = '';
      this.smsSending = true;

      try {
        const data = await sendSmsCode(phone, this.authMode);
        this.startSmsCooldown(60);
        this.authMessage = this.authMode === 'login'
          ? '验证码已发送，请输入后完成登录。'
          : '验证码已发送，请输入后完成注册。';
        this.devCodeHint = data.debugMode && data.debugCode
          ? `本次验证码：${data.debugCode}`
          : '';
      } catch (error) {
        this.authError = error instanceof Error ? error.message : '验证码发送失败，请稍后重试。';
      } finally {
        this.smsSending = false;
      }
    },

    /**
     * Validates the auth modal input before it is submitted.
     */
    validateAuthForm(this: any) {
      const phone = this.authForm.phone.trim();
      const code = this.authForm.code.trim();

      if (!PHONE_PATTERN.test(phone)) {
        throw new Error('请输入正确的 11 位手机号。');
      }
      if (!CODE_PATTERN.test(code)) {
        throw new Error('请输入 6 位验证码。');
      }
      if (this.authMode === 'register' && this.authForm.nickname.trim().length > 20) {
        throw new Error('昵称长度请控制在 20 个字符以内。');
      }
    },

    /**
     * Saves the authenticated session and updates the shared navigation state.
     */
    applySession(this: any, session: AuthResultVO) {
      saveAuthSession(session);
      this.authToken = session.token;
      this.currentUser = session.user;
    },

    /**
     * Syncs the root component state from the session helper.
     */
    syncSessionState(this: any, snapshot?: SessionSnapshot) {
      const activeSnapshot = snapshot || {
        token: loadStoredToken(),
        user: loadStoredUser()
      };
      this.authToken = activeSnapshot.token;
      this.currentUser = activeSnapshot.user;
    },

    /**
     * Submits the current auth form to the corresponding backend endpoint.
     */
    async submitAuth(this: any) {
      try {
        this.validateAuthForm();
      } catch (error) {
        this.authError = error instanceof Error ? error.message : '请检查输入内容。';
        return;
      }

      this.authLoading = true;
      this.authError = '';
      this.authMessage = '';

      try {
        const session = this.authMode === 'login'
          ? await loginByPhone(this.authForm.phone.trim(), this.authForm.code.trim())
          : await registerByPhone(
              this.authForm.phone.trim(),
              this.authForm.code.trim(),
              this.authForm.nickname.trim()
            );

        this.applySession(session);
        this.authMessage = this.authMode === 'login' ? '登录成功，欢迎回来。' : '注册成功，欢迎加入社区。';
        this.authForm = createAuthForm();
        this.resetSmsCooldown();
        this.isAuthModalVisible = false;

        const redirectPath = this.getRouteRedirectPath();
        this.routeDrivenAuth = false;

        if (redirectPath) {
          await router.push(redirectPath);
        } else {
          await this.clearRouteAuthIntent();
        }
      } catch (error) {
        this.authError = error instanceof Error ? error.message : '提交失败，请稍后重试。';
      } finally {
        this.authLoading = false;
      }
    },

    /**
     * Restores the current session from the backend when the page loads.
     */
    async restoreSession(this: any) {
      if (!this.authToken) {
        return;
      }

      try {
        const currentUser = await fetchCurrentAuthUser(this.authToken);
        updateStoredUser(currentUser);
        this.syncSessionState({
          token: this.authToken,
          user: currentUser
        });
      } catch (error) {
        if (error instanceof ApiError && error.code === 401) {
          await this.handleUnauthorizedSession();
          return;
        }
        this.authError = error instanceof Error ? error.message : '';
      }
    },

    /**
     * Logs the current user out and clears the local session state.
     */
    async logout(this: any) {
      const token = this.authToken;

      try {
        if (token) {
          await logoutCurrentSession(token);
        }
      } catch {
        // The local session is still cleared even if the backend call fails.
      } finally {
        clearStoredSession();
        this.syncSessionState({
          token: '',
          user: null
        });
        this.authError = '';
        this.authMessage = '';
        this.devCodeHint = '';
        this.isAuthModalVisible = false;
        this.routeDrivenAuth = false;
        this.authForm = createAuthForm();
        this.resetSmsCooldown();

        if (router.currentRoute.value.meta?.requiresAuth) {
          await router.push(RoutePaths.HOME);
        } else {
          await this.clearRouteAuthIntent();
        }
      }
    },

    /**
     * Handles invalid or expired sessions and redirects protected pages back to the login modal.
     */
    async handleUnauthorizedSession(this: any) {
      clearStoredSession();
      this.syncSessionState({
        token: '',
        user: null
      });
      this.isAuthModalVisible = false;
      this.routeDrivenAuth = false;
      this.authForm = createAuthForm();
      this.resetSmsCooldown();

      const currentRoute = router.currentRoute.value;
      if (currentRoute.meta?.requiresAuth) {
        await router.push({
          path: RoutePaths.HOME,
          query: {
            auth: 'login',
            redirect: currentRoute.fullPath
          }
        });
        return;
      }

      await this.clearRouteAuthIntent();
    },

    /**
     * Responds to route-level login intents created by the auth guard.
     */
    syncAuthModalWithRoute(this: any) {
      const routeMode = this.getRouteAuthMode();
      if (!routeMode) {
        return;
      }

      if (this.currentUser) {
        const redirectPath = this.getRouteRedirectPath();
        if (redirectPath) {
          void router.replace(redirectPath);
          return;
        }
        void this.clearRouteAuthIntent();
        return;
      }

      this.routeDrivenAuth = true;
      this.isAuthModalVisible = true;

      if (this.authMode !== routeMode) {
        this.switchAuthMode(routeMode);
      }
    },

    /**
     * Removes route query params that were only used to trigger the login modal.
     */
    async clearRouteAuthIntent(this: any) {
      const route = router.currentRoute.value;
      if (!('auth' in route.query) && !('redirect' in route.query)) {
        return;
      }

      const nextQuery = { ...route.query };
      delete nextQuery.auth;
      delete nextQuery.redirect;
      await router.replace({
        path: route.path,
        query: nextQuery
      });
    },

    /**
     * Returns the route-level auth mode requested by the router guard.
     */
    getRouteAuthMode(this: any): AuthMode | '' {
      const rawMode = router.currentRoute.value.query.auth;
      if (rawMode === 'login' || rawMode === 'register') {
        return rawMode;
      }
      return '';
    },

    /**
     * Returns the requested redirect path after successful authentication.
     */
    getRouteRedirectPath(this: any) {
      const rawRedirect = router.currentRoute.value.query.redirect;
      if (typeof rawRedirect !== 'string' || !rawRedirect.startsWith('/')) {
        return '';
      }
      return rawRedirect;
    },

    /**
     * Resolves relative avatar paths returned by the backend.
     */
    resolveAvatarUrl(this: any, path: string | null | undefined) {
      return resolveAssetUrl(path);
    },

    /**
     * Returns the leading character used by the nav avatar chip.
     */
    getUserInitial(this: any) {
      const nickname = this.currentUser?.nickname || 'K';
      return nickname.slice(0, 1).toUpperCase();
    }
  },
  async mounted(this: any) {
    this.unsubscribeSessionChange = subscribeSessionChange((snapshot) => {
      this.syncSessionState(snapshot);
      this.syncAuthModalWithRoute();
    });
    await this.restoreSession();
    this.syncAuthModalWithRoute();
  },
  beforeUnmount(this: any) {
    this.resetSmsCooldown();
    this.unsubscribeSessionChange?.();
  },
  template: `
    <div class="site-bg"></div>
    <header class="site-header">
      <nav class="site-nav">
        <div class="site-nav__brand">
          <router-link :to="RoutePaths.HOME" class="brand-mark">
            <span class="brand-mark__icon">
              <img src="${brandLogo}" alt="Kite Universe logo" />
            </span>
            <span>
              <strong>Kite Universe</strong>
              <small>社区入口</small>
            </span>
          </router-link>
          <div class="site-nav__links">
            <router-link
              :to="RoutePaths.HOME"
              :class="{ active: isRouteActive(RouteNames.HOME) }"
            >
              首页
            </router-link>
            <router-link
              :to="RoutePaths.BOARDS"
              :class="{ active: isRouteActive(RouteNames.BOARDS) }"
            >
              版区
            </router-link>
            <router-link
              v-if="currentUser"
              :to="RoutePaths.PROFILE"
              :class="{ active: isRouteActive(RouteNames.PROFILE) }"
            >
              个人中心
            </router-link>
            <router-link
              :to="RoutePaths.BOARDS"
              :class="{ active: isRouteActive(RouteNames.BOARDS) }"
            >
              热帖
            </router-link>
            <router-link
              :to="RoutePaths.COMPOSE"
              :class="{ active: isRouteActive(RouteNames.COMPOSE) }"
            >
              发布
            </router-link>
          </div>
        </div>

        <div class="site-nav__tools">
          <label class="search-box">
            <span>搜索内容</span>
            <input
              type="text"
              placeholder="查找帖子、攻略、创作或社区公告"
              v-model="searchKeyword"
              @keydown.enter="doSearch"
            />
          </label>
          <div v-if="currentUser" class="nav-user">
            <router-link :to="RoutePaths.PROFILE" class="nav-user__summary">
              <div class="nav-user__avatar">
                <img
                  v-if="currentUser.avatar"
                  :src="resolveAvatarUrl(currentUser.avatar)"
                  :alt="currentUser.nickname + ' avatar'"
                />
                <span v-else>{{ getUserInitial() }}</span>
              </div>
              <div class="nav-user__content">
                <strong>{{ currentUser.nickname }}</strong>
                <small>{{ currentUser.phone }}</small>
              </div>
            </router-link>
            <button class="button button--ghost button--small" @click="logout">退出登录</button>
          </div>
          <div v-else class="site-nav__actions">
            <button class="button button--ghost" @click="openAuthModal('login')">登录</button>
            <button class="button button--primary" @click="openAuthModal('register')">立即加入</button>
          </div>
        </div>
      </nav>
    </header>

    <router-view></router-view>

    <div v-if="isAuthModalVisible" class="auth-modal-mask" @click.self="closeAuthModal">
      <section class="auth-modal">
        <div class="auth-modal__visual">
          <span class="eyebrow">手机验证</span>
          <h2>用手机号验证码快速进入社区。</h2>
          <p>
            登录后就能继续浏览常逛版区、参与讨论、更新个人主页，
            也可以随时发布自己的整理、感想或创作内容。
          </p>
          <div class="auth-visual__cards">
            <article class="auth-visual__card">
              <span>进入之后</span>
              <strong>社区主页</strong>
              <p>首页推荐、常逛版区和个人资料都会跟着你的登录状态一起同步。</p>
            </article>
            <article class="auth-visual__card">
              <span>当前方式</span>
              <strong>{{ authMode === 'login' ? '手机号登录' : '手机号注册' }}</strong>
              <p>{{ authMode === 'login' ? '输入验证码后就能继续上次没逛完的话题。' : '完成验证后会自动创建主页，昵称稍后也能修改。' }}</p>
            </article>
          </div>
        </div>

        <div class="auth-modal__panel">
          <button class="auth-modal__close" type="button" @click="closeAuthModal" aria-label="关闭">×</button>
          <div class="auth-tabs">
            <button
              type="button"
              class="auth-tab"
              :class="{ active: authMode === 'login' }"
              @click="switchAuthMode('login')"
            >
              手机号登录
            </button>
            <button
              type="button"
              class="auth-tab"
              :class="{ active: authMode === 'register' }"
              @click="switchAuthMode('register')"
            >
              手机号注册
            </button>
          </div>

          <div class="auth-panel__head">
            <h3>{{ authMode === 'login' ? '欢迎回来' : '创建账号' }}</h3>
            <p>{{ authMode === 'login' ? '输入手机号和验证码即可登录。' : '完成验证码校验后会直接创建账号并进入社区。' }}</p>
          </div>

          <form class="auth-form" @submit.prevent="submitAuth">
            <label class="auth-field">
              <span>手机号</span>
              <input
                v-model.trim="authForm.phone"
                type="text"
                inputmode="numeric"
                maxlength="11"
                placeholder="请输入 11 位手机号"
              />
            </label>

            <label v-if="authMode === 'register'" class="auth-field">
              <span>社区昵称</span>
              <input
                v-model.trim="authForm.nickname"
                type="text"
                maxlength="20"
                placeholder="选填，留空会自动生成昵称"
              />
            </label>

            <div class="auth-code-row">
              <label class="auth-field auth-field--grow">
                <span>验证码</span>
                <input
                  v-model.trim="authForm.code"
                  type="text"
                  inputmode="numeric"
                  maxlength="6"
                  placeholder="请输入 6 位验证码"
                />
              </label>
              <button
                type="button"
                class="button button--ghost auth-code-row__button"
                :disabled="smsSending || smsCooldown > 0"
                @click="sendSmsCode"
              >
                {{ getSmsButtonText() }}
              </button>
            </div>

            <div class="auth-tips">
              <p>验证码 5 分钟内有效，每次提交只校验最近一条。</p>
              <p v-if="devCodeHint" class="auth-tips__debug">{{ devCodeHint }}</p>
            </div>

            <p v-if="authError" class="auth-feedback auth-feedback--error">{{ authError }}</p>
            <p v-if="!authError && authMessage" class="auth-feedback auth-feedback--success">{{ authMessage }}</p>

            <button class="button button--primary auth-submit" type="submit" :disabled="authLoading">
              {{ authLoading ? '提交中...' : getSubmitButtonText() }}
            </button>
          </form>
        </div>
      </section>
    </div>

    <footer class="site-footer">
      <div class="site-footer__inner">
        <div>
          <strong>Kite Universe</strong>
          <p>面向游戏社区的内容聚合首页，聚焦情报、攻略、创作与玩家交流。</p>
        </div>
        <div class="site-footer__links">
          <a href="#">关于我们</a>
          <a href="#">社区公约</a>
          <a href="#">隐私政策</a>
          <a href="#">帮助中心</a>
        </div>
      </div>
    </footer>
  `
};

createApp(App).use(router).mount('#app');
