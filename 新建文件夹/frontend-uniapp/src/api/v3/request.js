/**
 * V3 专用 HTTP 请求适配器
 * - BASE_URL 指向服务器根路径（不含 /api/v1 或 /api/v3）
 * - 返回完整的 { code, data, message } 对象，由调用层判断 code === 200
 * - 自动挂载 Bearer token
 * - 认证失效（401/4001）自动跳登录页
 */

// 从环境变量读取，或默认本地开发服务器
const BASE = (() => {
  try {
    const envBase = import.meta.env?.VITE_API_BASE || ''
    // 去掉 /api/v1 后缀，保留纯域名+端口
    return envBase.replace(/\/api\/v1\/?$/, '') || 'http://127.0.0.1:8080'
  } catch {
    return 'http://127.0.0.1:8080'
  }
})()

function getToken() {
  try {
    return uni.getStorageSync('token') || ''
  } catch {
    return ''
  }
}

function buildHeaders(extra = {}) {
  const token = getToken()
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...extra,
  }
}

function handleUnauth() {
  uni.removeStorageSync('token')
  uni.showToast({ title: '登录已失效，请重新登录', icon: 'none' })
  setTimeout(() => {
    uni.reLaunch({ url: '/pages/common/login' })
  }, 500)
}

/**
 * 核心请求方法
 * @param {string} url  完整路径，如 /api/v3/zones
 * @param {object} opts  { method, data, params, headers }
 * @returns {Promise<{code:number, data:any, message:string}>}
 */
function v3Request(url, opts = {}) {
  const { method = 'GET', data, params, headers: extraHeaders = {} } = opts

  // 拼查询参数
  let fullUrl = BASE + url
  if (params && Object.keys(params).length) {
    const qs = Object.entries(params)
      .filter(([, v]) => v !== null && v !== undefined)
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
      .join('&')
    if (qs) fullUrl += (fullUrl.includes('?') ? '&' : '?') + qs
  }

  const headers = buildHeaders(extraHeaders)

  return new Promise((resolve) => {
    uni.request({
      url: fullUrl,
      method: method.toUpperCase(),
      data: data ? JSON.stringify(data) : undefined,
      header: headers,
      success(res) {
        const payload = res.data || {}
        // 401 / 4001: 认证失效
        if (res.statusCode === 401 || payload.code === 4001) {
          handleUnauth()
          resolve({ code: 401, data: null, message: '登录已失效' })
          return
        }
        // 返回完整 payload，调用层自行判断 code
        resolve(payload)
      },
      fail(err) {
        console.error('[V3 Request Error]', url, err)
        resolve({ code: -1, data: null, message: '网络异常，请检查网络后重试' })
      }
    })
  })
}

// ===== 语法糖方法 =====
const request = {
  get(url, opts)    { return v3Request(url, { ...opts, method: 'GET' }) },
  post(url, data, opts)   { return v3Request(url, { ...opts, method: 'POST', data }) },
  put(url, data, opts)    { return v3Request(url, { ...opts, method: 'PUT', data }) },
  delete(url, opts) { return v3Request(url, { ...opts, method: 'DELETE' }) },
}

export default request
