<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="keyword" placeholder="搜索订单号/收货人" clearable style="width:220px" @keyup.enter="loadOrders" />
          <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width:140px;margin-left:12px" @change="loadOrders">
            <el-option label="未付款" :value="0" />
            <el-option label="未发货" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="待退货审核" :value="ORDER_STATUS_RETURN_PENDING" />
          </el-select>
        </div>
      </div>

      <div v-loading="loading" style="margin-top:12px">
        <CatEmpty v-if="orders.length === 0" description="暂无订单" />
        <el-card v-for="order in orders" :key="order.id" class="order-card" shadow="never">
          <div class="order-header">
            <div>
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ order.createTime }}</span>
            </div>
            <el-tag :type="orderTagType(order)" size="small">{{ orderText(order) }}</el-tag>
          </div>
          <el-table :data="order.items" style="width:100%" size="small">
            <el-table-column label="图书" min-width="200">
              <template #default="{ row }">{{ row.bookTitle }}</template>
            </el-table-column>
            <el-table-column label="单价" width="100">
              <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
            </el-table-column>
            <el-table-column label="数量" width="80" prop="quantity" />
            <el-table-column label="小计" width="100">
              <template #default="{ row }"><span class="price">¥{{ formatPrice(row.subtotal) }}</span></template>
            </el-table-column>
          </el-table>
          <div class="order-footer">
            <div>
              <span>收货人：{{ order.receiverName }} | {{ order.receiverPhone }}</span>
              <span style="margin-left:12px">地址：{{ order.address }}</span>
            </div>
            <div class="order-actions">
              <span class="pay-amount">实付：<strong>¥{{ formatPrice(order.payAmount) }}</strong></span>
              <el-button v-if="order.status === 1" type="primary" size="small" @click="handleShip(order)">发货</el-button>
              <el-button v-if="order.pendingReturnCount > 0" type="warning" size="small" @click="openReturnAudit(order)">退货审核</el-button>
              <el-button text type="primary" size="small" @click="showDetail(order)">详情</el-button>
            </div>
          </div>
        </el-card>
      </div>

      <el-pagination
        v-if="total > 0" style="margin-top:16px;justify-content:center"
        background layout="prev, pager, next, total"
        :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadOrders"
      />
    </el-card>

    <el-drawer v-model="drawerVisible" title="订单详情" size="500px">
      <template v-if="detailOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ detailOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="ORDER_STATUS_COLOR[detailOrder.status]">{{ detailOrder.statusText || ORDER_STATUS[detailOrder.status] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detailOrder.receiverName }} {{ detailOrder.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ detailOrder.address }}</el-descriptions-item>
          <el-descriptions-item label="商品总额">¥{{ formatPrice(detailOrder.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="折扣优惠">-¥{{ formatPrice(detailOrder.discountAmount) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额"><strong class="price">¥{{ formatPrice(detailOrder.payAmount) }}</strong></el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ detailOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ detailOrder.payTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detailOrder.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <el-dialog v-model="returnDialogVisible" title="退货审核" width="560px">
      <div v-if="pendingReturns.length === 0" class="empty-tip">该订单没有待审核的退货申请</div>
      <div v-for="r in pendingReturns" :key="r.id" class="return-item">
        <div class="return-item-info">
          <div class="return-book">{{ r.bookTitle }}</div>
          <div class="return-meta">
            数量：{{ r.quantity }} 件 · {{ r.statusText }}
            <span v-if="r.reason" class="return-reason">原因：{{ r.reason }}</span>
          </div>
        </div>
        <div class="return-item-actions">
          <el-button size="small" type="success" @click="handleApprove(r)">同意</el-button>
          <el-button size="small" type="danger" @click="handleReject(r)">拒绝</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllOrders, shipOrder } from '@/api/order'
import { getPendingReturn, auditReturn } from '@/api/return'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ORDER_STATUS, ORDER_STATUS_COLOR, ORDER_STATUS_RETURN_PENDING, RETURN_STATE, RETURN_STATE_COLOR, formatPrice } from '@/utils/constants'
import CatEmpty from '@/components/CatEmpty.vue'

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const statusFilter = ref('')
const drawerVisible = ref(false)
const detailOrder = ref(null)
const returnDialogVisible = ref(false)
const pendingReturns = ref([])

onMounted(() => loadOrders())

function orderText(order) {
  if (order.returnState && order.returnState > 0) return RETURN_STATE[order.returnState] || '无退货'
  return order.statusText || ORDER_STATUS[order.status] || ''
}

function orderTagType(order) {
  if (order.returnState && order.returnState > 0) return RETURN_STATE_COLOR[order.returnState] || 'info'
  return ORDER_STATUS_COLOR[order.status] || 'info'
}

async function loadOrders() {
  loading.value = true
  try {
    const params = { current: currentPage.value, size: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res = await getAllOrders(params)
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
    // 为每笔订单补齐待审核退货数量，用于显示"退货审核"按钮
    await Promise.all(orders.value.map(async (o) => {
      try {
        const p = await getPendingReturn(o.id)
        o.pendingReturnCount = (p.data || []).length
      } catch { o.pendingReturnCount = 0 }
    }))
  } finally { loading.value = false }
}

async function handleShip(order) {
  await ElMessageBox.confirm(`确认发货订单 ${order.orderNo}？`, '确认发货')
  await shipOrder(order.id)
  ElMessage.success('已发货')
  loadOrders()
}

function showDetail(order) {
  detailOrder.value = order
  drawerVisible.value = true
}

async function openReturnAudit(order) {
  const res = await getPendingReturn(order.id)
  pendingReturns.value = res.data || []
  returnDialogVisible.value = true
}

async function handleApprove(r) {
  await ElMessageBox.confirm(`确认同意退货「${r.bookTitle}」× ${r.quantity}？`, '确认操作')
  await auditReturn({ returnId: r.id, status: 1 })
  ElMessage.success('已同意退货')
  pendingReturns.value = pendingReturns.value.filter(x => x.id !== r.id)
  if (pendingReturns.value.length === 0) returnDialogVisible.value = false
  loadOrders()
}

async function handleReject(r) {
  await ElMessageBox.confirm(`确认拒绝退货「${r.bookTitle}」× ${r.quantity}？`, '确认操作', { type: 'warning' })
  await auditReturn({ returnId: r.id, status: 2 })
  ElMessage.success('已拒绝退货')
  pendingReturns.value = pendingReturns.value.filter(x => x.id !== r.id)
  if (pendingReturns.value.length === 0) returnDialogVisible.value = false
  loadOrders()
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.toolbar-left { display: flex; align-items: center; }
.order-card { margin-bottom: 12px; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.order-no { font-weight: bold; color: #303133; }
.order-time { margin-left: 12px; color: #909399; font-size: 13px; }
.order-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 12px; border-top: 1px solid #ebeef5; font-size: 13px; color: #606266; }
.order-actions { display: flex; align-items: center; gap: 8px; }
.pay-amount { color: #f56c6c; margin-right: 8px; }
.price { color: #f56c6c; font-weight: bold; }
.empty-tip { color: #909399; text-align: center; padding: 24px 0; }
.return-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #ebeef5; }
.return-item:last-child { border-bottom: none; }
.return-book { font-weight: bold; color: #303133; }
.return-meta { font-size: 13px; color: #606266; margin-top: 4px; }
.return-reason { margin-left: 8px; color: #909399; }
.return-item-actions { display: flex; gap: 8px; }
</style>
