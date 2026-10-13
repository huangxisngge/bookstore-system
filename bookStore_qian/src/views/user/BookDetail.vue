<template>
  <div class="book-detail-page" v-loading="loading">
    <div v-if="book" class="detail-container">
      <el-row :gutter="32">
        <el-col :span="10">
          <div class="cover-section">
            <div class="cover-wrap">
              <BookCover :src="book.coverImage" :title="book.title" :author="book.author" width="100%" height="420px" />
            </div>
            <div class="cover-meta" v-if="book.onPromo">
              <el-tag type="danger" effect="dark" size="small">限时促销中</el-tag>
            </div>
          </div>
        </el-col>
        <el-col :span="14">
          <div class="info-section">
            <div class="category-wrap" v-if="book.categoryName">
              <el-tag size="small" effect="plain">{{ book.categoryName }}</el-tag>
            </div>
            <h1 class="book-title">{{ book.title }}</h1>
            <div class="book-meta">
              <span class="meta-item"><el-icon><User /></el-icon> {{ book.author }}</span>
              <span class="meta-item"><el-icon><Document /></el-icon> {{ book.isbn }}</span>
              <span class="meta-item"><el-icon><OfficeBuilding /></el-icon> {{ book.publisher }}</span>
            </div>

            <div class="price-section">
              <div class="price-main">
                <span class="price-label">折后价</span>
                <span class="final-price">¥{{ formatPrice(currentPrice) }}</span>
                <el-tag type="danger" size="small" v-if="book.onPromo">促销价 ¥{{ formatPrice(book.promoPrice) }}</el-tag>
              </div>
              <div class="price-row" v-if="currentPrice < Number(book.price)">
                <span class="price-label">原价</span>
                <span class="original-price">¥{{ formatPrice(book.price) }}</span>
              </div>
              <div class="price-row">
                <span class="price-label">折扣</span>
                <span class="discount-text">{{ (Number(book.discount) * 10).toFixed(1) }}折</span>
              </div>
              <div class="price-row" v-if="userStore.isLoggedIn && userStore.memberDiscount < 1">
                <span class="price-label">会员价</span>
                <span class="member-text">{{ memberLevelName }}再享 {{ (userStore.memberDiscount * 10).toFixed(1) }}折</span>
              </div>
            </div>

            <div class="stock-info">
              <div class="stock-item">库存 <strong>{{ book.stock }}</strong></div>
              <div class="stock-divider"></div>
              <div class="stock-item">已售 <strong>{{ book.sales || 0 }}</strong></div>
            </div>

            <div class="grab-panel" v-if="grabNotStarted">
              <div class="grab-panel-head">
                <el-icon class="grab-flame"><Timer /></el-icon>
                <span class="grab-panel-title">距离新书开抢还有</span>
              </div>
              <div class="grab-nums">
                <span class="grab-num">{{ grabNum.dd }}</span><span class="grab-unit">天</span>
                <span class="grab-num">{{ grabNum.hh }}</span><span class="grab-unit">时</span>
                <span class="grab-num">{{ grabNum.mm }}</span><span class="grab-unit">分</span>
                <span class="grab-num">{{ grabNum.ss }}</span><span class="grab-unit">秒</span>
              </div>
              <div class="grab-hint">开抢时间 {{ formatGrabDate(book.grabStartTime) }}，到点后开抢，销量直通畅销榜</div>
            </div>

            <div class="action-row" v-if="book.status === 1">
              <el-input-number v-model="quantity" :min="1" :max="Math.min(book.stock, 99)" size="large" :disabled="grabStillWaiting" />
              <el-button v-if="isGrabBook" class="buy-btn grab-cta" size="large" :disabled="book.stock <= 0 || grabStillWaiting" @click="handleBuyNow">
                <el-icon><Lightning /></el-icon> {{ grabStillWaiting ? '未开抢，敬请期待' : '已开抢，立即抢购' }}
              </el-button>
              <template v-else>
                <el-button class="cart-btn" type="primary" size="large" :disabled="book.stock <= 0" @click="handleAddCart">
                  <el-icon><ShoppingCart /></el-icon> 加入购物车
                </el-button>
                <el-button class="buy-btn" size="large" :disabled="book.stock <= 0" @click="handleBuyNow">
                  <el-icon><Lightning /></el-icon> 立即购买
                </el-button>
              </template>
            </div>
            <div class="grab-auto-hint" v-if="grabStillWaiting">倒计时归零将自动进入结算，无需手动刷新</div>
            <div v-if="book.status !== 1" class="off-shelf">该书已下架</div>

            <div class="desc-section">
              <h4>图书简介</h4>
              <p>{{ book.description || '暂无简介' }}</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getBookDetail } from '@/api/book'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ShoppingCart, User, Document, OfficeBuilding, Timer, Lightning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getFinalPrice, formatPrice, MEMBER_LEVEL } from '@/utils/constants'
import { calcGrabRemainSeconds, pad2, syncServerTime } from '@/utils/countdown'
import BookCover from '@/components/BookCover.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const book = ref(null)
const loading = ref(true)
const quantity = ref(1)

const now = ref(Date.now())
let timer = null
// 开抢跳转只触发一次，避免重复弹提示/重复跳转
const flippedToBuy = ref(false)

const currentPrice = computed(() => book.value ? getFinalPrice(book.value) : 0)
const memberLevelName = computed(() => MEMBER_LEVEL[userStore.memberLevel] || '普通会员')

const grabNotStarted = computed(() => !!book.value?.grabNotStarted)
// 是否为开抢图书（grab_start_time 非空，含已开抢）
const isGrabBook = computed(() => !!book.value?.grabStartTime)
// 仍在等待开抢：结合本地时钟判定，倒计时归零瞬间自动松开并切换按钮文案
const grabStillWaiting = computed(() =>
  !!book.value?.grabNotStarted && calcGrabRemainSeconds(book.value?.grabStartTime, now.value) > 0
)
const grabNum = computed(() => {
  const s = calcGrabRemainSeconds(book.value?.grabStartTime, now.value)
  return {
    dd: pad2(Math.floor(s / 86400)),
    hh: pad2(Math.floor((s % 86400) / 3600)),
    mm: pad2(Math.floor((s % 3600) / 60)),
    ss: pad2(s % 60)
  }
})

// 倒计时归零 → 自动进入结算界面，无需用户手动刷新
watch(() => calcGrabRemainSeconds(book.value?.grabStartTime, now.value), (remain) => {
  if (!book.value?.grabNotStarted || flippedToBuy.value) return
  if (remain <= 0) {
    flippedToBuy.value = true
    ElMessage.success('开抢时间到！正在快速进入结算...')
    router.push(`/checkout?bookId=${book.value.id}`)
  }
})

function formatGrabDate(ts) {
  if (!ts) return ''
  return String(ts).replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  syncServerTime()
  timer = setInterval(() => { now.value = Date.now() }, 1000)
  try {
    const res = await getBookDetail(route.params.id)
    book.value = res.data
  } finally { loading.value = false }
})

onBeforeUnmount(() => { clearInterval(timer) })

async function handleAddCart() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  try {
    await cartStore.addItem(book.value.id, quantity.value)
    ElMessage.success('已加入购物车')
  } catch (e) {}
}

// 立即购买（一键抢购）：直接进入结算页，收货地址与订单商品已预填
function handleBuyNow() {
  if (!userStore.isLoggedIn) { ElMessage.warning('请先登录'); return }
  if (book.value.stock <= 0 || !book.value.status) { ElMessage.warning('该图书当前不可购买'); return }
  router.push(`/checkout?bookId=${book.value.id}&qty=${quantity.value}`)
}
</script>

<style scoped>
.detail-container { max-width: 1100px; margin: 0 auto; padding: 28px; }
.cover-section { background: #fff; border-radius: 16px; overflow: hidden; padding: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); position: sticky; top: 88px; }
.cover-wrap { border-radius: 12px; overflow: hidden; }
.cover-meta { margin-top: 12px; text-align: center; }

.info-section { background: var(--cat-cream); border-radius: 14px; padding: 28px; border: 1px solid var(--cat-border); }
.category-wrap { margin-bottom: 10px; }
.book-title { font-size: 26px; color: #2c1810; margin: 0 0 14px; font-weight: 700; line-height: 1.3; }
.book-meta { display: flex; flex-wrap: wrap; gap: 16px; margin-bottom: 18px; }
.meta-item { display: flex; align-items: center; gap: 4px; color: #8a7a6a; font-size: 14px; }

.price-section { background: #f0ebe4; padding: 18px 20px; border-radius: 10px; margin-bottom: 18px; border-left: 3px solid #8B3A4A; }
.price-main { display: flex; align-items: baseline; gap: 10px; margin-bottom: 8px; }
.price-label { color: #8a7a6a; width: 56px; flex-shrink: 0; font-size: 14px; }
.final-price { color: #B33A3A; font-size: 32px; font-weight: 800; }
.price-row { display: flex; align-items: baseline; margin-top: 4px; }
.original-price { color: #c0b0a0; text-decoration: line-through; font-size: 16px; }
.discount-text { color: #8B3A4A; font-weight: 700; }
.member-text { color: #8B3A4A; font-weight: 700; }

.stock-info { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; padding: 12px 0; border-top: 1px solid var(--cat-border); border-bottom: 1px solid var(--cat-border); }
.stock-item { font-size: 14px; color: #8a7a6a; }
.stock-item strong { color: #2c1810; font-weight: 700; }
.stock-divider { width: 1px; height: 16px; background: #d5cec5; }

.grab-panel { border: 1px solid rgba(179,58,58,0.35); border-radius: 12px; padding: 16px 20px; margin-bottom: 20px; background: linear-gradient(135deg, rgba(139,58,74,0.08), rgba(196,163,90,0.15)); }
.grab-panel-head { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.grab-flame { color: #B33A3A; }
.grab-panel-title { font-size: 15px; font-weight: 700; color: #2c1810; }
.grab-nums { display: flex; align-items: baseline; gap: 8px; }
.grab-num { display: inline-flex; min-width: 52px; justify-content: center; padding: 8px 6px; background: #2c1810; color: #f5f0e8; font-size: 28px; font-weight: 800; border-radius: 8px; font-variant-numeric: tabular-nums; }
.grab-unit { color: #8a7a6a; font-size: 13px; margin-right: 4px; }
.grab-hint { margin-top: 10px; font-size: 12px; color: #a08a70; }

.action-row { display: flex; gap: 16px; align-items: center; margin-bottom: 24px; }
.cart-btn { height: 46px !important; padding: 0 36px !important; font-size: 16px !important; font-weight: 600 !important; border-radius: 23px !important; background: #8B3A4A !important; border: none !important; box-shadow: 0 4px 14px rgba(139,58,74,0.3) !important; }
.cart-btn:hover { background: #a85c6a !important; box-shadow: 0 6px 18px rgba(139,58,74,0.4) !important; }
.buy-btn { height: 46px !important; padding: 0 32px !important; font-size: 16px !important; font-weight: 600 !important; border-radius: 23px !important; background: #B33A3A !important; border: none !important; color: #fff !important; box-shadow: 0 4px 14px rgba(179,58,58,0.3) !important; }
.buy-btn:hover { background: #d45252 !important; box-shadow: 0 6px 18px rgba(179,58,58,0.4) !important; }
.buy-btn.grab-cta { padding: 0 44px !important; font-size: 17px !important; box-shadow: 0 5px 18px rgba(179,58,58,0.38) !important; }
.grab-auto-hint { margin: -14px 0 24px; font-size: 13px; color: #B33A3A; background: rgba(179,58,58,0.06); border: 1px dashed rgba(179,58,58,0.3); border-radius: 8px; padding: 8px 12px; display: inline-block; }
.off-shelf { color: #8a7a6a; margin: 16px 0; font-size: 15px; }

.desc-section { margin-top: 8px; }
.desc-section h4 { color: #2c1810; margin-bottom: 10px; font-size: 16px; font-weight: 700; }
.desc-section p { color: #606266; line-height: 1.9; font-size: 14px; }
</style>
