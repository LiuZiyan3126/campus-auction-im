<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const form = ref({ studentId: '', email: '', password: '', confirmPassword: '', phone: '' })
const loading = ref(false)

async function handleRegister() {
  if (!form.value.studentId || !form.value.email || !form.value.password) {
    ElMessage.warning('请填写所有必填项')
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  if (form.value.password.length < 8) {
    ElMessage.warning('密码至少8位')
    return
  }
  loading.value = true
  try {
    await request.post('/auth/register', form.value)
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err.response?.data?.message || '注册失败，请重试')
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
          <h2>加入我们</h2>
          <p>注册账号，开启校园二手交易之旅</p>
        </div>
        <div class="side-decor">
          <div class="decor-circle c1"></div>
          <div class="decor-circle c2"></div>
        </div>
      </div>

      <!-- Form -->
      <div class="auth-form">
        <h1 class="form-title">创建账号</h1>
        <p class="form-sub">已有账号？<router-link to="/login">去登录</router-link></p>

        <div class="form-fields">
          <div class="input-row">
            <div class="input-group">
              <label>学号/工号 <span class="required">*</span></label>
              <input v-model="form.studentId" type="text" placeholder="请输入学号或工号" />
            </div>
          </div>
          <div class="input-group">
            <label>邮箱 <span class="required">*</span></label>
            <input v-model="form.email" type="email" placeholder="请输入邮箱" />
          </div>
          <div class="input-group">
            <label>手机号</label>
            <input v-model="form.phone" type="tel" placeholder="选填" />
          </div>
          <div class="input-row">
            <div class="input-group">
              <label>密码 <span class="required">*</span></label>
              <input v-model="form.password" type="password" placeholder="至少8位，含字母数字" />
            </div>
            <div class="input-group">
              <label>确认密码 <span class="required">*</span></label>
              <input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" />
            </div>
          </div>
        </div>

        <button class="submit-btn" :class="{ loading }" :disabled="loading" @click="handleRegister">
          <span v-if="loading" class="spinner"></span>
          <span v-else>注册</span>
        </button>
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
  min-height: 560px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--color-border-light);
}

.auth-side {
  width: 340px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #06b6d4, var(--color-primary), #8b5cf6);
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
.c1 { width: 220px; height: 220px; top: -50px; right: -60px; }
.c2 { width: 160px; height: 160px; bottom: -30px; left: -50px; }

.auth-form {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
}
.form-title { font-size: 1.6em; font-weight: 700; margin-bottom: 4px; }
.form-sub { font-size: .875em; color: var(--color-text-muted); margin-bottom: var(--space-lg); }
.form-sub a { font-weight: 500; }

.form-fields { display: flex; flex-direction: column; gap: var(--space-md); margin-bottom: var(--space-lg); }

.input-row { display: flex; gap: var(--space-md); }
.input-row .input-group { flex: 1; }

.input-group label {
  display: block;
  font-size: .85em;
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}
.required { color: var(--color-danger); }
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

.submit-btn {
  width: 100%;
  padding: 13px;
  background: linear-gradient(135deg, var(--color-primary), #8b5cf6);
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

@media (max-width: 768px) {
  .auth-side { display: none; }
  .auth-form { padding: 32px 24px; }
  .auth-card { max-width: 420px; }
  .input-row { flex-direction: column; }
}
</style>
