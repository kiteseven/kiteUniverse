import { HomePage } from '../pages/index';
import { BoardsPage } from '../pages/boards';
import { ProfilePage } from '../pages/profile';
import { BoardDetailPage } from '../pages/board-detail';
import { PostDetailPage } from '../pages/post-detail';
import { PostComposePage } from '../pages/post-compose';
import { SearchPage } from '../pages/search';
import { UserProfilePage } from '../pages/user-profile';
import { NotificationsPage } from '../pages/notifications';
import { DiscoverPage } from '../pages/discover';
import { TopicsPage } from '../pages/topics';
import { GameToolsPage } from '../pages/game-tools';
import { WikiPage } from '../pages/wiki';
import { StrategiesPage } from '../pages/strategies';
import { CheckInPage } from '../pages/checkin';
import { MessagesPage } from '../pages/messages';
import { AdminPage } from '../pages/admin';
import { hasStoredToken } from '../services/session';

export const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomePage,
    meta: {
      title: '首页',
      icon: 'home'
    }
  },
  {
    path: '/boards',
    name: 'Boards',
    component: BoardsPage,
    meta: {
      title: '版区',
      icon: 'grid'
    }
  },
  {
    path: '/boards/:boardId',
    name: 'BoardDetail',
    component: BoardDetailPage,
    meta: {
      title: '版区详情',
      icon: 'layers'
    }
  },
  {
    path: '/posts/:postId',
    name: 'PostDetail',
    component: PostDetailPage,
    meta: {
      title: '帖子详情',
      icon: 'file'
    }
  },
  {
    path: '/compose',
    name: 'Compose',
    component: PostComposePage,
    meta: {
      title: '发布帖子',
      icon: 'edit',
      requiresAuth: true
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: ProfilePage,
    meta: {
      title: '个人中心',
      icon: 'user',
      requiresAuth: true
    }
  },
  {
    path: '/users/:userId',
    name: 'UserProfile',
    component: UserProfilePage,
    meta: {
      title: '用户主页',
      icon: 'user'
    }
  },
  {
    path: '/search',
    name: 'Search',
    component: SearchPage,
    meta: {
      title: '搜索结果',
      icon: 'search'
    }
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: NotificationsPage,
    meta: {
      title: '消息中心',
      icon: 'bell',
      requiresAuth: true
    }
  },
  {
    path: '/discover',
    name: 'Discover',
    component: DiscoverPage,
    meta: {
      title: '发现',
      icon: 'compass'
    }
  },
  {
    path: '/topics/:badge',
    name: 'Topics',
    component: TopicsPage,
    meta: {
      title: '话题',
      icon: 'tag'
    }
  },
  {
    path: '/game-tools',
    name: 'GameTools',
    component: GameToolsPage,
    meta: {
      title: '游戏工具',
      icon: 'gamepad',
      requiresAuth: true
    }
  },
  {
    path: '/wiki',
    name: 'Wiki',
    component: WikiPage,
    meta: {
      title: '玩家百科',
      icon: 'book'
    }
  },
  {
    path: '/strategies',
    name: 'Strategies',
    component: StrategiesPage,
    meta: {
      title: '攻略合集',
      icon: 'map'
    }
  },
  {
    path: '/checkin',
    name: 'CheckIn',
    component: CheckInPage,
    meta: {
      title: '每日签到',
      icon: 'calendar',
      requiresAuth: true
    }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: MessagesPage,
    meta: {
      title: '私信',
      icon: 'message',
      requiresAuth: true
    }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: AdminPage,
    meta: {
      title: '管理后台',
      icon: 'shield',
      requiresAuth: true
    }
  }
];

export const RouteNames = {
  HOME: 'Home',
  BOARDS: 'Boards',
  BOARD_DETAIL: 'BoardDetail',
  POST_DETAIL: 'PostDetail',
  COMPOSE: 'Compose',
  PROFILE: 'Profile',
  USER_PROFILE: 'UserProfile',
  SEARCH: 'Search'
} as const;

export const RoutePaths = {
  HOME: '/',
  BOARDS: '/boards',
  COMPOSE: '/compose',
  PROFILE: '/profile'
} as const;

/**
 * Creates the Vue Router instance and wires a simple login guard for protected pages.
 */
export function createRouterInstance(VueRouter: any) {
  const { createRouter, createWebHashHistory } = VueRouter;

  const router = createRouter({
    history: createWebHashHistory(),
    routes
  });

  router.beforeEach((to: any) => {
    if (!to.meta?.requiresAuth) {
      return true;
    }

    if (hasStoredToken()) {
      return true;
    }

    return {
      path: RoutePaths.HOME,
      query: {
        auth: 'login',
        redirect: to.fullPath
      }
    };
  });

  router.afterEach((to: any) => {
    const title = to.meta?.title ? `Kite Universe - ${String(to.meta.title)}` : 'Kite Universe';
    document.title = title;
  });

  return router;
}

export const routerHelper = {
  pushByName(router: any, name: string, params?: any) {
    return router.push({ name, params });
  },

  pushByPath(router: any, path: string) {
    return router.push(path);
  },

  getCurrentRoute(router: any) {
    return router.currentRoute.value;
  },

  isActiveRoute(router: any, name: string) {
    return router.currentRoute.value.name === name;
  }
};

export default {
  routes,
  RouteNames,
  RoutePaths,
  createRouterInstance,
  routerHelper
};
