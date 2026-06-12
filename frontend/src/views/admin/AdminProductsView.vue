<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import type { PageVO } from '@/types'

interface ProductAdmin {
  id: number; sellerName: string; title: string; type: string;
  currentPrice: number | null; fixedPrice: number | null;
  status: string; viewCount: number; createdAt: string;
}

const products = ref<ProductAdmin[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const statusFilter = ref('PENDING')

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
</script>

<template>
  <div class="admin-page">
    <h2>商品管理</h2>
    <el-select v-model="statusFilter" @change="fetchProducts" clearable style="width:150px;margin-bottom:16px">
      <el-option value="PENDING" label="待审核" />
      <el-option value="ACTIVE" label="已通过" />
      <el-option value="BANNED" label="已下架" />
    </el-select>
    <el-table :data="products" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="sellerName" label="卖家" />
      <el-table-column prop="type" label="类型">
        <template #default="{ row }"><el-tag :type="row.type === 'AUCTION' ? 'warning' : 'success'" size="small">{{ row.type === 'AUCTION' ? '拍卖' : '一口价' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="价格">
        <template #default="{ row }">¥{{ row.currentPrice || row.fixedPrice || 0 }}</template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'warning'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="150">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="success" size="small" @click="audit(row.id, 'APPROVE')">通过</el-button>
          <el-button v-if="row.status !== 'BANNED'" type="danger" size="small" @click="audit(row.id, 'REJECT')">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="20" @current-change="fetchProducts" layout="prev, pager, next" style="margin-top:16px;justify-content:center" />
  </div>
</template>

<style scoped>
.admin-page { padding: 20px; }
.admin-page h2 { margin-bottom: 16px; }
</style>
