<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

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
const imagePreviews = ref<string[]>([])
const categories = ref<{ id: number; name: string; parentId: number | null }[]>([])
const submitting = ref(false)

const step = ref(1)
const isAuction = computed(() => form.value.type === 'AUCTION')
const canNext = computed(() => {
  if (step.value === 1) return form.value.title.trim().length > 0
  if (step.value === 2) {
    if (isAuction.value) return (form.value.startPrice ?? 0) > 0 && form.value.auctionDuration > 0
    return (form.value.fixedPrice ?? 0) > 0
  }
  return true
})

onMounted(async () => {
  try {
    const res = await request.get('/products/categories')
    categories.value = res.data.data || []
  } catch { /* ignore */ }
  const draft = localStorage.getItem(DRAFT_KEY)
  if (draft) {
    try { Object.assign(form.value, JSON.parse(draft)) } catch { /* ignore */ }
  }
})

const saveDraft = () => localStorage.setItem(DRAFT_KEY, JSON.stringify(form.value))

function handleImageChange(file: File) {
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('图片不能超过5MB'); return }
  images.value.push(file)
  const reader = new FileReader()
  reader.onload = (e) => imagePreviews.value.push(e.target?.result as string)
  reader.readAsDataURL(file)
}

function removeImage(index: number) {
  images.value.splice(index, 1)
  imagePreviews.value.splice(index, 1)
}

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
    <div class="page-header">
      <h1>发布商品</h1>
      <p>填写商品信息，让更多人看到你的好物</p>
    </div>

    <!-- Steps -->
    <div class="steps">
      <div class="step" :class="{ active: step >= 1, done: step > 1 }">
        <div class="step-dot">1</div>
        <span>基本信息</span>
      </div>
      <div class="step-line" :class="{ done: step > 1 }"></div>
      <div class="step" :class="{ active: step >= 2, done: step > 2 }">
        <div class="step-dot">2</div>
        <span>价格设置</span>
      </div>
      <div class="step-line" :class="{ done: step > 2 }"></div>
      <div class="step" :class="{ active: step >= 3 }">
        <div class="step-dot">3</div>
        <span>上传图片</span>
      </div>
    </div>

    <!-- Step 1: Basic info -->
    <div v-show="step === 1" class="step-content">
      <div class="form-group">
        <label>商品标题 <span class="req">*</span></label>
        <input v-model="form.title" maxlength="200" placeholder="例如：九五新高等数学教材" @input="saveDraft" />
        <span class="char-count">{{ form.title.length }}/200</span>
      </div>
      <div class="form-group">
        <label>商品描述</label>
        <textarea v-model="form.description" rows="5" maxlength="2000" placeholder="描述商品的成色、使用情况、购入渠道等..." @input="saveDraft"></textarea>
        <span class="char-count">{{ form.description.length }}/2000</span>
      </div>
      <div class="form-group">
        <label>分类</label>
        <div class="cat-grid">
          <button
            v-for="c in categories" :key="c.id"
            class="cat-option"
            :class="{ selected: form.categoryId === c.id }"
            @click="form.categoryId = c.id; saveDraft()"
          >{{ c.name }}</button>
        </div>
      </div>
      <div class="step-actions">
        <button class="btn-next" :disabled="!canNext" @click="step = 2">下一步</button>
      </div>
    </div>

    <!-- Step 2: Price -->
    <div v-show="step === 2" class="step-content">
      <div class="form-group">
        <label>交易模式</label>
        <div class="mode-switch">
          <button :class="{ active: isAuction }" @click="form.type = 'AUCTION'; saveDraft()">
            <span class="mode-icon">🔨</span>
            <span class="mode-name">拍卖</span>
            <span class="mode-desc">买家竞价，价高者得</span>
          </button>
          <button :class="{ active: !isAuction }" @click="form.type = 'SALE'; saveDraft()">
            <span class="mode-icon">💰</span>
            <span class="mode-name">一口价</span>
            <span class="mode-desc">设定价格，直接购买</span>
          </button>
        </div>
      </div>

      <template v-if="isAuction">
        <div class="form-group">
          <label>起拍价 <span class="req">*</span></label>
          <div class="price-input-wrap">
            <span class="currency">¥</span>
            <input v-model.number="form.startPrice" type="number" min="0" step="0.01" placeholder="0.00" @input="saveDraft" />
          </div>
        </div>
        <div class="form-group">
          <label>拍卖时长</label>
          <div class="duration-options">
            <button
              v-for="d in [{v:1,l:'1小时'},{v:6,l:'6小时'},{v:12,l:'12小时'},{v:24,l:'24小时'},{v:72,l:'3天'}]"
              :key="d.v"
              class="dur-btn"
              :class="{ selected: form.auctionDuration === d.v }"
              @click="form.auctionDuration = d.v; saveDraft()"
            >{{ d.l }}</button>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="form-group">
          <label>价格 <span class="req">*</span></label>
          <div class="price-input-wrap">
            <span class="currency">¥</span>
            <input v-model.number="form.fixedPrice" type="number" min="0" step="0.01" placeholder="0.00" @input="saveDraft" />
          </div>
        </div>
      </template>

      <div class="step-actions">
        <button class="btn-back" @click="step = 1">上一步</button>
        <button class="btn-next" :disabled="!canNext" @click="step = 3">下一步</button>
      </div>
    </div>

    <!-- Step 3: Images + Submit -->
    <div v-show="step === 3" class="step-content">
      <div class="form-group">
        <label>商品图片</label>
        <p class="hint">支持 jpg/png/gif/webp，单张不超过 5MB</p>
        <div class="upload-area">
          <div v-for="(preview, i) in imagePreviews" :key="i" class="preview-item">
            <img :src="preview" alt="预览" />
            <button class="remove-btn" @click="removeImage(i)">×</button>
          </div>
          <label class="upload-btn" v-if="imagePreviews.length < 6">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            <span>添加图片</span>
            <input type="file" accept=".jpg,.jpeg,.png,.gif,.webp" hidden @change="(e: Event) => { const f = (e.target as HTMLInputElement).files?.[0]; if (f) handleImageChange(f) }" />
          </label>
        </div>
      </div>

      <div class="review-card">
        <h3>确认信息</h3>
        <div class="review-row"><span>标题</span><span>{{ form.title }}</span></div>
        <div class="review-row"><span>模式</span><span>{{ isAuction ? '拍卖' : '一口价' }}</span></div>
        <div class="review-row"><span>价格</span><span>¥{{ isAuction ? form.startPrice : form.fixedPrice }}</span></div>
        <div v-if="isAuction" class="review-row"><span>时长</span><span>{{ form.auctionDuration }}小时</span></div>
      </div>

      <div class="step-actions">
        <button class="btn-back" @click="step = 2">上一步</button>
        <button class="btn-submit" :disabled="submitting" @click="handleSubmit">
          <span v-if="submitting" class="btn-spinner"></span>
          <span v-else>发布商品</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.create-page { max-width: 660px; margin: 0 auto; }
.page-header { text-align: center; margin-bottom: var(--space-xl); }
.page-header h1 { font-size: 1.6em; font-weight: 700; }
.page-header p { color: var(--color-text-muted); margin-top: 4px; font-size: .9em; }

/* Steps indicator */
.steps {
  display: flex; align-items: center; justify-content: center;
  margin-bottom: var(--space-xl); gap: 0;
}
.step { display: flex; align-items: center; gap: 8px; font-size: .85em; color: var(--color-text-muted); }
.step.active { color: var(--color-primary); font-weight: 600; }
.step.done { color: var(--color-success); }
.step-dot {
  width: 28px; height: 28px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: .85em;
  background: var(--color-bg); color: var(--color-text-muted);
  transition: all var(--transition-fast);
}
.step.active .step-dot { background: var(--color-primary); color: white; }
.step.done .step-dot { background: var(--color-success); color: white; }
.step-line {
  width: 60px; height: 2px; background: var(--color-border);
  margin: 0 12px; transition: background var(--transition-fast);
}
.step-line.done { background: var(--color-success); }

.step-content { animation: fadeInUp .3s ease; }

/* Form groups */
.form-group { margin-bottom: var(--space-lg); }
.form-group label {
  display: block; font-size: .9em; font-weight: 600;
  margin-bottom: 6px; color: var(--color-text);
}
.req { color: var(--color-danger); }
.hint { font-size: .8em; color: var(--color-text-muted); margin-bottom: 8px; }

.form-group input[type="text"],
.form-group input[type="number"],
.form-group textarea {
  width: 100%; padding: 12px 16px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: .95em; outline: none;
  transition: border-color var(--transition-fast);
  font-family: inherit; color: var(--color-text);
  background: var(--color-surface);
}
.form-group input:focus,
.form-group textarea:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(99,102,241,.1);
}
.char-count { display: block; text-align: right; font-size: .75em; color: var(--color-text-muted); margin-top: 4px; }

/* Category grid */
.cat-grid { display: flex; gap: 8px; flex-wrap: wrap; }
.cat-option {
  padding: 10px 20px; border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md); background: var(--color-surface);
  font-size: .875em; cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit; color: var(--color-text-secondary);
}
.cat-option:hover { border-color: var(--color-primary-light); }
.cat-option.selected {
  border-color: var(--color-primary); background: var(--color-primary-bg);
  color: var(--color-primary); font-weight: 600;
}

/* Mode switch */
.mode-switch { display: flex; gap: 12px; }
.mode-switch > button {
  flex: 1; padding: 20px; border: 2px solid var(--color-border);
  border-radius: var(--radius-lg); background: var(--color-surface);
  cursor: pointer; transition: all var(--transition-fast);
  text-align: center; font-family: inherit;
}
.mode-switch > button:hover { border-color: var(--color-primary-light); }
.mode-switch > button.active {
  border-color: var(--color-primary);
  background: var(--color-primary-bg);
}
.mode-icon { font-size: 1.6em; display: block; margin-bottom: 6px; }
.mode-name { font-weight: 700; display: block; color: var(--color-text); }
.mode-desc { font-size: .8em; color: var(--color-text-muted); margin-top: 2px; display: block; }

/* Price */
.price-input-wrap {
  display: flex; align-items: center;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md); overflow: hidden;
  transition: border-color var(--transition-fast);
  background: var(--color-surface);
}
.price-input-wrap:focus-within { border-color: var(--color-primary); }
.currency {
  padding: 0 16px; font-size: 1.1em; font-weight: 600;
  color: var(--color-text-muted);
}
.price-input-wrap input {
  flex: 1; border: none; outline: none;
  padding: 12px 12px 12px 0; font-size: 1.1em;
  font-family: inherit;
}

/* Duration */
.duration-options { display: flex; gap: 8px; flex-wrap: wrap; }
.dur-btn {
  padding: 10px 18px; border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md); background: var(--color-surface);
  font-size: .875em; cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit; color: var(--color-text-secondary);
}
.dur-btn:hover { border-color: var(--color-primary-light); }
.dur-btn.selected {
  border-color: var(--color-primary); background: var(--color-primary-bg);
  color: var(--color-primary); font-weight: 600;
}

/* Upload */
.upload-area { display: flex; gap: 12px; flex-wrap: wrap; }
.preview-item {
  width: 120px; height: 120px; border-radius: var(--radius-md);
  overflow: hidden; position: relative;
}
.preview-item img { width: 100%; height: 100%; object-fit: cover; }
.remove-btn {
  position: absolute; top: 4px; right: 4px;
  width: 24px; height: 24px; border-radius: 50%;
  background: rgba(0,0,0,.5); color: white; border: none;
  font-size: 1em; cursor: pointer; display: flex;
  align-items: center; justify-content: center;
}
.upload-btn {
  width: 120px; height: 120px; border: 2px dashed var(--color-border);
  border-radius: var(--radius-md); display: flex;
  flex-direction: column; align-items: center; justify-content: center;
  gap: 6px; cursor: pointer; color: var(--color-text-muted);
  transition: all var(--transition-fast); font-size: .8em;
}
.upload-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }

/* Steps actions */
.step-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: var(--space-xl); }
.btn-back {
  padding: 12px 24px; border: 1.5px solid var(--color-border);
  background: var(--color-surface); border-radius: var(--radius-md);
  font-size: .9em; cursor: pointer; font-family: inherit;
  transition: all var(--transition-fast);
}
.btn-back:hover { background: var(--color-bg); }
.btn-next {
  padding: 12px 28px; border: none;
  background: var(--color-primary); color: white;
  border-radius: var(--radius-md); font-size: .9em; font-weight: 600;
  cursor: pointer; transition: all var(--transition-fast);
  font-family: inherit;
}
.btn-next:hover:not(:disabled) { background: var(--color-primary-dark); }
.btn-next:disabled { opacity: .5; cursor: not-allowed; }
.btn-submit {
  padding: 12px 32px; border: none;
  background: linear-gradient(135deg, var(--color-primary), #8b5cf6);
  color: white; border-radius: var(--radius-md);
  font-size: .95em; font-weight: 600; cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit; display: flex; align-items: center; gap: 8px;
}
.btn-submit:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(99,102,241,.35); }
.btn-submit:disabled { opacity: .7; cursor: not-allowed; }
.btn-spinner {
  width: 18px; height: 18px; border: 2px solid rgba(255,255,255,.3);
  border-top-color: white; border-radius: 50%; animation: spin .6s linear infinite;
}

/* Review */
.review-card {
  background: var(--color-bg); border-radius: var(--radius-md);
  padding: 16px; margin-top: var(--space-lg);
}
.review-card h3 { font-size: .9em; margin-bottom: 12px; }
.review-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: .85em; }
.review-row span:first-child { color: var(--color-text-muted); }

@keyframes spin { to { transform: rotate(360deg); } }
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
