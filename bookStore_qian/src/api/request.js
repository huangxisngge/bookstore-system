import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => Promise.reject(error))

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    // 静默模式：调用方自行处理（如开抢失败自动重试），不弹全局提示
    if (response.config?.silent) {
      const err = new Error(res.message || '请求失败')
      err.businessCode = res.code
      err.noToast = true
      return Promise.reject(err)
    }
    // 409 = 库存冲突/抢购失败类业务提示，用警告样式而非报错样式
    if (res.code === 409) {
      ElMessage.warning({ message: res.message || '请求未成功', duration: 4000 })
    } else {
      ElMessage.error(res.message || '请求失败')
    }
    if (res.code === 401) {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      router.push('/login')
    }
    const err = new Error(res.message)
    err.businessCode = res.code
    return Promise.reject(err)
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('user')
        router.push('/login')
        ElMessage.error(data?.message || '登录已过期，请重新登录')
      } else if (status === 409) {
        ElMessage.warning({ message: data?.message || '库存不足', duration: 4000 })
      } else {
        ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误')
    }
    const err = new Error(error.message)
    err.businessCode = error.response?.status
    return Promise.reject(err)
  }
)

export default request
