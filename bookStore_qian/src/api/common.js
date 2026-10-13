import request from './request'

export function getServerTime() {
  return request.get('/common/time', { silent: true })
}