<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/useUserStore'
import { useAuction } from '@/composables/useAuction'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const productId = Number(route.params.id)

interface ProductDetail {
  id: number; sellerId: number; sellerName: string; sellerAvatar: string | null
  title: string; description: string; categoryName: string
  type: 'SALE' | 'AUCTION'; startPrice: number | null; fixedPrice: number | null
  currentPrice: number | null; status: string; auctionStart: string | null
  auctionEnd: string | null; winnerId: number | null; winnerName: string | null
  viewCount: number; images: string[]; createdAt: string
}

const product = ref<ProductDetail | null>(null)
const loading = ref(true)
const favorited = ref(false)
const mainImage = ref('')

const {
  currentPrice, bids, winner, expired,
  fetchCurrentPrice, fetchBidHistory, placeBid, connectAuction, disconnect
} = useAuction(productId)

const remaining = ref('')
let timer: ReturnType<typeof setInterval> | null = null

function updateCountdown() {
  if (!product.value?.auctionEnd) return
  const diff = new Date(product.value.auctionEnd).getTime() - Date.now()
  if (diff <= 0) { remaining.value = '已结束'; stopTimer(); return }
  const h = Math.floor(diff / 3600000)
  const m = Math.floor((diff % 3600000) / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  remaining.value = `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function stopTimer() { if (timer) { clearInterval(timer); timer = null } }

const isSeller = computed(() => userStore.userInfo?.id === product.value?.sellerId)
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')
const canManage = computed(() => isSeller.value || isAdmin.value)
const isActive = computed(() => product.value?.status === 'ACTIVE')
const displayPrice = computed(() => currentPrice.value || product.value?.currentPrice || product.value?.startPrice)

const editing = ref(false)
const editForm = ref({ title: '', description: '', fixedPrice: null as number | null, startPrice: null as number | null })
const saving = ref(false)
const deleting = ref(false)

function startEdit() {
  if (!product.value) return
  editForm.value = {
    title: product.value.title,
    description: product.value.description,
    fixedPrice: product.value.fixedPrice,
    startPrice: product.value.startPrice,
  }
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function saveEdit() {
  if (!product.value) return
  saving.value = true
  try {
    const res = await request.put(`/products/${productId}`, editForm.value)
    product.value = res.data.data
    editing.value = false
    ElMessage.success('保存成功')
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err.response?.data?.message || '保存失败')
  }
  saving.value = false
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？删除后无法恢复。', '确认删除', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning',
    })
  } catch { return }
  deleting.value = true
  try {
    await request.delete(`/products/${productId}`)
    ElMessage.success('已删除')
    router.push('/my-products')
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
  deleting.value = false
}

onMounted(async () => {
  try {
    const res = await request.get(`/products/${productId}`)
    product.value = res.data.data
    mainImage.value = product.value?.images?.[0] || ''
    if (product.value?.type === 'AUCTION') {
      connectAuction()
      if (product.value.auctionEnd) {
        updateCountdown()
        timer = setInterval(updateCountdown, 1000)
      }
      await fetchCurrentPrice()
      await fetchBidHistory()
    }
  } catch { /* pass */ }
  loading.value = false
})

onUnmounted(() => { stopTimer(); disconnect() })

async function toggleFav() {
  try {
    const res = await request.post(`/products/${productId}/favorite`)
    favorited.value = res.data.data
    ElMessage.success(favorited.value ? '已收藏' : '已取消收藏')
  } catch { /* ignore */ }
}

function goBuyNow() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); router.push('/login'); return }
  ElMessageBox.confirm(
    `确认以 ¥${product.value?.fixedPrice} 购买「${product.value?.title}」？`,
    '确认购买',
    { confirmButtonText: '确认购买', type: 'info' }
  ).then(async () => {
    try {
      await request.post('/orders', { productId: product.value!.id })
      ElMessage.success('下单成功！请前往订单页支付')
      router.push('/orders')
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } }
      ElMessage.error(err.response?.data?.message || '下单失败')
    }
  }).catch(() => {})
}

const bidAmount = ref(0)
</script>

<template>
  <div v-if="loading" style="display:flex;justify-content:center;padding:80px;">
    <div class="loading-spin"></div>
  </div>
  <div v-else-if="!product" style="padding:80px 0">
    <EmptyState description="商品不存在或已下架" />
  </div>

  <div v-else class="detail-page">
    <!-- Breadcrumb -->
    <div class="breadcrumb">
      <a @click="router.push('/')">首页</a>
      <span>/</span>
      <span>{{ product.categoryName }}</span>
      <span>/</span>
      <span>{{ product.title }}</span>
    </div>

    <div class="detail-grid">
      <!-- Gallery -->
      <div class="gallery">
        <div class="main-image-wrap">
          <img :src="mainImage || '/placeholder.svg'" :alt="product.title" class="main-image" />
          <span v-if="product.type === 'AUCTION'" class="type-badge auction">拍卖</span>
          <span v-else class="type-badge sale">一口价</span>
        </div>
        <div class="thumb-strip" v-if="product.images?.length > 1">
          <button
            v-for="(img, i) in product.images" :key="i"
            class="thumb-btn" :class="{ active: mainImage === img }"
            @click="mainImage = img"
          >
            <img :src="img" :alt="`图片 ${i + 1}`" />
          </button>
        </div>
      </div>

      <!-- Info -->
      <div class="info-panel">
        <h1 class="product-title">{{ product.title }}</h1>
        <div class="product-meta">
          <span class="meta-item">{{ product.categoryName }}</span>
          <span class="meta-sep">·</span>
          <span class="meta-item">{{ product.viewCount }} 次浏览</span>
          <span class="meta-sep">·</span>
          <span class="meta-item">{{ product.createdAt }}</span>
        </div>

        <!-- Auction mode -->
        <template v-if="product.type === 'AUCTION'">
          <div class="price-card">
            <div class="price-main">
              <span class="price-label">当前价</span>
              <span class="price-value">¥{{ displayPrice }}</span>
            </div>
            <div class="price-sub">
              <span>起拍价 ¥{{ product.startPrice }}</span>
            </div>

            <!-- Countdown -->
            <div v-if="isActive && remaining && remaining !== '已结束'" class="countdown-bar">
              <div class="countdown-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              </div>
              <div class="countdown-text">
                <span class="countdown-label">剩余时间</span>
                <span class="countdown-value">{{ remaining }}</span>
              </div>
            </div>
            <div v-else-if="remaining === '已结束'" class="countdown-bar ended">
              <span>拍卖已结束</span>
            </div>
          </div>

          <!-- Winner -->
          <div v-if="winner" class="result-card success">
            <span class="result-icon">🎉</span>
            <div>
              <strong>成交！</strong>
              <p>得主：{{ winner.winnerName }} · 成交价：¥{{ winner.finalPrice }}</p>
            </div>
          </div>
          <div v-if="expired" class="result-card warning">
            <span class="result-icon">⏰</span>
            <div>
              <strong>流拍</strong>
              <p>拍卖结束，无人出价</p>
            </div>
          </div>

          <!-- Bid area -->
          <div v-if="isActive && product.auctionEnd && new Date(product.auctionEnd) > new Date() && !isSeller" class="bid-area">
            <div class="bid-input">
              <span class="bid-currency">¥</span>
              <input
                v-model.number="bidAmount"
                type="number"
                :min="(displayPrice || 0) + 1"
                step="0.01"
                placeholder="输入出价金额"
              />
            </div>
            <button class="bid-btn" @click="placeBid(bidAmount)">出价</button>
            <span class="bid-hint">最小出价：¥{{ ((displayPrice || 0) + 1).toFixed(2) }}</span>
          </div>
          <div v-if="isSeller" class="seller-notice">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            这是您发布的商品，不能参与竞拍
          </div>

          <!-- Bid history -->
          <div class="bid-history" v-if="bids.length">
            <h3>出价记录 <span class="count">{{ bids.length }}</span></h3>
            <div class="bid-list">
              <div v-for="b in bids" :key="b.id" class="bid-row">
                <div class="bid-user">
                  <div class="bid-avatar">{{ b.bidderName?.[0] }}</div>
                  <span>{{ b.bidderName }}</span>
                </div>
                <span class="bid-amount">¥{{ b.bidAmount }}</span>
                <span class="bid-time">{{ b.bidTime }}</span>
              </div>
            </div>
          </div>
        </template>

        <!-- Fixed price mode -->
        <template v-else>
          <div class="price-card">
            <div class="price-main">
              <span class="price-label">价格</span>
              <span class="price-value">¥{{ product.fixedPrice }}</span>
            </div>
          </div>
          <button class="buy-btn" v-if="isActive && !isSeller" @click="goBuyNow">
            立即购买
          </button>
        </template>

        <!-- Seller card -->
        <div class="seller-section">
          <div class="seller-card" @click="router.push(`/user/${product.sellerId}`)">
            <div class="seller-avatar-lg">{{ product.sellerName?.[0] }}</div>
            <div class="seller-info">
              <span class="seller-name">{{ product.sellerName }}</span>
              <span class="seller-label">卖家</span>
            </div>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
          </div>
          <div class="seller-actions">
            <button class="action-btn" v-if="!isSeller && userStore.isLoggedIn" @click="router.push(`/chat/${product.sellerId}`)">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              联系卖家
            </button>
            <button class="action-btn" :class="{ favorited }" v-if="userStore.isLoggedIn" @click="toggleFav">
              <svg width="16" height="16" viewBox="0 0 24 24" :fill="favorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
              {{ favorited ? '已收藏' : '收藏' }}
            </button>
          </div>
        </div>

        <!-- Management for seller / admin -->
        <div v-if="canManage" class="manage-section">
          <div class="manage-buttons">
            <button v-if="!editing" class="mgmt-btn edit" @click="startEdit">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              编辑商品
            </button>
            <button class="mgmt-btn delete" :disabled="deleting" @click="handleDelete">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
              {{ deleting ? '删除中...' : '删除商品' }}
            </button>
          </div>

          <!-- Inline edit form -->
          <div v-if="editing" class="edit-form">
            <div class="form-group">
              <label>标题</label>
              <input v-model="editForm.title" maxlength="200" placeholder="商品标题" />
            </div>
            <div class="form-group">
              <label>描述</label>
              <textarea v-model="editForm.description" rows="4" maxlength="2000" placeholder="商品描述"></textarea>
            </div>
            <div class="form-row">
              <div class="form-group" v-if="product.type === 'AUCTION'">
                <label>起拍价</label>
                <div class="price-input">
                  <span>¥</span>
                  <input v-model.number="editForm.startPrice" type="number" min="0" step="0.01" />
                </div>
              </div>
              <div class="form-group" v-else>
                <label>价格</label>
                <div class="price-input">
                  <span>¥</span>
                  <input v-model.number="editForm.fixedPrice" type="number" min="0" step="0.01" />
                </div>
              </div>
            </div>
            <div class="edit-actions">
              <button class="save-btn" :disabled="saving" @click="saveEdit">
                <span v-if="saving" class="btn-spin"></span>
                <span v-else>保存修改</span>
              </button>
              <button class="cancel-btn" @click="cancelEdit">取消</button>
            </div>
          </div>
        </div>

        <!-- Description -->
        <div class="desc-section">
          <h3>商品描述</h3>
          <p>{{ product.description || '卖家很懒，什么都没写...' }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page { max-width: 1100px; margin: 0 auto; }
.breadcrumb {
  font-size: .85em;
  color: var(--color-text-muted);
  margin-bottom: var(--space-lg);
  display: flex; align-items: center; gap: 8px;
}
.breadcrumb a { color: var(--color-text-secondary); cursor: pointer; }
.breadcrumb a:hover { color: var(--color-primary); }

.detail-grid { display: flex; gap: 32px; }

/* Gallery */
.gallery { flex: 1; max-width: 500px; }
.main-image-wrap {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--color-bg);
  aspect-ratio: 4/3;
}
.main-image { width: 100%; height: 100%; object-fit: cover; }

.type-badge {
  position: absolute; top: 12px; left: 12px;
  padding: 5px 14px; border-radius: var(--radius-full);
  font-size: .8em; font-weight: 600; color: white;
}
.type-badge.auction { background: rgba(245,158,11,.9); }
.type-badge.sale { background: rgba(16,185,129,.9); }

.thumb-strip { display: flex; gap: 8px; margin-top: 12px; }
.thumb-btn {
  width: 64px; height: 64px; border-radius: var(--radius-md);
  overflow: hidden; border: 2px solid transparent;
  cursor: pointer; padding: 0; transition: all var(--transition-fast);
}
.thumb-btn img { width: 100%; height: 100%; object-fit: cover; }
.thumb-btn.active { border-color: var(--color-primary); }
.thumb-btn:hover { opacity: .85; }

/* Info */
.info-panel { flex: 1; }
.product-title { font-size: 1.6em; font-weight: 700; line-height: 1.3; margin-bottom: 8px; }
.product-meta { font-size: .85em; color: var(--color-text-muted); margin-bottom: var(--space-lg); display: flex; gap: 6px; align-items: center; }
.meta-sep { color: var(--color-border); }

/* Price */
.price-card {
  background: linear-gradient(135deg, var(--color-primary-bg), white);
  border: 1px solid var(--color-primary-lighter);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: var(--space-md);
}
.price-main { display: flex; align-items: baseline; gap: 12px; }
.price-label { font-size: .9em; color: var(--color-text-muted); }
.price-value { font-size: 2.2em; font-weight: 800; color: var(--color-danger); }
.price-sub { font-size: .85em; color: var(--color-text-muted); margin-top: 4px; }

/* Countdown */
.countdown-bar {
  display: flex; align-items: center; gap: 12px;
  margin-top: 14px; padding: 12px;
  background: rgba(245,158,11,.08);
  border-radius: var(--radius-md);
}
.countdown-icon { color: var(--color-accent); display: flex; }
.countdown-text { display: flex; flex-direction: column; }
.countdown-label { font-size: .75em; color: var(--color-text-muted); }
.countdown-value { font-size: 1.1em; font-weight: 700; color: var(--color-accent); font-family: var(--font-mono); }
.countdown-bar.ended { justify-content: center; color: var(--color-text-muted); }

/* Results */
.result-card {
  display: flex; align-items: center; gap: 12px;
  padding: 14px; border-radius: var(--radius-md); margin-bottom: var(--space-md);
}
.result-card.success { background: var(--color-success-bg); border: 1px solid rgba(16,185,129,.2); }
.result-card.warning { background: var(--color-warning-bg); border: 1px solid rgba(245,158,11,.2); }
.result-icon { font-size: 1.5em; }
.result-card strong { display: block; font-size: .95em; }
.result-card p { font-size: .85em; color: var(--color-text-secondary); margin-top: 2px; }

/* Bid area */
.bid-area {
  margin: var(--space-md) 0;
}
.bid-input {
  display: flex; align-items: center;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: border-color var(--transition-fast);
}
.bid-input:focus-within { border-color: var(--color-primary); }
.bid-currency {
  padding: 0 14px;
  font-size: 1.1em; font-weight: 600;
  color: var(--color-text-muted);
  background: var(--color-bg);
  align-self: stretch; display: flex; align-items: center;
}
.bid-input input {
  flex: 1; border: none; outline: none;
  padding: 14px; font-size: 1.1em;
  font-family: inherit; color: var(--color-text);
}
.bid-btn {
  display: block; width: 100%;
  margin-top: 10px;
  padding: 14px;
  background: linear-gradient(135deg, var(--color-danger), #dc2626);
  color: white; border: none;
  border-radius: var(--radius-md);
  font-size: 1.05em; font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit;
}
.bid-btn:hover { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(239,68,68,.35); }
.bid-hint { display: block; margin-top: 6px; font-size: .75em; color: var(--color-text-muted); }

.seller-notice {
  display: flex; align-items: center; gap: 8px;
  padding: 12px; background: var(--color-bg);
  border-radius: var(--radius-md);
  color: var(--color-text-muted); font-size: .9em; margin: var(--space-md) 0;
}

/* Buy button */
.buy-btn {
  width: 100%; padding: 14px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: white; border: none;
  border-radius: var(--radius-md);
  font-size: 1.05em; font-weight: 700;
  cursor: pointer; margin: var(--space-md) 0;
  transition: all var(--transition-fast);
  font-family: inherit;
}
.buy-btn:hover { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(99,102,241,.35); }

/* Seller */
.seller-section { margin-top: var(--space-lg); }
.seller-card {
  display: flex; align-items: center; gap: 12px;
  padding: 14px; border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg); cursor: pointer;
  transition: all var(--transition-fast);
}
.seller-card:hover { border-color: var(--color-primary-light); background: var(--color-primary-bg); }
.seller-avatar-lg {
  width: 44px; height: 44px; border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: white; display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 1.1em;
}
.seller-info { flex: 1; display: flex; flex-direction: column; }
.seller-name { font-weight: 600; }
.seller-label { font-size: .8em; color: var(--color-text-muted); }

.seller-actions { display: flex; gap: 8px; margin-top: 10px; }
.action-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 18px; border: 1px solid var(--color-border);
  border-radius: var(--radius-md); background: var(--color-surface);
  font-size: .85em; cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit; color: var(--color-text-secondary);
}
.action-btn:hover { border-color: var(--color-primary); color: var(--color-primary); background: var(--color-primary-bg); }
.action-btn.favorited { color: var(--color-danger); border-color: rgba(239,68,68,.3); background: var(--color-danger-bg); }

/* Bid history */
.bid-history { margin-top: var(--space-lg); }
.bid-history h3 { font-size: 1em; margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
.count {
  font-size: .8em; background: var(--color-bg);
  padding: 2px 8px; border-radius: var(--radius-full);
  font-weight: 400; color: var(--color-text-secondary);
}
.bid-list { max-height: 300px; overflow-y: auto; }
.bid-row {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 0; border-bottom: 1px solid var(--color-border-light);
}
.bid-user { display: flex; align-items: center; gap: 8px; flex: 1; font-size: .9em; }
.bid-avatar {
  width: 28px; height: 28px; border-radius: var(--radius-full);
  background: var(--color-bg); color: var(--color-text-secondary);
  display: flex; align-items: center; justify-content: center;
  font-size: .8em; font-weight: 600;
}
.bid-amount { font-weight: 700; color: var(--color-danger); }
.bid-time { font-size: .8em; color: var(--color-text-muted); }

/* Management */
.manage-section { margin-top: var(--space-lg); }
.manage-buttons { display: flex; gap: 8px; margin-bottom: var(--space-md); }
.mgmt-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 18px; border: 1px solid var(--color-border);
  border-radius: var(--radius-md); background: var(--color-surface);
  font-size: .85em; cursor: pointer; font-family: inherit;
  transition: all var(--transition-fast); color: var(--color-text-secondary);
}
.mgmt-btn.edit:hover { border-color: var(--color-primary); color: var(--color-primary); background: var(--color-primary-bg); }
.mgmt-btn.delete:hover { border-color: var(--color-danger); color: var(--color-danger); background: var(--color-danger-bg); }
.mgmt-btn:disabled { opacity: .5; cursor: not-allowed; }

.edit-form {
  background: var(--color-bg);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 20px;
  margin-bottom: var(--space-md);
}
.edit-form .form-group { margin-bottom: 14px; }
.edit-form .form-group label { display: block; font-size: .85em; font-weight: 600; margin-bottom: 6px; color: var(--color-text); }
.edit-form .form-group input,
.edit-form .form-group textarea {
  width: 100%; padding: 10px 14px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md); font-size: .9em;
  outline: none; font-family: inherit; color: var(--color-text);
  background: var(--color-surface); transition: border-color var(--transition-fast);
}
.edit-form .form-group input:focus,
.edit-form .form-group textarea:focus { border-color: var(--color-primary); }

.form-row { display: flex; gap: 16px; }
.form-row .form-group { flex: 1; }
.price-input {
  display: flex; align-items: center;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md); overflow: hidden;
  background: var(--color-surface);
}
.price-input span {
  padding: 0 14px; font-weight: 600; color: var(--color-text-muted);
}
.price-input input {
  flex: 1; border: none; outline: none;
  padding: 10px 10px 10px 0; font-size: .9em;
  font-family: inherit; background: transparent;
}

.edit-actions { display: flex; gap: 10px; margin-top: 16px; }
.save-btn {
  padding: 10px 24px; background: var(--color-primary); color: white;
  border: none; border-radius: var(--radius-md); font-size: .9em;
  font-weight: 600; cursor: pointer; font-family: inherit;
  transition: all var(--transition-fast); display: flex; align-items: center; gap: 6px;
}
.save-btn:hover:not(:disabled) { background: var(--color-primary-dark); }
.save-btn:disabled { opacity: .7; cursor: not-allowed; }
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

/* Description */
.desc-section { margin-top: var(--space-lg); padding-top: var(--space-lg); border-top: 1px solid var(--color-border-light); }
.desc-section h3 { font-size: 1em; margin-bottom: 10px; }
.desc-section p { font-size: .9em; color: var(--color-text-secondary); line-height: 1.7; white-space: pre-wrap; }

/* Loading */
.loading-spin {
  width: 36px; height: 36px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .detail-grid { flex-direction: column; }
  .gallery { max-width: 100%; }
  .product-title { font-size: 1.3em; }
  .price-value { font-size: 1.6em; }
}
</style>
