<template>
  <div class="order-detail-page" v-loading="loading">
    <h2>订单详情</h2>
    <template v-if="order">
      <el-card class="section-card">
        <div class="status-bar">
          <el-tag :type="returnTagType" size="large">
            {{ returnStateText }}
          </el-tag>
          <span class="order-no">订单号：{{ order.orderNo }}</span>
        </div>
        <div class="info-grid">
          <div><strong>收货人：</strong>{{ order.receiverName }}</div>
          <div><strong>手机号：</strong>{{ order.receiverPhone }}</div>
          <div style="grid-column:span 2"><strong>地址：</strong>{{ order.address }}</div>
          <div v-if="order.remark" style="grid-column:span 2"><strong>备注：</strong>{{ order.remark }}</div>
        </div>
        <div class="time-info">
          <span>下单时间：{{ order.createTime }}</span>
          <span v-if="order.payTime">付款时间：{{ order.payTime }}</span>
          <span v-if="order.shipTime">发货时间：{{ order.shipTime }}</span>
          <span v-if="order.receiveTime">收货时间：{{ order.receiveTime }}</span>
        </div>
      </el-card>

      <el-card class="section-card" style="margin-top:16px">
        <template #header><span>订单商品</span></template>
        <el-table :data="order.items" style="width:100%">
          <el-table-column label="图书" min-width="180">
            <template #default="{ row }">
              <div style="display:flex;align-items:center;gap:8px">
                <BookCover :src="row.bookCover" :title="row.bookTitle" width="40px" height="56px" />
                <span>{{ row.bookTitle }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="90">
            <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column label="数量" width="70" prop="quantity" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }"><span class="price">¥{{ formatPrice(row.subtotal) }}</span></template>
          </el-table-column>
          <el-table-column label="已退/可退" width="100">
            <template #default="{ row }">{{ row.returnedQty || 0 }} / {{ row.returnableQty || 0 }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button v-if="canApplyReturn && (row.returnableQty || 0) > 0"
                         size="small" type="warning" text @click="openReturn(row)">申请退货</el-button>
              <span v-else-if="(row.returnableQty || 0) <= 0">已退完</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="section-card" style="margin-top:16px">
        <template #header><span>费用明细</span></template>
        <div class="fee-row"><span>商品总额</span><span>¥{{ formatPrice(order.totalAmount) }}</span></div>
        <div class="fee-row discount" v-if="order.discountAmount > 0">
          <span>折扣优惠</span><span>-¥{{ formatPrice(order.discountAmount) }}</span>
        </div>
        <div class="fee-row discount" v-if="order.fullReductionAmount > 0">
          <span>满减优惠</span><span>-¥{{ formatPrice(order.fullReductionAmount) }}</span>
        </div>
        <el-divider />
        <div class="fee-row total"><span>实付金额</span><span class="total-price">¥{{ formatPrice(order.payAmount) }}</span></div>
      </el-card>

      <div class="action-bar">
        <el-button v-if="order.status === 0" type="warning" @click="handlePay">去付款</el-button>
        <el-button v-if="order.status === 2" type="success" @click="handleReceive">确认收货</el-button>
        <el-button v-if="order.status === 0" type="danger" text @click="handleCancel">取消订单</el-button>
      </div>

      <el-dialog v-model="returnVisible" title="申请退货" width="440px">
        <el-form :model="returnForm" label-width="80px">
          <el-form-item label="退货图书">
            <span>{{ returnForm.bookTitle }}</span>
          </el-form-item>
          <el-form-item label="退货数量">
            <el-input-number v-model="returnForm.quantity" :min="1" :max="returnForm.maxQty" />
          </el-form-item>
          <el-form-item label="退货原因">
            <el-input v-model="returnForm.reason" type="textarea" :rows="3" placeholder="请输入退货原因" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="returnVisible = false">取消</el-button>
          <el-button type="primary" :loading="returnLoading" @click="handleReturn">提交申请</el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, payOrder, receiveOrder, cancelOrder } from '@/api/order'
import { applyReturn } from '@/api/return'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ORDER_STATUS, ORDER_STATUS_COLOR, RETURN_STATE, RETURN_STATE_COLOR, formatPrice } from '@/utils/constants'
import BookCover from '@/components/BookCover.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const order = ref(null)
const returnVisible = ref(false)
const returnLoading = ref(false)
const returnForm = reactive({ orderId: 0, orderItemId: 0, bookTitle: '', quantity: 1, maxQty: 1, reason: '' })

const canApplyReturn = computed(() => {
  return order.value && (order.value.status === 1 || order.value.status === 3)
})

const returnStateText = computed(() => {
  const o = order.value
  if (!o) return ''
  if (o.returnState && o.returnState > 0) return RETURN_STATE[o.returnState] || '无退货'
  return (o.statusText || ORDER_STATUS[o.status] || '')
})

const returnTagType = computed(() => {
  const o = order.value
  if (!o) return 'info'
  if (o.returnState && o.returnState > 0) return RETURN_STATE_COLOR[o.returnState] || 'info'
  return ORDER_STATUS_COLOR[o.status] || 'info'
})

onMounted(async () => {
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res.data
  } finally { loading.value = false }
})

async function handlePay() {
  await ElMessageBox.confirm(`确认支付 ¥${formatPrice(order.value.payAmount)}？`)
  await payOrder(order.value.id)
  ElMessage.success('支付成功')
  refreshOrder()
}

async function handleReceive() {
  await ElMessageBox.confirm('确认已收到货物？')
  await receiveOrder(order.value.id)
  ElMessage.success('已确认收货')
  refreshOrder()
}

async function handleCancel() {
  await ElMessageBox.confirm('确定取消该订单？', '提示', { type: 'warning' })
  await cancelOrder(order.value.id)
  ElMessage.success('已取消')
  refreshOrder()
}

function openReturn(item) {
  returnForm.orderId = order.value.id
  returnForm.orderItemId = item.id
  returnForm.bookTitle = item.bookTitle
  returnForm.maxQty = item.returnableQty || item.quantity
  returnForm.quantity = 1
  returnForm.reason = ''
  returnVisible.value = true
}

async function handleReturn() {
  returnLoading.value = true
  try {
    await applyReturn({
      orderId: returnForm.orderId,
      orderItemId: returnForm.orderItemId,
      quantity: returnForm.quantity,
      reason: returnForm.reason
    })
    ElMessage.success('退货申请已提交')
    returnVisible.value = false
    refreshOrder()
  } finally { returnLoading.value = false }
}

async function refreshOrder() {
  const res = await getOrderDetail(route.params.id)
  order.value = res.data
}
</script>

<style scoped>
h2 { margin: 0 0 20px; color: #303133; }
.status-bar { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
.order-no { color: #909399; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 14px; color: #606266; }
.time-info { margin-top: 12px; display: flex; gap: 16px; font-size: 12px; color: #909399; }
.fee-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; color: #606266; }
.fee-row.discount { color: #e6a23c; }
.fee-row.total { font-size: 16px; font-weight: bold; color: #303133; }
.total-price { color: #f56c6c; font-size: 24px; }
.price { color: #f56c6c; font-weight: bold; }
.action-bar { margin-top: 20px; display: flex; gap: 12px; }
</style>
