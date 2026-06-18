<script setup lang="ts">
defineProps<{ description?: string; icon?: string }>()
</script>

<template>
  <div class="empty-state">
    <div class="empty-icon">
      <svg v-if="!icon || icon === 'empty'" width="80" height="80" viewBox="0 0 120 120" fill="none">
        <rect x="20" y="30" width="80" height="65" rx="12" stroke="currentColor" stroke-width="2.5" opacity=".3"/>
        <path d="M45 55h30M45 68h20" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" opacity=".25"/>
        <circle cx="85" cy="48" r="16" fill="currentColor" opacity=".08"/>
        <circle cx="85" cy="48" r="8" stroke="currentColor" stroke-width="2" opacity=".3"/>
      </svg>
      <svg v-else-if="icon === 'search'" width="80" height="80" viewBox="0 0 120 120" fill="none">
        <circle cx="52" cy="52" r="22" stroke="currentColor" stroke-width="2.5" opacity=".3"/>
        <path d="m68 68 15 15" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" opacity=".3"/>
        <circle cx="52" cy="52" r="6" fill="currentColor" opacity=".1"/>
      </svg>
    </div>
    <p class="empty-text">{{ description || '暂无数据' }}</p>
  </div>
</template>

<style scoped>
.empty-state {
  text-align: center;
  padding: 80px 20px;
  animation: fadeInUp .5s ease;
}
.empty-icon {
  color: var(--color-text-muted);
  margin-bottom: var(--space-md);
  display: flex;
  justify-content: center;
}
.empty-text {
  color: var(--color-text-muted);
  font-size: .95em;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
