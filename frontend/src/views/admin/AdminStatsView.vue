<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import request from '@/api/request'

interface TrendItem { date: string; newUsers: number; newOrders: number; totalAmount: number }

const trend = ref<TrendItem[]>([])
const chartRef = ref<HTMLElement | null>(null)

onMounted(async () => {
  try {
    const res = await request.get('/admin/stats/trend', { params: { days: 7 } })
    trend.value = res.data.data || []
    renderChart()
  } catch { /* ignore */ }
})

function renderChart() {
  if (!chartRef.value) return
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '新增订单'] },
    xAxis: { type: 'category', data: trend.value.map(t => t.date) },
    yAxis: { type: 'value' },
    series: [
      { name: '新增用户', type: 'line', data: trend.value.map(t => t.newUsers), smooth: true },
      { name: '新增订单', type: 'line', data: trend.value.map(t => t.newOrders), smooth: true },
    ],
  })
}
</script>

<template>
  <div class="admin-page">
    <h2>数据统计</h2>
    <el-card>
      <h3 style="margin-bottom:16px">近7日趋势</h3>
      <div ref="chartRef" style="width:100%;height:400px"></div>
    </el-card>
  </div>
</template>

<style scoped>
.admin-page { padding: 20px; }
.admin-page h2 { margin-bottom: 16px; }
</style>
