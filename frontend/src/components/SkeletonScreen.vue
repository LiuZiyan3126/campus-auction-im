<script setup lang="ts">
defineProps<{ count?: number }>()
</script>

<template>
  <div class="skeleton-list">
    <div v-for="i in count || 6" :key="i" class="skeleton-card">
      <div class="skeleton-image"></div>
      <div class="skeleton-text skeleton-title"></div>
      <div class="skeleton-text skeleton-price"></div>
    </div>
  </div>
</template>

<style scoped>
.skeleton-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; }
.skeleton-card { animation: shimmer 1.5s infinite; }
.skeleton-image { width: 100%; height: 160px; background: #eee; border-radius: 8px; margin-bottom: 10px; }
.skeleton-text { height: 16px; background: #eee; border-radius: 4px; margin-bottom: 6px; }
.skeleton-title { width: 80%; }
.skeleton-price { width: 40%; }
@keyframes shimmer {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}
</style>
