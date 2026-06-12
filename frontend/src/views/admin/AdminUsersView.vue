<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import type { UserInfo, PageVO } from '@/types'

const users = ref<UserInfo[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const statusFilter = ref('')

onMounted(() => fetchUsers())

async function fetchUsers() {
  loading.value = true
  try {
    const res = await request.get<{ data: PageVO<UserInfo> }>('/admin/users', {
      params: { page: page.value, size: 20, status: statusFilter.value || undefined }
    })
    users.value = res.data.data?.items || []
    total.value = res.data.data?.total || 0
  } catch { /* ignore */ }
  loading.value = false
}

async function verifyUser(id: number, action: string) {
  try {
    await request.put(`/admin/users/${id}/verify`, { action })
    ElMessage.success(action === 'APPROVE' ? '已通过' : '已驳回')
    fetchUsers()
  } catch { /* ignore */ }
}

async function banUser(id: number, action: string) {
  try {
    await request.put(`/admin/users/${id}/ban`, { action })
    ElMessage.success(action === 'BAN' ? '已封禁' : '已解封')
    fetchUsers()
  } catch { /* ignore */ }
}
</script>

<template>
  <div class="admin-page">
    <h2>用户管理</h2>
    <el-select v-model="statusFilter" placeholder="状态筛选" @change="fetchUsers" clearable style="width:150px;margin-bottom:16px">
      <el-option value="PENDING" label="待审核" />
      <el-option value="ACTIVE" label="正常" />
      <el-option value="BANNED" label="已封禁" />
    </el-select>
    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="studentId" label="学号" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="reputationScore" label="信誉分" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'PENDING' ? 'warning' : 'danger'">
            {{ row.status === 'ACTIVE' ? '正常' : row.status === 'PENDING' ? '待审核' : '已封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="verifyUser(row.id, 'APPROVE')">通过</el-button>
          <el-button v-if="row.status === 'PENDING'" type="warning" size="small" @click="verifyUser(row.id, 'REJECT')">驳回</el-button>
          <el-button v-if="row.status === 'ACTIVE' && row.role !== 'ADMIN'" type="danger" size="small" @click="banUser(row.id, 'BAN')">封禁</el-button>
          <el-button v-if="row.status === 'BANNED'" type="primary" size="small" @click="banUser(row.id, 'UNBAN')">解封</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="20" @current-change="fetchUsers" layout="prev, pager, next" style="margin-top:16px;justify-content:center" />
  </div>
</template>

<style scoped>
.admin-page { padding: 20px; }
.admin-page h2 { margin-bottom: 16px; }
</style>
