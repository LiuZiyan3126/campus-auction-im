<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { logger } from '@/utils/logger'

const router = useRouter()
const DRAFT_KEY = 'product_draft'

const form = ref({
  title: '', description: '', categoryId: null as number | null,
  type: 'AUCTION' as 'SALE' | 'AUCTION',
  startPrice: null as number | null,
  fixedPrice: null as number | null,
  auctionDuration: 24,
})
const images = ref<File[]>([])
const categories = ref<{ id: number; name: string; parentId: number | null }[]>([])
const submitting = ref(false)

// 加载分类
onMounted(async () => {
  try {
    const res = await request.get('/products/categories')
    categories.value = res.data.data || []
  } catch { /* 分类暂未实现 */ }
  // 恢复草稿
  const draft = localStorage.getItem(DRAFT_KEY)
  if (draft) {
    try { Object.assign(form.value, JSON.parse(draft)) } catch { /* ignore */ }
  }
})

// 自动保存草稿
const saveDraft = () => localStorage.setItem(DRAFT_KEY, JSON.stringify(form.value))

function handleImageChange(file: File) {
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('图片不能超过5MB'); return }
  images.value.push(file)
}

function removeImage(index: number) { images.value.splice(index, 1) }

async function handleSubmit() {
  if (!form.value.title) { ElMessage.warning('请输入商品标题'); return }
  submitting.value = true
  try {
    const fd = new FormData()
    fd.append('data', new Blob([JSON.stringify(form.value)], { type: 'application/json' }))
    images.value.forEach((img) => fd.append('images', img))
    await request.post('/products', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    ElMessage.success('发布成功')
    localStorage.removeItem(DRAFT_KEY)
    router.push('/')
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err.response?.data?.message || '发布失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="create-page">
    <h2>发布商品</h2>
    <el-form label-width="100px" @submit.prevent="handleSubmit">
      <el-form-item label="标题" required>
        <el-input v-model="form.title" maxlength="200" show-word-limit @input="saveDraft" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="4" maxlength="2000" show-word-limit @input="saveDraft" />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="form.categoryId" placeholder="选择分类" style="width:200px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="交易模式">
        <el-radio-group v-model="form.type" @change="saveDraft">
          <el-radio value="AUCTION">拍卖</el-radio>
          <el-radio value="SALE">一口价</el-radio>
        </el-radio-group>
      </el-form-item>
      <template v-if="form.type === 'AUCTION'">
        <el-form-item label="起拍价" required>
          <el-input-number v-model="form.startPrice" :min="0" :precision="2" @change="saveDraft" />
        </el-form-item>
        <el-form-item label="拍卖时长">
          <el-select v-model="form.auctionDuration" style="width:200px">
            <el-option :value="1" label="1小时" />
            <el-option :value="6" label="6小时" />
            <el-option :value="12" label="12小时" />
            <el-option :value="24" label="24小时" />
            <el-option :value="72" label="3天" />
          </el-select>
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.fixedPrice" :min="0" :precision="2" @change="saveDraft" />
        </el-form-item>
      </template>
      <el-form-item label="图片">
        <el-upload :auto-upload="false" :on-change="(f: any) => handleImageChange(f.raw)" :on-remove="(f: any) => removeImage(images.indexOf(f.raw))" list-type="picture-card" accept=".jpg,.jpeg,.png,.gif,.webp">
          <el-icon><span>+</span></el-icon>
        </el-upload>
        <div style="color:#999;font-size:0.85em;">支持 jpg/png/gif/webp，单张≤5MB</div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">发布</el-button>
        <el-button @click="router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.create-page { max-width: 700px; margin: 0 auto; padding: 20px; }
.create-page h2 { margin-bottom: 24px; }
</style>
