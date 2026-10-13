import request from './request'

export function getCategoryList() {
  return request.get('/category/list')
}

export function addCategory(data) {
  return request.post('/category/add', data)
}

export function updateCategory(data) {
  return request.put('/category/update', data)
}

export function deleteCategory(id) {
  return request.delete(`/category/delete/${id}`)
}
