<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import EmptyState from '@/components/EmptyState.vue'

interface Conversation {
  id: number; otherUserId: number; otherUserName: string; otherAvatar: string | null;
  productId: number | null; productTitle: string | null;
  lastMessage: string | null; lastMessageAt: string; unreadCount: number;
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
</script>

<template>
  <div class="chat-list-page">
    <h2>消息</h2>
    <div v-if="loading" style="text-align:center;padding:40px;color:#999;">加载中...</div>
    <EmptyState v-else-if="!conversations.length" description="暂无会话" />
    <div v-else class="conv-list">
      <div v-for="c in conversations" :key="c.id" class="conv-item" @click="goToChat(c)">
        <el-badge :value="c.unreadCount" :hidden="c.unreadCount === 0">
          <el-avatar :size="48" :src="c.otherAvatar">{{ c.otherUserName?.[0] }}</el-avatar>
        </el-badge>
        <div class="conv-body">
          <div class="conv-header">
            <span class="conv-name">{{ c.otherUserName }}</span>
            <span class="conv-time">{{ c.lastMessageAt }}</span>
          </div>
          <p class="conv-preview">{{ c.lastMessage || '暂无消息' }}</p>
          <p v-if="c.productTitle" class="conv-product">商品: {{ c.productTitle }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-list-page { max-width: 600px; margin: 0 auto; padding: 20px; }
.chat-list-page h2 { margin-bottom: 16px; }
.conv-item { display: flex; align-items: center; gap: 14px; padding: 14px; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.conv-item:hover { background: #fafafa; }
.conv-body { flex: 1; min-width: 0; }
.conv-header { display: flex; justify-content: space-between; }
.conv-name { font-weight: bold; }
.conv-time { color: #999; font-size: 0.8em; }
.conv-preview { color: #666; font-size: 0.9em; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.conv-product { color: #409eff; font-size: 0.8em; margin-top: 2px; }
</style>
