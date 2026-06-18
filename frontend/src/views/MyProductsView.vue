<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/useUserStore'
import type { PageVO, ProductCard } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const products = ref<ProductCard[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const statusFilter = ref('')
const deleting = ref<number | null>(null)

const statusTabs = [
  { label: '全部', value: '' },
  { label: '在售', value: 'ACTIVE' },
  { label: '已售出', value: 'SOLD' },
  { label: '待审核', value: 'PENDING' },
  { label: '已下架', value: 'BANNED' },
  { label: '已过期', value: 'EXPIRED' },
]

const statusLabel: Record<string, string> = {
  ACTIVE: '在售',
  SOLD: '已售出',
  PENDING: '待审核',
  BANNED: '已下架',
  EXPIRED: '已过期',
}

const statusClass: Record<string, string> = {
  ACTIVE: 'active',
  SOLD: 'sold',
  PENDING: 'pending',
  BANNED: 'banned',
  EXPIRED: 'expired',
}

onMounted(() => fetchProducts())

async function fetchProducts() {
  loading.value = true
  try {
    const res = await request.get<{ data: PageVO<ProductCard> }>('/products/my', {
      params: { page: page.value, size: 12, status: statusFilter.value || undefined }
    })
    products.value = res.data.data?.items || []
    total.value = res.data.data?.total || 0
  } catch { /* ignore */ }
  loading.value = false
}

function switchFilter(val: string) {
  statusFilter.value = val
  page.value = 1
  fetchProducts()
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

function priceText(p: ProductCard) {
  if (p.type === 'AUCTION') {
    return p.currentPrice != null ? `当前 ¥${p.currentPrice}` : `起拍 ¥${p.startPrice}`
  }
  return p.fixedPrice != null ? `¥${p.fixedPrice}` : '暂无价格'
}

function formatDate(d: string) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' })
}
</script>

<template>
  <div class="my-products-page">
    <div class="page-header">
      <h1>我发布的商品</h1>
      <button class="create-btn" @click="router.push('/product/create')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        发布新商品
      </button>
    </div>

    <!-- Status filter tabs -->
    <div class="filter-tabs">
      <button
        v-for="tab in statusTabs" :key="tab.value"
        class="filter-tab"
        :class="{ active: statusFilter === tab.value }"
        @click="switchFilter(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Products grid -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="!products.length" class="empty-state">
      <p class="empty-icon">📦</p>
      <p class="empty-title">还没有发布任何商品</p>
      <p class="empty-desc">点击上方按钮发布您的第一件商品</p>
    </div>

    <div v-else class="products-grid">
      <div v-for="p in products" :key="p.id" class="product-card card">
        <div class="card-img" @click="handleEdit(p.id)">
          <img v-if="p.coverImage" :src="p.coverImage" :alt="p.title" />
          <div v-else class="img-placeholder">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
          </div>
          <span class="card-type-badge" :class="p.type === 'AUCTION' ? 'auction' : 'sale'">
            {{ p.type === 'AUCTION' ? '拍卖' : '一口价' }}
          </span>
          <span class="card-status-badge" :class="statusClass[p.status] || 'pending'">
            {{ statusLabel[p.status] || p.status }}
          </span>
        </div>
        <div class="card-body">
          <h3 class="card-title" @click="handleEdit(p.id)">{{ p.title }}</h3>
          <div class="card-meta">
            <span class="card-price">{{ priceText(p) }}</span>
            <span class="card-views">{{ p.viewCount }} 浏览</span>
          </div>
          <div class="card-footer">
            <span class="card-date">{{ formatDate(p.createdAt) }}</span>
            <div class="card-actions">
              <button class="action-btn edit" @click="handleEdit(p.id)" title="编辑">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                编辑
              </button>
              <button class="action-btn delete" @click="handleDelete(p.id)" :disabled="deleting === p.id" title="删除">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                {{ deleting === p.id ? '删除中...' : '删除' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="total > 12" class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="12"
        @current-change="fetchProducts"
        layout="prev, pager, next"
        background
      />
    </div>
  </div>
</template>

<style scoped>
.my-products-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: var(--space-lg);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
  flex-wrap: wrap;
  gap: 12px;
}
.page-header h1 {
  font-size: 1.4em;
  font-weight: 700;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: .9em;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition-fast);
}
.create-btn:hover { background: var(--color-primary-dark); }

/* Filter tabs */
.filter-tabs {
  display: flex;
  gap: 4px;
  background: var(--color-bg);
  border-radius: var(--radius-md);
  padding: 3px;
  margin-bottom: var(--space-lg);
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}
.filter-tab {
  padding: 8px 16px;
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  font-size: .85em;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
  font-family: inherit;
  white-space: nowrap;
}
.filter-tab:hover { color: var(--color-text); }
.filter-tab.active {
  background: var(--color-surface);
  color: var(--color-text);
  font-weight: 600;
  box-shadow: var(--shadow-xs);
}

/* Loading */
.loading-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--color-text-muted);
}
.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin .8s linear infinite;
  margin: 0 auto 16px;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Empty */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}
.empty-icon { font-size: 3em; margin-bottom: 12px; }
.empty-title { font-size: 1.1em; font-weight: 600; color: var(--color-text); margin-bottom: 6px; }
.empty-desc { font-size: .9em; color: var(--color-text-muted); }

/* Grid */
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.product-card {
  overflow: hidden;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}
.product-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

/* Card image */
.card-img {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  cursor: pointer;
  background: var(--color-bg);
}
.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}
.product-card:hover .card-img img { transform: scale(1.05); }
.img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
}

.card-type-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: .7em;
  font-weight: 600;
  color: white;
}
.card-type-badge.auction { background: rgba(245,158,11,.85); }
.card-type-badge.sale { background: rgba(16,185,129,.85); }

.card-status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: .7em;
  font-weight: 600;
}
.card-status-badge.active { background: rgba(16,185,129,.85); color: white; }
.card-status-badge.sold { background: rgba(99,102,241,.85); color: white; }
.card-status-badge.pending { background: rgba(245,158,11,.85); color: white; }
.card-status-badge.banned { background: rgba(239,68,68,.85); color: white; }
.card-status-badge.expired { background: rgba(107,114,128,.85); color: white; }

/* Card body */
.card-body { padding: 16px; }
.card-title {
  font-size: .95em;
  font-weight: 600;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 10px;
  color: var(--color-text);
}
.card-title:hover { color: var(--color-primary); }

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.card-price {
  font-size: 1.05em;
  font-weight: 700;
  color: var(--color-danger);
}
.card-views {
  font-size: .8em;
  color: var(--color-text-muted);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-light);
}
.card-date {
  font-size: .8em;
  color: var(--color-text-muted);
}

.card-actions {
  display: flex;
  gap: 6px;
}
.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: .75em;
  font-weight: 500;
  cursor: pointer;
  background: var(--color-surface);
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
  font-family: inherit;
}
.action-btn.edit:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-bg);
}
.action-btn.delete:hover {
  border-color: var(--color-danger);
  color: var(--color-danger);
  background: var(--color-danger-bg);
}
.action-btn:disabled {
  opacity: .5;
  cursor: not-allowed;
}

/* Pagination */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: var(--space-xl);
}

@media (max-width: 640px) {
  .my-products-page { padding: var(--space-md); }
  .products-grid { grid-template-columns: 1fr; }
  .filter-tabs { gap: 2px; }
  .filter-tab { padding: 6px 12px; font-size: .8em; }
}
</style>
