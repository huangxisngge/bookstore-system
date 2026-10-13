<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand-section">
        <img :src="catHero" alt="喵喵书店店猫" class="brand-cat" />
        <h1>喵喵书店</h1>
        <p>注册领养一只会送书的猫</p>
        <div class="features">
          <div class="feature-item"><span class="feature-paw">🐾</span><span>新用户立享会员价</span></div>
          <div class="feature-item"><span class="feature-paw">🐾</span><span>猫咪店长每日荐书</span></div>
        </div>
      </div>
      <div class="deco-paws">
        <span v-for="n in 4" :key="n" :class="'paw paw-' + n">🐾</span>
      </div>
    </div>
    <div class="login-right">
      <div class="login-card">
        <div class="card-header">
          <h2>创建账户</h2>
          <p>注册新用户以享受更多优惠</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名（3-50个字符）" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码（6-100个字符）" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="realName">
            <el-input v-model="form.realName" placeholder="真实姓名（选填）" />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="form.phone" placeholder="手机号（选填）" />
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="form.email" placeholder="邮箱（选填）" />
          </el-form-item>
          <el-form-item>
            <el-button class="submit-btn" type="primary" :loading="loading" @click="handleRegister">注 册</el-button>
          </el-form-item>
        </el-form>
        <div class="login-footer">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import catHero from '@/assets/logo-cat.jpg'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '', confirmPassword: '', realName: '', phone: '', email: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次输入的密码不一致'))
  else callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '6-100个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    const { confirmPassword, ...data } = form
    await userStore.register(data)
    ElMessage.success('注册成功')
    router.push('/')
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
.brand-cat { width: 170px; display: block; border-radius: 50%; object-fit: cover; aspect-ratio: 1; margin-bottom: 10px; filter: drop-shadow(0 10px 22px rgba(0,0,0,0.3)); animation: cat-sway 4.5s ease-in-out infinite; }
@keyframes cat-sway { 0%, 100% { transform: rotate(-3deg); } 50% { transform: rotate(3deg) translateY(-5px); } }
.brand-section h1 { font-size: 36px; font-weight: 800; color: #f5f0e8; letter-spacing: 4px; margin-bottom: 8px; }
.brand-section > p { font-size: 15px; color: rgba(245,240,232,0.55); margin-bottom: 22px; }
.features { display: flex; flex-direction: column; gap: 11px; align-items: flex-start; }
.feature-item { display: flex; align-items: center; gap: 10px; color: rgba(245,240,232,0.7); font-size: 14px; font-weight: 500; }
.feature-paw { font-size: 15px; transform: rotate(-18deg); opacity: 0.5; color: #C4A35A; }

.deco-paws { position: absolute; inset: 0; z-index: 1; pointer-events: none; }
.paw { position: absolute; font-size: 26px; opacity: 0.06; animation: paw-float 7s ease-in-out infinite; }
.paw-1 { top: 10%; right: 10%; transform: rotate(24deg); }
.paw-2 { bottom: 14%; left: 7%; transform: rotate(-16deg); font-size: 34px; animation-delay: 1.2s; }
.paw-3 { top: 44%; left: 13%; transform: rotate(40deg); font-size: 20px; animation-delay: 2.4s; }
.paw-4 { bottom: 28%; right: 15%; transform: rotate(12deg); font-size: 30px; animation-delay: 3.6s; }
@keyframes paw-float { 0%, 100% { translate: 0 0; } 50% { translate: 0 -16px; } }

.login-right { width: 520px; display: flex; align-items: center; justify-content: center; padding: 40px; background: var(--cat-cream); border-left: 1px solid var(--cat-border); }
.login-card { width: 100%; max-width: 380px; }
.card-header { margin-bottom: 24px; }
.card-header h2 { font-size: 26px; font-weight: 700; color: #2c1810; margin-bottom: 6px; }
.card-header p { font-size: 14px; color: #8a7a6a; }
.submit-btn { width: 100% !important; height: 48px !important; font-size: 16px !important; font-weight: 600 !important; border-radius: 24px !important; background: #8B3A4A !important; border: none !important; box-shadow: 0 4px 14px rgba(139,58,74,0.3) !important; }
.submit-btn:hover { background: #a85c6a !important; transform: translateY(-1px); }
.login-footer { text-align: center; color: #8a7a6a; font-size: 14px; margin-top: 8px; }
.login-footer a { color: #8B3A4A; text-decoration: none; font-weight: 600; }
</style>
