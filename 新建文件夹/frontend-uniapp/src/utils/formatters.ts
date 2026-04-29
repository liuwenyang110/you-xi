// 坐标格式化
export function formatCoord(lat: number | null, lng: number | null): string {
  if (lat == null || lng == null) return '暂未设置'
  return `${lat}, ${lng}`
}

// 地点别名映射（根据demand-create.vue）
const LOCATION_ALIAS_MAP: Record<string, string> = {
  东河村: 'donghe village',
  'donghe村': 'donghe village',
  虹桥片区: 'hongqiao demo',
  虹桥: 'hongqiao',
  苏州示范点: 'suzhou demo',
  苏州: 'suzhou',
  杭州: 'hangzhou',
  杭州东站: 'hangzhou east station',
  上海: 'shanghai',
  上海虹桥站: 'hongqiao station',
  上海虹桥机场: 'hongqiao airport'
}

// 地点名称格式化
export function formatLocationName(value: string, raw?: string): string {
  return LOCATION_ALIAS_MAP[value] || LOCATION_ALIAS_MAP[raw || ''] || value
}

// 数字格式化（保留小数）
export function formatNumber(num: number, decimals: number = 2): string {
  return num.toFixed(decimals)
}

// 手机号脱敏
export function maskPhone(phone: string): string {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 时间格式化（简单版）
export function formatTime(date: Date | string | number): string {
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}