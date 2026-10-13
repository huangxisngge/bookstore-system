import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCartList, addCartItem, updateCartItem, removeCartItem, clearCart } from '@/api/cart'
import { getFinalPrice } from '@/utils/constants'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)

  const totalCount = computed(() => items.value.reduce((sum, i) => sum + i.quantity, 0))
  const totalPrice = computed(() => items.value.reduce((sum, i) => sum + Number(i.finalPrice) * i.quantity, 0))

  async function fetchCart() {
    loading.value = true
    try {
      const res = await getCartList()
      items.value = res.data || []
    } finally {
      loading.value = false
    }
  }

  async function addItem(bookId, quantity = 1) {
    await addCartItem({ bookId, quantity })
    await fetchCart()
  }

  async function updateQuantity(cartId, quantity) {
    if (quantity <= 0) {
      await removeCartItem(cartId)
    } else {
      await updateCartItem(cartId, quantity)
    }
    await fetchCart()
  }

  async function removeItem(cartId) {
    await removeCartItem(cartId)
    await fetchCart()
  }

  async function clearAll() {
    await clearCart()
    items.value = []
  }

  return { items, loading, totalCount, totalPrice, fetchCart, addItem, updateQuantity, removeItem, clearAll }
})
