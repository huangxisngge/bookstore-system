<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand-section">
        <img :src="catHero" alt="喵喵书店店猫" class="brand-cat" />
        <h1>喵喵书店</h1>
        <p>让每本书，都找到爱它的铲屎官</p>
        <div class="features">
          <div class="feature-item" v-for="f in features" :key="f.text">
            <span class="feature-paw">🐾</span>
            <span>{{ f.text }}</span>
          </div>
        </div>
      </div>
      <div class="deco-paws">
        <span v-for="n in 5" :key="n" :class="'paw paw-' + n">🐾</span>
      </div>
    </div>
    <div class="login-right">
      <div class="login-card">
        <div class="card-header">
          <h2>欢迎回来</h2>
          <p>登录您的账户以继续</p>
        </div>
        <div class="login-tabs">
          <div :class="['tab-item', { active: loginType === 'user' }]" @click="loginType = 'user'">
            <el-icon><User /></el-icon> 用户登录
          </div>
          <div :class="['tab-item', { active: loginType === 'admin' }]" @click="loginType = 'admin'">
            <el-icon><Setting /></el-icon> 管理员
          </div>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          <el-form-item>
            <el-button class="submit-btn" type="primary" :loading="loading" @click="handleLogin">
              {{ loginType === 'admin' ? '管理员登录' : '用户登录' }}
            </el-button>
          </el-form-item>
        </el-form>
        <div class="login-footer">
          <template v-if="loginType === 'user'">
            还没有账号？<router-link to="/register">立即注册</router-link>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import { User, Setting } from '@element-plus/icons-vue'
import catHero from '@/assets/logo-cat.jpg'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const formRef = ref(null)
const loading = ref(false)
const loginType = ref('user')

const features = [
  { text: '猫咪店长亲自选书' },
  { text: '会员专属折扣' },
  { text: '满减优惠活动' },
  { text: '不满意就挠沙发——七天无理由退换' }
]

const form = reactive({ username: 'admin', password: '123456' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    await cartStore.fetchCart()

    if (loginType.value === 'admin') {
      if (!userStore.isAdmin) {
        ElMessage.warning('该账号不是管理员账号，请使用管理员账号登录')
        userStore.logout()
        return
      }
      router.push('/admin')
    } else {
      if (userStore.isAdmin) {
        ElMessage.warning('管理员账号请使用管理员登录入口')
        userStore.logout()
        return
      }
      router.push(route.query.redirect || '/')
    }
  } catch (e) {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { min-height: calc(100vh - 64px); display: flex; }

.login-left {
  flex: 1;
  background:
    radial-gradient(circle at 20% 15%, rgba(139, 58, 74, 0.15) 0%, transparent 45%),
    linear-gradient(160deg, #4A3728 0%, #3D2B1F 50%, #2c1810 100%);
  display: flex; align-items: center; justify-content: center; padding: 60px;
  position: relative; overflow: hidden;
}
.brand-section { position: relative; z-index: 2; max-width: 400px; display: flex; flex-direction: column; align-items: center; text-align: center; }
.brand-cat { width: 200px; display: block; border-radius: 50%; object-fit: cover; aspect-ratio: 1; filter: drop-shadow(0 10px 22px rgba(0,0,0,0.3)); animation: cat-sway 4.5s ease-in-out infinite; }
@keyframes cat-sway { 0%, 100% { transform: rotate(-3deg); } 50% { transform: rotate(3deg) translateY(-5px); } }
.brand-section h1 { font-size: 38px; font-weight: 800; color: #f5f0e8; letter-spacing: 4px; margin-bottom: 8px; }
.brand-section > p { font-size: 15px; color: rgba(245,240,232,0.55); margin-bottom: 30px; }
.features { display: flex; flex-direction: column; gap: 13px; align-items: flex-start; }
.feature-item { display: flex; align-items: center; gap: 10px; color: rgba(245,240,232,0.7); font-size: 14px; font-weight: 500; }
.feature-paw { font-size: 15px; transform: rotate(-18deg); opacity: 0.5; color: #C4A35A; }

.deco-paws { position: absolute; inset: 0; z-index: 1; pointer-events: none; }
.paw { position: absolute; font-size: 26px; opacity: 0.06; animation: paw-float 7s ease-in-out infinite; }
.paw-1 { top: 8%; right: 10%; transform: rotate(24deg); }
.paw-2 { bottom: 14%; left: 7%; transform: rotate(-16deg); font-size: 34px; animation-delay: 1.2s; }
.paw-3 { top: 42%; left: 12%; transform: rotate(40deg); font-size: 20px; animation-delay: 2.4s; }
.paw-4 { top: 16%; left: 32%; transform: rotate(-30deg); animation-delay: 3.1s; }
.paw-5 { bottom: 26%; right: 16%; transform: rotate(12deg); font-size: 30px; animation-delay: 4s; opacity: 0.05; }
@keyframes paw-float { 0%, 100% { translate: 0 0; } 50% { translate: 0 -16px; } }

.login-right { width: 520px; display: flex; align-items: center; justify-content: center; padding: 60px; background: var(--cat-cream); border-left: 1px solid var(--cat-border); }
.login-card { width: 100%; max-width: 380px; }
.card-header { margin-bottom: 28px; }
.card-header h2 { font-size: 26px; font-weight: 700; color: #2c1810; margin-bottom: 6px; }
.card-header p { font-size: 14px; color: #8a7a6a; }

.login-tabs { display: flex; margin-bottom: 28px; background: #ede6dc; border-radius: 10px; padding: 4px; }
.tab-item { flex: 1; text-align: center; padding: 10px 0; cursor: pointer; font-size: 14px; color: #8a7a6a; transition: all 0.3s; display: flex; align-items: center; justify-content: center; gap: 6px; border-radius: 8px; font-weight: 500; }
.tab-item:hover { color: #8B3A4A; }
.tab-item.active { background: #fff; color: #8B3A4A; box-shadow: 0 2px 8px rgba(44,24,16,0.08); }

.submit-btn { width: 100% !important; height: 48px !important; font-size: 16px !important; font-weight: 600 !important; border-radius: 24px !important; background: #8B3A4A !important; border: none !important; box-shadow: 0 4px 14px rgba(139,58,74,0.3) !important; }
.submit-btn:hover { background: #a85c6a !important; transform: translateY(-1px); box-shadow: 0 6px 18px rgba(139,58,74,0.4) !important; }

.login-footer { text-align: center; color: #8a7a6a; font-size: 14px; margin-top: 8px; }
.login-footer a { color: #8B3A4A; text-decoration: none; font-weight: 600; }
</style>
