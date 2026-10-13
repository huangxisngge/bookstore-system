import request from './request'

export function getBookList(params) {
  return request.get('/book/list', { params })
}

export function getBookDetail(id) {
  return request.get(`/book/detail/${id}`)
}

export function getHotBooks(params) {
  return request.get('/book/hot', { params })
}

export function getUpcomingGrabs(params) {
  return request.get('/book/upcoming-grab', { params })
}

export function addBook(data) {
  return request.post('/book/add', data)
}

export function updateBook(data) {
  return request.put('/book/update', data)
}

export function clearGrab(id) {
  return request.put(`/book/clear-grab/${id}`)
}

export function deleteBook(id) {
  return request.delete(`/book/delete/${id}`)
}
