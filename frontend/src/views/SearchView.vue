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

const categories = [
  { id: 1, name: '教材教辅' },
  { id: 2, name: '电子数码' },
  { id: 3, name: '生活用品' },
  { id: 4, name: '服饰美妆' },
  { id: 5, name: '运动户外' },
]

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
    const cleanQuery: Record<string, string> = {}
    if (q.value) cleanQuery.q = q.value
    if (categoryId.value) cleanQuery.categoryId = String(categoryId.value)
    if (minPrice.value != null) cleanQuery.minPrice = String(minPrice.value)
    if (maxPrice.value != null) cleanQuery.maxPrice = String(maxPrice.value)
    if (type.value) cleanQuery.type = type.value
    router.replace({ query: cleanQuery })
  } catch { /* ignore */ }
  loading.value = false
}

function formatPrice(p: ProductCard) {
  return '¥' + Number(p.currentPrice || p.fixedPrice || p.startPrice || 0).toFixed(2)
}

function viewProduct(id: number) {
  router.push({ name: 'ProductDetail', params: { id } })
}

onMounted(doSearch)
watch(page, doSearch)
watch(sort, () => { page.value = 1; doSearch() })
</script>

<template>
  <div class="search-page">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-card">
        <h4>搜索</h4>
        <div class="search-input-wrap">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input v-model="q" placeholder="搜索关键词..." @keyup.enter="doSearch" />
        </div>

        <h4>分类</h4>
        <div class="category-list">
          <button
            v-for="cat in categories" :key="cat.id"
            class="cat-btn"
            :class="{ active: categoryId === cat.id }"
            @click="categoryId = categoryId === cat.id ? null : cat.id; doSearch()"
          >{{ cat.name }}</button>
        </div>

        <h4>价格区间</h4>
        <div class="price-inputs">
          <input v-model.number="minPrice" type="number" placeholder="最低" @change="doSearch" />
          <span class="price-sep">—</span>
          <input v-model.number="maxPrice" type="number" placeholder="最高" @change="doSearch" />
        </div>

        <h4>交易类型</h4>
        <div class="type-toggles">
          <button :class="{ active: !type }" @click="type = ''; doSearch()">全部</button>
          <button :class="{ active: type === 'AUCTION' }" @click="type = type === 'AUCTION' ? '' : 'AUCTION'; doSearch()">拍卖</button>
          <button :class="{ active: type === 'SALE' }" @click="type = type === 'SALE' ? '' : 'SALE'; doSearch()">一口价</button>
        </div>
      </div>
    </aside>

    <!-- Results -->
    <div class="results">
      <div class="sort-bar">
        <div class="sorts">
          <button :class="{ active: sort === 'created_at' }" @click="sort = 'created_at'">最新</button>
          <button :class="{ active: sort === 'price_asc' }" @click="sort = 'price_asc'">价格 ↑</button>
          <button :class="{ active: sort === 'price_desc' }" @click="sort = 'price_desc'">价格 ↓</button>
          <button :class="{ active: sort === 'view_count' }" @click="sort = 'view_count'">最多浏览</button>
        </div>
        <span class="result-count">共 <strong>{{ total }}</strong> 件</span>
      </div>

      <SkeletonScreen v-if="loading" :count="6" />
      <EmptyState v-else-if="!products.length" icon="search" description="未找到匹配的商品" />

      <div v-else class="product-grid">
        <article
          v-for="p in products" :key="p.id"
          class="product-card card"
          @click="viewProduct(p.id)"
        >
          <div class="card-image">
            <img :src="p.coverImage || '/placeholder.svg'" :alt="p.title" loading="lazy" />
            <span class="card-badge" :class="p.type === 'AUCTION' ? 'badge-auction' : 'badge-sale'">
              {{ p.type === 'AUCTION' ? '拍卖' : '一口价' }}
            </span>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ p.title }}</h3>
            <div class="card-price">{{ formatPrice(p) }}</div>
            <div class="card-meta">
              <span>{{ p.sellerName }}</span>
              <span v-if="p.viewCount">{{ p.viewCount }} 浏览</span>
            </div>
          </div>
        </article>
      </div>

      <div v-if="total > size" class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          background
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  display: flex;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

/* Sidebar */
.sidebar { width: 240px; flex-shrink: 0; }
.sidebar-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 20px;
  border: 1px solid var(--color-border-light);
  position: sticky;
  top: 80px;
}
.sidebar-card h4 {
  font-size: .8em;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: var(--color-text-muted);
  margin: 16px 0 8px;
}
.sidebar-card h4:first-child { margin-top: 0; }

.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: border-color var(--transition-fast);
  color: var(--color-text-muted);
}
.search-input-wrap:focus-within { border-color: var(--color-primary); }
.search-input-wrap input {
  border: none; outline: none;
  width: 100%;
  font-size: .875em;
  background: transparent;
  font-family: inherit;
}

.category-list { display: flex; flex-direction: column; gap: 4px; }
.cat-btn {
  padding: 8px 12px;
  border: none;
  background: transparent;
  text-align: left;
  font-size: .875em;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  font-family: inherit;
}
.cat-btn:hover { background: var(--color-bg); color: var(--color-text); }
.cat-btn.active { background: var(--color-primary-bg); color: var(--color-primary); font-weight: 500; }

.price-inputs {
  display: flex;
  align-items: center;
  gap: 6px;
}
.price-inputs input {
  width: 0;
  flex: 1;
  padding: 8px 10px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: .85em;
  outline: none;
  font-family: inherit;
}
.price-inputs input:focus { border-color: var(--color-primary); }
.price-sep { color: var(--color-text-muted); font-size: .85em; }

.type-toggles {
  display: flex;
  background: var(--color-bg);
  border-radius: var(--radius-md);
  padding: 3px;
}
.type-toggles button {
  flex: 1;
  padding: 7px 0;
  border: none;
  background: transparent;
  font-size: .8em;
  cursor: pointer;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
  font-family: inherit;
}
.type-toggles button.active {
  background: var(--color-surface);
  color: var(--color-text);
  font-weight: 500;
  box-shadow: var(--shadow-xs);
}

/* Results */
.results { flex: 1; min-width: 0; }

.sort-bar {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-lg);
  flex-wrap: wrap;
  gap: 12px;
}
.sorts {
  display: flex;
  gap: 4px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: 3px;
  border: 1px solid var(--color-border-light);
}
.sorts button {
  padding: 7px 14px;
  border: none;
  background: transparent;
  font-size: .85em;
  cursor: pointer;
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
  font-family: inherit;
}
.sorts button.active {
  background: var(--color-primary);
  color: white;
  font-weight: 500;
}
.result-count { margin-left: auto; font-size: .875em; color: var(--color-text-muted); }
.result-count strong { color: var(--color-text); }

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
}

.product-card {
  cursor: pointer;
  overflow: hidden;
  border: 1px solid var(--color-border-light);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
}
.product-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-card-hover); }

.card-image {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: var(--color-bg);
}
.card-image img {
  width: 100%; height: 100%;
  object-fit: cover;
  transition: transform .4s ease;
}
.product-card:hover .card-image img { transform: scale(1.05); }

.card-badge {
  position: absolute;
  top: 8px; left: 8px;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: .7em;
  font-weight: 600;
  backdrop-filter: blur(8px);
}
.badge-auction { background: rgba(245,158,11,.85); color: white; }
.badge-sale { background: rgba(16,185,129,.85); color: white; }

.card-body { padding: 14px; }
.card-title {
  font-size: .9em; font-weight: 600;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  margin-bottom: 6px;
}
.card-price { font-size: 1.1em; font-weight: 700; color: var(--color-danger); margin-bottom: 8px; }
.card-meta {
  display: flex; justify-content: space-between;
  font-size: .75em; color: var(--color-text-muted);
}

.pagination-wrap {
  display: flex; justify-content: center;
  margin-top: var(--space-xl);
}

@media (max-width: 768px) {
  .search-page { flex-direction: column; }
  .sidebar { width: 100%; }
  .sidebar-card { position: static; }
  .product-grid { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; }
}
</style>
