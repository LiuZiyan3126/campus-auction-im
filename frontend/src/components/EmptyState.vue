<script setup lang="ts">
defineProps<{ description?: string }>()
</script>

<template>
  <div class="empty-state">
    <el-icon :size="64" color="#ccc"><svg viewBox="0 0 1024 1024" width="64" height="64"><path d="M192 128h640a64 64 0 0164 64v640a64 64 0 01-64 64H192a64 64 0 01-64-64V192a64 64 0 0164-64z" fill="#f0f0f0"/><path d="M384 384h256v64H384zM384 512h160v64H384z" fill="#ccc"/></svg></el-icon>
    <p>{{ description || '暂无数据' }}</p>
  </div>
</template>

<style scoped>
.empty-state { text-align: center; padding: 60px 0; color: #999; }
.empty-state p { margin-top: 12px; font-size: 0.95em; }
</style>
