import { uiStore } from '../store/ui'

const HOME_BY_ROLE = {
  FARMER: '/pages/farmer/home',
  OWNER: '/pages/owner/home',
  ADMIN: '/pages/admin/entry'
}

function showMessage(message) {
  if (typeof uni === 'undefined' || typeof uni.showToast !== 'function') {
    return
  }
  uni.showToast({ title: message, icon: 'none' })
}

function relaunch(url) {
  if (typeof window !== 'undefined' && window.location) {
    const hashUrl = url.startsWith('/') ? `#${url}` : `#/${url}`
    window.location.replace(hashUrl)
    return
  }
  if (typeof uni === 'undefined' || typeof uni.reLaunch !== 'function') {
    return
  }
  setTimeout(() => {
    uni.reLaunch({ url })
  }, 250)
}

export function ensurePageAccess(expectedRole) {
  uiStore.hydrate()
  if (!uiStore.token) {
    showMessage('请先登录测试账号')
    relaunch('/pages/common/login')
    return false
  }
  if (expectedRole && uiStore.role !== expectedRole) {
    showMessage('当前账号无权访问该页面')
    relaunch(HOME_BY_ROLE[uiStore.role] || '/pages/common/login')
    return false
  }
  return true
}
