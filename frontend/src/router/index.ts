import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'Home', component: () => import('@/views/HomeView.vue') },
  { path: '/login', name: 'Login', component: () => import('@/views/LoginView.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/RegisterView.vue') },
  { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetailView.vue') },
  { path: '/product/create', name: 'ProductCreate', component: () => import('@/views/ProductCreateView.vue'), meta: { requiresAuth: true } },
  { path: '/search', name: 'Search', component: () => import('@/views/SearchView.vue') },
  { path: '/chat', name: 'ChatList', component: () => import('@/views/ChatListView.vue'), meta: { requiresAuth: true } },
  { path: '/chat/:userId', name: 'ChatWindow', component: () => import('@/views/ChatWindowView.vue'), meta: { requiresAuth: true } },
  { path: '/user/:id', name: 'UserProfile', component: () => import('@/views/UserProfileView.vue') },
  { path: '/user/profile', name: 'MyProfile', component: () => import('@/views/MyProfileView.vue'), meta: { requiresAuth: true } },
  { path: '/orders', name: 'Orders', component: () => import('@/views/OrdersView.vue'), meta: { requiresAuth: true } },
  { path: '/admin', name: 'AdminDashboard', component: () => import('@/views/admin/AdminDashboardView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/users', name: 'AdminUsers', component: () => import('@/views/admin/AdminUsersView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/products', name: 'AdminProducts', component: () => import('@/views/admin/AdminProductsView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/admin/stats', name: 'AdminStats', component: () => import('@/views/admin/AdminStatsView.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/')
  } else {
    next()
  }
})

export default router
