// UI工具函数
import { ref } from 'vue'

// 显示Toast
export function showToast(title: string, icon: 'success' | 'loading' | 'none' = 'none') {
  if (typeof uni !== 'undefined' && uni.showToast) {
    uni.showToast({ title, icon, duration: 2000 })
  } else {
    console.log('[Toast]', title)
  }
}

// 显示Loading
export function showLoading(title: string = '加载中') {
  if (typeof uni !== 'undefined' && uni.showLoading) {
    uni.showLoading({ title, mask: true })
  } else {
    console.log('[Loading]', title)
  }
}

// 隐藏Loading
export function hideLoading() {
  if (typeof uni !== 'undefined' && uni.hideLoading) {
    uni.hideLoading()
  }
}

// 显示Modal
export function showModal(options: {
  title?: string
  content: string
  showCancel?: boolean
  confirmText?: string
  cancelText?: string
}): Promise<boolean> {
  return new Promise((resolve) => {
    if (typeof uni !== 'undefined' && uni.showModal) {
      uni.showModal({
        title: options.title || '提示',
        content: options.content,
        showCancel: options.showCancel !== false,
        confirmText: options.confirmText || '确定',
        cancelText: options.cancelText || '取消',
        success: (res) => resolve(res.confirm),
        fail: () => resolve(false)
      })
    } else {
      console.log('[Modal]', options.content)
      resolve(true)
    }
  })
}

// 通用状态管理（简单版）
export function useState<T>(initialValue: T) {
  const state = ref(initialValue)
  const setState = (value: T) => {
    state.value = value
  }
  return [state, setState] as const
}