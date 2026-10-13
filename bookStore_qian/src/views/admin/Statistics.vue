<template>
  <div class="statistics-page">
    <el-row :gutter="16" class="overview-cards">
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ overview.totalUsers || 0 }}</div>
          <div class="stat-label">总用户数</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ overview.totalBooks || 0 }}</div>
          <div class="stat-label">图书总数</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ overview.totalOrders || 0 }}</div>
          <div class="stat-label">订单总数</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value revenue">¥{{ formatPrice(overview.totalRevenue || 0) }}</div>
          <div class="stat-label">总收入</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value warning">{{ overview.pendingOrders || 0 }}</div>
          <div class="stat-label">待处理订单</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value danger">{{ overview.lowStockBooks || 0 }}</div>
          <div class="stat-label">库存预警</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value warning">{{ overview.returnOrders || 0 }}</div>
          <div class="stat-label">退货订单</div>
        </el-card>
      </el-col>
      <el-col :span="3">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value danger">¥{{ formatPrice(overview.returnAmount || 0) }}</div>
          <div class="stat-label">退货金额</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card>
          <template #header><span>销量排行 TOP10</span></template>
          <div ref="salesChartRef" style="height:350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>月度销售趋势</span></template>
          <div ref="trendChartRef" style="height:350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card>
          <template #header><span>分类销售占比</span></template>
          <div ref="pieChartRef" style="height:350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>月度退货趋势</span></template>
          <div ref="returnTrendChartRef" style="height:350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span>库存预警</span>
              <el-input-number v-model="alertThreshold" :min="1" :max="50" size="small" @change="loadAlerts" />
            </div>
          </template>
          <el-table :data="alerts" style="width:100%" max-height="300">
            <el-table-column prop="title" label="书名" min-width="150" show-overflow-tooltip />
            <el-table-column prop="author" label="作者" width="100" show-overflow-tooltip />
            <el-table-column label="库存" width="80">
              <template #default="{ row }">
                <span :class="{ 'low-stock': row.stock <= 5 }">{{ row.stock }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getSalesRanking, getMonthlyTrend, getCategoryDistribution, getInventoryAlert, getReturnTrend } from '@/api/statistics'
import { formatPrice } from '@/utils/constants'

const overview = ref({})
const alerts = ref([])
const alertThreshold = ref(10)

const salesChartRef = ref(null)
const trendChartRef = ref(null)
const pieChartRef = ref(null)
const returnTrendChartRef = ref(null)

onMounted(async () => {
  await Promise.all([loadOverview(), loadSalesRanking(), loadTrend(), loadPie(), loadAlerts(), loadReturnTrend()])
})

async function loadOverview() {
  const res = await getOverview()
  overview.value = res.data || {}
}

async function loadSalesRanking() {
  const res = await getSalesRanking({ limit: 10 })
  const data = res.data || []
  await nextTick()
  const chart = echarts.init(salesChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '10%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: data.map(d => d.bookName).reverse(), axisLabel: { width: 80, overflow: 'truncate' } },
    series: [{ type: 'bar', data: data.map(d => d.totalSales).reverse(), itemStyle: { color: '#8B3A4A', borderRadius: [0, 6, 6, 0] }, barWidth: '50%' }]
  })
}

async function loadTrend() {
  const res = await getMonthlyTrend()
  const data = res.data || []
  await nextTick()
  const chart = echarts.init(trendChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.month) },
    yAxis: [
      { type: 'value', name: '销售额' },
      { type: 'value', name: '订单数', splitLine: { show: false } }
    ],
    series: [
      { name: '销售额', type: 'line', data: data.map(d => d.totalAmount), smooth: true, itemStyle: { color: '#8B3A4A' }, areaStyle: { color: 'rgba(139,58,74,0.15)' } },
      { name: '订单数', type: 'bar', yAxisIndex: 1, data: data.map(d => d.orderCount), itemStyle: { color: '#C4A35A' }, barWidth: '40%' }
    ]
  })
}

async function loadPie() {
  const res = await getCategoryDistribution()
  const data = res.data || []
  await nextTick()
  const chart = echarts.init(pieChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'center' },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['55%', '50%'],
      color: ['#8B3A4A', '#C4A35A', '#3D2B1F', '#4A7C59', '#5c3a28', '#a85c6a', '#7a4a3a', '#8a7a6a'],
      data: data.map(d => ({ name: d.categoryName, value: Number(d.totalSales) })),
      label: { formatter: '{b}\n{d}%' },
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
    }]
  })
}

async function loadAlerts() {
  const res = await getInventoryAlert({ threshold: alertThreshold.value })
  alerts.value = res.data || []
}

async function loadReturnTrend() {
  const res = await getReturnTrend()
  const data = res.data || []
  await nextTick()
  const chart = echarts.init(returnTrendChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.month) },
    yAxis: [
      { type: 'value', name: '退货数' },
      { type: 'value', name: '退货金额', splitLine: { show: false } }
    ],
    series: [
      { name: '退货数', type: 'bar', data: data.map(d => d.returnCount), itemStyle: { color: '#e6a23c' }, barWidth: '40%' },
      { name: '退货金额', type: 'line', yAxisIndex: 1, data: data.map(d => d.returnAmount), smooth: true, itemStyle: { color: '#f56c6c' }, areaStyle: { color: 'rgba(245,108,108,0.15)' } }
    ]
  })
}
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.stat-value.revenue { color: #67c23a; }
.stat-value.warning { color: #e6a23c; }
.stat-value.danger { color: #f56c6c; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.low-stock { color: #f56c6c; font-weight: bold; }
</style>
