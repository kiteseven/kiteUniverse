/**
 * Shared frontend API helpers for authentication, profile, and community pages.
 */

export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export interface SmsCodeSendVO {
  phone: string;
  scene: string;
  expireSeconds: number;
  debugMode: boolean;
  debugCode: string | null;
}

export interface AuthUserVO {
  id: number;
  username: string;
  nickname: string;
  phone: string;
  avatar: string | null;
  status: number;
}

export interface AuthResultVO {
  token: string;
  tokenType: string;
  expiresIn: number;
  expiresAt: string;
  user: AuthUserVO;
}

export interface UserDetailVO {
  id: number;
  username: string;
  nickname: string | null;
  email: string | null;
  phone: string | null;
  avatar: string | null;
  gender: number | null;
  status: number | null;
  lastLoginTime: string | null;
  realName: string | null;
  birthday: string | null;
  signature: string | null;
  profile: string | null;
  country: string | null;
  province: string | null;
  city: string | null;
  website: string | null;
  backgroundImage: string | null;
  followerCount: number | null;
  followingCount: number | null;
  createTime: string | null;
  updateTime: string | null;
}

export interface UserInfoUpdatePayload {
  nickname: string | null;
  gender: number | null;
  realName: string | null;
  birthday: string | null;
  signature: string | null;
  profile: string | null;
  country: string | null;
  province: string | null;
  city: string | null;
  website: string | null;
  backgroundImage: string | null;
}

/**
 * Generic metric card used by the homepage and boards page.
 */
export interface ContentMetricCard {
  title: string;
  value: string;
  description: string;
}

/**
 * Featured topic card displayed on the homepage.
 */
export interface HomeTopicCard {
  badge: string;
  title: string;
  excerpt: string;
  author: string;
  meta: string;
  stats: string[];
  link: string;
}

/**
 * Small quick-link item displayed as a pill on the homepage.
 */
export interface HomeQuickLink {
  label: string;
  link: string;
}

/**
 * Group of quick links displayed on the homepage sidebar.
 */
export interface HomeQuickSection {
  title: string;
  items: HomeQuickLink[];
}

/**
 * Community bulletin item displayed on the homepage timeline.
 */
export interface HomeTimelineItem {
  time: string;
  title: string;
  description: string;
  link: string;
}

/**
 * Hero payload displayed at the top of the homepage.
 */
export interface HomeHeroContent {
  eyebrow: string;
  title: string;
  description: string;
  primaryActionLabel: string;
  primaryActionLink: string;
  secondaryActionLabel: string;
  secondaryActionLink: string;
  visualAlt: string;
  floatingTitle: string;
  floatingValue: string;
  floatingDescription: string;
  secondaryFloatingTitle: string;
  secondaryFloatingValue: string;
  secondaryFloatingDescription: string;
}

/**
 * Homepage payload returned by the backend.
 */
export interface HomePageData {
  hero: HomeHeroContent;
  heroMetrics: ContentMetricCard[];
  featuredTopics: HomeTopicCard[];
  quickSections: HomeQuickSection[];
  timeline: HomeTimelineItem[];
  moments: ContentMetricCard[];
}

/**
 * Boards hero payload returned by the backend.
 */
export interface BoardsHeroContent {
  eyebrow: string;
  title: string;
  description: string;
  metrics: ContentMetricCard[];
}

/**
 * Board card payload displayed on the boards page.
 */
export interface BoardGroup {
  tag: string;
  title: string;
  description: string;
  stats: string[];
  update: string;
  actionLabel: string;
  actionLink: string;
}

/**
 * Boards-page payload returned by the backend.
 */
export interface BoardsPageData {
  hero: BoardsHeroContent;
  boardGroups: BoardGroup[];
  notices: string[];
  overviewCards: ContentMetricCard[];
}

/**
 * Board summary payload returned by the board APIs.
 */
export interface BoardSummaryData {
  id: number;
  name: string;
  slug: string;
  tagName: string;
  description: string;
  topicCount: number;
  todayPostCount: number;
  latestPostTitle: string;
  latestPublishedAt: string | null;
}

/**
 * Post summary payload returned by list endpoints.
 */
export interface PostSummaryData {
  id: number;
  boardId: number;
  boardName: string;
  boardSlug: string;
  boardTagName: string;
  authorId: number | null;
  authorName: string;
  badge: string;
  title: string;
  summary: string;
  viewCount: number;
  commentCount: number;
  favoriteCount: number;
  likeCount: number;
  publishedAt: string | null;
}

/**
 * Post-detail payload returned by the post-detail API.
 */
export interface PostDetailData extends PostSummaryData {
  authorAvatar: string | null;
  content: string;
  createTime: string | null;
  updateTime: string | null;
}

/**
 * Comment payload returned by the post-comments API.
 */
export interface PostCommentData {
  id: number;
  postId: number;
  authorId: number | null;
  authorName: string;
  authorAvatar: string | null;
  content: string;
  likeCount: number;
  liked: boolean | null;
  createTime: string | null;
}

/**
 * Payload used when creating a new post.
 */
export interface PostCreatePayload {
  boardId: number;
  title: string;
  summary: string;
  content: string;
  badge: string | null;
}

/**
 * Payload used when creating a new post comment.
 */
export interface PostCommentCreatePayload {
  content: string;
}

/**
 * Current user's favorite state for a specific post.
 */
export interface PostFavoriteStateData {
  postId: number;
  favorited: boolean;
  favoriteCount: number;
}

/**
 * Error object thrown when the backend returns a failed business result.
 */
export class ApiError extends Error {
  code: number;
  status: number;

  constructor(message: string, code = 500, status = 500) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.status = status;
  }
}

export const API_BASE_URL = String(import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8081');

/**
 * Wraps backend requests and normalizes the shared response envelope.
 */
export async function requestApi<T>(path: string, options: RequestInit = {}, token = ''): Promise<T> {
  const headers = new Headers(options.headers);
  const isFormDataBody = typeof FormData !== 'undefined' && options.body instanceof FormData;

  if (options.body && !headers.has('Content-Type') && !isFormDataBody) {
    headers.set('Content-Type', 'application/json');
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers
    });
  } catch {
    throw new ApiError('暂时无法连接社区服务，请稍后重试。', 503, 503);
  }

  const responseText = await response.text();
  if (!responseText) {
    throw new ApiError(`服务没有返回内容，HTTP 状态码 ${response.status}。`, response.status, response.status);
  }

  let payload: ApiResult<T>;
  try {
    payload = JSON.parse(responseText) as ApiResult<T>;
  } catch {
    throw new ApiError('服务返回了无法解析的数据。', response.status, response.status);
  }

  if (!response.ok || payload.code !== 200) {
    throw new ApiError(payload.message || '请求失败，请稍后重试。', payload.code || response.status, response.status);
  }

  return payload.data;
}

/**
 * Sends a phone verification code for the specified business scene.
 */
export function sendSmsCode(phone: string, scene: 'login' | 'register') {
  return requestApi<SmsCodeSendVO>('/api/auth/sms-code', {
    method: 'POST',
    body: JSON.stringify({ phone, scene })
  });
}

/**
 * Logs in an existing user with phone and verification code.
 */
export function loginByPhone(phone: string, code: string) {
  return requestApi<AuthResultVO>('/api/auth/phone/login', {
    method: 'POST',
    body: JSON.stringify({ phone, code })
  });
}

/**
 * Registers a new user with phone and verification code.
 */
export function registerByPhone(phone: string, code: string, nickname: string) {
  return requestApi<AuthResultVO>('/api/auth/phone/register', {
    method: 'POST',
    body: JSON.stringify({ phone, code, nickname })
  });
}

/**
 * Loads the current logged-in user summary from the backend.
 */
export function fetchCurrentAuthUser(token: string) {
  return requestApi<AuthUserVO>('/api/auth/me', { method: 'GET' }, token);
}

/**
 * Notifies the backend that the current frontend session is logging out.
 */
export function logoutCurrentSession(token: string) {
  return requestApi<null>('/api/auth/logout', { method: 'POST' }, token);
}

/**
 * Loads the full current-user profile for the personal center.
 */
export function fetchCurrentUserProfile(token: string) {
  return requestApi<UserDetailVO>('/api/users/me', { method: 'GET' }, token);
}

/**
 * Updates the current-user profile and returns the latest detail data.
 */
export function updateCurrentUserProfile(token: string, payload: UserInfoUpdatePayload) {
  return requestApi<UserDetailVO>('/api/users/me/info', {
    method: 'PUT',
    body: JSON.stringify(payload)
  }, token);
}

/**
 * Uploads the current-user avatar and returns the latest detail data.
 */
export function uploadCurrentUserAvatar(token: string, file: File) {
  const formData = new FormData();
  formData.append('file', file);

  return requestApi<UserDetailVO>('/api/users/me/avatar', {
    method: 'POST',
    body: formData
  }, token);
}

/**
 * Loads the backend-driven homepage payload.
 */
export function fetchHomePageData() {
  return requestApi<HomePageData>('/api/content/home', { method: 'GET' });
}

/**
 * Loads the backend-driven boards-page payload.
 */
export function fetchBoardsPageData() {
  return requestApi<BoardsPageData>('/api/content/boards', { method: 'GET' });
}

/**
 * Loads the active board list.
 */
export function fetchBoardList() {
  return requestApi<BoardSummaryData[]>('/api/boards', { method: 'GET' });
}

/**
 * Loads the detail summary for a specific board.
 */
export function fetchBoardDetail(boardId: number | string) {
  return requestApi<BoardSummaryData>(`/api/boards/${boardId}`, { method: 'GET' });
}

/**
 * Loads the latest posts under a specific board.
 */
export function fetchPostsByBoard(boardId: number | string, limit = 10) {
  return requestApi<PostSummaryData[]>(`/api/posts/board/${boardId}?limit=${limit}`, { method: 'GET' });
}

/**
 * Loads a specific post detail payload.
 */
export function fetchPostDetail(postId: number | string) {
  return requestApi<PostDetailData>(`/api/posts/${postId}`, { method: 'GET' });
}

/**
 * Loads the editable post detail for the current author without adding a view.
 */
export function fetchManagePostDetail(token: string, postId: number | string) {
  return requestApi<PostDetailData>(`/api/posts/${postId}/manage`, { method: 'GET' }, token);
}

/**
 * Loads the comments under a specific post (optionally with liked state when token is provided).
 */
export function fetchPostComments(postId: number | string, token = '') {
  return requestApi<PostCommentData[]>(`/api/posts/${postId}/comments`, { method: 'GET' }, token);
}

/**
 * Creates a new community post.
 */
export function createCommunityPost(token: string, payload: PostCreatePayload) {
  return requestApi<PostDetailData>('/api/posts', {
    method: 'POST',
    body: JSON.stringify(payload)
  }, token);
}

/**
 * Updates an existing community post owned by the current user.
 */
export function updateCommunityPost(token: string, postId: number | string, payload: PostCreatePayload) {
  return requestApi<PostDetailData>(`/api/posts/${postId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  }, token);
}

/**
 * Deletes an existing community post owned by the current user.
 */
export function deleteCommunityPost(token: string, postId: number | string) {
  return requestApi<null>(`/api/posts/${postId}`, {
    method: 'DELETE'
  }, token);
}

/**
 * Creates a new comment under a post.
 */
export function createCommunityPostComment(token: string, postId: number | string, payload: PostCommentCreatePayload) {
  return requestApi<PostCommentData>(`/api/posts/${postId}/comments`, {
    method: 'POST',
    body: JSON.stringify(payload)
  }, token);
}

/**
 * Loads the current user's favorite state for a post.
 */
export function fetchPostFavoriteState(token: string, postId: number | string) {
  return requestApi<PostFavoriteStateData>(`/api/posts/${postId}/favorite-state`, { method: 'GET' }, token);
}

/**
 * Favorites a post for the current user.
 */
export function favoriteCommunityPost(token: string, postId: number | string) {
  return requestApi<PostFavoriteStateData>(`/api/posts/${postId}/favorite`, {
    method: 'POST'
  }, token);
}

/**
 * Removes a post from the current user's favorites.
 */
export function unfavoriteCommunityPost(token: string, postId: number | string) {
  return requestApi<PostFavoriteStateData>(`/api/posts/${postId}/favorite`, {
    method: 'DELETE'
  }, token);
}

/**
 * Loads the current user's own post list.
 */
export function fetchCurrentUserPosts(token: string, limit = 10) {
  return requestApi<PostSummaryData[]>(`/api/posts/mine?limit=${limit}`, { method: 'GET' }, token);
}

/**
 * Loads the current user's favorite post list.
 */
export function fetchCurrentUserFavoritePosts(token: string, limit = 10) {
  return requestApi<PostSummaryData[]>(`/api/posts/mine/favorites?limit=${limit}`, { method: 'GET' }, token);
}

/**
 * Searches posts by keyword (matches title, summary, badge).
 */
export function searchPosts(keyword: string, limit = 20) {
  const params = new URLSearchParams({ keyword, limit: String(limit) });
  return requestApi<PostSummaryData[]>(`/api/posts/search?${params}`, { method: 'GET' });
}

/**
 * Current user's like state for a specific post.
 */
export interface PostLikeStateData {
  postId: number;
  liked: boolean;
  likeCount: number;
}

/**
 * Current user's like state for a specific comment.
 */
export interface CommentLikeStateData {
  commentId: number;
  liked: boolean;
  likeCount: number;
}

/**
 * Current user's follow state for a specific user.
 */
export interface UserFollowStateData {
  userId: number;
  followed: boolean;
  followerCount: number;
  followingCount: number;
}

/**
 * Item displayed in follower/following lists.
 */
export interface UserFollowItemData {
  id: number;
  username: string;
  nickname: string;
  avatar: string | null;
  signature: string | null;
}

/**
 * Loads a public user profile by ID.
 */
export function fetchUserDetail(userId: number | string) {
  return requestApi<UserDetailVO>(`/api/users/${userId}`, { method: 'GET' });
}

/**
 * Loads the current user's like state for a post.
 */
export function fetchPostLikeState(token: string, postId: number | string) {
  return requestApi<PostLikeStateData>(`/api/posts/${postId}/like-state`, { method: 'GET' }, token);
}

/**
 * Likes a post for the current user.
 */
export function likeCommunityPost(token: string, postId: number | string) {
  return requestApi<PostLikeStateData>(`/api/posts/${postId}/like`, { method: 'POST' }, token);
}

/**
 * Removes a post like for the current user.
 */
export function unlikeCommunityPost(token: string, postId: number | string) {
  return requestApi<PostLikeStateData>(`/api/posts/${postId}/like`, { method: 'DELETE' }, token);
}

/**
 * Loads the current user's like state for a specific comment.
 */
export function fetchCommentLikeState(token: string, postId: number | string, commentId: number | string) {
  return requestApi<CommentLikeStateData>(`/api/posts/${postId}/comments/${commentId}/like-state`, { method: 'GET' }, token);
}

/**
 * Likes a comment for the current user.
 */
export function likeCommunityComment(token: string, postId: number | string, commentId: number | string) {
  return requestApi<CommentLikeStateData>(`/api/posts/${postId}/comments/${commentId}/like`, { method: 'POST' }, token);
}

/**
 * Removes a comment like for the current user.
 */
export function unlikeCommunityComment(token: string, postId: number | string, commentId: number | string) {
  return requestApi<CommentLikeStateData>(`/api/posts/${postId}/comments/${commentId}/like`, { method: 'DELETE' }, token);
}

/**
 * Loads the current user's follow state for a target user.
 */
export function fetchFollowState(token: string, userId: number | string) {
  return requestApi<UserFollowStateData>(`/api/users/${userId}/follow-state`, { method: 'GET' }, token);
}

/**
 * Follows a user.
 */
export function followUser(token: string, userId: number | string) {
  return requestApi<UserFollowStateData>(`/api/users/${userId}/follow`, { method: 'POST' }, token);
}

/**
 * Unfollows a user.
 */
export function unfollowUser(token: string, userId: number | string) {
  return requestApi<UserFollowStateData>(`/api/users/${userId}/follow`, { method: 'DELETE' }, token);
}

/**
 * Loads the followers of a user.
 */
export function fetchFollowers(userId: number | string, limit = 20) {
  return requestApi<UserFollowItemData[]>(`/api/users/${userId}/followers?limit=${limit}`, { method: 'GET' });
}

/**
 * Loads the users that a user is following.
 */
export function fetchFollowing(userId: number | string, limit = 20) {
  return requestApi<UserFollowItemData[]>(`/api/users/${userId}/following?limit=${limit}`, { method: 'GET' });
}

/**
 * Loads the public posts of a user by ID.
 */
export function fetchUserPosts(userId: number | string, limit = 10) {
  return requestApi<PostSummaryData[]>(`/api/posts/user/${userId}?limit=${limit}`, { method: 'GET' });
}

/**
 * Resolves backend-relative image paths to a browser-safe absolute URL.
 */
export function resolveAssetUrl(path: string | null | undefined) {
  if (!path) {
    return '';
  }
  if (/^(https?:|data:|blob:)/i.test(path)) {
    return path;
  }
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`;
}
