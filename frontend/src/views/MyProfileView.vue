<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/useUserStore'
import type { UserInfo } from '@/types'

const userStore = useUserStore()
const profile = ref<UserInfo | null>(null)
const editing = ref(false)
const editForm = ref({ username: '', phone: '' })
const saving = ref(false)

onMounted(async () => {
  try {
    const res = await request.get('/users/me')
    profile.value = res.data.data
    editForm.value = { username: profile.value?.username || '', phone: profile.value?.phone || '' }
  } catch { /* ignore */ }
})

async function save() {
  saving.value = true
  try {
    const res = await request.put('/users/me', editForm.value)
    profile.value = res.data.data
    userStore.setUserInfo(res.data.data)
    editing.value = false
    ElMessage.success('保存成功')
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err.response?.data?.message || '保存失败')
  }
  saving.value = false
}
</script>

<template>
  <div class="profile-page" v-if="profile">
    <div class="profile-header">
      <div class="cover-bg"></div>
      <div class="profile-avatar-wrap">
        <div class="profile-avatar">
          <span>{{ profile.username?.[0] || 'U' }}</span>
        </div>
        <h1 class="profile-name">{{ profile.username }}</h1>
        <p class="profile-role">
          <span class="role-badge" :class="profile.role === 'ADMIN' ? 'admin' : 'user'">
            {{ profile.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </span>
        </p>
      </div>
    </div>

    <!-- Stats -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-val">{{ profile.reputationScore || 0 }}</span>
        <span class="stat-lbl">信誉分</span>
      </div>
      <div class="stat-card">
        <span class="stat-val">{{ profile.studentId }}</span>
        <span class="stat-lbl">学号</span>
      </div>
    </div>

    <!-- Info -->
    <div class="info-card card">
      <div class="card-header">
        <h2>个人信息</h2>
        <button v-if="!editing" class="edit-btn" @click="editing = true">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          编辑
        </button>
      </div>

      <template v-if="!editing">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">学号</span>
            <span class="info-value">{{ profile.studentId }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">昵称</span>
            <span class="info-value">{{ profile.username }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ profile.email }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">手机</span>
            <span class="info-value">{{ profile.phone || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">信誉分</span>
            <span class="info-value highlight">{{ profile.reputationScore }}</span>
          </div>
        </div>
      </template>

      <template v-else>
        <div class="edit-form">
          <div class="field">
            <label>昵称</label>
            <input v-model="editForm.username" placeholder="输入昵称" />
          </div>
          <div class="field">
            <label>手机号</label>
            <input v-model="editForm.phone" placeholder="输入手机号" />
          </div>
          <div class="edit-actions">
            <button class="save-btn" :disabled="saving" @click="save">
              <span v-if="saving" class="btn-spin"></span>
              <span v-else>保存</span>
            </button>
            <button class="cancel-btn" @click="editing = false">取消</button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.profile-page { max-width: 640px; margin: 0 auto; }

.profile-header {
  position: relative; text-align: center;
  margin-bottom: var(--space-xl);
}
.cover-bg {
  height: 120px; border-radius: var(--radius-xl);
  background: linear-gradient(135deg, var(--color-primary), #8b5cf6, var(--color-secondary));
}
.profile-avatar-wrap { margin-top: -44px; }
.profile-avatar {
  width: 88px; height: 88px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: white; display: flex; align-items: center; justify-content: center;
  font-size: 2em; font-weight: 700; margin: 0 auto;
  border: 4px solid white; box-shadow: var(--shadow-md);
}
.profile-name { font-size: 1.3em; font-weight: 700; margin-top: 12px; }
.profile-role { margin-top: 4px; }
.role-badge {
  padding: 3px 14px; border-radius: var(--radius-full);
  font-size: .8em; font-weight: 500;
}
.role-badge.user { background: var(--color-primary-bg); color: var(--color-primary); }
.role-badge.admin { background: rgba(239,68,68,.1); color: var(--color-danger); }

/* Stats */
.stats-row {
  display: flex; gap: 12px; margin-bottom: var(--space-xl);
}
.stat-card {
  flex: 1; background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg); padding: 20px;
  text-align: center;
}
.stat-val { display: block; font-size: 1.6em; font-weight: 700; color: var(--color-primary); }
.stat-lbl { font-size: .8em; color: var(--color-text-muted); margin-top: 2px; display: block; }

/* Info card */
.info-card { padding: 24px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.card-header h2 { font-size: 1.1em; font-weight: 600; }
.edit-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 7px 16px; border: 1px solid var(--color-border);
  background: var(--color-surface); border-radius: var(--radius-md);
  font-size: .85em; cursor: pointer; color: var(--color-text-secondary);
  transition: all var(--transition-fast); font-family: inherit;
}
.edit-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }

.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-label { font-size: .8em; color: var(--color-text-muted); }
.info-value { font-size: .95em; color: var(--color-text); font-weight: 500; }
.info-value.highlight { color: var(--color-primary); font-weight: 600; }

/* Edit form */
.edit-form { display: flex; flex-direction: column; gap: 16px; }
.field label { display: block; font-size: .85em; font-weight: 500; color: var(--color-text-secondary); margin-bottom: 6px; }
.field input {
  width: 100%; padding: 12px 16px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md); font-size: .95em;
  outline: none; transition: border-color var(--transition-fast);
  font-family: inherit; color: var(--color-text);
  background: var(--color-bg-alt);
}
.field input:focus { border-color: var(--color-primary); }
.edit-actions { display: flex; gap: 10px; margin-top: 4px; }
.save-btn {
  padding: 10px 24px; background: var(--color-primary); color: white;
  border: none; border-radius: var(--radius-md); font-size: .9em;
  font-weight: 600; cursor: pointer; font-family: inherit;
  transition: all var(--transition-fast); display: flex; align-items: center; gap: 6px;
}
.save-btn:hover:not(:disabled) { background: var(--color-primary-dark); }
.save-btn:disabled { opacity: .7; }
.cancel-btn {
  padding: 10px 24px; border: 1.5px solid var(--color-border);
  background: var(--color-surface); border-radius: var(--radius-md);
  font-size: .9em; cursor: pointer; font-family: inherit;
  transition: all var(--transition-fast);
}
.cancel-btn:hover { background: var(--color-bg); }

.btn-spin {
  width: 16px; height: 16px; border: 2px solid rgba(255,255,255,.3);
  border-top-color: white; border-radius: 50%; animation: spin .6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 480px) {
  .info-grid { grid-template-columns: 1fr; }
}
</style>
