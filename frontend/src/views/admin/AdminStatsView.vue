<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import request from '@/api/request'

interface TrendItem { date: string; newUsers: number; newOrders: number; totalAmount: number }

const trend = ref<TrendItem[]>([])
const loading = ref(true)
const chartRef = ref<HTMLElement | null>(null)

onMounted(async () => {
  try {
    const res = await request.get('/admin/stats/trend', { params: { days: 7 } })
    trend.value = res.data.data || []
    renderChart()
  } catch { /* ignore */ }
  loading.value = false
})

function renderChart() {
  if (!chartRef.value) return
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: {
      data: ['新增用户', '新增订单', '交易额'],
      bottom: 0,
      textStyle: { color: '#64748b' },
    },
    grid: { top: 20, left: 10, right: 10, bottom: 40, containLabel: true },
    xAxis: {
      type: 'category',
      data: trend.value.map(t => t.date),
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#94a3b8' },
    },
    yAxis: [
      {
        type: 'value',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { color: '#f1f5f9' } },
        axisLabel: { color: '#94a3b8' },
      },
      {
        type: 'value',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { color: '#94a3b8', formatter: '¥{value}' },
      },
    ],
    series: [
      {
        name: '新增用户',
        type: 'line',
        data: trend.value.map(t => t.newUsers),
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#6366f1', width: 2.5 },
        itemStyle: { color: '#6366f1' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(99,102,241,.15)' },
            { offset: 1, color: 'rgba(99,102,241,.0)' },
          ]),
        },
      },
      {
        name: '新增订单',
        type: 'line',
        data: trend.value.map(t => t.newOrders),
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#10b981', width: 2.5 },
        itemStyle: { color: '#10b981' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16,185,129,.15)' },
            { offset: 1, color: 'rgba(16,185,129,.0)' },
          ]),
        },
      },
      {
        name: '交易额',
        type: 'bar',
        yAxisIndex: 1,
        data: trend.value.map(t => t.totalAmount),
        barWidth: 16,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(139,92,246,.7)' },
            { offset: 1, color: 'rgba(139,92,246,.15)' },
          ]),
          borderRadius: [6, 6, 0, 0],
        },
      },
    ],
  })
  window.addEventListener('resize', () => chart.resize())
}
</script>

<template>
  <div class="stats-page">
    <div class="page-header">
      <h1>数据统计</h1>
    </div>

    <div v-if="loading" style="text-align:center;padding:60px;color:var(--color-text-muted);">
      <div class="spinner"></div>
    </div>
    <div v-else class="chart-card card">
      <div class="chart-header">
        <h2>近7日趋势</h2>
        <div class="chart-legend">
          <span class="legend-item"><span class="dot" style="background:#6366f1"></span>新增用户</span>
          <span class="legend-item"><span class="dot" style="background:#10b981"></span>新增订单</span>
          <span class="legend-item"><span class="dot" style="background:#8b5cf6"></span>交易额</span>
        </div>
      </div>
      <div ref="chartRef" class="chart"></div>
    </div>
  </div>
</template>

<style scoped>
.stats-page { max-width: 1000px; margin: 0 auto; }
.page-header { margin-bottom: var(--space-lg); }
.page-header h1 { font-size: 1.4em; font-weight: 700; }

.spinner {
  width: 30px; height: 30px; margin: 0 auto;
  border: 2.5px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.chart-card { padding: 24px; }
.chart-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--space-md); flex-wrap: wrap; gap: 12px;
}
.chart-header h2 { font-size: 1.05em; font-weight: 600; }
.chart-legend { display: flex; gap: 16px; }
.legend-item { display: flex; align-items: center; gap: 6px; font-size: .8em; color: var(--color-text-muted); }
.dot { width: 8px; height: 8px; border-radius: 50%; display: block; }

.chart { width: 100%; height: 420px; }
</style>
