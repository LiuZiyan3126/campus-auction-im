import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from './useUserStore'

describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('初始状态应为未登录', () => {
    const store = useUserStore()
    expect(store.isLoggedIn).toBe(false)
    expect(store.isAdmin).toBe(false)
    expect(store.userInfo).toBeNull()
  })

  it('setTokens 应正确设置Token并持久化', () => {
    const store = useUserStore()
    store.setTokens('access-123', 'refresh-456')

    expect(store.accessToken).toBe('access-123')
    expect(store.refreshToken).toBe('refresh-456')
    expect(store.isLoggedIn).toBe(true)
    expect(localStorage.getItem('accessToken')).toBe('access-123')
  })

  it('setUserInfo 应正确设置用户信息', () => {
    const store = useUserStore()
    store.setUserInfo({
      id: 1,
      studentId: '20210001',
      username: '测试用户',
      avatar: null,
      reputationScore: 100,
      role: 'USER',
      status: 'ACTIVE',
    })

    expect(store.userInfo?.username).toBe('测试用户')
    expect(store.isAdmin).toBe(false)
  })

  it('管理员角色应正确识别', () => {
    const store = useUserStore()
    store.setUserInfo({
      id: 99,
      studentId: 'admin',
      username: '管理员',
      avatar: null,
      reputationScore: 100,
      role: 'ADMIN',
      status: 'ACTIVE',
    })

    expect(store.isAdmin).toBe(true)
  })

  it('logout 应清除所有状态', () => {
    const store = useUserStore()
    store.setTokens('tok', 'ref')
    store.setUserInfo({
      id: 1, studentId: 'x', username: 'x', avatar: null,
      reputationScore: 100, role: 'USER', status: 'ACTIVE',
    })

    store.logout()

    expect(store.isLoggedIn).toBe(false)
    expect(store.userInfo).toBeNull()
    expect(localStorage.getItem('accessToken')).toBeNull()
  })
})
