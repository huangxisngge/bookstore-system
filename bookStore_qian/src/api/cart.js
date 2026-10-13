import request from './request'

export function getCartList() {
  return request.get('/cart/list')
}

export function addCartItem(data) {
  return request.post('/cart/add', data)
}

export function updateCartItem(cartId, quantity) {
  return request.put(`/cart/update/${cartId}`, null, { params: { quantity } })
}

export function removeCartItem(cartId) {
  return request.delete(`/cart/remove/${cartId}`)
}

export function clearCart() {
  return request.delete('/cart/clear')
}
