<script setup lang="ts">
import { useUserStore } from '@/stores/useUserStore'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

function goTo(path: string) {
  router.push(path)
}

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<template>
  <el-menu mode="horizontal" :ellipsis="false" router>
    <el-menu-item index="/">
      <span style="font-weight: bold; font-size: 1.1em;">校园拍卖</span>
    </el-menu-item>
    <el-menu-item index="/search">搜索</el-menu-item>
    <div style="flex-grow: 1" />
    <template v-if="userStore.isLoggedIn">
      <el-menu-item index="/product/create">发布</el-menu-item>
      <el-menu-item index="/chat">消息</el-menu-item>
      <el-menu-item index="/orders">订单</el-menu-item>
      <el-menu-item @click="goTo('/user/profile')">我的</el-menu-item>
      <el-menu-item v-if="userStore.isAdmin" @click="goTo('/admin')">管理</el-menu-item>
      <el-menu-item @click="handleLogout">退出</el-menu-item>
    </template>
    <template v-else>
      <el-menu-item index="/login">登录</el-menu-item>
      <el-menu-item index="/register">注册</el-menu-item>
    </template>
  </el-menu>
</template>
