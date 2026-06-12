<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api/request'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const userId = Number(route.params.id)

const profile = ref<{
  userInfo: { id: number; username: string; avatar: string | null; studentId: string; reputationScore: number; createdAt: string }
  tradeCount: number
  goodRate: number
  recentReviews: { id: number; reviewerName: string; rating: number; content: string; createdAt: string }[]
} | null>(null)

onMounted(async () => {
  try {
    const res = await request.get(`/users/${userId}`)
    profile.value = res.data.data
  } catch { /* ignore */ }
})
</script>

<template>
  <div class="user-page" v-if="profile">
    <div class="header">
      <el-avatar :size="80" :src="profile.userInfo.avatar">{{ profile.userInfo.username?.[0] }}</el-avatar>
      <h2>{{ profile.userInfo.username }}</h2>
      <p class="meta">信誉分: {{ profile.userInfo.reputationScore }} · 成交 {{ profile.tradeCount }} 笔 · 好评率 {{ profile.goodRate?.toFixed(1) }}%</p>
    </div>
    <div class="reviews">
      <h3>交易评价</h3>
      <EmptyState v-if="!profile.recentReviews?.length" description="暂无评价" />
      <div v-for="r in profile.recentReviews" :key="r.id" class="review-item">
        <div class="review-header">
          <span>{{ r.reviewerName }}</span>
          <span class="stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
        </div>
        <p>{{ r.content }}</p>
        <span class="time">{{ r.createdAt }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-page { max-width: 600px; margin: 0 auto; padding: 20px; }
.header { text-align: center; padding: 30px 0; }
.header h2 { margin-top: 12px; }
.meta { color: #666; margin-top: 6px; }
.reviews { margin-top: 30px; }
.reviews h3 { margin-bottom: 16px; }
.review-item { padding: 14px 0; border-bottom: 1px solid #f0f0f0; }
.review-header { display: flex; justify-content: space-between; }
.stars { color: #f7ba2a; }
.time { color: #999; font-size: 0.8em; }
</style>
