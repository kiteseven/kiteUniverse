import {
  getConversations,
  getChatMessages,
  sendPrivateMessage,
  markMessagesRead,
  resolveAssetUrl,
  type ConversationData,
  type MessageData
} from '../services/api';
import { loadStoredToken, loadStoredUser } from '../services/session';
import { wsService } from '../services/ws';

function timeAgo(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return '刚刚';
  if (mins < 60) return `${mins} 分钟前`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours} 小时前`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days} 天前`;
  return new Date(dateStr).toLocaleDateString('zh-CN');
}

export const MessagesPage = {
  data() {
    return {
      token: '' as string,
      currentUserId: 0 as number,
      conversations: [] as ConversationData[],
      convLoading: true,
      convError: '' as string,

      activeConv: null as ConversationData | null,
      messages: [] as MessageData[],
      msgLoading: false,
      msgError: '' as string,

      inputText: '' as string,
      sending: false,

      _wsUnsub: null as (() => void) | null
    };
  },

  async created(this: any) {
    this.token = loadStoredToken() || '';
    const user = loadStoredUser();
    this.currentUserId = user?.id ?? 0;
    if (!this.token) { this.convLoading = false; return; }
    await this.loadConversations();

    // Subscribe to incoming messages via WebSocket (shared connection from App)
    this._wsUnsub = wsService.onNewMessage((msg: Record<string, unknown>) => {
      this.handleIncomingMessage(msg);
    });

    // If ?userId= in URL, open that conversation
    const params = new URLSearchParams(window.location.hash.split('?')[1] || '');
    const uid = params.get('userId');
    if (uid) {
      const otherId = parseInt(uid, 10);
      const existing = this.conversations.find((c: ConversationData) => c.otherUserId === otherId);
      if (existing) {
        this.openConversation(existing);
      } else {
        const synth: ConversationData = {
          otherUserId: otherId,
          otherUserName: `用户 ${otherId}`,
          otherUserAvatar: null,
          lastMessageContent: '',
          lastMessageByMe: false,
          unreadCount: 0,
          lastMessageTime: ''
        };
        this.openConversation(synth);
      }
    }
  },

  unmounted(this: any) {
    if (this._wsUnsub) {
      this._wsUnsub();
      this._wsUnsub = null;
    }
  },

  methods: {
    async loadConversations(this: any) {
      this.convLoading = true;
      this.convError = '';
      try {
        this.conversations = await getConversations(this.token);
      } catch (e: any) {
        this.convError = e?.message || '加载失败';
      } finally {
        this.convLoading = false;
      }
    },

    async openConversation(this: any, conv: ConversationData) {
      this.activeConv = conv;
      this.messages = [];
      this.msgError = '';
      this.msgLoading = true;
      try {
        this.messages = await getChatMessages(this.token, conv.otherUserId, 50, 0);
        await markMessagesRead(this.token, conv.otherUserId);
        conv.unreadCount = 0;
        this.scrollToBottom();
      } catch (e: any) {
        this.msgError = e?.message || '加载消息失败';
      } finally {
        this.msgLoading = false;
      }
    },

    /**
     * Handles a new message pushed via WebSocket from the server.
     * If the message is in the active conversation, appends it and marks it read.
     * Otherwise, increments the unread badge on the relevant conversation.
     */
    handleIncomingMessage(this: any, msg: Record<string, unknown>) {
      const senderId = msg.senderId as number;
      const recipientId = msg.recipientId as number;

      // Only handle messages addressed to this user
      if (recipientId !== this.currentUserId) return;

      // Update or create conversation entry
      const existing = this.conversations.find((c: ConversationData) => c.otherUserId === senderId);
      if (existing) {
        existing.lastMessageContent = (msg.content as string) || '';
        existing.lastMessageByMe = false;
        existing.lastMessageTime = (msg.createTime as string) || '';
        // Move to top
        const idx = this.conversations.indexOf(existing);
        if (idx > 0) {
          this.conversations.splice(idx, 1);
          this.conversations.unshift(existing);
        }
      } else {
        const newConv: ConversationData = {
          otherUserId: senderId,
          otherUserName: (msg.senderName as string) || `用户 ${senderId}`,
          otherUserAvatar: (msg.senderAvatar as string | null) || null,
          lastMessageContent: (msg.content as string) || '',
          lastMessageByMe: false,
          unreadCount: 1,
          lastMessageTime: (msg.createTime as string) || ''
        };
        this.conversations.unshift(newConv);
      }

      if (this.activeConv && this.activeConv.otherUserId === senderId) {
        // Append to open chat and mark as read
        this.messages.push(msg as unknown as MessageData);
        markMessagesRead(this.token, senderId).catch(() => {/* silent */});
        if (existing) existing.unreadCount = 0;
        this.scrollToBottom();
      } else {
        // Increment unread badge
        const conv = this.conversations.find((c: ConversationData) => c.otherUserId === senderId);
        if (conv) conv.unreadCount = (conv.unreadCount || 0) + 1;
      }
    },

    async handleSend(this: any) {
      if (!this.inputText.trim() || !this.activeConv || this.sending) return;
      this.sending = true;
      const text = this.inputText.trim();
      this.inputText = '';
      try {
        const msg = await sendPrivateMessage(this.token, this.activeConv.otherUserId, text);
        this.messages.push(msg);
        this.activeConv.lastMessageContent = msg.content;
        this.activeConv.lastMessageByMe = true;
        this.activeConv.lastMessageTime = msg.createTime;
        this.scrollToBottom();
        // move conv to top
        const idx = this.conversations.findIndex((c: ConversationData) => c.otherUserId === this.activeConv!.otherUserId);
        if (idx > 0) {
          const [item] = this.conversations.splice(idx, 1);
          this.conversations.unshift(item);
        } else if (idx === -1) {
          this.conversations.unshift(this.activeConv);
        }
      } catch (e: any) {
        this.inputText = text;
        this.msgError = e?.message || '发送失败，请重试';
      } finally {
        this.sending = false;
      }
    },

    handleKeydown(this: any, e: KeyboardEvent) {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        this.handleSend();
      }
    },

    scrollToBottom(this: any) {
      this.$nextTick(() => {
        const el = document.querySelector('.chat-messages');
        if (el) el.scrollTop = el.scrollHeight;
      });
    },

    avatarUrl(this: any, url: string | null) {
      return resolveAssetUrl(url);
    },

    timeAgo(this: any, d: string) {
      return timeAgo(d);
    },

    isMine(this: any, msg: MessageData) {
      return msg.senderId === this.currentUserId;
    },

    formatTime(this: any, dateStr: string) {
      return new Date(dateStr).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    }
  },

  template: `
    <div class="messages-page page-container">
      <div class="page-hero">
        <div class="page-hero__eyebrow">私信</div>
        <h1 class="page-hero__title">消息中心</h1>
        <p class="page-hero__description">与其他探索者一对一交流</p>
      </div>

      <div v-if="!token" class="empty-state">
        <div class="empty-state__icon">🔒</div>
        <div class="empty-state__title">请先登录</div>
        <div class="empty-state__description">登录后即可发送和接收私信</div>
      </div>

      <div v-else class="messages-layout">
        <!-- conversation list -->
        <div class="conv-panel">
          <div class="conv-panel__header">
            <span class="conv-panel__title">会话</span>
          </div>

          <div v-if="convLoading" class="loading-spinner loading-spinner--sm">
            <div class="spinner"></div>
          </div>
          <div v-else-if="convError" class="form-error conv-error">{{ convError }}</div>
          <div v-else-if="conversations.length === 0" class="empty-state empty-state--compact">
            <div class="empty-state__icon">💬</div>
            <div class="empty-state__title">暂无会话</div>
            <div class="empty-state__description">前往用户主页发起私信</div>
          </div>
          <div v-else class="conv-list">
            <div
              v-for="conv in conversations"
              :key="conv.otherUserId"
              :class="['conv-item', activeConv && activeConv.otherUserId === conv.otherUserId ? 'conv-item--active' : '']"
              @click="openConversation(conv)"
            >
              <div class="conv-item__avatar">
                <img v-if="avatarUrl(conv.otherUserAvatar)" :src="avatarUrl(conv.otherUserAvatar)" :alt="conv.otherUserName" />
                <div v-else class="conv-item__avatar-fallback">{{ (conv.otherUserName || '?')[0] }}</div>
                <span v-if="conv.unreadCount > 0" class="conv-item__badge">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
              </div>
              <div class="conv-item__body">
                <div class="conv-item__name">{{ conv.otherUserName }}</div>
                <div class="conv-item__preview">
                  <span v-if="conv.lastMessageByMe" class="conv-item__me">我：</span>
                  <span>{{ conv.lastMessageContent || '…' }}</span>
                </div>
              </div>
              <div class="conv-item__time">{{ conv.lastMessageTime ? timeAgo(conv.lastMessageTime) : '' }}</div>
            </div>
          </div>
        </div>

        <!-- chat area -->
        <div class="chat-panel">
          <div v-if="!activeConv" class="chat-empty">
            <div class="chat-empty__icon">💬</div>
            <div class="chat-empty__text">选择一个会话开始聊天</div>
          </div>

          <template v-else>
            <div class="chat-header">
              <div class="chat-header__avatar">
                <img v-if="avatarUrl(activeConv.otherUserAvatar)" :src="avatarUrl(activeConv.otherUserAvatar)" :alt="activeConv.otherUserName" />
                <div v-else class="chat-header__avatar-fallback">{{ (activeConv.otherUserName || '?')[0] }}</div>
              </div>
              <div class="chat-header__name">{{ activeConv.otherUserName }}</div>
            </div>

            <div class="chat-messages" ref="msgBox">
              <div v-if="msgLoading" class="loading-spinner loading-spinner--sm">
                <div class="spinner"></div>
              </div>
              <div v-else-if="msgError" class="form-error">{{ msgError }}</div>
              <div v-else-if="messages.length === 0" class="chat-empty chat-empty--inline">
                <div class="chat-empty__text">还没有消息，发送第一条吧</div>
              </div>
              <template v-else>
                <div
                  v-for="msg in messages"
                  :key="msg.id"
                  :class="['chat-bubble-wrap', isMine(msg) ? 'chat-bubble-wrap--mine' : 'chat-bubble-wrap--theirs']"
                >
                  <img
                    v-if="!isMine(msg) && avatarUrl(msg.senderAvatar)"
                    class="chat-bubble__avatar"
                    :src="avatarUrl(msg.senderAvatar)"
                    :alt="msg.senderName"
                  />
                  <div v-else-if="!isMine(msg)" class="chat-bubble__avatar chat-bubble__avatar--fallback">
                    {{ (msg.senderName || '?')[0] }}
                  </div>
                  <div class="chat-bubble-col">
                    <div :class="['chat-bubble', isMine(msg) ? 'chat-bubble--mine' : 'chat-bubble--theirs']">
                      {{ msg.content }}
                    </div>
                    <div class="chat-bubble__time">{{ formatTime(msg.createTime) }}</div>
                  </div>
                </div>
              </template>
            </div>

            <div class="chat-input-bar">
              <textarea
                class="chat-input"
                v-model="inputText"
                placeholder="输入消息，Enter 发送，Shift+Enter 换行"
                rows="2"
                :disabled="sending"
                @keydown="handleKeydown"
              ></textarea>
              <button
                class="button button--primary chat-send-btn"
                :disabled="sending || !inputText.trim()"
                @click="handleSend"
              >
                {{ sending ? '…' : '发送' }}
              </button>
            </div>
          </template>
        </div>
      </div>
    </div>
  `
};
