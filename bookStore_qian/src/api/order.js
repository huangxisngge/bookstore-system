import request from './request'

export function createOrder(data, config) {
  return request.post('/order/create', data, config)
}

export function getRecentAddresses() {
  return request.get('/order/address-list')
}

export function getOrderDetail(orderId) {
  return request.get(`/order/detail/${orderId}`)
}

export function getMyOrders(params) {
  return request.get('/order/my', { params })
}

export function getAllOrders(params) {
  return request.get('/order/list', { params })
}

export function payOrder(orderId) {
  return request.post(`/order/pay/${orderId}`)
}

export function shipOrder(orderId) {
  return request.post(`/order/ship/${orderId}`)
}

export function receiveOrder(orderId) {
  return request.post(`/order/receive/${orderId}`)
}

export function cancelOrder(orderId) {
  return request.post(`/order/cancel/${orderId}`)
}
