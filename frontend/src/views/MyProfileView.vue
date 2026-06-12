<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/useUserStore'
import type { UserInfo } from '@/types'
import { logger } from '@/utils/logger'

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
  <div class="profile-page">
    <h2>个人资料</h2>
    <el-card v-if="profile" style="max-width:500px;margin-top:20px">
      <div style="text-align:center;margin-bottom:20px">
        <el-avatar :size="80" :src="profile.avatar">{{ profile.username?.[0] }}</el-avatar>
      </div>
      <template v-if="!editing">
        <p><strong>学号：</strong>{{ profile.studentId }}</p>
        <p><strong>昵称：</strong>{{ profile.username }}</p>
        <p><strong>邮箱：</strong>{{ profile.email }}</p>
        <p><strong>手机：</strong>{{ profile.phone || '未设置' }}</p>
        <p><strong>信誉分：</strong>{{ profile.reputationScore }}</p>
        <p><strong>角色：</strong><el-tag :type="profile.role === 'ADMIN' ? 'danger' : 'primary'" size="small">{{ profile.role === 'ADMIN' ? '管理员' : '用户' }}</el-tag></p>
        <el-button type="primary" style="margin-top:16px" @click="editing = true">编辑</el-button>
      </template>
      <template v-else>
        <el-form label-width="80px">
          <el-form-item label="昵称"><el-input v-model="editForm.username" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="editForm.phone" /></el-form-item>
        </el-form>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        <el-button @click="editing = false">取消</el-button>
      </template>
    </el-card>
  </div>
</template>

<style scoped>
.profile-page { max-width: 600px; margin: 0 auto; padding: 20px; }
.profile-page h2 { margin-bottom: 16px; }
p { margin: 8px 0; }
</style>
