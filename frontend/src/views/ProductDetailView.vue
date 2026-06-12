<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { ProductCard } from '@/types'
import request from '@/api/request'
import { useUserStore } from '@/stores/useUserStore'
import { useAuction } from '@/composables/useAuction'
import { logger } from '@/utils/logger'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const productId = Number(route.params.id)

interface ProductDetail {
  id: number; sellerId: number; sellerName: string; sellerAvatar: string | null;
  title: string; description: string; categoryName: string;
  type: 'SALE' | 'AUCTION'; startPrice: number | null; fixedPrice: number | null;
  currentPrice: number | null; status: string; auctionStart: string | null;
  auctionEnd: string | null; winnerId: number | null; winnerName: string | null;
  viewCount: number; images: string[]; createdAt: string;
}

const product = ref<ProductDetail | null>(null)
const loading = ref(true)
const favorited = ref(false)
const mainImage = ref('')

const {
  currentPrice, bids, winner, expired,
  fetchCurrentPrice, fetchBidHistory, placeBid, connectAuction, disconnect
} = useAuction(productId)

// 倒计时
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
const isActive = computed(() => product.value?.status === 'ACTIVE')

onMounted(async () => {
  try {
    const res = await request.get(`/products/${productId}`)
    product.value = res.data.data
    mainImage.value = product.value?.images?.[0] || ''
    if (product.value?.type === 'AUCTION') {
      connectAuction()
      fetchCurrentPrice()
      fetchBidHistory()
      if (product.value.auctionEnd) {
        timer = setInterval(updateCountdown, 1000)
        updateCountdown()
      }
    }
  } catch { /* fall through */ }
  loading.value = false
})

onUnmounted(() => { stopTimer(); disconnect() })

async function toggleFav() {
  try {
    const res = await request.post(`/products/${productId}/favorite`)
    favorited.value = res.data.data
  } catch { /* ignore */ }
}

function goToSellerProfile() {
  if (product.value) router.push(`/user/${product.value.sellerId}`)
}

function goToChat() {
  if (product.value) router.push(`/chat/${product.value.sellerId}`)
}

function buyNow() {
  ElMessage.info('购买功能将在订单模块实现')
}
</script>

<template>
  <div v-if="loading" style="text-align:center;padding:60px;"><el-icon :size="32" class="is-loading"><span>⟳</span></el-icon></div>
  <div v-else-if="!product" style="text-align:center;padding:60px;color:#999;">商品不存在</div>
  <div v-else class="detail-page">
    <!-- 图片区域 -->
    <div class="images">
      <img :src="mainImage || '/placeholder.svg'" class="main-image" />
      <div class="thumb-list" v-if="product.images?.length > 1">
        <img v-for="(img, i) in product.images" :key="i" :src="img" class="thumb" :class="{ active: mainImage === img }" @click="mainImage = img" />
      </div>
    </div>

    <!-- 商品信息 -->
    <div class="info">
      <h2>{{ product.title }}</h2>
      <p class="meta">{{ product.categoryName }} · 浏览 {{ product.viewCount }} · {{ product.createdAt }}</p>

      <!-- 拍卖模式 -->
      <template v-if="product.type === 'AUCTION'">
        <div class="price-box">
          <span class="label">当前价</span>
          <span class="price">¥{{ currentPrice || product.currentPrice || product.startPrice }}</span>
          <span class="label" style="margin-left:20px">起拍价 ¥{{ product.startPrice }}</span>
        </div>
        <div class="countdown" v-if="isActive && remaining">
          <span v-if="remaining !== '已结束'">⏱ 剩余 {{ remaining }}</span>
          <span v-else>拍卖已结束</span>
        </div>
        <div v-if="winner" class="winner-box">
          🎉 成交！得主：{{ winner.winnerName }} 成交价：¥{{ winner.finalPrice }}
        </div>
        <div v-if="expired" class="expired-box">该拍卖已流拍（无人出价）</div>

        <!-- 出价 -->
        <div v-if="isActive && product.auctionEnd && new Date(product.auctionEnd) > new Date() && !isSeller" class="bid-area">
          <el-input-number v-model="bidAmount" :min="(currentPrice || product.startPrice || 0) + 1" :precision="2" />
          <el-button type="danger" @click="placeBid(bidAmount)" style="margin-left:10px">出价</el-button>
        </div>
        <div v-if="isSeller" class="seller-tip">这是您发布的商品，不能出价</div>

        <!-- 出价记录 -->
        <div class="bid-history">
          <h4>出价记录 ({{ bids.length }})</h4>
          <div v-for="b in bids" :key="b.id" class="bid-item">
            <span>{{ b.bidderName }}</span>
            <span class="bid-amount">¥{{ b.bidAmount }}</span>
            <span class="bid-time">{{ b.bidTime }}</span>
          </div>
        </div>
      </template>

      <!-- 一口价模式 -->
      <template v-else>
        <div class="price-box">
          <span class="label">价格</span>
          <span class="price">¥{{ product.fixedPrice }}</span>
        </div>
        <el-button type="primary" size="large" @click="buyNow" v-if="isActive && !isSeller">立即购买</el-button>
      </template>

      <!-- 卖家信息 -->
      <div class="seller-card" @click="goToSellerProfile">
        <el-avatar :src="product.sellerAvatar" :size="40">{{ product.sellerName?.[0] }}</el-avatar>
        <span>{{ product.sellerName }}</span>
      </div>
      <el-button @click="goToChat" v-if="!isSeller && userStore.isLoggedIn">联系卖家</el-button>

      <el-button @click="toggleFav" :type="favorited ? 'warning' : 'default'" v-if="userStore.isLoggedIn">
        {{ favorited ? '已收藏' : '收藏' }}
      </el-button>

      <!-- 描述 -->
      <div class="desc">
        <h4>商品描述</h4>
        <p>{{ product.description || '暂无描述' }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page { display: flex; gap: 30px; max-width: 1000px; margin: 0 auto; padding: 20px; }
.images { flex: 1; }
.main-image { width: 100%; max-height: 400px; object-fit: cover; border-radius: 8px; }
.thumb-list { display: flex; gap: 8px; margin-top: 10px; }
.thumb { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; cursor: pointer; border: 2px solid transparent; }
.thumb.active { border-color: #409eff; }
.info { flex: 1; }
.meta { color: #999; font-size: 0.85em; margin: 6px 0 16px; }
.price-box { margin: 16px 0; }
.price { font-size: 2em; color: #e74c3c; font-weight: bold; }
.label { color: #999; font-size: 0.9em; }
.countdown { font-size: 1.1em; color: #e6a23c; margin: 10px 0; font-weight: bold; }
.winner-box { background: #f0f9eb; padding: 12px; border-radius: 6px; color: #67c23a; margin: 10px 0; }
.expired-box { background: #fdf6ec; padding: 12px; border-radius: 6px; color: #e6a23c; margin: 10px 0; }
.bid-area { margin: 16px 0; display: flex; align-items: center; }
.seller-tip { color: #999; font-style: italic; margin: 10px 0; }
.bid-history { margin-top: 20px; }
.bid-item { display: flex; gap: 12px; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.bid-amount { color: #e74c3c; font-weight: bold; }
.bid-time { color: #999; font-size: 0.85em; margin-left: auto; }
.seller-card { display: flex; align-items: center; gap: 10px; margin: 20px 0 10px; cursor: pointer; padding: 10px; border: 1px solid #eee; border-radius: 8px; }
.desc { margin-top: 20px; }
.desc p { color: #666; margin-top: 8px; white-space: pre-wrap; }
</style>
