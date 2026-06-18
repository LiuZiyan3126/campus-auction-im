<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const searchText = ref('')
const mobileMenuOpen = ref(false)

const isActive = (path: string) => route.path === path

function goTo(path: string) {
  mobileMenuOpen.value = false
  router.push(path)
}

function handleSearch() {
  if (searchText.value.trim()) {
    router.push({ path: '/search', query: { q: searchText.value.trim() } })
    searchText.value = ''
  }
}

function handleLogout() {
  userStore.logout()
  mobileMenuOpen.value = false
  router.push('/')
}
</script>

<template>
  <header class="navbar glass">
    <div class="navbar-inner">
      <!-- Logo -->
      <div class="nav-brand" @click="goTo('/')">
        <div class="logo-icon">
          <svg viewBox="0 0 32 32" fill="none" width="28" height="28">
            <rect width="32" height="32" rx="8" fill="url(#logo-grad)"/>
            <path d="M10 22V12l6 4 6-4v10" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <defs><linearGradient id="logo-grad" x1="0" y1="0" x2="32" y2="32"><stop stop-color="#6366f1"/><stop offset="1" stop-color="#06b6d4"/></linearGradient></defs>
          </svg>
        </div>
        <span class="brand-text">校园拍卖</span>
      </div>

      <!-- Desktop Nav -->
      <nav class="nav-links">
        <a class="nav-link" :class="{ active: isActive('/') }" @click="goTo('/')">首页</a>
        <a class="nav-link" :class="{ active: isActive('/search') }" @click="goTo('/search')">发现</a>

        <div class="search-box">
          <input v-model="searchText" placeholder="搜索商品..." @keyup.enter="handleSearch" />
          <button @click="handleSearch" class="search-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          </button>
        </div>
      </nav>

      <!-- Actions -->
      <div class="nav-actions">
        <template v-if="userStore.isLoggedIn">
          <button class="nav-btn" title="发布" @click="goTo('/product/create')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            <span class="btn-label">发布</span>
          </button>
          <button class="nav-btn" title="消息" @click="goTo('/chat')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            <span class="btn-label">消息</span>
          </button>
          <button class="nav-btn" title="订单" @click="goTo('/orders')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            <span class="btn-label">订单</span>
          </button>
          <button class="nav-btn" title="我的发布" @click="goTo('/my-products')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/></svg>
            <span class="btn-label">发布</span>
          </button>
          <!-- Avatar dropdown -->
          <div class="avatar-group" @click="goTo('/user/profile')" title="个人中心">
            <div class="user-avatar">
              {{ userStore.userInfo?.username?.[0] || 'U' }}
            </div>
          </div>
          <button v-if="userStore.isAdmin" class="nav-btn admin-btn" @click="goTo('/admin')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
          </button>
          <button class="nav-btn logout-btn" @click="handleLogout" title="退出">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          </button>
        </template>
        <template v-else>
          <button class="btn-ghost" @click="goTo('/login')">登录</button>
          <button class="btn-primary-sm" @click="goTo('/register')">注册</button>
        </template>
      </div>

      <!-- Mobile hamburger -->
      <button class="hamburger" @click="mobileMenuOpen = !mobileMenuOpen">
        <span :class="{ open: mobileMenuOpen }"></span>
      </button>
    </div>

    <!-- Mobile menu -->
    <div class="mobile-menu" :class="{ open: mobileMenuOpen }" @click.self="mobileMenuOpen = false">
      <div class="mobile-search">
        <input v-model="searchText" placeholder="搜索商品..." @keyup.enter="handleSearch" />
      </div>
      <a class="mobile-link" @click="goTo('/')">首页</a>
      <a class="mobile-link" @click="goTo('/search')">发现</a>
      <template v-if="userStore.isLoggedIn">
        <a class="mobile-link" @click="goTo('/product/create')">发布商品</a>
        <a class="mobile-link" @click="goTo('/chat')">消息</a>
        <a class="mobile-link" @click="goTo('/orders')">订单</a>
        <a class="mobile-link" @click="goTo('/my-products')">我的发布</a>
        <a class="mobile-link" @click="goTo('/user/profile')">个人中心</a>
        <a v-if="userStore.isAdmin" class="mobile-link" @click="goTo('/admin')">管理后台</a>
        <a class="mobile-link logout" @click="handleLogout">退出登录</a>
      </template>
      <template v-else>
        <a class="mobile-link" @click="goTo('/login')">登录</a>
        <a class="mobile-link highlight" @click="goTo('/register')">注册</a>
      </template>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid var(--color-border-light);
}

.navbar-inner {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 var(--space-lg);
  gap: var(--space-lg);
}

/* Brand */
.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
}
.logo-icon { display: flex; }
.brand-text {
  font-size: 1.2em;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Desktop nav */
.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}
.nav-link {
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-size: .9em;
  font-weight: 500;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  text-decoration: none;
}
.nav-link:hover { color: var(--color-text); background: var(--color-bg); }
.nav-link.active { color: var(--color-primary); background: var(--color-primary-bg); }

/* Search */
.search-box {
  display: flex;
  align-items: center;
  background: var(--color-bg);
  border-radius: var(--radius-full);
  padding: 0 4px 0 16px;
  margin-left: 8px;
  border: 1px solid transparent;
  transition: all var(--transition-fast);
  max-width: 260px;
  flex: 1;
}
.search-box:focus-within {
  border-color: var(--color-primary-light);
  background: var(--color-surface);
  box-shadow: 0 0 0 3px rgba(99,102,241,.1);
}
.search-box input {
  border: none;
  outline: none;
  background: transparent;
  font-size: .875em;
  color: var(--color-text);
  width: 100%;
  font-family: inherit;
}
.search-box input::placeholder { color: var(--color-text-muted); }
.search-btn {
  background: var(--color-primary);
  border: none;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition-fast);
  flex-shrink: 0;
}
.search-btn:hover { background: var(--color-primary-dark); }

/* Actions */
.nav-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}
.nav-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 10px;
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-radius: var(--radius-md);
  font-size: .85em;
  transition: all var(--transition-fast);
  white-space: nowrap;
}
.nav-btn:hover { color: var(--color-text); background: var(--color-bg); }
.admin-btn:hover { color: var(--color-primary); }
.logout-btn:hover { color: var(--color-danger); }

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: .9em;
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  margin-left: 4px;
}
.user-avatar:hover {
  transform: scale(1.08);
  box-shadow: 0 0 0 3px rgba(99,102,241,.2);
}

/* Buttons */
.btn-ghost {
  padding: 8px 18px;
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: .9em;
  font-weight: 500;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}
.btn-ghost:hover { color: var(--color-text); background: var(--color-bg); }
.btn-primary-sm {
  padding: 8px 20px;
  border: none;
  background: var(--color-primary);
  color: white;
  font-size: .9em;
  font-weight: 600;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  box-shadow: 0 2px 8px rgba(99,102,241,.3);
}
.btn-primary-sm:hover { background: var(--color-primary-dark); transform: translateY(-1px); box-shadow: 0 4px 12px rgba(99,102,241,.4); }

/* Hamburger */
.hamburger {
  display: none;
  width: 32px;
  height: 32px;
  background: none;
  border: none;
  cursor: pointer;
  position: relative;
}
.hamburger span,
.hamburger span::before,
.hamburger span::after {
  display: block;
  width: 20px;
  height: 2px;
  background: var(--color-text);
  transition: all var(--transition-base);
  position: absolute;
  left: 6px;
}
.hamburger span { top: 15px; }
.hamburger span::before { content: ''; top: -6px; }
.hamburger span::after { content: ''; top: 6px; }
.hamburger span.open { background: transparent; }
.hamburger span.open::before { top: 0; transform: rotate(45deg); }
.hamburger span.open::after { top: 0; transform: rotate(-45deg); }

/* Mobile menu */
.mobile-menu {
  display: none;
  flex-direction: column;
  padding: 0 var(--space-lg) var(--space-lg);
  background: var(--color-surface);
  border-top: 1px solid var(--color-border-light);
  max-height: 0;
  overflow: hidden;
  transition: max-height var(--transition-slow), padding var(--transition-slow);
}
.mobile-menu.open { max-height: 500px; padding-bottom: var(--space-lg); }
.mobile-search { margin-bottom: var(--space-sm); }
.mobile-search input {
  width: 100%;
  padding: 10px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: .9em;
  outline: none;
  font-family: inherit;
}
.mobile-link {
  padding: 12px 8px;
  font-size: .95em;
  color: var(--color-text);
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: color var(--transition-fast);
  text-decoration: none;
}
.mobile-link:hover { color: var(--color-primary); }
.mobile-link.logout { color: var(--color-danger); }
.mobile-link.highlight { color: var(--color-primary); font-weight: 600; }

@media (max-width: 768px) {
  .nav-links, .nav-actions .btn-label { display: none; }
  .nav-actions { gap: 2px; }
  .hamburger { display: block; }
  .mobile-menu { display: flex; }
}
</style>
