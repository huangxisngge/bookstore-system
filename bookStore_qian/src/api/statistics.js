import request from './request'

export function getOverview() {
  return request.get('/statistics/overview')
}

export function getSalesRanking(params) {
  return request.get('/statistics/sales-ranking', { params })
}

export function getMonthlyTrend() {
  return request.get('/statistics/monthly-trend')
}

export function getCategoryDistribution() {
  return request.get('/statistics/category-distribution')
}

export function getInventoryAlert(params) {
  return request.get('/statistics/inventory-alert', { params })
}

export function getReturnTrend() {
  return request.get('/statistics/return-trend')
}
