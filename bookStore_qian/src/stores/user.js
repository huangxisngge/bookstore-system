import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(sessionStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(sessionStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 1)
  const username = computed(() => userInfo.value?.username || '')
  const memberLevel = computed(() => userInfo.value?.memberLevel || 1)
  const memberDiscount = computed(() => Number(userInfo.value?.memberDiscount || 1))

  async function login(form) {
    const res = await loginApi(form)
    const data = res.data
    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      role: data.role,
      memberLevel: data.memberLevel,
      memberDiscount: data.memberDiscount,
      avatar: data.avatar
    }
    sessionStorage.setItem('token', data.token)
    sessionStorage.setItem('user', JSON.stringify(userInfo.value))
    return data
  }

  async function register(form) {
    const res = await registerApi(form)
    const data = res.data
    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      role: data.role,
      memberLevel: data.memberLevel,
      memberDiscount: data.memberDiscount,
      avatar: data.avatar
    }
    sessionStorage.setItem('token', data.token)
    sessionStorage.setItem('user', JSON.stringify(userInfo.value))
    return data
  }

  async function fetchUserInfo() {
    const res = await getUserInfo()
    userInfo.value = res.data
    sessionStorage.setItem('user', JSON.stringify(res.data))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
  }

  return { token, userInfo, isLoggedIn, isAdmin, username, memberLevel, memberDiscount, login, register, fetchUserInfo, logout }
})
