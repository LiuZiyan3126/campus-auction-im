<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/useUserStore'
import EmptyState from '@/components/EmptyState.vue'

const userStore = useUserStore()

interface OrderItem {
  id: number; productId: number; productTitle: string; productImage: string | null;
  buyerId: number; buyerName: string; sellerId: number; sellerName: string;
  amount: number; status: string; statusLabel: string;
  createdAt: string; paidAt: string | null; shippedAt: string | null; completedAt: string | null;
}

const orders = ref<OrderItem[]>([])
const loading = ref(true)
const activeTab = ref('')

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
</script>

<template>
  <div class="orders-page">
    <h2>我的订单</h2>
    <el-tabs v-model="activeTab" @tab-change="fetchOrders">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待付款" name="PENDING" />
      <el-tab-pane label="已付款" name="PAID" />
      <el-tab-pane label="已发货" name="SHIPPED" />
      <el-tab-pane label="已完成" name="COMPLETED" />
      <el-tab-pane label="已取消" name="CANCELLED" />
    </el-tabs>

    <div v-if="loading" style="text-align:center;padding:40px;color:#999;">加载中...</div>
    <EmptyState v-else-if="!orders.length" description="暂无订单" />
    <div v-else class="order-list">
      <el-card v-for="o in orders" :key="o.id" class="order-card">
        <div class="order-header">
          <span>{{ o.productTitle }}</span>
          <el-tag :type="o.status === 'COMPLETED' ? 'success' : o.status === 'CANCELLED' ? 'info' : 'warning'">{{ o.statusLabel }}</el-tag>
        </div>
        <p>金额: <strong>¥{{ o.amount }}</strong></p>
        <p class="order-meta">{{ isBuyer(o) ? '卖家' : '买家' }}: {{ isBuyer(o) ? o.sellerName : o.buyerName }} · {{ o.createdAt }}</p>
        <div class="order-actions">
          <template v-if="isBuyer(o)">
            <el-button v-if="o.status === 'PENDING'" type="primary" size="small" @click="doAction(o.id, 'pay', '确认支付 ¥' + o.amount)">立即支付</el-button>
            <el-button v-if="o.status === 'PENDING'" size="small" @click="doAction(o.id, 'cancel', '确认取消订单？')">取消订单</el-button>
            <el-button v-if="o.status === 'SHIPPED'" type="success" size="small" @click="doAction(o.id, 'confirm', '确认已收到商品？')">确认收货</el-button>
          </template>
          <template v-else>
            <el-button v-if="o.status === 'PAID'" type="primary" size="small" @click="doAction(o.id, 'ship', '确认已发货？')">标记发货</el-button>
          </template>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.orders-page { max-width: 700px; margin: 0 auto; padding: 20px; }
.orders-page h2 { margin-bottom: 16px; }
.order-card { margin-bottom: 14px; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; font-weight: bold; }
.order-meta { color: #999; font-size: 0.85em; margin-top: 4px; }
.order-actions { margin-top: 10px; display: flex; gap: 8px; }
</style>
