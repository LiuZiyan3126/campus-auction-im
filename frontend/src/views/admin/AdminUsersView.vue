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
    <div class="page-header">
      <h1>用户管理</h1>
      <div class="filter-bar">
        <button :class="{ active: !statusFilter }" @click="statusFilter = ''; fetchUsers()">全部</button>
        <button :class="{ active: statusFilter === 'PENDING' }" @click="statusFilter = 'PENDING'; fetchUsers()">待审核</button>
        <button :class="{ active: statusFilter === 'ACTIVE' }" @click="statusFilter = 'ACTIVE'; fetchUsers()">正常</button>
        <button :class="{ active: statusFilter === 'BANNED' }" @click="statusFilter = 'BANNED'; fetchUsers()">已封禁</button>
      </div>
    </div>

    <div class="table-card card">
      <table class="data-table" v-loading="loading">
        <thead>
          <tr>
            <th>ID</th>
            <th>学号</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>信誉分</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id">
            <td class="id-cell">{{ u.id }}</td>
            <td>{{ u.studentId }}</td>
            <td>
              <div class="user-cell">
                <div class="user-avatar-sm">{{ u.username?.[0] }}</div>
                <span>{{ u.username }}</span>
              </div>
            </td>
            <td>{{ u.email }}</td>
            <td>{{ u.reputationScore }}</td>
            <td>
              <span class="status-badge" :class="u.status === 'ACTIVE' ? 'active' : u.status === 'PENDING' ? 'pending' : 'banned'">
                {{ u.status === 'ACTIVE' ? '正常' : u.status === 'PENDING' ? '待审核' : '已封禁' }}
              </span>
            </td>
            <td>
              <div class="action-btns">
                <template v-if="u.status === 'PENDING'">
                  <button class="act-sm approve" @click="verifyUser(u.id, 'APPROVE')">通过</button>
                  <button class="act-sm reject" @click="verifyUser(u.id, 'REJECT')">驳回</button>
                </template>
                <button v-if="u.status === 'ACTIVE' && u.role !== 'ADMIN'" class="act-sm danger" @click="banUser(u.id, 'BAN')">封禁</button>
                <button v-if="u.status === 'BANNED'" class="act-sm primary" @click="banUser(u.id, 'UNBAN')">解封</button>
                <span v-if="u.role === 'ADMIN'" style="color:var(--color-text-muted);font-size:.8em;">管理员</span>
              </div>
            </td>
          </tr>
          <tr v-if="!loading && !users.length">
            <td colspan="7" style="text-align:center;padding:40px;color:var(--color-text-muted);">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="total > 20" class="pagination-wrap">
      <el-pagination v-model:current-page="page" :total="total" :page-size="20" @current-change="fetchUsers" layout="prev, pager, next" background />
    </div>
  </div>
</template>

<style scoped>
.admin-page { max-width: 1100px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); flex-wrap: wrap; gap: 12px; }
.page-header h1 { font-size: 1.4em; font-weight: 700; }

.filter-bar { display: flex; gap: 4px; background: var(--color-bg); border-radius: var(--radius-md); padding: 3px; }
.filter-bar button {
  padding: 7px 16px; border: none; background: transparent;
  border-radius: var(--radius-sm); font-size: .85em; cursor: pointer;
  color: var(--color-text-secondary); transition: all var(--transition-fast);
  font-family: inherit;
}
.filter-bar button.active { background: var(--color-surface); color: var(--color-text); font-weight: 600; box-shadow: var(--shadow-xs); }

.table-card { padding: 0; overflow: hidden; }

.data-table { width: 100%; border-collapse: collapse; font-size: .9em; }
.data-table thead { background: var(--color-bg); }
.data-table th {
  padding: 12px 16px; text-align: left;
  font-weight: 600; color: var(--color-text-secondary);
  font-size: .8em; text-transform: uppercase; letter-spacing: .3px;
}
.data-table td { padding: 14px 16px; border-bottom: 1px solid var(--color-border-light); }
.data-table tbody tr { transition: background var(--transition-fast); }
.data-table tbody tr:hover { background: var(--color-bg); }

.id-cell { color: var(--color-text-muted); font-family: var(--font-mono); font-size: .85em; }
.user-cell { display: flex; align-items: center; gap: 8px; }
.user-avatar-sm {
  width: 28px; height: 28px; border-radius: 50%;
  background: var(--color-primary-bg); color: var(--color-primary);
  display: flex; align-items: center; justify-content: center;
  font-size: .75em; font-weight: 600;
}

.status-badge {
  padding: 3px 10px; border-radius: var(--radius-full);
  font-size: .75em; font-weight: 600;
}
.status-badge.active { background: var(--color-success-bg); color: var(--color-success); }
.status-badge.pending { background: var(--color-warning-bg); color: var(--color-warning); }
.status-badge.banned { background: var(--color-danger-bg); color: var(--color-danger); }

.action-btns { display: flex; gap: 6px; }
.act-sm {
  padding: 5px 12px; border-radius: var(--radius-sm);
  font-size: .75em; font-weight: 500; cursor: pointer;
  border: none; transition: all var(--transition-fast);
  font-family: inherit;
}
.act-sm.approve { background: rgba(16,185,129,.1); color: var(--color-success); }
.act-sm.approve:hover { background: var(--color-success); color: white; }
.act-sm.reject { background: rgba(245,158,11,.1); color: var(--color-warning); }
.act-sm.reject:hover { background: var(--color-warning); color: white; }
.act-sm.danger { background: rgba(239,68,68,.1); color: var(--color-danger); }
.act-sm.danger:hover { background: var(--color-danger); color: white; }
.act-sm.primary { background: var(--color-primary-bg); color: var(--color-primary); }
.act-sm.primary:hover { background: var(--color-primary); color: white; }

.pagination-wrap { display: flex; justify-content: center; margin-top: var(--space-lg); }
</style>
