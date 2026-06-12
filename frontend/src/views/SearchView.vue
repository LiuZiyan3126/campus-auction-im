<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api/request'
import type { ProductCard, PageVO } from '@/types'
import SkeletonScreen from '@/components/SkeletonScreen.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()

const q = ref((route.query.q as string) || '')
const categoryId = ref(route.query.categoryId ? Number(route.query.categoryId) : null)
const minPrice = ref(route.query.minPrice ? Number(route.query.minPrice) : null)
const maxPrice = ref(route.query.maxPrice ? Number(route.query.maxPrice) : null)
const type = ref((route.query.type as string) || '')
const sort = ref('created_at')
const page = ref(1)
const size = 20

const loading = ref(true)
const products = ref<ProductCard[]>([])
const total = ref(0)

async function doSearch() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size, sort: sort.value }
    if (q.value) params.q = q.value
    if (categoryId.value) params.categoryId = categoryId.value
    if (minPrice.value != null) params.minPrice = minPrice.value
    if (maxPrice.value != null) params.maxPrice = maxPrice.value
    if (type.value) params.type = type.value
    const res = await request.get<{ data: PageVO<ProductCard> }>('/products/search', { params })
    const data = res.data.data
    products.value = data?.items || []
    total.value = data?.total || 0
    // 更新URL
    router.replace({ query: { ...params } })
  } catch { /* ignore */ }
  loading.value = false
}

onMounted(doSearch)
watch(page, doSearch)
watch(sort, () => { page.value = 1; doSearch() })
</script>

<template>
  <div class="search-page">
    <div class="sidebar">
      <el-input v-model="q" placeholder="搜索..." @keyup.enter="doSearch" />
      <div class="filters">
        <h4>分类</h4>
        <el-select v-model="categoryId" clearable placeholder="全部分类" @change="doSearch" style="width:100%">
          <el-option :value="1" label="教材教辅" />
          <el-option :value="2" label="电子数码" />
          <el-option :value="3" label="生活用品" />
          <el-option :value="4" label="服饰美妆" />
          <el-option :value="5" label="运动户外" />
        </el-select>
        <h4 style="margin-top:16px">价格区间</h4>
        <el-input-number v-model="minPrice" :min="0" placeholder="最低" style="width:100%;margin-bottom:8px" @change="doSearch" />
        <el-input-number v-model="maxPrice" :min="0" placeholder="最高" style="width:100%" @change="doSearch" />
        <h4 style="margin-top:16px">交易类型</h4>
        <el-radio-group v-model="type" @change="doSearch">
          <el-radio value="">全部</el-radio>
          <el-radio value="AUCTION">拍卖</el-radio>
          <el-radio value="SALE">一口价</el-radio>
        </el-radio-group>
      </div>
    </div>
    <div class="results">
      <div class="sort-bar">
        <el-radio-group v-model="sort">
          <el-radio-button value="created_at">最新</el-radio-button>
          <el-radio-button value="price_asc">价格↑</el-radio-button>
          <el-radio-button value="price_desc">价格↓</el-radio-button>
          <el-radio-button value="view_count">最多浏览</el-radio-button>
        </el-radio-group>
        <span style="color:#999;margin-left:auto">共 {{ total }} 件</span>
      </div>
      <SkeletonScreen v-if="loading" />
      <EmptyState v-else-if="!products.length" description="未找到商品" />
      <div v-else class="product-grid">
        <el-card v-for="p in products" :key="p.id" class="product-card" @click="router.push(`/product/${p.id}`)">
          <img :src="p.coverImage || '/placeholder.svg'" style="width:100%;height:160px;object-fit:cover" />
          <div style="padding:10px">
            <h3>{{ p.title }}</h3>
            <p class="price">{{ p.type === 'AUCTION' ? '当前价' : '价格' }}: ¥{{ p.currentPrice || p.fixedPrice }}</p>
            <p class="seller">{{ p.sellerName }}</p>
          </div>
        </el-card>
      </div>
      <el-pagination v-if="total > size" v-model:current-page="page" :page-size="size" :total="total" layout="prev, pager, next" style="margin-top:20px;justify-content:center" />
    </div>
  </div>
</template>

<style scoped>
.search-page { display: flex; gap: 24px; max-width: 1100px; margin: 0 auto; }
.sidebar { width: 200px; flex-shrink: 0; }
.filters { margin-top: 16px; }
.filters h4 { margin-bottom: 6px; font-size: 0.95em; }
.results { flex: 1; }
.sort-bar { display: flex; align-items: center; margin-bottom: 16px; }
.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 14px; }
.price { color: #e74c3c; font-weight: bold; margin-top: 4px; }
.seller { color: #999; font-size: 0.8em; margin-top: 2px; }
.product-card { cursor: pointer; transition: transform .15s; }
.product-card:hover { transform: translateY(-2px); }
</style>
