import request from './request'

export function applyReturn(data) {
  return request.post('/return/apply', data)
}

export function auditReturn(data) {
  return request.post('/return/audit', data)
}

export function getMyReturns(params) {
  return request.get('/return/my', { params })
}

export function getAllReturns(params) {
  return request.get('/return/list', { params })
}

export function getPendingReturn(orderId) {
  return request.get(`/return/pending/${orderId}`)
}
