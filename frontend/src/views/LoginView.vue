<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'
import request from '@/api/request'
import type { ResultVO, UserInfo } from '@/types'
import { logger } from '@/utils/logger'

const router = useRouter()
const userStore = useUserStore()
const studentId = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

async function handleLogin() {
  if (!studentId.value || !password.value) return
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await request.post<ResultVO<{ accessToken: string; refreshToken: string; userInfo: UserInfo }>>('/auth/login', {
      studentId: studentId.value,
      password: password.value,
    })
    const { accessToken, refreshToken, userInfo } = res.data.data
    userStore.setTokens(accessToken, refreshToken)
    userStore.setUserInfo(userInfo)
    router.push('/')
  } catch (e: unknown) {
    const err = e as { response?: { data?: ResultVO } }
    errorMsg.value = err.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="form-page">
    <h2>登录</h2>
    <el-form @submit.prevent="handleLogin" label-width="0">
      <el-form-item>
        <el-input v-model="studentId" placeholder="学号/工号" size="large" />
      </el-form-item>
      <el-form-item>
        <el-input v-model="password" type="password" placeholder="密码" size="large" show-password />
      </el-form-item>
      <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon style="margin-bottom:16px" />
      <el-form-item>
        <el-button type="primary" @click="handleLogin" :loading="loading" size="large" style="width:100%">登录</el-button>
      </el-form-item>
    </el-form>
    <p style="text-align:center">还没有账号？<router-link to="/register">立即注册</router-link></p>
  </div>
</template>

<style scoped>
.form-page { max-width: 400px; margin: 60px auto; padding: 30px; }
.form-page h2 { text-align: center; margin-bottom: 24px; }
</style>
