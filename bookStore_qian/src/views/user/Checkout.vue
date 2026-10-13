<template>
  <div class="checkout-page" v-loading="loading">
    <h2>订单结算</h2>
    <div class="wait-banner" v-if="directMode && waitingForOpen">
      <el-icon class="wait-icon"><Timer /></el-icon>
      <span class="wait-text">已就位候场，服务器时间归零自动放行（无需刷新）</span>
      <span class="wait-count">{{ formatGrabRemainText(waitRemainSeconds) }}</span>
    </div>
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="section-card">
          <template #header><span>收货信息</span></template>
          <div class="addr-history" v-if="addressList.length > 0">
            <div class="addr-hint">常用地址，点击一键填充</div>
            <div class="addr-list">
              <div
                v-for="(a, i) in addressList"
                :key="i"
                class="addr-item"
                :class="{ active: form.address === a.address && form.receiverPhone === a.receiverPhone }"
                @click="fillAddress(a)"
              >
                <span class="addr-name">{{ a.receiverName }}</span>
                <span class="addr-phone">{{ a.receiverPhone }}</span>
                <span class="addr-text" :title="a.address">{{ a.address }}</span>
              </div>
            </div>
          </div>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="收货人" prop="receiverName">
              <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="receiverPhone">
              <el-input v-model="form.receiverPhone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="收货地址" prop="address">
              <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入详细地址" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="订单备注（选填）" />
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="section-card" style="margin-top:16px">
          <template #header>
            <span>订单商品</span>
            <el-tag v-if="directMode" type="danger" size="small" style="margin-left:8px">一键抢购</el-tag>
          </template>
          <el-table :data="items" style="width:100%">
            <el-table-column label="图书" min-width="200">
              <template #default="{ row }">
                <div style="display:flex;align-items:center;gap:8px">
                  <BookCover :src="row.bookCover" :title="row.bookTitle" width="40px" height="56px" />
                  <span>{{ row.bookTitle }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="100">
              <template #default="{ row }">¥{{ formatPrice(row.finalPrice) }}</template>
            </el-table-column>
            <el-table-column label="数量" width="150">
              <template #default="{ row }">
                <el-input-number
                  v-if="directMode"
                  v-model="directQty"
                  :min="1"
                  :max="Math.min(row.stock, 99)"
                  size="small"
                />
                <span v-else>{{ row.quantity }}</span>
              </template>
            </el-table-column>
            <el-table-column label="库存状态" width="110">
              <template #default="{ row }">
                <el-tag v-if="(row.stock ?? 0) <= 0" type="danger" size="small">已售罄</el-tag>
                <el-tag v-else-if="row.quantity > row.stock" type="warning" size="small">仅剩{{ row.stock }}件</el-tag>
                <el-tag v-else type="success" size="small">有货</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100">
              <template #default="{ row }">
                <span class="price">¥{{ formatPrice(row.subtotal) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="section-card summary-card">
          <template #header><span>折扣明细</span></template>
          <div class="summary-row">
            <span>商品总额</span>
            <span>¥{{ formatPrice(totalAmount) }}</span>
          </div>
          <div class="summary-row discount" v-if="bookDiscount > 0">
            <span>图书折扣</span>
            <span>-¥{{ formatPrice(bookDiscount) }}</span>
          </div>
          <div class="summary-row discount" v-if="memberDiscountAmount > 0">
            <span>{{ memberLevelName }}折扣（{{ (userStore.memberDiscount * 10).toFixed(1) }}折）</span>
            <span>-¥{{ formatPrice(memberDiscountAmount) }}</span>
          </div>
          <div class="summary-row discount" v-if="fullReduction > 0">
            <span>满减（满{{ reductionThreshold }}减{{ fullReduction }}）</span>
            <span>-¥{{ formatPrice(fullReduction) }}</span>
          </div>
          <el-divider />
          <div class="summary-row total">
            <span>应付金额</span>
            <span class="total-price">¥{{ formatPrice(payAmount) }}</span>
          </div>
          <el-button type="primary" size="large" style="width:100%;margin-top:16px" :loading="submitting" :disabled="directMode && waitingForOpen" @click="handleSubmit">
            {{ confirmText }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <!-- 未抢到结果大弹窗（仪式化结果卡） -->
    <transition name="fade">
      <div v-if="missShow" class="miss-mask">
        <div class="miss-card">
          <div class="miss-badge">
            <img class="miss-badge-img" src="/miss-cat.jpg" alt="猫猫" />
          </div>
          <h3 class="miss-title">很遗憾，没抢到</h3>
          <p class="miss-sub">猫咪店长已经把 <strong>{{ missBookTitle }}</strong> 卖光了</p>
          <p class="miss-line">下次手要更快呀，本喵也很难过 🐾</p>
          <div class="miss-actions">
            <el-button type="primary" size="large" @click="goBrowse">继续逛逛其他书</el-button>
            <el-button size="large" @click="goBack">返回图书详情</el-button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { createOrder, payOrder, getRecentAddresses } from '@/api/order'
import { getBookDetail } from '@/api/book'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer } from '@element-plus/icons-vue'
import { formatPrice, getFullReduction, getFinalPrice, MEMBER_LEVEL } from '@/utils/constants'
import { calcGrabRemainSeconds, syncServerTime, formatGrabRemainText } from '@/utils/countdown'
import BookCover from '@/components/BookCover.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const loading = ref(false)

// 一键抢购直购模式：/checkout?bookId=xx&qty=x 时绕过购物车，直接对单本图书结算
const directMode = !!route.query.bookId
const directBook = ref(null)
const initQty = Math.max(1, Number(route.query.qty) || 1)
const directQty = ref(initQty)
const addressList = ref([])

// 候场倒计时（演唱会"刷票"放行逻辑）：提前进入结算页集中等待，服务器时间到点即自动放开，不必临时刷新
const now = ref(Date.now())
const grabStartTime = ref(null)
const waitingForOpen = ref(false)
let countdownTimer = null

// 未抢到结果大弹窗
const missShow = ref(false)
const missBookTitle = ref('')
const missTargetFrom = ref('book')

const form = reactive({ receiverName: '', receiverPhone: '', address: '', remark: '' })
const rules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

const memberLevelName = computed(() => MEMBER_LEVEL[userStore.memberLevel] || '普通会员')

const items = computed(() => {
  if (directBook.value) {
    const f = getFinalPrice(directBook.value)
    return [{
      bookId: directBook.value.id,
      bookTitle: directBook.value.title,
      bookCover: directBook.value.coverImage,
      price: Number(directBook.value.price),
      finalPrice: f,
      quantity: directQty.value,
      stock: Number(directBook.value.stock),
      subtotal: f * directQty.value
    }]
  }
  return cartStore.items
})

const waitRemainSeconds = computed(() =>
  waitingForOpen.value ? calcGrabRemainSeconds(grabStartTime.value, now.value) : 0
)

const totalAmount = computed(() => items.value.reduce((s, i) => s + Number(i.finalPrice) * i.quantity, 0))
const memberDiscountAmount = computed(() => totalAmount.value * (1 - userStore.memberDiscount))
const afterMember = computed(() => totalAmount.value * userStore.memberDiscount)

const reductionThreshold = computed(() => {
  if (afterMember.value >= 500) return 500
  if (afterMember.value >= 200) return 200
  if (afterMember.value >= 100) return 100
  return 0
})
const fullReduction = computed(() => getFullReduction(afterMember.value))
const bookDiscount = computed(() => {
  return items.value.reduce((s, i) => {
    const original = Number(i.price) * i.quantity
    return s + (original - Number(i.finalPrice) * i.quantity)
  }, 0)
})
const payAmount = computed(() => Math.max(0, afterMember.value - fullReduction.value))

// 库存可用性预检：售罄或数量超库存的商品不允许进入支付流程
const unavailableItems = computed(() =>
  items.value.filter(i => (i.stock ?? 0) <= 0 || i.quantity > i.stock)
)

// 结算确认按钮文案：开抢等待期间禁用并显示剩余时间，自动放行后变为"已开抢，立即抢购"
const confirmText = computed(() => {
  if (waitingForOpen.value) return `距开抢 ${formatGrabRemainText(waitRemainSeconds.value)}`
  return '确认支付'
})

// 开始候场倒计时：高频刷新，服务器时间到点精确放开（开抢前不再"未开抢"弹回、慢一步）
function startWaitingForOpen() {
  waitingForOpen.value = true
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    now.value = Date.now()
    if (calcGrabRemainSeconds(grabStartTime.value, now.value) <= 0) {
      waitingForOpen.value = false
      clearInterval(countdownTimer)
      countdownTimer = null
      ElMessage.success('已开抢！请立即确认抢购')
    }
  }, 250)
}

function stopWaitingForOpen() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  waitingForOpen.value = false
}

// 常用收货地址：点击一键填充到表单（外卖式地址复用）
async function fetchAddresses() {
  try {
    const res = await getRecentAddresses()
    addressList.value = res.data || []
  } catch (e) {}
}

function fillAddress(a) {
  form.receiverName = a.receiverName
  form.receiverPhone = a.receiverPhone
  form.address = a.address
  ElMessage.success(`已选用「${a.receiverName}」的收货地址`)
}

async function loadDirectBook() {
  loading.value = true
  try {
    // 以服务器时间为基准校准本地时钟，保证"归零即放行"，开抢前不会因时钟偏差被误判慢一步
    await syncServerTime()
    const res = await getBookDetail(route.query.bookId)
    const b = res.data
    if (!b || b.status !== 1 || b.stock <= 0) {
      await ElMessageBox.alert('该图书已下架或售罄，请返回查看', '无法结算', { type: 'warning' }).catch(() => {})
      router.replace(`/book/${route.query.bookId}`)
      return
    }
    directBook.value = b
    directQty.value = Math.max(1, Math.min(initQty, Number(b.stock) || 1))
    grabStartTime.value = b.grabStartTime
    // 未开抢：不在结算页弹窗赶人，而是进入候场等待，服务器时间归零自动放开（开抢前不再因"未开抢"慢一步）
    if (b.grabNotStarted) {
      startWaitingForOpen()
    }
  } finally {
    loading.value = false
  }
}

// 未开抢期间禁止直接提交；watch 到服务器时间归零后自动允许
watch(waitingForOpen, (w) => {
  if (!w) {
    ElMessage.success('已开抢，可以抢购了')
  }
})

onMounted(async () => {
  fetchAddresses()
  if (directMode) {
    loadDirectBook()
  } else {
    cartStore.fetchCart()
  }
})

onBeforeUnmount(() => { stopWaitingForOpen() })

// 开抢前若被后端以"尚未开抢"拦截，静默短等后自动重试几次（消化闸门恰好打开的毫秒级截断）
// 仅在基本确定已开抢（进入候场等待后归零）时启用，避免无限循环
async function submitWithRetry(payload, retries = 4, waitMs = 150) {
  for (let i = 0; i < retries; i++) {
    try {
      return await createOrder(payload, { silent: true })
    } catch (e) {
      const isNotOpen = e.businessCode === 409 && /尚未开抢|未开抢/.test(e.message || '')
      if (isNotOpen && directMode && waitingForOpen.value === false && i < retries - 1) {
        await new Promise(r => setTimeout(r, waitMs))
        continue
      }
      throw e
    }
  }
  throw new Error('下单失败')
}

async function handleSubmit() {
  await formRef.value.validate()
  if (items.value.length === 0) {
    ElMessage.warning('暂无可结算的商品')
    return
  }
  if (unavailableItems.value.length > 0) {
    const names = unavailableItems.value.map(i => `《${i.bookTitle}》`).join('、')
    ElMessage.warning(`以下商品库存已变化，无法结算：${names}`)
    if (directMode) {
      router.replace(`/book/${directBook.value.id}`)
    } else {
      await cartStore.fetchCart()
      router.push('/cart')
    }
    return
  }
  if (directMode && waitingForOpen.value) {
    ElMessage.warning(`尚未开抢，剩余 ${formatGrabRemainText(waitRemainSeconds.value)}，请等待自动放行`)
    return
  }

  // 结算确认（抢购快速确认，一步直达付款）
  const goodsText = items.value.map(i => `《${i.bookTitle}》×${i.quantity}`).join('、')
  try {
    await ElMessageBox.confirm(
      `请确认购买：${goodsText}，应付 ¥${formatPrice(payAmount.value)}。确认后将创建订单并直接支付。`,
      '确认结算',
      { confirmButtonText: '确认支付', cancelButtonText: '再想想', type: 'warning' }
    )
  } catch (_) {
    return
  }

  submitting.value = true
  try {
    const payload = directMode
      ? { ...form, bookId: directBook.value.id, quantity: directQty.value }
      : { ...form }
    const res = await submitWithRetry(payload)
    await payOrder(res.data.id)
    ElMessage.success('支付成功')
    await cartStore.fetchCart()
    router.push(`/order/${res.data.id}`)
  } catch (e) {
    // 抢购失败（库存被其他用户抢完）：打开仪式化"很遗憾"结果卡
    if (e.businessCode === 409) {
      missBookTitle.value = directMode && directBook.value
        ? directBook.value.title
        : (items.value[0] && items.value[0].bookTitle) || '该图书'
      missTargetFrom.value = directMode ? 'book' : 'cart'
      missShow.value = true
      await cartStore.fetchCart()
    }
  } finally {
    submitting.value = false
  }
}

// 结果卡：继续逛逛
function goBrowse() {
  missShow.value = false
  router.push('/books')
}

// 结果卡：返回图书详情 / 购物车
function goBack() {
  missShow.value = false
  if (missTargetFrom.value === 'cart') {
    router.push('/cart')
  } else {
    router.push(directBook.value ? `/book/${directBook.value.id}` : '/books')
  }
}
</script>

<style scoped>
h2 { margin: 0 0 20px; color: #303133; }
.wait-banner { margin: 0 0 20px; padding: 12px 16px; background: linear-gradient(90deg, rgba(139,58,74,0.1), rgba(196,163,90,0.18)); border: 1px solid rgba(179,58,58,0.3); border-radius: 10px; display: flex; align-items: center; gap: 8px; }
.wait-icon { color: #B33A3A; }
.wait-text { font-size: 14px; color: #2c1810; font-weight: 700; flex: 1; }
.wait-count { font-size: 16px; font-weight: 800; color: #B33A3A; font-variant-numeric: tabular-nums; }
.section-card { margin-bottom: 0; }
.addr-history { margin-bottom: 16px; padding: 12px 14px; background: #f7f3ec; border: 1px dashed #d5cec5; border-radius: 10px; }
.addr-hint { font-size: 12px; color: #a08a70; margin-bottom: 10px; }
.addr-list { display: flex; flex-wrap: wrap; gap: 10px; }
.addr-item { display: flex; align-items: center; gap: 8px; padding: 8px 12px; background: #fff; border: 1px solid var(--cat-border); border-radius: 8px; cursor: pointer; transition: all 0.2s; max-width: 100%; }
.addr-item:hover { border-color: #8B3A4A; box-shadow: 0 2px 8px rgba(139,58,74,0.12); }
.addr-item.active { border-color: #8B3A4A; background: rgba(139,58,74,0.06); }
.addr-name { font-size: 13px; font-weight: 700; color: #2c1810; flex-shrink: 0; }
.addr-phone { font-size: 12px; color: #8a7a6a; flex-shrink: 0; }
.addr-text { font-size: 13px; color: #606266; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 300px; }
.summary-card :deep(.el-card__body) { padding: 16px; }
.summary-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; color: #606266; }
.summary-row.discount { color: #e6a23c; }
.summary-row.total { font-size: 16px; font-weight: bold; color: #303133; }
.total-price { color: #f56c6c; font-size: 24px; }
.price { color: #f56c6c; font-weight: bold; }

/* 未抢到结果卡 */
.fade-enter-active, .fade-leave-active { transition: opacity .25s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.miss-mask { position: fixed; inset: 0; background: rgba(44,24,16,0.5); backdrop-filter: blur(3px); display: flex; align-items: center; justify-content: center; z-index: 3000; }
.miss-card { width: 420px; max-width: 90vw; background: linear-gradient(160deg, #fffdf9, #f7f0e6); border: 1px solid var(--cat-border); border-radius: 20px; padding: 36px 30px 30px; text-align: center; box-shadow: 0 24px 60px rgba(44,24,16,0.3); }
.miss-badge { width: 96px; height: 96px; margin: 0 auto 18px; border-radius: 50%; overflow: hidden; box-shadow: 0 10px 26px rgba(139,58,74,0.35); border: 3px solid #fff; background: #fff; }
.miss-badge-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.miss-title { font-size: 22px; color: #B33A3A; margin: 0 0 8px; font-weight: 800; }
.miss-sub { font-size: 14px; color: #8a7a6a; margin: 0 0 6px; }
.miss-sub strong { color: #2c1810; }
.miss-line { font-size: 13px; color: #a08a70; margin: 0 0 24px; }
.miss-actions { display: flex; flex-direction: column; gap: 10px; }
.miss-actions .el-button { width: 100%; margin-left: 0; }
</style>