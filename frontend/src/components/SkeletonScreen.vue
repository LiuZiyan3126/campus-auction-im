<script setup lang="ts">
defineProps<{ count?: number }>()
</script>

<template>
  <div class="skeleton-grid">
    <div v-for="i in count || 6" :key="i" class="skeleton-card">
      <div class="sk-image shimmer-bg"></div>
      <div class="sk-body">
        <div class="sk-line w-80 shimmer-bg"></div>
        <div class="sk-line w-40 shimmer-bg"></div>
        <div class="sk-line w-60 shimmer-bg"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}
.skeleton-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border-light);
}
.sk-image {
  width: 100%;
  height: 200px;
}
.sk-body { padding: 16px; display: flex; flex-direction: column; gap: 10px; }
.sk-line { height: 14px; border-radius: var(--radius-sm); }
.w-80 { width: 80%; }
.w-60 { width: 60%; }
.w-40 { width: 40%; }

.shimmer-bg {
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
