<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api/request'
import { useWebSocket } from '@/composables/useWebSocket'
import { useUserStore } from '@/stores/useUserStore'
import type { WSMessage } from '@/types'
import { logger } from '@/utils/logger'

const route = useRoute()
const userStore = useUserStore()
const otherUserId = Number(route.params.userId)
const convId = ref(Number(route.query.convId) || 0)

interface Msg {
  id: number; conversationId: number; senderId: number;
  senderName: string; senderAvatar: string | null;
  content: string; contentType: string; isRead: number; createdAt: string;
}

const messages = ref<Msg[]>([])
const inputText = ref('')
const chatEl = ref<HTMLElement | null>(null)

const { subscribe } = useWebSocket()

onMounted(async () => {
  // 如无convId，创建一个
  if (!convId.value) {
    try {
      const res = await request.post('/conversations', { targetUserId: otherUserId })
      convId.value = res.data.data.id
    } catch { return }
  }
  // 加载历史消息
  try {
    const res = await request.get(`/conversations/${convId.value}/messages`)
    messages.value = res.data.data?.items || []
    await nextTick()
    scrollBottom()
  } catch { /* ignore */ }

  // 标记已读
  try { await request.put(`/conversations/${convId.value}/read`) } catch { /* ignore */ }

  // WebSocket订阅
  subscribe(`/user/${userStore.userInfo?.id}/queue/chat`, (msg: WSMessage) => {
    if (msg.type === 'NEW_MESSAGE') {
      const p = msg.payload as unknown as Msg
      if (p.conversationId === convId.value) {
        messages.value.push(p)
        nextTick(() => scrollBottom())
      }
    }
  })
})

function scrollBottom() {
  if (chatEl.value) chatEl.value.scrollTop = chatEl.value.scrollHeight
}

async function send() {
  const text = inputText.value.trim()
  if (!text) return
  inputText.value = ''
  try {
    await request.post(`/conversations/${convId.value}/messages`, { content: text, contentType: 'TEXT' })
  } catch (e) {
    logger.error('发送失败', e)
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send() }
}
</script>

<template>
  <div class="chat-window">
    <div class="chat-messages" ref="chatEl">
      <div v-for="m in messages" :key="m.id" class="msg-row" :class="{ mine: m.senderId === userStore.userInfo?.id }">
        <div class="msg-bubble">
          <p>{{ m.content }}</p>
          <span class="msg-time">{{ m.createdAt }}</span>
        </div>
      </div>
    </div>
    <div class="chat-input">
      <el-input v-model="inputText" type="textarea" :rows="2" placeholder="输入消息... Enter发送" @keydown="handleKeydown" />
      <el-button type="primary" @click="send" style="margin-left:8px">发送</el-button>
    </div>
  </div>
</template>

<style scoped>
.chat-window { max-width: 700px; margin: 0 auto; display: flex; flex-direction: column; height: calc(100vh - 120px); }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; }
.msg-row { display: flex; margin-bottom: 12px; }
.msg-row.mine { justify-content: flex-end; }
.msg-bubble { max-width: 70%; padding: 10px 14px; border-radius: 12px; background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,.1); }
.mine .msg-bubble { background: #95ec69; }
.msg-time { font-size: 0.7em; color: #999; display: block; margin-top: 4px; }
.chat-input { display: flex; padding: 12px; border-top: 1px solid #eee; background: #fff; }
</style>
