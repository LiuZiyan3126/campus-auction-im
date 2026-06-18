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

const loading = ref(true)

onMounted(async () => {
  try {
    const res = await request.get(`/users/${userId}`)
    profile.value = res.data.data
  } catch { /* ignore */ }
  loading.value = false
})

function formatDate(d: string) {
  return d?.slice(0, 10) || ''
}
</script>

<template>
  <div class="user-page" v-if="profile">
    <div class="profile-hero">
      <div class="hero-avatar">{{ profile.userInfo.username?.[0] || '?' }}</div>
      <h1 class="hero-name">{{ profile.userInfo.username }}</h1>
      <p class="hero-id">学号：{{ profile.userInfo.studentId }}</p>
      <div class="hero-stats">
        <div class="hero-stat">
          <span class="hs-val">{{ profile.userInfo.reputationScore }}</span>
          <span class="hs-lbl">信誉分</span>
        </div>
        <div class="hero-stat">
          <span class="hs-val">{{ profile.tradeCount }}</span>
          <span class="hs-lbl">成交</span>
        </div>
        <div class="hero-stat">
          <span class="hs-val">{{ profile.goodRate?.toFixed(1) || '0' }}%</span>
          <span class="hs-lbl">好评率</span>
        </div>
      </div>
      <p class="hero-since">{{ formatDate(profile.userInfo.createdAt) }} 加入</p>
    </div>

    <!-- Reviews -->
    <div class="reviews-section card">
      <h2>交易评价</h2>
      <EmptyState v-if="!loading && !profile.recentReviews?.length" description="暂无评价" />
      <div v-for="r in profile.recentReviews" :key="r.id" class="review-item">
        <div class="review-top">
          <div class="reviewer">
            <div class="reviewer-avatar">{{ r.reviewerName?.[0] }}</div>
            <span>{{ r.reviewerName }}</span>
          </div>
          <div class="review-stars">
            <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= r.rating }">{{ i <= r.rating ? '★' : '☆' }}</span>
          </div>
        </div>
        <p class="review-content">{{ r.content }}</p>
        <span class="review-date">{{ formatDate(r.createdAt) }}</span>
      </div>
    </div>
  </div>
  <div v-else-if="!loading" style="padding:60px 0">
    <EmptyState description="用户不存在" />
  </div>
</template>

<style scoped>
.user-page { max-width: 640px; margin: 0 auto; }

.profile-hero {
  text-align: center; padding: 40px 0;
}
.hero-avatar {
  width: 96px; height: 96px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: white; display: flex; align-items: center; justify-content: center;
  font-size: 2.2em; font-weight: 700; margin: 0 auto;
  box-shadow: var(--shadow-lg);
}
.hero-name { font-size: 1.5em; font-weight: 700; margin-top: 16px; }
.hero-id { font-size: .9em; color: var(--color-text-muted); margin-top: 4px; }

.hero-stats {
  display: flex; justify-content: center; gap: var(--space-xl);
  margin-top: var(--space-lg);
}
.hero-stat { text-align: center; }
.hs-val { display: block; font-size: 1.3em; font-weight: 700; color: var(--color-primary); }
.hs-lbl { font-size: .75em; color: var(--color-text-muted); }
.hero-since { font-size: .8em; color: var(--color-text-muted); margin-top: var(--space-lg); }

/* Reviews */
.reviews-section { padding: 24px; margin-top: var(--space-lg); }
.reviews-section h2 { font-size: 1.1em; font-weight: 600; margin-bottom: var(--space-md); }

.review-item {
  padding: 16px 0; border-bottom: 1px solid var(--color-border-light);
}
.review-item:last-child { border-bottom: none; }
.review-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.reviewer { display: flex; align-items: center; gap: 8px; font-size: .9em; font-weight: 500; }
.reviewer-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--color-bg); color: var(--color-text-secondary);
  display: flex; align-items: center; justify-content: center;
  font-size: .8em; font-weight: 600;
}
.star { font-size: .9em; }
.star.filled { color: #f59e0b; }
.star:not(.filled) { color: var(--color-border); }
.review-content { font-size: .9em; color: var(--color-text-secondary); line-height: 1.6; }
.review-date { font-size: .75em; color: var(--color-text-muted); display: block; margin-top: 6px; }
</style>
