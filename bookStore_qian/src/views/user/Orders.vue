<template>
  <div class="orders-page">
    <h2>我的订单</h2>
    <el-card>
      <el-tabs v-model="activeStatus" @tab-change="loadOrders">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="待付款" name="0" />
        <el-tab-pane label="未发货" name="1" />
        <el-tab-pane label="已发货" name="2" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已取消" name="4" />
      </el-tabs>

      <div v-loading="loading">
        <CatEmpty v-if="orders.length === 0" description="还没有订单，猫主子在等第一单" />
        <el-card v-for="order in orders" :key="order.id" class="order-card" shadow="never">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <el-tag :type="ORDER_STATUS_COLOR[order.status]" size="small">{{ order.statusText || ORDER_STATUS[order.status] }}</el-tag>
          </div>
          <el-table :data="order.items" style="width:100%" size="small">
            <el-table-column label="图书" min-width="200">
              <template #default="{ row }">
                <div style="display:flex;align-items:center;gap:8px">
                  <BookCover :src="row.bookCover" :title="row.bookTitle" width="36px" height="48px" />
                  <span>{{ row.bookTitle }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="100">
              <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
            </el-table-column>
            <el-table-column label="数量" width="80" prop="quantity" />
            <el-table-column label="小计" width="100">
              <template #default="{ row }">
                <span class="price">¥{{ formatPrice(row.subtotal) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="order-footer">
            <div class="order-info">
              <span>下单时间：{{ order.createTime }}</span>
              <span v-if="order.payTime">支付时间：{{ order.payTime }}</span>
            </div>
            <div class="order-actions">
              <span class="pay-amount">实付：<strong>¥{{ formatPrice(order.payAmount) }}</strong></span>
              <el-button text type="primary" @click="$router.push(`/order/${order.id}`)">查看详情</el-button>
              <el-button v-if="order.status === 0" type="warning" size="small" @click="handlePay(order)">去付款</el-button>
              <el-button v-if="order.status === 2" type="success" size="small" @click="handleReceive(order)">确认收货</el-button>
              <el-button v-if="order.status === 0" type="danger" size="small" text @click="handleCancel(order)">取消订单</el-button>
            </div>
          </div>
        </el-card>
      </div>

      <el-pagination
        v-if="total > 0"
        style="margin-top:20px;justify-content:center"
        background layout="prev, pager, next, total"
        :total="total" :page-size="pageSize" v-model:current-page="currentPage"
        @current-change="loadOrders"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyOrders, payOrder, receiveOrder, cancelOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ORDER_STATUS, ORDER_STATUS_COLOR, formatPrice } from '@/utils/constants'
import BookCover from '@/components/BookCover.vue'
import CatEmpty from '@/components/CatEmpty.vue'

const activeStatus = ref('')
const loading = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

onMounted(() => loadOrders())

async function loadOrders() {
  loading.value = true
  try {
    const params = { current: currentPage.value, size: pageSize.value }
    if (activeStatus.value !== '') params.status = activeStatus.value
    const res = await getMyOrders(params)
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function handlePay(order) {
  await ElMessageBox.confirm(`确认支付 ¥${formatPrice(order.payAmount)}？`, '确认支付')
  await payOrder(order.id)
  ElMessage.success('支付成功')
  loadOrders()
}

async function handleReceive(order) {
  await ElMessageBox.confirm('确认已收到货物？', '确认收货')
  await receiveOrder(order.id)
  ElMessage.success('已确认收货')
  loadOrders()
}

async function handleCancel(order) {
  await ElMessageBox.confirm('确定取消该订单？', '取消订单', { type: 'warning' })
  await cancelOrder(order.id)
  ElMessage.success('已取消')
  loadOrders()
}
</script>

<style scoped>
h2 { margin: 0 0 20px; color: #303133; }
.order-card { margin-bottom: 16px; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.order-no { color: #606266; font-size: 14px; }
.order-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 12px; border-top: 1px solid #ebeef5; }
.order-info { display: flex; gap: 16px; font-size: 12px; color: #909399; }
.order-actions { display: flex; align-items: center; gap: 8px; }
.pay-amount { color: #f56c6c; margin-right: 8px; }
.price { color: #f56c6c; font-weight: bold; }
</style>
