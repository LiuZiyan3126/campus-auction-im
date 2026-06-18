<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'
import request from '@/api/request'
import type { ResultVO, UserInfo } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const studentId = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

async function handleLogin() {
  if (!studentId.value || !password.value) return
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await request.post<ResultVO<{ accessToken: string; refreshToken: string; userInfo: UserInfo }>>('/auth/login', {
      studentId: studentId.value,
      password: password.value,
    })
    const { accessToken, refreshToken, userInfo } = res.data.data
    userStore.setTokens(accessToken, refreshToken)
    userStore.setUserInfo(userInfo)
    router.push('/')
  } catch (e: unknown) {
    const err = e as { response?: { data?: ResultVO } }
    errorMsg.value = err.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- Left decoration -->
      <div class="auth-side">
        <div class="auth-side-content">
          <div class="side-logo">
            <svg viewBox="0 0 32 32" fill="none" width="48" height="48">
              <rect width="32" height="32" rx="8" fill="white" fill-opacity=".2"/>
              <path d="M10 22V12l6 4 6-4v10" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h2>欢迎回来</h2>
          <p>登录您的校园拍卖账号，继续探索好物</p>
        </div>
        <div class="side-decor">
          <div class="decor-circle c1"></div>
          <div class="decor-circle c2"></div>
          <div class="decor-circle c3"></div>
        </div>
      </div>

      <!-- Form -->
      <div class="auth-form">
        <h1 class="form-title">登录</h1>
        <p class="form-sub">还没有账号？<router-link to="/register">立即注册</router-link></p>

        <div class="form-fields">
          <div class="input-group">
            <label>学号/工号</label>
            <input v-model="studentId" type="text" placeholder="请输入学号或工号" @keyup.enter="handleLogin" />
          </div>
          <div class="input-group">
            <label>密码</label>
            <input v-model="password" type="password" placeholder="请输入密码" @keyup.enter="handleLogin" />
          </div>
        </div>

        <div v-if="errorMsg" class="error-alert">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
          {{ errorMsg }}
        </div>

        <button class="submit-btn" :class="{ loading }" :disabled="loading" @click="handleLogin">
          <span v-if="loading" class="spinner"></span>
          <span v-else>登录</span>
        </button>

        <div class="demo-hint">
          <span>演示账号：admin / admin123</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 180px);
  padding: var(--space-lg);
}

.auth-card {
  display: flex;
  max-width: 880px;
  width: 100%;
  min-height: 520px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--color-border-light);
}

/* Left side */
.auth-side {
  width: 340px;
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary), #8b5cf6);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 40px;
}
.auth-side-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}
.auth-side-content h2 { font-size: 1.5em; margin: 16px 0 8px; }
.auth-side-content p { font-size: .9em; opacity: .8; line-height: 1.5; }

.side-decor { position: absolute; inset: 0; }
.decor-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,.08);
}
.c1 { width: 200px; height: 200px; top: -40px; right: -60px; }
.c2 { width: 140px; height: 140px; bottom: -30px; left: -40px; }
.c3 { width: 80px; height: 80px; top: 50%; right: 30px; }

/* Form */
.auth-form {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
}
.form-title { font-size: 1.6em; font-weight: 700; margin-bottom: 4px; }
.form-sub { font-size: .875em; color: var(--color-text-muted); margin-bottom: var(--space-xl); }
.form-sub a { font-weight: 500; }

.form-fields { display: flex; flex-direction: column; gap: var(--space-md); margin-bottom: var(--space-lg); }

.input-group label {
  display: block;
  font-size: .85em;
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}
.input-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: .95em;
  color: var(--color-text);
  transition: all var(--transition-fast);
  outline: none;
  background: var(--color-bg-alt);
  font-family: inherit;
}
.input-group input:focus {
  border-color: var(--color-primary);
  background: var(--color-surface);
  box-shadow: 0 0 0 3px rgba(99,102,241,.1);
}
.input-group input::placeholder { color: var(--color-text-muted); }

.error-alert {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: var(--color-danger-bg);
  color: var(--color-danger);
  border-radius: var(--radius-md);
  font-size: .85em;
  margin-bottom: var(--space-md);
  border: 1px solid rgba(239,68,68,.15);
}

.submit-btn {
  width: 100%;
  padding: 13px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 1em;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.submit-btn:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(99,102,241,.35); }
.submit-btn:disabled { opacity: .7; cursor: not-allowed; }

.spinner {
  width: 20px; height: 20px;
  border: 2px solid rgba(255,255,255,.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin .6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.demo-hint {
  text-align: center;
  margin-top: auto;
  padding-top: var(--space-lg);
}
.demo-hint span {
  font-size: .8em;
  color: var(--color-text-muted);
  background: var(--color-bg);
  padding: 6px 14px;
  border-radius: var(--radius-full);
}

@media (max-width: 768px) {
  .auth-side { display: none; }
  .auth-form { padding: 32px 24px; }
  .auth-card { max-width: 420px; }
}
</style>
