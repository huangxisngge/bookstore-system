<template>
  <div class="user-layout">
    <el-header class="header">
      <div class="header-inner">
        <div class="header-left">
          <router-link to="/" class="logo">
            <img :src="logoCat" alt="喵喵书店" class="logo-cat" />
            <div class="logo-text-wrap">
              <span class="logo-text">喵喵书店</span>
              <span class="logo-slogan">好书不寂寞</span>
            </div>
          </router-link>
          <el-menu mode="horizontal" :ellipsis="false" router :default-active="$route.path" class="nav-menu">
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/books">全部图书</el-menu-item>
          </el-menu>
        </div>
        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0" class="cart-badge">
              <el-button class="cart-btn" text @click="$router.push('/cart')">
                <el-icon size="20"><ShoppingCart /></el-icon>
                <span style="margin-left:4px">购物车</span>
              </el-button>
            </el-badge>
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="34" :src="userStore.userInfo?.avatar || undefined" class="user-avatar">
                  {{ (userStore.userInfo?.realName || userStore.username || '').charAt(0) }}
                </el-avatar>
                <span class="user-name">{{ userStore.userInfo?.realName || userStore.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile"><el-icon><User /></el-icon> 个人信息</el-dropdown-item>
                  <el-dropdown-item command="orders"><el-icon><Document /></el-icon> 我的订单</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided><el-icon><Setting /></el-icon> 后台管理</el-dropdown-item>
                  <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button class="login-btn" @click="$router.push('/login')">登录</el-button>
            <el-button class="register-btn" type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view />
    </el-main>
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <img :src="logoCat" alt="" class="footer-cat" />
          <div class="footer-text">
            <div class="footer-logo">喵喵书店</div>
            <p class="footer-desc">好书不寂寞 · 每一本都在等它的读者</p>
          </div>
        </div>
        <div class="footer-copy">喵喵书店 © 2026 · 以书会友</div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ShoppingCart, ArrowDown, User, Document, Setting, SwitchButton } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import logoCat from '@/assets/logo-cat.jpg'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

onMounted(() => {
  if (userStore.isLoggedIn) {
    cartStore.fetchCart()
  }
})

function handleCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'orders') router.push('/orders')
  else if (cmd === 'admin') router.push('/admin')
  else if (cmd === 'logout') {
    ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
      userStore.logout()
      cartStore.items = []
      router.push('/')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.user-layout { min-height: 100vh; display: flex; flex-direction: column; }
.header {
  background: #3D2B1F;
  box-shadow: 0 2px 10px rgba(44, 24, 16, 0.25);
  padding: 0;
  z-index: 100;
  position: sticky;
  top: 0;
}
.header-inner { max-width: 1200px; margin: 0 auto; display: flex; align-items: center; justify-content: space-between; height: 64px; padding: 0 24px; }
.header-left { display: flex; align-items: center; }
.logo { display: flex; align-items: center; gap: 10px; text-decoration: none; margin-right: 36px; }
.logo-cat { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; transition: transform 0.25s ease; }
.logo:hover .logo-cat { transform: rotate(-6deg) scale(1.06); }
.logo-text-wrap { display: flex; flex-direction: column; line-height: 1.15; }
.logo-text { font-size: 18px; font-weight: 700; color: #f5f0e8; letter-spacing: 2px; }
.logo-slogan { font-size: 10px; color: rgba(245, 240, 232, 0.45); letter-spacing: 1px; }
.nav-menu { border-bottom: none !important; background: transparent; }
.nav-menu :deep(.el-menu-item) { color: rgba(245, 240, 232, 0.6) !important; font-size: 15px; border-bottom: none !important; height: 64px; line-height: 64px; }
.nav-menu :deep(.el-menu-item:hover) { color: #f5f0e8 !important; background: rgba(255,255,255,0.06) !important; }
.nav-menu :deep(.el-menu-item.is-active) { color: #f5f0e8 !important; background: rgba(255,255,255,0.08) !important; border-bottom: 2px solid #C4A35A !important; }
.header-right { display: flex; align-items: center; gap: 12px; }
.cart-btn { color: rgba(245, 240, 232, 0.78) !important; }
.cart-btn:hover { color: #f5f0e8 !important; }
.user-info { display: flex; align-items: center; cursor: pointer; padding: 4px 8px; border-radius: 20px; transition: background 0.2s; }
.user-info:hover { background: rgba(255, 255, 255, 0.06); }
.user-avatar { background: #8B3A4A; color: #fff; font-weight: bold; }
.user-name { margin-left: 8px; color: #f5f0e8; font-size: 14px; }
.login-btn { border-color: rgba(245, 240, 232, 0.3) !important; color: #f5f0e8 !important; background: transparent !important; }
.login-btn:hover { border-color: #f5f0e8 !important; background: rgba(255, 255, 255, 0.06) !important; }
.register-btn { background: #8B3A4A !important; border-color: #8B3A4A !important; box-shadow: 0 3px 10px rgba(139, 58, 74, 0.3); font-weight: 600; }
.register-btn:hover { background: #a85c6a !important; }
.main-content { flex: 1; background: transparent; padding: 0; min-height: calc(100vh - 64px); }
.footer { background: #3D2B1F; padding: 28px 24px; text-align: center; }
.footer-inner { max-width: 1200px; margin: 0 auto; }
.footer-brand { margin-bottom: 12px; display: inline-flex; align-items: center; gap: 12px; }
.footer-cat { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; opacity: 0.85; }
.footer-text { text-align: left; }
.footer-logo { font-size: 16px; font-weight: 700; color: #f5f0e8; letter-spacing: 1.5px; }
.footer-desc { color: rgba(245, 240, 232, 0.4); font-size: 12px; margin-top: 3px; }
.footer-copy { color: rgba(245, 240, 232, 0.25); font-size: 12px; }
</style>
