<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { logger } from '@/utils/logger'

const router = useRouter()
const form = ref({ studentId: '', email: '', password: '', confirmPassword: '', phone: '' })
const loading = ref(false)

async function handleRegister() {
  if (form.value.password !== form.value.confirmPassword) {
    logger.warn('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await request.post('/auth/register', form.value)
    router.push('/login')
  } catch (e) {
    logger.error('注册失败', e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="form-page">
    <h2>注册</h2>
    <el-form @submit.prevent="handleRegister" label-width="0">
      <el-form-item><el-input v-model="form.studentId" placeholder="学号/工号" size="large" /></el-form-item>
      <el-form-item><el-input v-model="form.email" placeholder="邮箱" size="large" /></el-form-item>
      <el-form-item><el-input v-model="form.phone" placeholder="手机号（选填）" size="large" /></el-form-item>
      <el-form-item><el-input v-model="form.password" type="password" placeholder="密码（至少8位，含字母数字）" size="large" show-password /></el-form-item>
      <el-form-item><el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" size="large" show-password /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleRegister" :loading="loading" size="large" style="width:100%">注册</el-button>
      </el-form-item>
    </el-form>
    <p style="text-align:center">已有账号？<router-link to="/login">去登录</router-link></p>
  </div>
</template>

<style scoped>
.form-page { max-width: 400px; margin: 60px auto; padding: 30px; }
.form-page h2 { text-align: center; margin-bottom: 24px; }
</style>
