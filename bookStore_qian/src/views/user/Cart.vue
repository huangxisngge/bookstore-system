<template>
  <div class="cart-page">
    <h2>我的购物车</h2>
    <el-card v-loading="cartStore.loading">
      <CatEmpty v-if="cartStore.items.length === 0" description="猫咪打了个盹，购物车就空了">
        <el-button type="primary" round @click="$router.push('/books')">带猫去逛逛</el-button>
      </CatEmpty>
      <template v-else>
        <el-table ref="tableRef" :data="cartStore.items" style="width:100%" @selection-change="handleSelectChange">
          <el-table-column type="selection" width="50" :selectable="row => !row.grabNotStarted && (row.stock ?? 0) > 0 && row.quantity <= row.stock" />
          <el-table-column label="图书" min-width="240">
            <template #default="{ row }">
              <div class="cart-book" @click="$router.push(`/book/${row.bookId}`)">
                <BookCover :src="row.bookCover" :title="row.bookTitle" width="60px" height="80px" />
                <span class="book-title">{{ row.bookTitle }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              <span class="price">¥{{ formatPrice(row.finalPrice) }}</span>
              <el-tag size="small" v-if="row.onPromo" type="danger" style="margin-left:4px">促销</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="库存状态" width="150">
            <template #default="{ row }">
              <el-tag v-if="row.grabNotStarted" type="danger" size="small">距开抢 {{ formatGrabRemainText(calcGrabRemainSeconds(row.grabStartTime, now)) }}</el-tag>
              <el-tag v-else-if="(row.stock ?? 0) <= 0" type="danger" size="small">已售罄</el-tag>
              <el-tag v-else-if="row.quantity > row.stock" type="warning" size="small">仅剩{{ row.stock }}件</el-tag>
              <el-tag v-else type="success" size="small">有货</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="160">
            <template #default="{ row }">
              <el-input-number
                :model-value="row.quantity"
                :min="1"
                :max="Math.max(1, Math.min(row.stock ?? 0, 99))"
                :disabled="(row.stock ?? 0) <= 0 || row.grabNotStarted"
                size="small"
                @change="(val) => handleQuantityChange(row.id, val)"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              <span class="price">¥{{ formatPrice(row.subtotal) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="danger" text size="small" @click="handleRemove(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="cart-footer">
          <div class="footer-left">
            <el-checkbox v-model="isAllSelected" @change="toggleSelectAll">全选</el-checkbox>
            <el-button text type="danger" @click="handleClear">清空购物车</el-button>
          </div>
          <div class="footer-right">
            <span>已选 <strong>{{ selectedItems.length }}</strong> 件</span>
            <span class="total-label">合计：</span>
            <span class="total-price">¥{{ formatPrice(selectedTotal) }}</span>
            <el-button type="primary" size="large" :disabled="selectedItems.length === 0" @click="goCheckout">
              去结算 ({{ selectedItems.length }})
            </el-button>
          </div>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { ElMessageBox, ElMessage } from 'element-plus'
import { formatPrice } from '@/utils/constants'
import { calcGrabRemainSeconds, formatGrabRemainText } from '@/utils/countdown'
import BookCover from '@/components/BookCover.vue'
import CatEmpty from '@/components/CatEmpty.vue'

const router = useRouter()
const cartStore = useCartStore()
const tableRef = ref(null)
const selectedItems = ref([])
const isAllSelected = ref(false)

const now = ref(Date.now())
let timer = null

const selectedTotal = computed(() =>
  selectedItems.value.reduce((sum, i) => sum + Number(i.finalPrice) * i.quantity, 0)
)

onMounted(() => {
  cartStore.fetchCart()
  timer = setInterval(() => { now.value = Date.now() }, 1000)
})

onBeforeUnmount(() => { clearInterval(timer) })

function handleSelectChange(sel) {
  selectedItems.value = sel
  isAllSelected.value = sel.length === cartStore.items.length && cartStore.items.length > 0
}

function toggleSelectAll(val) {
  if (!tableRef.value) return
  if (val) {
    cartStore.items.forEach(row => tableRef.value.toggleRowSelection(row, true))
  } else {
    tableRef.value.clearSelection()
  }
}

async function handleQuantityChange(cartId, val) {
  if (val === undefined || val === null) return
  await cartStore.updateQuantity(cartId, val)
}

async function handleRemove(cartId) {
  await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
  await cartStore.removeItem(cartId)
  ElMessage.success('已删除')
}

async function handleClear() {
  await ElMessageBox.confirm('确定清空购物车？', '提示', { type: 'warning' })
  await cartStore.clearAll()
  ElMessage.success('已清空')
}

function goCheckout() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请至少选择一件商品')
    return
  }
  const soldOut = selectedItems.value.filter(i => (i.stock ?? 0) <= 0)
  const shortStock = selectedItems.value.filter(i => (i.stock ?? 0) > 0 && i.quantity > i.stock)
  const notStarted = selectedItems.value.filter(i => i.grabNotStarted)
  if (notStarted.length > 0) {
    ElMessage.warning(`《${notStarted.map(i => i.bookTitle).join('》《')}》尚未开抢，请等待开抢倒计时结束后再结算`)
    return
  }
  if (soldOut.length > 0) {
    ElMessage.warning(`很遗憾，《${soldOut.map(i => i.bookTitle).join('》《')}》已被抢光，请移除后再结算`)
    return
  }
  if (shortStock.length > 0) {
    ElMessage.warning(`《${shortStock[0].bookTitle}》仅剩 ${shortStock[0].stock} 件，请调整购买数量`)
    return
  }
  router.push('/checkout')
}
</script>

<style scoped>
h2 { margin: 0 0 20px; color: #303133; }
.cart-book { display: flex; align-items: center; gap: 12px; cursor: pointer; }
.book-title { color: #303133; }
.price { color: #f56c6c; font-weight: bold; }
.cart-footer { display: flex; align-items: center; justify-content: space-between; margin-top: 20px; padding-top: 16px; border-top: 1px solid #ebeef5; }
.footer-left { display: flex; align-items: center; gap: 16px; }
.footer-right { display: flex; align-items: center; gap: 12px; }
.total-label { font-size: 14px; color: #606266; }
.total-price { color: #f56c6c; font-size: 24px; font-weight: bold; }
.img-placeholder { display: flex; align-items: center; justify-content: center; background: #f5f7fa; color: #c0c4cc; border-radius: 4px; }
</style>
