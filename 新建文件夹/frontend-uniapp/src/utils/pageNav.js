export function relaunchTo(url) {
  if (typeof window !== 'undefined' && window.location) {
    const hashUrl = url.startsWith('/') ? `#${url}` : `#/${url}`
    window.location.replace(hashUrl)
    return
  }
  if (typeof uni !== 'undefined' && typeof uni.reLaunch === 'function') {
    uni.reLaunch({ url })
  }
}

export function safeBack(fallbackUrl) {
  if (typeof window !== 'undefined' && window.history && window.history.length > 1) {
    window.history.back()
    return
  }
  if (typeof uni !== 'undefined' && typeof uni.navigateBack === 'function') {
    uni.navigateBack({
      fail: () => relaunchTo(fallbackUrl)
    })
    return
  }
  relaunchTo(fallbackUrl)
}
