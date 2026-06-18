<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api/request'
import { useWebSocket } from '@/composables/useWebSocket'
import { useUserStore } from '@/stores/useUserStore'
import type { WSMessage } from '@/types'
import { logger } from '@/utils/logger'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const otherUserId = Number(route.params.userId)
const convId = ref(Number(route.query.convId) || 0)

interface Msg {
  id: number; conversationId: number; senderId: number
  senderName: string; senderAvatar: string | null
  content: string; contentType: string; isRead: number; createdAt: string
}

const messages = ref<Msg[]>([])
const inputText = ref('')
const chatEl = ref<HTMLElement | null>(null)
const loading = ref(true)

const { subscribe } = useWebSocket()

onMounted(async () => {
  if (!convId.value) {
    try {
      const res = await request.post('/conversations', { targetUserId: otherUserId })
      convId.value = res.data.data.id
    } catch { return }
  }
  try {
    const res = await request.get(`/conversations/${convId.value}/messages`)
    messages.value = res.data.data?.items || []
    await nextTick()
    scrollBottom()
  } catch { /* ignore */ }

  try { await request.put(`/conversations/${convId.value}/read`) } catch { /* ignore */ }

  subscribe(`/user/${userStore.userInfo?.id}/queue/chat`, (msg: WSMessage) => {
    if (msg.type === 'NEW_MESSAGE') {
      const p = msg.payload as unknown as Msg
      if (p.conversationId === convId.value && !messages.value.some(m => m.id === p.id)) {
        messages.value.push(p)
        nextTick(() => scrollBottom())
      }
    }
  })
  loading.value = false
})

onUnmounted(() => {})

function scrollBottom() {
  if (chatEl.value) {
    chatEl.value.scrollTop = chatEl.value.scrollHeight
  }
}

async function send() {
  const text = inputText.value.trim()
  if (!text) return
  inputText.value = ''
  try {
    const res = await request.post(`/conversations/${convId.value}/messages`, { content: text, contentType: 'TEXT' })
    const sent = res.data.data as Msg
    if (sent && sent.id) {
      messages.value.push(sent)
      await nextTick()
      scrollBottom()
    }
  } catch (e) {
    logger.error('发送失败', e)
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send() }
}

const isMe = (m: Msg) => m.senderId === userStore.userInfo?.id

function formatTime(d: string) {
  if (!d) return ''
  const date = new Date(d)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  if (isToday) return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }) + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <div class="chat-window">
    <!-- Header -->
    <div class="chat-header">
      <button class="back-btn" @click="router.push('/chat')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <div class="chat-partner">
        <div class="partner-avatar">{{ route.params.userId?.toString()?.[0] || '?' }}</div>
        <span class="partner-name">用户 #{{ otherUserId }}</span>
      </div>
    </div>

    <!-- Messages -->
    <div class="chat-messages" ref="chatEl">
      <div v-if="loading" style="text-align:center;padding:40px;color:var(--color-text-muted);">加载中...</div>
      <div v-else-if="!messages.length" style="text-align:center;padding:60px;color:var(--color-text-muted);">
        <p style="font-size:2em;margin-bottom:8px;">💬</p>
        <p>发送第一条消息，开始交流</p>
      </div>
      <template v-else>
        <div
          v-for="(m, i) in messages" :key="m.id"
          class="msg-wrapper"
          :class="{ mine: isMe(m) }"
        >
          <!-- Date divider -->
          <div v-if="i === 0 || formatTime(m.createdAt) !== formatTime(messages[i-1].createdAt)" class="date-divider">
            <span>{{ formatTime(m.createdAt) }}</span>
          </div>
          <div class="msg-row">
            <div class="msg-avatar" v-if="!isMe(m)">{{ m.senderName?.[0] || '?' }}</div>
            <div class="msg-bubble" :class="{ mine: isMe(m) }">
              <p>{{ m.content }}</p>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Input -->
    <div class="chat-input-bar">
      <textarea
        v-model="inputText"
        placeholder="输入消息... (Enter 发送，Shift+Enter 换行)"
        rows="1"
        @keydown="handleKeydown"
        @input="(e: Event) => { const t = e.target as HTMLTextAreaElement; t.style.height = 'auto'; t.style.height = Math.min(t.scrollHeight, 120) + 'px' }"
      ></textarea>
      <button class="send-btn" :class="{ disabled: !inputText.trim() }" @click="send" :disabled="!inputText.trim()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-window {
  display: flex; flex-direction: column;
  height: calc(100vh - 140px);
  max-width: 760px; margin: 0 auto;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-md);
}

/* Header */
.chat-header {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--color-border-light);
  background: var(--color-surface);
}
.back-btn {
  width: 36px; height: 36px; border-radius: var(--radius-md);
  border: none; background: transparent; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}
.back-btn:hover { background: var(--color-bg); color: var(--color-text); }
.chat-partner { display: flex; align-items: center; gap: 10px; }
.partner-avatar {
  width: 36px; height: 36px; border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: white; display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: .9em;
}
.partner-name { font-weight: 600; font-size: .95em; }

/* Messages */
.chat-messages {
  flex: 1; overflow-y: auto; padding: 16px;
  background: var(--color-bg);
  scroll-behavior: smooth;
}

.date-divider { text-align: center; margin: 16px 0; }
.date-divider span {
  font-size: .75em; color: var(--color-text-muted);
  background: var(--color-bg); padding: 4px 12px;
  border-radius: var(--radius-full);
}

.msg-wrapper { margin-bottom: 4px; }
.msg-row { display: flex; align-items: flex-end; gap: 8px; max-width: 80%; }
.msg-wrapper.mine .msg-row { margin-left: auto; flex-direction: row-reverse; }

.msg-avatar {
  width: 30px; height: 30px; border-radius: var(--radius-full);
  background: var(--color-bg); color: var(--color-text-muted);
  display: flex; align-items: center; justify-content: center;
  font-size: .75em; font-weight: 600; flex-shrink: 0;
  border: 1px solid var(--color-border-light);
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 16px 16px 16px 4px;
  background: var(--color-surface);
  box-shadow: var(--shadow-xs);
  font-size: .9em; line-height: 1.5;
  color: var(--color-text);
}
.msg-bubble.mine {
  border-radius: 16px 16px 4px 16px;
  background: linear-gradient(135deg, var(--color-primary), #7c3aed);
  color: white;
}

/* Input */
.chat-input-bar {
  display: flex; align-items: flex-end; gap: 10px;
  padding: 12px 18px;
  border-top: 1px solid var(--color-border-light);
  background: var(--color-surface);
}
.chat-input-bar textarea {
  flex: 1; border: 1.5px solid var(--color-border);
  border-radius: var(--radius-lg); padding: 10px 16px;
  font-size: .9em; resize: none; outline: none;
  font-family: inherit; color: var(--color-text);
  background: var(--color-bg); max-height: 120px;
  transition: border-color var(--transition-fast);
}
.chat-input-bar textarea:focus { border-color: var(--color-primary); }
.send-btn {
  width: 42px; height: 42px; border-radius: var(--radius-full);
  border: none; background: var(--color-primary); color: white;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all var(--transition-fast); flex-shrink: 0;
}
.send-btn:hover:not(:disabled) { background: var(--color-primary-dark); transform: scale(1.05); }
.send-btn.disabled { opacity: .4; cursor: not-allowed; }

@media (max-width: 640px) {
  .chat-window { height: calc(100vh - 120px); border-radius: 0; border: none; }
  .msg-row { max-width: 90%; }
}
</style>
