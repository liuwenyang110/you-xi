<script>
import { uiStore } from './store/ui'
import { useV3UserStore } from './stores/v3/userStore'
import { useV3ZoneStore } from './stores/v3/zoneStore'

export default {
  async onLaunch() {
    // 旧版 uiStore 兼容处理
    uiStore.hydrate()

    // ===== V3 启动检测 =====
    const token = uni.getStorageSync('token')
    if (!token) return  // 未登录，由各页面的 onLoad 自行跳转登录页

    const userStore = useV3UserStore()
    const zoneStore = useV3ZoneStore()

    try {
      // 拉取 V3 用户档案
      await userStore.fetchProfile()

      if (!userStore.profile) {
        // token 有效但未选角色（新用户）
        return  // 由 login 页跳转处理
      }

      // 预加载字典（公开接口，不影响权限）
      zoneStore.loadDicts().catch(() => {})

      // 如果有片区，预加载片区信息
      if (userStore.zoneId) {
        zoneStore.loadZone(userStore.zoneId).catch(() => {})
      }
    } catch (e) {
      // 网络失败或 token 失效，不强制跳转，由页面自己处理
      console.warn('[V3 启动检测] 拉取用户信息失败', e)
    }
  }
}
</script>

<style>
/* 公益绿字体 — Noto Sans SC（适老友好，完整中文字符集） */
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;700;900&display=swap');

/* 全局设计系统 */
@import './styles/index.css';

/* ==========================================================================
   适老版（长辈版）全局 CSS 增强
   这些样式在设计系统基础上进一步优化
   ========================================================================== */

/* 全局文字增强 */
page.elder-mode .text-lg { font-size: var(--font-lg) !important; }
page.elder-mode .text-xl { font-size: var(--font-xl) !important; }

/* 按钮增强 */
page.elder-mode button,
page.elder-mode .btn {
  min-height: 96rpx !important;
  font-size: 34rpx !important;
}

/* 图标按钮增强 */
page.elder-mode .icon-btn {
  width: 130rpx !important;
  height: 130rpx !important;
  font-size: 56rpx !important;
  border-radius: var(--radius-2xl) !important;
}

/* 输入框增强 */
page.elder-mode input,
page.elder-mode .input {
  height: 96rpx !important;
  font-size: 36rpx !important;
}

/* 核心操作按钮超级放大 */
page.elder-mode .primary-action {
  min-height: 110rpx !important;
  font-size: var(--font-xl) !important;
}
</style>
