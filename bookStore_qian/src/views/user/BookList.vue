<template>
  <div class="book-list-page" style="max-width:1200px;margin:0 auto;padding:24px">
    <div class="filter-section">
      <div class="filter-header">
        <div class="filter-title"><el-icon size="18"><Search /></el-icon> 图书筛选</div>
      </div>
      <el-row :gutter="16" align="middle">
        <el-col :span="6">
          <el-select v-model="filters.categoryId" placeholder="全部分类" clearable @change="loadBooks">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-input v-model="filters.keyword" placeholder="搜索书名、作者、ISBN" clearable @keyup.enter="loadBooks">
            <template #append>
              <el-button @click="loadBooks"><el-icon><Search /></el-icon></el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filters.sort" placeholder="排序方式" @change="loadBooks">
            <el-option label="默认排序" value="" />
            <el-option label="价格从低到高" value="price_asc" />
            <el-option label="价格从高到低" value="price_desc" />
          </el-select>
        </el-col>
      </el-row>
    </div>

    <div class="book-grid" v-loading="loading">
      <CatEmpty v-if="!loading && books.length === 0" description="这个分类还没有书，猫猫正在补货" />
      <el-row :gutter="18">
        <el-col :span="4" v-for="book in books" :key="book.id">
          <div class="book-card" @click="$router.push(`/book/${book.id}`)">
            <div class="book-cover-wrap">
              <BookCover :src="book.coverImage" :title="book.title" :author="book.author" width="100%" height="220px" />
              <div class="book-hover-mask">
                <el-icon size="24" color="#fff"><View /></el-icon>
              </div>
              <el-tag class="promo-badge" v-if="book.grabNotStarted" type="danger" size="small" effect="dark">
                距开抢 {{ formatGrabRemain(calcGrabRemainSeconds(book.grabStartTime, now)) }}
              </el-tag>
              <el-tag class="promo-badge" v-else-if="book.onPromo" type="danger" size="small" effect="dark">促销</el-tag>
            </div>
            <div class="book-info">
              <div class="book-title" :title="book.title">{{ book.title }}</div>
              <div class="book-author">{{ book.author }}</div>
              <div class="book-price-row">
                <span class="final-price">¥{{ formatPrice(getFinalPrice(book)) }}</span>
                <span class="original-price" v-if="getFinalPrice(book) < Number(book.price)">¥{{ formatPrice(book.price) }}</span>
              </div>
              <div class="book-meta">已售 {{ book.sales || 0 }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-pagination
      v-if="total > 0"
      style="margin-top:28px;justify-content:center"
      background
      layout="prev, pager, next, total"
      :total="total"
      :page-size="pageSize"
      v-model:current-page="currentPage"
      @current-change="loadBooks"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { getBookList } from '@/api/book'
import { getCategoryList } from '@/api/category'
import { Search, View } from '@element-plus/icons-vue'
import { getFinalPrice, formatPrice } from '@/utils/constants'
import { formatGrabRemain, calcGrabRemainSeconds } from '@/utils/countdown'
import BookCover from '@/components/BookCover.vue'
import CatEmpty from '@/components/CatEmpty.vue'

const route = useRoute()
const loading = ref(false)
const books = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(18)

const now = ref(Date.now())
let timer = null

const filters = reactive({
  categoryId: route.query.categoryId || '',
  keyword: route.query.keyword || '',
  sort: ''
})

onMounted(async () => {
  timer = setInterval(() => { now.value = Date.now() }, 1000)
  const res = await getCategoryList()
  categories.value = res.data || []
  if (route.query.categoryId) filters.categoryId = Number(route.query.categoryId)
  loadBooks()
})

onBeforeUnmount(() => { clearInterval(timer) })

async function loadBooks() {
  loading.value = true
  try {
    const params = { current: currentPage.value, size: pageSize.value }
    if (filters.categoryId) params.categoryId = filters.categoryId
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.sort) params.sort = filters.sort
    const res = await getBookList(params)
    books.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}
</script>

<style scoped>
.filter-section { background: var(--cat-cream); border-radius: 12px; padding: 20px 24px; margin-bottom: 20px; border: 1px solid var(--cat-border); }
.filter-header { margin-bottom: 14px; }
.filter-title { font-size: 15px; font-weight: 600; color: #2c1810; display: flex; align-items: center; gap: 6px; }

.book-card { cursor: pointer; border-radius: 12px; overflow: hidden; transition: all 0.3s; background: var(--cat-cream); border: 1px solid var(--cat-border); margin-bottom: 18px; }
.book-card:hover { transform: translateY(-6px); box-shadow: 0 12px 32px rgba(44,24,16,0.1); border-color: #c8b8a8; }
.book-cover-wrap { position: relative; border-radius: 8px; overflow: hidden; margin: 8px 8px 0; }
.book-hover-mask { position: absolute; inset: 0; background: rgba(44,24,16,0.45); display: flex; align-items: center; justify-content: center; opacity: 0; transition: opacity 0.3s; }
.book-card:hover .book-hover-mask { opacity: 1; }
.promo-badge { position: absolute; top: 8px; right: 8px; z-index: 2; }
.book-info { padding: 12px; }
.book-title { font-size: 14px; color: #2c1810; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.book-author { font-size: 12px; color: #8a7a6a; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.book-price-row { margin-top: 8px; display: flex; align-items: baseline; }
.final-price { color: #B33A3A; font-size: 17px; font-weight: 700; }
.original-price { color: #c0b0a0; font-size: 12px; text-decoration: line-through; margin-left: 6px; }
.book-meta { font-size: 12px; color: #c0b0a0; margin-top: 4px; }
</style>
