<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import type { ProductCard, PageVO } from '@/types'
import SkeletonScreen from '@/components/SkeletonScreen.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const loading = ref(true)
const products = ref<ProductCard[]>([])
const total = ref(0)
const searchText = ref('')

const categories = [
  { id: 1, name: '教材教辅', icon: '📚', color: '#6366f1' },
  { id: 2, name: '电子数码', icon: '💻', color: '#06b6d4' },
  { id: 3, name: '生活用品', icon: '🏠', color: '#10b981' },
  { id: 4, name: '服饰美妆', icon: '👗', color: '#f59e0b' },
  { id: 5, name: '运动户外', icon: '⚽', color: '#ef4444' },
]

onMounted(async () => {
  try {
    const res = await request.get('/products', { params: { page: 1, size: 20 } })
    const result = res.data
    if (result.code === 200 && result.data) {
      products.value = result.data.items || []
      total.value = result.data.total || 0
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err.response?.data?.message || '加载失败')
  }
  loading.value = false
})

function goSearch(q?: string) {
  const query: Record<string, string> = {}
  if (q) query.q = q
  router.push({ path: '/search', query })
}

function goCategory(id: number) {
  router.push({ path: '/search', query: { categoryId: String(id) } })
}

function viewProduct(id: number) {
  router.push({ name: 'ProductDetail', params: { id } })
}

function formatPrice(p: ProductCard) {
  const price = p.currentPrice || p.fixedPrice || p.startPrice || 0
  return '¥' + Number(price).toFixed(2)
}
</script>

<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-blob blob-1"></div>
        <div class="hero-blob blob-2"></div>
        <div class="hero-blob blob-3"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">
          <span class="dot"></span>
          校园二手交易，安全又便捷
        </div>
        <h1 class="hero-title">
          发现<span class="gradient-text">好物</span>
          <br>享受<span class="gradient-text">竞拍</span>乐趣
        </h1>
        <p class="hero-sub">在线 {{ total }} 件商品等你来淘 · 英式拍卖，价高者得</p>

        <!-- Search bar -->
        <div class="hero-search">
          <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input
            v-model="searchText"
            placeholder="搜索你想要的商品..."
            @keyup.enter="goSearch(searchText)"
          />
          <button @click="goSearch(searchText)" class="search-submit">搜索</button>
        </div>

        <!-- Stats -->
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-value">{{ total }}</span>
            <span class="stat-label">在线商品</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">5</span>
            <span class="stat-label">商品分类</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">100%</span>
            <span class="stat-label">安全交易</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Categories -->
    <section class="section">
      <div class="section-header">
        <h2>浏览分类</h2>
        <a class="section-link" @click="goSearch()">查看全部 →</a>
      </div>
      <div class="category-row">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="category-pill"
          :style="{ '--cat-color': cat.color }"
          @click="goCategory(cat.id)"
        >
          <span class="cat-icon">{{ cat.icon }}</span>
          <span class="cat-name">{{ cat.name }}</span>
        </div>
      </div>
    </section>

    <!-- Products -->
    <section class="section">
      <div class="section-header">
        <h2>最新商品</h2>
        <a class="section-link" @click="goSearch()">更多 →</a>
      </div>

      <SkeletonScreen v-if="loading" :count="8" />
      <EmptyState v-else-if="!products.length" description="还没有商品，快来发布第一个吧" icon="empty" />

      <div v-else class="product-grid">
        <article
          v-for="p in products"
          :key="p.id"
          class="product-card card"
          @click="viewProduct(p.id)"
        >
          <div class="card-image">
            <img :src="p.coverImage || '/placeholder.svg'" :alt="p.title" loading="lazy" />
            <span class="card-badge" :class="p.type === 'AUCTION' ? 'badge-auction' : 'badge-sale'">
              {{ p.type === 'AUCTION' ? '拍卖' : '一口价' }}
            </span>
            <div class="card-overlay">
              <span>查看详情</span>
            </div>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ p.title }}</h3>
            <div class="card-price">{{ formatPrice(p) }}</div>
            <div class="card-meta">
              <div class="seller-info">
                <div class="seller-avatar-sm">{{ p.sellerName?.[0] || '?' }}</div>
                <span>{{ p.sellerName }}</span>
              </div>
              <span v-if="p.viewCount" class="views">{{ p.viewCount }} 浏览</span>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* ===== Hero ===== */
.hero {
  position: relative;
  padding: 80px 0 60px;
  text-align: center;
  overflow: hidden;
  margin: -20px -24px 0;
  border-radius: 0 0 40px 40px;
  background: linear-gradient(135deg, #eef2ff 0%, #ecfeff 50%, #f5f3ff 100%);
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}
.hero-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: .4;
  animation: float 8s ease-in-out infinite;
}
.blob-1 {
  width: 300px; height: 300px;
  background: rgba(99,102,241,.3);
  top: -60px; right: -40px;
  animation-delay: 0s;
}
.blob-2 {
  width: 250px; height: 250px;
  background: rgba(6,182,212,.25);
  bottom: -40px; left: -40px;
  animation-delay: -3s;
}
.blob-3 {
  width: 200px; height: 200px;
  background: rgba(139,92,246,.2);
  top: 40%; left: 50%;
  animation-delay: -5s;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 640px;
  margin: 0 auto;
  padding: 0 var(--space-lg);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: rgba(255,255,255,.8);
  border-radius: var(--radius-full);
  font-size: .85em;
  color: var(--color-text-secondary);
  margin-bottom: var(--space-lg);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,.5);
}
.dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: var(--color-success);
  animation: pulse 2s infinite;
}

.hero-title {
  font-size: 3em;
  font-weight: 800;
  line-height: 1.2;
  color: var(--color-text);
  margin-bottom: var(--space-md);
  letter-spacing: -.02em;
}
.hero-sub {
  font-size: 1.1em;
  color: var(--color-text-secondary);
  margin-bottom: var(--space-xl);
}

/* Hero search */
.hero-search {
  display: flex;
  align-items: center;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 6px 6px 6px 20px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border-light);
  transition: box-shadow var(--transition-fast);
  max-width: 500px;
  margin: 0 auto var(--space-xl);
}
.hero-search:focus-within { box-shadow: var(--shadow-xl), 0 0 0 3px rgba(99,102,241,.1); }
.hero-search input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 1em;
  color: var(--color-text);
  background: transparent;
  font-family: inherit;
}
.hero-search input::placeholder { color: var(--color-text-muted); }
.search-icon { color: var(--color-text-muted); flex-shrink: 0; margin-right: 10px; }
.search-submit {
  padding: 12px 28px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: white;
  border: none;
  border-radius: var(--radius-xl);
  font-size: .95em;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit;
}
.search-submit:hover { opacity: .9; transform: scale(1.02); }

/* Hero stats */
.hero-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--space-xl);
}
.stat-item {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 1.6em;
  font-weight: 700;
  color: var(--color-text);
}
.stat-label {
  font-size: .8em;
  color: var(--color-text-muted);
  margin-top: 2px;
}
.stat-divider {
  width: 1px;
  height: 36px;
  background: var(--color-border);
}

/* ===== Sections ===== */
.section { margin-top: var(--space-2xl); }
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
}
.section-header h2 {
  font-size: 1.4em;
  font-weight: 700;
}
.section-link {
  font-size: .875em;
  color: var(--color-primary);
  cursor: pointer;
  font-weight: 500;
  transition: color var(--transition-fast);
}
.section-link:hover { color: var(--color-primary-dark); }

/* ===== Categories ===== */
.category-row {
  display: flex;
  gap: var(--space-md);
  flex-wrap: wrap;
}
.category-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-base);
  box-shadow: var(--shadow-xs);
}
.category-pill:hover {
  border-color: var(--cat-color);
  background: color-mix(in srgb, var(--cat-color) 5%, white);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,.06);
}
.cat-icon { font-size: 1.3em; }
.cat-name { font-weight: 500; font-size: .9em; }

/* ===== Product Grid ===== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.product-card {
  cursor: pointer;
  overflow: hidden;
  border: none;
}

.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: var(--color-bg);
}
.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .4s ease;
}
.product-card:hover .card-image img { transform: scale(1.06); }

.card-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: .75em;
  font-weight: 600;
  backdrop-filter: blur(8px);
  letter-spacing: .02em;
}
.badge-auction {
  background: rgba(245,158,11,.9);
  color: white;
}
.badge-sale {
  background: rgba(16,185,129,.9);
  color: white;
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,.35);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  pointer-events: none;
  transition: opacity var(--transition-base);
}
.card-overlay span {
  color: white;
  font-weight: 600;
  padding: 8px 20px;
  border: 2px solid rgba(255,255,255,.8);
  border-radius: var(--radius-full);
  font-size: .9em;
}
.product-card:hover .card-overlay { opacity: 1; }

.card-body { padding: 16px; }
.card-title {
  font-size: .95em;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
  color: var(--color-text);
}
.card-price {
  font-size: 1.2em;
  font-weight: 700;
  color: var(--color-danger);
  margin-bottom: 10px;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: .8em;
  color: var(--color-text-muted);
}
.seller-info {
  display: flex;
  align-items: center;
  gap: 6px;
}
.seller-avatar-sm {
  width: 22px;
  height: 22px;
  border-radius: var(--radius-full);
  background: var(--color-primary-bg);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: .75em;
  font-weight: 600;
}

@media (max-width: 640px) {
  .hero { padding: 50px 0 40px; border-radius: 0 0 24px 24px; }
  .hero-title { font-size: 2em; }
  .product-grid { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; }
  .hero-stats { gap: var(--space-md); }
  .stat-value { font-size: 1.2em; }
  .category-row { overflow-x: auto; flex-wrap: nowrap; padding-bottom: 8px; }
}
</style>
