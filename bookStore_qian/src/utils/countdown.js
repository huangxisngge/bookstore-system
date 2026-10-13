import { getServerTime } from '@/api/common'

// 服务器时间偏移（毫秒）：offset = 服务器当前时刻 - 客户端当前时刻
// 所有抢购倒计时一律使用"服务器时刻"驱动，保证归零即服务端真实放行，避免时钟偏差抢慢一步
let serverOffsetMs = 0

export function setServerTimeOffset(serverTime) {
  if (serverTime) {
    serverOffsetMs = serverTime - Date.now()
  }
}

export function getServerNow() {
  return Date.now() + serverOffsetMs
}

/**
 * 拉取服务器时间并校准偏移（用 RTT/2 修正单向网络延迟），返回校准后的服务器当前时刻。
 * 失败时退回本地时钟，不影响倒计时展示。
 */
export async function syncServerTime() {
  try {
    const t0 = Date.now()
    const res = await getServerTime()
    const t1 = Date.now()
    setServerTimeOffset(res.data + Math.floor((t1 - t0) / 2) - t1)
    return getServerNow()
  } catch (e) {
    return Date.now()
  }
}

export function getGrabTimeMs(grabStartTime) {
  if (!grabStartTime) return 0
  return new Date(String(grabStartTime).replace(' ', 'T')).getTime()
}

export function calcGrabRemainSeconds(grabStartTime, now = getServerNow()) {
  const target = getGrabTimeMs(grabStartTime)
  if (!target) return 0
  return Math.max(0, Math.floor((target - now) / 1000))
}

export function pad2(n) {
  return String(n).padStart(2, '0')
}

export function formatGrabRemain(seconds) {
  if (seconds <= 0) return '即将开抢'
  const d = Math.floor(seconds / 86400)
  const h = Math.floor((seconds % 86400) / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return d > 0 ? `${d}天${pad2(h)}:${pad2(m)}:${pad2(s)}` : `${pad2(h)}:${pad2(m)}:${pad2(s)}`
}

export function formatGrabRemainText(seconds) {
  if (seconds <= 0) return '即将开抢'
  const d = Math.floor(seconds / 86400)
  const h = Math.floor((seconds % 86400) / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (d > 0) return `还有 ${d} 天 ${h} 时 ${m} 分`
  if (h > 0) return `还有 ${h} 时 ${m} 分 ${s} 秒`
  if (m > 0) return `还有 ${m} 分 ${s} 秒`
  return `还有 ${s} 秒`
}