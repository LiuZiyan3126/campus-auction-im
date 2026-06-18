<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import EmptyState from '@/components/EmptyState.vue'

interface Conversation {
  id: number; otherUserId: number; otherUserName: string; otherAvatar: string | null
  productId: number | null; productTitle: string | null
  lastMessage: string | null; lastMessageAt: string; unreadCount: number
}

const router = useRouter()
const conversations = ref<Conversation[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await request.get('/conversations')
    conversations.value = res.data.data || []
  } catch { /* ignore */ }
  loading.value = false
})

function goToChat(conv: Conversation) {
  router.push(`/chat/${conv.otherUserId}?convId=${conv.id}`)
}

function timeAgo(dateStr: string) {
  const diff = Date.now() - new Date(dateStr).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return '刚刚'
  if (mins < 60) return `${mins}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  return dateStr?.slice(0, 10) || ''
}
</script>

<template>
  <div class="chat-list-page">
    <div class="page-header">
      <h1>消息</h1>
      <span v-if="conversations.length" class="conv-count">{{ conversations.length }} 个会话</span>
    </div>

    <div v-if="loading" class="loading-wrap">
      <div class="spinner"></div>
    </div>
    <EmptyState v-else-if="!conversations.length" icon="empty" description="暂无会话，去商品详情页联系卖家吧" />

    <div v-else class="conv-list">
      <div
        v-for="c in conversations" :key="c.id"
        class="conv-item card"
        :class="{ unread: c.unreadCount > 0 }"
        @click="goToChat(c)"
      >
        <div class="conv-avatar" :class="{ online: c.unreadCount > 0 }">
          <span>{{ c.otherUserName?.[0] || '?' }}</span>
        </div>
        <div class="conv-body">
          <div class="conv-top">
            <span class="conv-name">{{ c.otherUserName }}</span>
            <span class="conv-time">{{ timeAgo(c.lastMessageAt) }}</span>
          </div>
          <p class="conv-preview">{{ c.lastMessage || '暂无消息' }}</p>
          <p v-if="c.productTitle" class="conv-product">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
            {{ c.productTitle }}
          </p>
        </div>
        <span v-if="c.unreadCount" class="unread-badge">{{ c.unreadCount > 99 ? '99+' : c.unreadCount }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-list-page { max-width: 640px; margin: 0 auto; }
.page-header {
  display: flex; align-items: baseline; gap: 12px;
  margin-bottom: var(--space-lg);
}
.page-header h1 { font-size: 1.5em; font-weight: 700; }
.conv-count { font-size: .85em; color: var(--color-text-muted); }

.loading-wrap { display: flex; justify-content: center; padding: 60px; }
.spinner {
  width: 32px; height: 32px;
  border: 2.5px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.conv-list { display: flex; flex-direction: column; gap: 4px; }

.conv-item {
  display: flex; align-items: center; gap: 14px;
  padding: 16px; cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
  border-radius: var(--radius-lg);
}
.conv-item:hover { border-color: var(--color-border-light); }
.conv-item.unread { background: var(--color-primary-bg); }

.conv-avatar {
  width: 50px; height: 50px; border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: white; display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 1.1em; flex-shrink: 0;
  position: relative;
}
.conv-avatar.online::after {
  content: ''; position: absolute; bottom: 2px; right: 2px;
  width: 12px; height: 12px; background: var(--color-success);
  border-radius: 50%; border: 2px solid white;
}

.conv-body { flex: 1; min-width: 0; }
.conv-top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.conv-name { font-weight: 600; font-size: .95em; }
.conv-time { font-size: .75em; color: var(--color-text-muted); }
.conv-preview {
  font-size: .85em; color: var(--color-text-muted);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.conv-product {
  display: flex; align-items: center; gap: 4px;
  font-size: .75em; color: var(--color-primary); margin-top: 4px;
}
.unread-badge {
  min-width: 20px; height: 20px; padding: 0 6px;
  background: var(--color-danger); color: white;
  border-radius: var(--radius-full); font-size: .7em;
  font-weight: 600; display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
</style>
