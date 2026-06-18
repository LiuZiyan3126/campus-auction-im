<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'

const router = useRouter()

interface Overview {
  totalUsers: number; activeUsers: number; totalProducts: number
  activeProducts: number; todayOrders: number; todayAmount: number
  auctionSuccessRate: number
}

const overview = ref<Overview | null>(null)

onMounted(async () => {
  try {
    const res = await request.get('/admin/stats/overview')
    overview.value = res.data.data
  } catch { /* ignore */ }
})
</script>

<template>
  <div class="dashboard">
    <div class="page-top">
      <h1>管理后台</h1>
      <p>概览 · 系统运行状态</p>
    </div>

    <div class="stats-grid" v-if="overview">
      <div class="stat-card" style="--accent: #6366f1">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ overview.totalUsers }}</span>
          <span class="stat-label">总用户</span>
        </div>
      </div>
      <div class="stat-card" style="--accent: #10b981">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ overview.activeUsers }}</span>
          <span class="stat-label">活跃用户</span>
        </div>
      </div>
      <div class="stat-card" style="--accent: #f59e0b">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ overview.totalProducts }}</span>
          <span class="stat-label">总商品</span>
        </div>
      </div>
      <div class="stat-card" style="--accent: #06b6d4">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ overview.todayOrders }}</span>
          <span class="stat-label">今日订单</span>
        </div>
      </div>
      <div class="stat-card" style="--accent: #8b5cf6">
        <div class="stat-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><line x1="12" y1="20" x2="12" y2="10"/><line x1="18" y1="20" x2="18" y2="4"/><line x1="6" y1="20" x2="6" y2="16"/></svg>
        </div>
        <div class="stat-body">
          <span class="stat-value">{{ overview.auctionSuccessRate }}%</span>
          <span class="stat-label">拍卖成交率</span>
        </div>
      </div>
    </div>

    <!-- Quick links -->
    <div class="quick-links">
      <h2>快捷管理</h2>
      <div class="link-grid">
        <button class="link-card" @click="router.push('/admin/users')">
          <div class="link-icon users">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
          </div>
          <span>用户管理</span>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
        <button class="link-card" @click="router.push('/admin/products')">
          <div class="link-icon products">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/></svg>
          </div>
          <span>商品管理</span>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
        <button class="link-card" @click="router.push('/admin/stats')">
          <div class="link-icon stats">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
          </div>
          <span>数据统计</span>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard { max-width: 1000px; margin: 0 auto; }
.page-top { margin-bottom: var(--space-xl); }
.page-top h1 { font-size: 1.5em; font-weight: 700; }
.page-top p { color: var(--color-text-muted); font-size: .875em; margin-top: 2px; }

/* Stats */
.stats-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 14px; margin-bottom: var(--space-xl); }
.stat-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex; align-items: center; gap: 16px;
  transition: all var(--transition-base);
}
.stat-card:hover { border-color: var(--accent); box-shadow: var(--shadow-md); }
.stat-icon {
  width: 48px; height: 48px; border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
  color: var(--accent); background: color-mix(in srgb, var(--accent) 8%, white);
}
.stat-body { display: flex; flex-direction: column; }
.stat-value { font-size: 1.5em; font-weight: 700; color: var(--color-text); }
.stat-label { font-size: .8em; color: var(--color-text-muted); }

/* Quick links */
.quick-links h2 { font-size: 1.1em; margin-bottom: 12px; }
.link-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; }
.link-card {
  display: flex; align-items: center; gap: 14px;
  padding: 18px; border: 1px solid var(--color-border-light);
  background: var(--color-surface); border-radius: var(--radius-lg);
  cursor: pointer; transition: all var(--transition-base);
  font-family: inherit; font-size: .95em; font-weight: 500;
  color: var(--color-text);
}
.link-card:hover { border-color: var(--color-primary-light); background: var(--color-primary-bg); box-shadow: var(--shadow-md); }
.link-icon {
  width: 40px; height: 40px; border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
}
.link-icon.users { background: rgba(99,102,241,.1); color: #6366f1; }
.link-icon.products { background: rgba(245,158,11,.1); color: #f59e0b; }
.link-icon.stats { background: rgba(139,92,246,.1); color: #8b5cf6; }
.link-card span { flex: 1; }
</style>
