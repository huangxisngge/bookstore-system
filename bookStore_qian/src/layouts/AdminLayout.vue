<template>
  <div class="admin-layout">
    <el-container style="min-height: 100vh">
      <el-aside width="220px" class="aside">
        <div class="aside-logo">
          <img :src="logoCat" alt="" class="aside-cat" />
          <span>喵喵书店后台</span>
        </div>
        <el-menu
          :default-active="$route.path"
          router
          background-color="#3D2B1F"
          text-color="#c8b8a8"
          active-text-color="#C4A35A"
        >
          <el-menu-item index="/admin/books">
            <el-icon><Notebook /></el-icon>
            <span>图书管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Grid /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/statistics">
            <el-icon><DataAnalysis /></el-icon>
            <span>统计分析</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="admin-header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <span style="margin-right:16px">{{ userStore.userInfo?.realName || userStore.username }}</span>
            <el-button text @click="goHome">
              <el-icon><HomeFilled /></el-icon> 前台
            </el-button>
            <el-button text @click="logout">
              <el-icon><SwitchButton /></el-icon> 退出
            </el-button>
          </div>
        </el-header>
        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Notebook, Grid, List, DataAnalysis, User, HomeFilled, SwitchButton } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import logoCat from '@/assets/logo-cat.jpg'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const titleMap = {
  '/admin/books': '图书管理',
  '/admin/categories': '分类管理',
  '/admin/orders': '订单管理',
  '/admin/statistics': '统计分析',
  '/admin/users': '用户管理'
}
const currentTitle = computed(() => titleMap[route.path] || '')

function goHome() { router.push('/') }
function logout() {
  ElMessageBox.confirm('确定退出？', '提示', { type: 'warning' }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.admin-layout { background: var(--cat-bg); }
.aside { background: #3D2B1F; overflow-y: auto; }
.aside-logo { height: 60px; display: flex; align-items: center; justify-content: center; color: #f5f0e8; font-size: 15px; font-weight: bold; gap: 8px; border-bottom: 1px solid #5c3a28; }
.aside-cat { width: 26px; height: 26px; border-radius: 50%; object-fit: cover; }
.el-menu { border-right: none; }
.admin-header { display: flex; align-items: center; justify-content: space-between; background: var(--cat-cream); box-shadow: 0 1px 4px rgba(44,24,16,0.08); padding: 0 20px; border-bottom: 1px solid var(--cat-border); }
.header-right { display: flex; align-items: center; gap: 8px; }
.admin-main { padding: 20px; }
</style>
