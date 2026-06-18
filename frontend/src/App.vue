<script setup lang="ts">
import NavBar from '@/components/NavBar.vue'
import Footer from '@/components/Footer.vue'
</script>

<template>
  <div class="app-shell">
    <NavBar />
    <main class="main-content">
      <router-view />
    </main>
    <Footer />
  </div>
</template>

<style>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.main-content {
  flex: 1;
  max-width: 1280px;
  margin: 0 auto;
  padding: var(--space-lg);
  width: 100%;
}

/* Page transitions */
.page-fade-enter-active { transition: opacity .2s ease, transform .2s ease; }
.page-fade-leave-active { transition: opacity .15s ease, transform .15s ease; }
.page-fade-enter-from { opacity: 0; transform: translateY(8px); }
.page-fade-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
