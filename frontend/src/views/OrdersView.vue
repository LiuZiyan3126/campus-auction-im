<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/useUserStore'
import EmptyState from '@/components/EmptyState.vue'

const userStore = useUserStore()

interface OrderItem {
  id: number; productId: number; productTitle: string; productImage: string | null
  buyerId: number; buyerName: string; sellerId: number; sellerName: string
  amount: number; status: string; statusLabel: string
  createdAt: string; paidAt: string | null; shippedAt: string | null; completedAt: string | null
}

const orders = ref<OrderItem[]>([])
const loading = ref(true)
const activeTab = ref('')

const statusTabs = [
  { key: '', label: '全部' },
  { key: 'PENDING', label: '待付款' },
  { key: 'PAID', label: '已付款' },
  { key: 'SHIPPED', label: '已发货' },
  { key: 'COMPLETED', label: '已完成' },
  { key: 'CANCELLED', label: '已取消' },
]

const statusConfig: Record<string, { color: string; bg: string }> = {
  PENDING: { color: '#f59e0b', bg: '#fffbeb' },
  PAID: { color: '#6366f1', bg: '#eef2ff' },
  SHIPPED: { color: '#06b6d4', bg: '#ecfeff' },
  COMPLETED: { color: '#10b981', bg: '#ecfdf5' },
  CANCELLED: { color: '#94a3b8', bg: '#f1f5f9' },
}

onMounted(() => fetchOrders())

async function fetchOrders() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: 1, size: 50 }
    if (activeTab.value) params.status = activeTab.value
    const res = await request.get('/orders', { params })
    orders.value = res.data.data?.items || []
  } catch { /* ignore */ }
  loading.value = false
}

async function doAction(orderId: number, action: string, confirmText: string) {
  try {
    await ElMessageBox.confirm(confirmText, '确认操作', { type: 'warning' })
    await request.post(`/orders/${orderId}/${action}`)
    ElMessage.success('操作成功')
    fetchOrders()
  } catch { /* cancelled */ }
}

function isBuyer(order: OrderItem) { return order.buyerId === userStore.userInfo?.id }

function getStatusStyle(status: string) {
  return statusConfig[status] || { color: '#94a3b8', bg: '#f1f5f9' }
}
</script>

<template>
  <div class="orders-page">
    <div class="page-header">
      <h1>我的订单</h1>
    </div>

    <!-- Tabs -->
    <div class="tab-bar">
      <button
        v-for="tab in statusTabs" :key="tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key; fetchOrders()"
      >{{ tab.label }}</button>
    </div>

    <!-- Content -->
    <div v-if="loading" style="text-align:center;padding:60px;color:var(--color-text-muted);">
      <div class="spinner"></div>
    </div>
    <EmptyState v-else-if="!orders.length" icon="empty" description="暂无订单" />

    <div v-else class="order-list">
      <div v-for="o in orders" :key="o.id" class="order-card card">
        <div class="order-top">
          <div class="order-product">
            <div class="product-img" v-if="o.productImage">
              <img :src="o.productImage" alt="" />
            </div>
            <div class="product-img placeholder" v-else>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/></svg>
            </div>
            <div>
              <h4 class="product-title">{{ o.productTitle }}</h4>
              <span class="order-role">{{ isBuyer(o) ? '买家' : '卖家' }} · {{ isBuyer(o) ? o.sellerName : o.buyerName }}</span>
            </div>
          </div>
          <div class="order-amount">
            <span class="amount-label">金额</span>
            <span class="amount-value">¥{{ o.amount }}</span>
          </div>
        </div>

        <div class="order-bottom">
          <div class="order-info">
            <span
              class="status-tag"
              :style="{ background: getStatusStyle(o.status).bg, color: getStatusStyle(o.status).color }"
            >{{ o.statusLabel }}</span>
            <span class="order-date">{{ o.createdAt }}</span>
          </div>

          <div class="order-actions">
            <template v-if="isBuyer(o)">
              <button v-if="o.status === 'PENDING'" class="act-btn primary" @click="doAction(o.id, 'pay', '确认支付 ¥' + o.amount)">立即支付</button>
              <button v-if="o.status === 'PENDING'" class="act-btn ghost" @click="doAction(o.id, 'cancel', '确认取消订单？')">取消</button>
              <button v-if="o.status === 'SHIPPED'" class="act-btn success" @click="doAction(o.id, 'confirm', '确认已收到商品？')">确认收货</button>
            </template>
            <template v-else>
              <button v-if="o.status === 'PAID'" class="act-btn primary" @click="doAction(o.id, 'ship', '确认已发货？')">标记发货</button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.orders-page { max-width: 720px; margin: 0 auto; }
.page-header { margin-bottom: var(--space-lg); }
.page-header h1 { font-size: 1.5em; font-weight: 700; }

/* Tabs */
.tab-bar {
  display: flex; gap: 4px;
  background: var(--color-bg);
  border-radius: var(--radius-lg);
  padding: 4px; margin-bottom: var(--space-lg);
  overflow-x: auto;
}
.tab-btn {
  padding: 8px 18px; border: none;
  background: transparent; border-radius: var(--radius-md);
  font-size: .875em; cursor: pointer; white-space: nowrap;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
  font-family: inherit;
}
.tab-btn:hover { color: var(--color-text); }
.tab-btn.active {
  background: var(--color-surface); color: var(--color-text);
  font-weight: 600; box-shadow: var(--shadow-xs);
}

/* Spinner */
.spinner {
  width: 30px; height: 30px; margin: 0 auto;
  border: 2.5px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Order list */
.order-list { display: flex; flex-direction: column; gap: 12px; }

.order-card {
  padding: 20px;
  border: 1px solid var(--color-border-light);
}

.order-top {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 14px;
}
.order-product { display: flex; align-items: center; gap: 12px; flex: 1; }
.product-img {
  width: 48px; height: 48px; border-radius: var(--radius-md);
  overflow: hidden; background: var(--color-bg);
  flex-shrink: 0;
}
.product-img img { width: 100%; height: 100%; object-fit: cover; }
.product-img.placeholder {
  display: flex; align-items: center; justify-content: center;
  color: var(--color-text-muted);
}
.product-title { font-size: .95em; font-weight: 600; margin-bottom: 2px; }
.order-role { font-size: .8em; color: var(--color-text-muted); }

.order-amount { text-align: right; }
.amount-label { display: block; font-size: .75em; color: var(--color-text-muted); }
.amount-value { font-size: 1.15em; font-weight: 700; color: var(--color-danger); }

.order-bottom { display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px solid var(--color-border-light); }
.order-info { display: flex; align-items: center; gap: 10px; }
.status-tag {
  padding: 3px 10px; border-radius: var(--radius-full);
  font-size: .75em; font-weight: 600;
}
.order-date { font-size: .8em; color: var(--color-text-muted); }

.order-actions { display: flex; gap: 8px; }
.act-btn {
  padding: 7px 16px; border-radius: var(--radius-md);
  font-size: .8em; font-weight: 500; cursor: pointer;
  border: none; transition: all var(--transition-fast);
  font-family: inherit;
}
.act-btn.primary { background: var(--color-primary); color: white; }
.act-btn.primary:hover { background: var(--color-primary-dark); }
.act-btn.success { background: var(--color-success); color: white; }
.act-btn.success:hover { opacity: .9; }
.act-btn.ghost { background: transparent; border: 1px solid var(--color-border); color: var(--color-text-secondary); }
.act-btn.ghost:hover { background: var(--color-bg); }
</style>
