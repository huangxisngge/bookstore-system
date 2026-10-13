export const ORDER_STATUS = {
  0: '未付款',
  1: '未发货',
  2: '已发货',
  3: '已完成',
  4: '已取消'
}

export const ORDER_STATUS_COLOR = {
  0: 'warning',
  1: 'primary',
  2: 'info',
  3: 'success',
  4: 'info'
}

// 管理端状态筛选特殊值：待退货审核（订单退货为派生状态）。order.status 不落库该值。
export const ORDER_STATUS_RETURN_PENDING = 10

export const MEMBER_LEVEL = {
  1: '普通会员',
  2: '银卡会员',
  3: '金卡会员'
}

export const MEMBER_DISCOUNT = {
  1: 1.0,
  2: 0.95,
  3: 0.9
}

export const RETURN_STATUS = {
  0: '待审核',
  1: '已批准',
  2: '已拒绝'
}

export const RETURN_STATUS_COLOR = {
  0: 'warning',
  1: 'success',
  2: 'danger'
}

// 订单级派生退货状态（支持部分退货后，0-3 覆盖整单的退货状态）
export const RETURN_STATE = {
  0: '无退货',
  1: '退货审核中',
  2: '部分退货',
  3: '已退货'
}

export const RETURN_STATE_COLOR = {
  0: 'info',
  1: 'warning',
  2: 'danger',
  3: 'danger'
}

export function getFinalPrice(book) {
  if (book.onPromo && book.promoPrice) {
    return Number(book.promoPrice)
  }
  return Number(book.price) * Number(book.discount)
}

export function formatPrice(val) {
  return Number(val).toFixed(2)
}

export function getFullReduction(amount) {
  if (amount >= 500) return 80
  if (amount >= 200) return 30
  if (amount >= 100) return 10
  return 0
}
