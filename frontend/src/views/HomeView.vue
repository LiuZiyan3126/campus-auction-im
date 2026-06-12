<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import type { ProductCard, PageVO } from '@/types'
import SkeletonScreen from '@/components/SkeletonScreen.vue'
import EmptyState from '@/components/EmptyState.vue'
import { logger } from '@/utils/logger'

const router = useRouter()
const loading = ref(true)
const products = ref<ProductCard[]>([])
const total = ref(0)

onMounted(async () => {
  try {
    const res = await request.get<{ data: PageVO<ProductCard> }>('/products', { params: { page: 1, size: 20 } })
    const data = res.data.data
    products.value = data?.items || []
    total.value = data?.total || 0
  } catch (e) {
    logger.error('加载商品列表失败', e)
  }
  loading.value = false
})
</script>

<template>
  <div>
    <div class="hero">
      <h1>校园二手拍卖</h1>
      <p>发现好物，享受竞拍乐趣 · {{ total }} 件商品在线</p>
      <el-input v-model="searchText" placeholder="搜索商品..." size="large" style="max-width:400px;margin:16px auto 0" @keyup.enter="router.push({ path: '/search', query: { q: searchText } })">
        <template #append><el-button @click="router.push({ path: '/search', query: { q: searchText } })">搜索</el-button></template>
      </el-input>
    </div>
    <SkeletonScreen v-if="loading" />
    <EmptyState v-else-if="!products.length" description="暂无商品" />
    <div v-else class="product-grid">
      <el-card v-for="p in products" :key="p.id" class="product-card" shadow="hover" @click="router.push(`/product/${p.id}`)">
        <img :src="p.coverImage || '/placeholder.svg'" :alt="p.title" />
        <div class="card-body">
          <h3>{{ p.title }}</h3>
          <p class="price">
            {{ p.type === 'AUCTION' ? '当前价' : '一口价' }}: <strong>¥{{ p.currentPrice || p.fixedPrice || p.startPrice }}</strong>
          </p>
          <div class="card-footer">
            <span class="seller">{{ p.sellerName }}</span>
            <el-tag :type="p.type === 'AUCTION' ? 'warning' : 'success'" size="small">{{ p.type === 'AUCTION' ? '拍卖' : '一口价' }}</el-tag>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.hero { text-align: center; padding: 50px 0 30px; }
.hero h1 { font-size: 2.2em; margin-bottom: 8px; }
.hero p { color: #666; font-size: 1.05em; }
.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(230px, 1fr)); gap: 18px; margin-top: 8px; }
.product-card { cursor: pointer; transition: transform .15s; }
.product-card:hover { transform: translateY(-3px); }
.product-card img { width: 100%; height: 180px; object-fit: cover; }
.card-body { padding: 12px; }
.card-body h3 { font-size: 1em; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price { color: #e74c3c; font-weight: bold; }
.card-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.seller { color: #999; font-size: 0.85em; }
</style>

<script lang="ts">
const searchText = ref('')
</script>
