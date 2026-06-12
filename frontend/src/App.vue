<script setup lang="ts">
import NavBar from '@/components/NavBar.vue'
import Footer from '@/components/Footer.vue'
</script>

<template>
  <div id="app-container">
    <NavBar />
    <main class="main-content">
      <router-view />
    </main>
    <Footer />
  </div>
</template>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif; }
#app-container { min-height: 100vh; display: flex; flex-direction: column; }
.main-content { flex: 1; max-width: 1200px; margin: 0 auto; padding: 20px; width: 100%; }
</style>
