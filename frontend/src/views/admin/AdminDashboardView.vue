<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'

interface Overview {
  totalUsers: number; activeUsers: number; totalProducts: number;
  activeProducts: number; todayOrders: number; todayAmount: number;
  auctionSuccessRate: number;
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
  <div class="admin-page">
    <h2>管理后台</h2>
    <el-row :gutter="16" v-if="overview">
      <el-col :span="6"><el-card><h3>{{ overview.totalUsers }}</h3><p>总用户数</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>{{ overview.activeUsers }}</h3><p>活跃用户</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>{{ overview.totalProducts }}</h3><p>总商品数</p></el-card></el-col>
      <el-col :span="6"><el-card><h3>{{ overview.todayOrders }}</h3><p>今日订单</p></el-card></el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px" v-if="overview">
      <el-col :span="6"><el-card><h3>{{ overview.auctionSuccessRate }}%</h3><p>拍卖成交率</p></el-card></el-col>
    </el-row>
    <div style="margin-top:20px">
      <el-button type="primary" @click="$router.push('/admin/users')">用户管理</el-button>
      <el-button type="primary" @click="$router.push('/admin/products')">商品管理</el-button>
      <el-button type="primary" @click="$router.push('/admin/stats')">数据统计</el-button>
    </div>
  </div>
</template>

<style scoped>
.admin-page { padding: 20px; }
.admin-page h2 { margin-bottom: 20px; }
h3 { font-size: 1.8em; color: #409eff; }
p { color: #999; margin-top: 4px; }
</style>
