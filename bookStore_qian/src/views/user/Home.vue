<template>
  <div class="home-page">
    <div class="banner-section">
      <el-carousel height="420px" :interval="5000" arrow="hover" indicator-position="none" class="banner">
        <el-carousel-item v-for="(img, i) in banners" :key="i">
          <div class="banner-item" :style="{ background: img.bg }">
            <div class="banner-content">
              <div class="banner-badge">{{ img.badge }}</div>
              <h2 class="banner-title">{{ img.title }}</h2>
              <p class="banner-desc">{{ img.desc }}</p>
              <el-button class="banner-btn" size="large" @click="$router.push('/books')">
                立即选购 <el-icon style="margin-left:6px"><ArrowRight /></el-icon>
              </el-button>
            </div>
            <div class="banner-deco" v-if="img.mascot">
              <span class="banner-mascot">{{ img.mascot }}</span>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="home-container">
      <div class="section-block fade-in-up" style="animation-delay: 0.1s">
        <div class="section-header">
          <div class="section-title-wrap">
            <div class="section-icon"><el-icon size="18" color="#fff"><Collection /></el-icon></div>
            <h3>图书分类</h3>
          </div>
        </div>
        <el-row :gutter="16">
          <el-col :span="4" v-for="(cat, idx) in categories" :key="cat.id">
            <div class="cat-card" :style="{ animationDelay: idx * 0.05 + 's' }" @click="$router.push(`/books?categoryId=${cat.id}`)">
              <div class="cat-icon-wrap" :style="{ background: catColors[idx % catColors.length] }">
                <el-icon size="24" color="#fff"><component :is="catIcons[idx % catIcons.length]" /></el-icon>
              </div>
              <div class="cat-name">{{ cat.name }}</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="section-block fade-in-up" style="animation-delay: 0.2s">
        <div class="section-header">
          <div class="section-title-wrap">
            <div class="section-icon hot"><el-icon size="18" color="#fff"><TrendCharts /></el-icon></div>
            <h3>热门图书</h3>
          </div>
          <el-button class="more-btn" text type="primary" @click="$router.push('/books')">查看更多 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <el-row :gutter="16">
          <el-col :span="4" v-for="book in hotBooks" :key="book.id">
            <div class="book-card" @click="$router.push(`/book/${book.id}`)">
              <div class="book-cover-wrap">
                <BookCover :src="book.coverImage" :title="book.title" :author="book.author" width="100%" height="200px" />
                <div class="book-hover-mask">
                  <el-icon size="24" color="#fff"><View /></el-icon>
                </div>
                <el-tag class="promo-badge" v-if="book.onPromo" type="danger" size="small" effect="dark">HOT</el-tag>
              </div>
              <div class="book-info">
                <div class="book-title" :title="book.title">{{ book.title }}</div>
                <div class="book-author">{{ book.author }}</div>
                <div class="book-price">
                  <span class="final-price">¥{{ formatPrice(getFinalPrice(book)) }}</span>
                  <span class="original-price" v-if="getFinalPrice(book) < book.price">¥{{ formatPrice(book.price) }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="section-block fade-in-up" style="animation-delay: 0.3s" v-if="visibleGrabs.length > 0">
        <div class="section-header">
          <div class="section-title-wrap">
            <div class="section-icon grab"><el-icon size="18" color="#fff"><Timer /></el-icon></div>
            <h3>新书开抢</h3>
            <span class="section-sub">倒计时结束立即开抢，销量直通畅销榜</span>
          </div>
          <el-button class="more-btn" text type="primary" @click="$router.push('/books')">全部图书 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <el-row :gutter="16">
          <el-col :span="4" v-for="book in visibleGrabs" :key="book.id">
            <div class="book-card book-card-grab" @click="$router.push(`/book/${book.id}`)">
              <div class="book-cover-wrap">
                <BookCover :src="book.coverImage" :title="book.title" :author="book.author" width="100%" height="200px" />
                <el-tag class="grab-badge" type="danger" size="small" effect="dark">即将开抢</el-tag>
              </div>
              <div class="book-info">
                <div class="book-title" :title="book.title">{{ book.title }}</div>
                <div class="book-author">{{ book.author }}</div>
                <div class="book-price">
                  <span class="final-price">¥{{ formatPrice(getFinalPrice(book)) }}</span>
                </div>
                <div class="grab-countdown">
                  <el-icon class="grab-clock"><Clock /></el-icon>
                  <span class="grab-label">距开抢</span>
                  <span class="grab-time">{{ formatGrabRemain(calcGrabRemainSeconds(book.grabStartTime, now)) }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { getHotBooks, getUpcomingGrabs } from '@/api/book'
import { getCategoryList } from '@/api/category'
import { Collection, TrendCharts, View, ArrowRight, Timer, Clock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getFinalPrice, formatPrice } from '@/utils/constants'
import { formatGrabRemain, calcGrabRemainSeconds, getGrabTimeMs, syncServerTime } from '@/utils/countdown'
import { useUserStore } from '@/stores/user'
import BookCover from '@/components/BookCover.vue'

const router = useRouter()
const userStore = useUserStore()

const banners = [
  { bg: 'linear-gradient(rgba(74,44,26,0.5), rgba(74,44,26,0.5)), url(/banner-promo.jpg) 15% center/cover', mascot: '', badge: '限时特惠', title: '新书上架', desc: '猫咪店长亲自叼来的新书，低至7折' },
  { bg: 'linear-gradient(rgba(74,44,26,0.5), rgba(74,44,26,0.5)), url(/banner-member.jpg) center/cover', mascot: '', badge: '会员专享', title: '金卡会员最高享9折', desc: '铲屎官等级越高，折扣越香' },
  { bg: 'linear-gradient(rgba(74,44,26,0.5), rgba(74,44,26,0.5)), url(/banner-discount.jpg) center/cover', mascot: '', badge: '满减活动', title: '满200减30，满500减80', desc: '多买多省，猫粮钱都帮你省了' }
]

const catColors = [
  '#8B3A4A',
  '#4A3728',
  '#C4A35A',
  '#5c3a28',
  '#4A7C59',
  '#7a4a3a'
]
const catIcons = [Collection, Collection, Collection, Collection, Collection]

const categories = ref([])
const hotBooks = ref([])

// 开抢倒计时的本地秒表：每秒刷新 once，触发卡片 re-render
const now = ref(Date.now())
const grabBooks = ref([])
const visibleGrabs = computed(() =>
  grabBooks.value.filter(book => calcGrabRemainSeconds(book.grabStartTime, now.value) > 0)
)
let timer = null
// 倒计时归零并自动跳转的书（一本只跳一次，避免重复打扰）
const flippedGrabIds = new Set()

onMounted(async () => {
  // 以服务器时间为准校准本地时钟，保证"倒计时归零自动跳结算"与后端放行时刻精确对齐
  syncServerTime()
  timer = setInterval(() => {
    now.value = Date.now()
    checkGrabOpening()
  }, 1000)
  try {
    const [catRes, bookRes, grabRes] = await Promise.all([
      getCategoryList(),
      getHotBooks({ limit: 6 }),
      getUpcomingGrabs({ limit: 6 })
    ])
    categories.value = (catRes.data || []).slice(0, 6)
    hotBooks.value = bookRes.data || []
    grabBooks.value = grabRes.data || []
  } catch (e) {}
})

onBeforeUnmount(() => { clearInterval(timer) })

// 开抢时间归零时自动进入结算界面（未登录则先去详情页提示登录）
function checkGrabOpening() {
  if (!grabBooks.value.length) return
  const nowMs = now.value
  const opened = grabBooks.value.find(book => {
    if (flippedGrabIds.has(book.id)) return false
    const target = getGrabTimeMs(book.grabStartTime)
    return target && target <= nowMs
  })
  if (!opened) return
  flippedGrabIds.add(opened.id)
  ElMessage.success(`《${opened.title}》开抢了！正在快速进入结算...`)
  router.push(userStore.isLoggedIn ? `/checkout?bookId=${opened.id}` : `/book/${opened.id}`)
}
</script>

<style scoped>
.home-page { background: transparent; }
.banner-section { margin-bottom: 32px; }
.banner-item { height: 100%; display: flex; align-items: center; justify-content: space-between; padding: 0 10%; position: relative; overflow: hidden; }
.banner-content { position: relative; z-index: 2; }
.banner-badge { display: inline-block; padding: 4px 14px; background: rgba(196,163,90,0.25); backdrop-filter: blur(8px); border-radius: 20px; color: #f5f0e8; font-size: 13px; font-weight: 500; margin-bottom: 16px; border: 1px solid rgba(196,163,90,0.35); }
.banner-title { font-size: 42px; font-weight: 800; color: #f5f0e8; margin-bottom: 12px; line-height: 1.2; text-shadow: 0 2px 10px rgba(44,24,16,0.2); }
.banner-desc { font-size: 18px; color: rgba(245,240,232,0.85); margin-bottom: 28px; }
.banner-btn { background: #C4A35A !important; color: #2c1810 !important; border: none !important; font-weight: 600; padding: 12px 32px !important; border-radius: 25px !important; font-size: 15px !important; box-shadow: 0 4px 15px rgba(196,163,90,0.35); transition: all 0.3s !important; }
.banner-btn:hover { background: #d4b36a !important; transform: translateY(-2px); box-shadow: 0 6px 20px rgba(196,163,90,0.45) !important; }
.banner-deco { position: absolute; right: 7%; top: 50%; transform: translateY(-50%); z-index: 1; }
.banner-mascot { font-size: 140px; line-height: 1; display: inline-block; filter: drop-shadow(0 10px 18px rgba(44,24,16,0.2)); animation: mascot-bob 4s ease-in-out infinite; }
@keyframes mascot-bob { 0%, 100% { transform: translateY(0) rotate(-4deg); } 50% { transform: translateY(-14px) rotate(5deg); } }

.home-container { max-width: 1200px; margin: 0 auto; padding: 0 24px 40px; }
.section-block { background: var(--cat-cream); border-radius: 14px; padding: 24px; margin-bottom: 24px; border: 1px solid var(--cat-border); }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.section-title-wrap { display: flex; align-items: center; gap: 10px; }
.section-icon { width: 32px; height: 32px; background: #8B3A4A; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.section-icon.hot { background: #B33A3A; }
.section-icon.grab { background: #2c1810; }
.section-sub { font-size: 12px; color: #b0a090; font-weight: 400; }
.section-header h3 { font-size: 18px; color: #2c1810; margin: 0; font-weight: 700; }
.more-btn { font-size: 14px !important; }

.cat-card { cursor: pointer; text-align: center; padding: 20px 8px; border-radius: 12px; transition: all 0.3s; border: 1px solid transparent; }
.cat-card:hover { transform: translateY(-4px); border-color: var(--cat-border); box-shadow: 0 6px 20px rgba(44,24,16,0.08); }
.cat-icon-wrap { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; transition: transform 0.3s; color: #fff; }
.cat-card:hover .cat-icon-wrap { transform: scale(1.08) rotate(-6deg); }
.cat-name { font-size: 14px; color: #2c1810; font-weight: 600; }

.book-card { cursor: pointer; border-radius: 12px; overflow: hidden; transition: all 0.3s; background: var(--cat-cream); border: 1px solid var(--cat-border); }
.book-card:hover { transform: translateY(-6px); box-shadow: 0 12px 32px rgba(44,24,16,0.1); border-color: #c8b8a8; }
.book-cover-wrap { position: relative; border-radius: 8px; overflow: hidden; margin: 8px 8px 0; }
.book-hover-mask { position: absolute; inset: 0; background: rgba(44,24,16,0.45); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.3s; }
.book-card:hover .book-hover-mask { opacity: 1; }
.promo-badge { position: absolute; top: 8px; right: 8px; z-index: 2; }
.grab-badge { position: absolute; top: 8px; left: 8px; z-index: 2; }
.book-card-grab .book-cover-wrap { outline: 2px solid #C4A35A; }
.grab-countdown { margin-top: 10px; display: flex; align-items: center; gap: 6px; background: linear-gradient(90deg, rgba(139,58,74,0.1), rgba(196,163,90,0.15)); border: 1px solid rgba(139,58,74,0.25); border-radius: 8px; padding: 6px 10px; }
.grab-clock { color: #B33A3A; }
.grab-label { font-size: 12px; color: #8a7a6a; }
.grab-time { font-size: 15px; font-weight: 700; color: #B33A3A; font-variant-numeric: tabular-nums; }
.book-info { padding: 12px; }
.book-title { font-size: 14px; color: #2c1810; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.book-author { font-size: 12px; color: #8a7a6a; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.book-price { margin-top: 8px; display: flex; align-items: baseline; }
.final-price { color: #B33A3A; font-size: 17px; font-weight: 700; }
.original-price { color: #c0b0a0; font-size: 12px; text-decoration: line-through; margin-left: 6px; }
</style>
