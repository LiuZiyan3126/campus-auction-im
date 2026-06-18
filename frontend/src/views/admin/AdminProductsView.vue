<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import type { PageVO } from '@/types'

interface ProductAdmin {
  id: number; sellerName: string; title: string; type: string
  currentPrice: number | null; fixedPrice: number | null
  status: string; viewCount: number; createdAt: string
}

const router = useRouter()
const products = ref<ProductAdmin[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const statusFilter = ref('PENDING')
const deleting = ref<number | null>(null)

onMounted(() => fetchProducts())

async function fetchProducts() {
  loading.value = true
  try {
    const res = await request.get<{ data: PageVO<ProductAdmin> }>('/admin/products', {
      params: { page: page.value, size: 20, status: statusFilter.value || undefined }
    })
    products.value = res.data.data?.items || []
    total.value = res.data.data?.total || 0
  } catch { /* ignore */ }
  loading.value = false
}

async function audit(id: number, action: string) {
  try {
    await request.put(`/admin/products/${id}/audit`, { action })
    ElMessage.success(action === 'APPROVE' ? '审核通过' : '已下架')
    fetchProducts()
  } catch { /* ignore */ }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？删除后无法恢复。', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch { return }

  deleting.value = id
  try {
    await request.delete(`/products/${id}`)
    ElMessage.success('已删除')
    fetchProducts()
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
  deleting.value = null
}

function handleEdit(id: number) {
  router.push(`/product/${id}`)
}
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <h1>商品管理</h1>
      <div class="filter-bar">
        <button :class="{ active: statusFilter === 'PENDING' }" @click="statusFilter = 'PENDING'; fetchProducts()">待审核</button>
        <button :class="{ active: statusFilter === 'ACTIVE' }" @click="statusFilter = 'ACTIVE'; fetchProducts()">已通过</button>
        <button :class="{ active: statusFilter === 'BANNED' }" @click="statusFilter = 'BANNED'; fetchProducts()">已下架</button>
      </div>
    </div>

    <div class="table-card card">
      <table class="data-table" v-loading="loading">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>卖家</th>
            <th>类型</th>
            <th>价格</th>
            <th>浏览</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in products" :key="p.id">
            <td class="id-cell">{{ p.id }}</td>
            <td class="title-cell">{{ p.title }}</td>
            <td>{{ p.sellerName }}</td>
            <td>
              <span class="type-badge" :class="p.type === 'AUCTION' ? 'auction' : 'sale'">
                {{ p.type === 'AUCTION' ? '拍卖' : '一口价' }}
              </span>
            </td>
            <td class="price-cell">¥{{ p.currentPrice || p.fixedPrice || 0 }}</td>
            <td>{{ p.viewCount }}</td>
            <td>
              <span class="status-badge" :class="p.status === 'ACTIVE' ? 'active' : p.status === 'PENDING' ? 'pending' : 'banned'">
                {{ p.status === 'ACTIVE' ? '正常' : p.status === 'PENDING' ? '待审核' : '已下架' }}
              </span>
            </td>
            <td>
              <div class="action-btns">
                <button v-if="p.status === 'PENDING'" class="act-sm approve" @click="audit(p.id, 'APPROVE')">通过</button>
                <button v-if="p.status !== 'BANNED'" class="act-sm danger" @click="audit(p.id, 'REJECT')">下架</button>
                <button class="act-sm edit" @click="handleEdit(p.id)">编辑</button>
                <button class="act-sm del" :disabled="deleting === p.id" @click="handleDelete(p.id)">
                  {{ deleting === p.id ? '删除中' : '删除' }}
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!loading && !products.length">
            <td colspan="8" style="text-align:center;padding:40px;color:var(--color-text-muted);">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="total > 20" class="pagination-wrap">
      <el-pagination v-model:current-page="page" :total="total" :page-size="20" @current-change="fetchProducts" layout="prev, pager, next" background />
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
.title-cell { max-width: 220px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price-cell { font-weight: 600; color: var(--color-danger); }

.type-badge {
  padding: 3px 10px; border-radius: var(--radius-full);
  font-size: .75em; font-weight: 600;
}
.type-badge.auction { background: rgba(245,158,11,.1); color: #f59e0b; }
.type-badge.sale { background: rgba(16,185,129,.1); color: #10b981; }

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
.act-sm.danger { background: rgba(239,68,68,.1); color: var(--color-danger); }
.act-sm.danger:hover { background: var(--color-danger); color: white; }
.act-sm.edit { background: rgba(99,102,241,.1); color: var(--color-primary); }
.act-sm.edit:hover { background: var(--color-primary); color: white; }
.act-sm.del { background: rgba(239,68,68,.1); color: var(--color-danger); }
.act-sm.del:hover:not(:disabled) { background: var(--color-danger); color: white; }
.act-sm:disabled { opacity: .5; cursor: not-allowed; }

.pagination-wrap { display: flex; justify-content: center; margin-top: var(--space-lg); }
</style>
