import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/user/Home.vue') },
      { path: 'books', name: 'BookList', component: () => import('@/views/user/BookList.vue') },
      { path: 'book/:id', name: 'BookDetail', component: () => import('@/views/user/BookDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('@/views/user/Cart.vue'), meta: { auth: true } },
      { path: 'checkout', name: 'Checkout', component: () => import('@/views/user/Checkout.vue'), meta: { auth: true } },
      { path: 'orders', name: 'Orders', component: () => import('@/views/user/Orders.vue'), meta: { auth: true } },
      { path: 'order/:id', name: 'OrderDetail', component: () => import('@/views/user/OrderDetail.vue'), meta: { auth: true } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/user/Profile.vue'), meta: { auth: true } }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { auth: true, admin: true },
    children: [
      { path: '', name: 'AdminBooks', component: () => import('@/views/admin/Books.vue') },
      { path: 'books', name: 'AdminBooksList', component: () => import('@/views/admin/Books.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('@/views/admin/Categories.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('@/views/admin/Orders.vue') },
      { path: 'statistics', name: 'AdminStatistics', component: () => import('@/views/admin/Statistics.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/Users.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.auth && !userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.meta.admin && !userStore.isAdmin) {
    next('/')
  } else if (to.meta.guest && userStore.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router
