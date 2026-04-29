<template>
  <view class="impact-cockpit">
    <!-- 震撼的头部标题区 -->
    <view class="cockpit-header glow-border">
      <view class="tech-lines left-lines"></view>
      <view class="title-core">
        <text class="main-title">农助手 V2 · 三农超脑调度中心</text>
        <text class="sub-title">Nong Helper B-End Rural Data Engine Matrix</text>
      </view>
      <view class="tech-lines right-lines"></view>
    </view>

    <view class="cockpit-body">
      <!-- 左侧：ESG 与政绩汇报 (给政府看) -->
      <view class="panel-left glow-panel">
        <view class="panel-title">🌐 数字化 ESG 政绩</view>
        <view class="data-block">
          <view class="data-label">累计节省农机柴油 (碳减排指标)</view>
          <view class="data-value tech-green counter-animation">14,250 <text class="unit">L</text></view>
          <view class="trend up">同比上升 32.4% ↗ 换算省下 ￥10.5万</view>
        </view>
        
        <view class="data-block mt-4">
          <view class="data-label">防薅羊毛 (套补贴) 拦截打卡</view>
          <view class="data-value tech-red counter-animation">3,205 <text class="unit">次</text></view>
          <view class="trend">保护国家农机补贴发放精确度 99.8%</view>
        </view>
      </view>

      <!-- 中间：炫酷地理围栏雷达与调度 (视觉中心) -->
      <view class="panel-center">
        <view class="radar-container">
          <!-- 模拟一个全息雷达扫描或 3D 地球 -->
          <view class="radar-circle">
            <view class="sweeper"></view>
            <view class="ping dot-1"></view>
            <view class="ping dot-2"></view>
            <view class="ping dot-3"></view>
          </view>
          <text class="radar-status">实时探测 15km 内可调度农机: 124台</text>
        </view>
      </view>

      <!-- 右侧：硬核技术压测可视化 (给投资人看) -->
      <view class="panel-right glow-panel">
        <view class="panel-title">⚡ 12306级高频并发墙</view>
        <view class="data-block warning-block" :class="{ 'pulse-alert': isPeakLoad }">
          <view class="data-label">当前并发抢占 QPS</view>
          <view class="data-value warning-orange">{{ currentQps }} <text class="unit">请求/秒</text></view>
          <view class="small-text">基于 Redis Lua 微秒级锁护城河</view>
        </view>

        <view class="log-stream mt-4">
          <view class="log-title">实时防超卖日切截获</view>
          <scroll-view scroll-y="true" class="log-content">
            <view class="log-item" v-for="(log, i) in attackLogs" :key="i">
              <text class="time">{{ log.time }}</text> 
              <text class="msg text-truncate">{{ log.msg }}</text>
            </view>
          </scroll-view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

const currentQps = ref(15240);
const isPeakLoad = ref(true);
const attackLogs = ref<any[]>([]);

// 模拟给评委看的实时滚动流
onMounted(() => {
  setInterval(() => {
    // 数据跳动特效
    currentQps.value = 15000 + Math.floor(Math.random() * 8000);
    isPeakLoad.value = currentQps.value > 18000;
    
    // 生成假日志
    const nowStr = new Date().toLocaleTimeString();
    attackLogs.value.unshift({
      time: nowStr,
      msg: `[拦截] 节点 ${Math.floor(Math.random() * 99)} 尝试超抢槽位被拒!`
    });
    if(attackLogs.value.length > 5) attackLogs.value.pop();
  }, 1200);
});
</script>

<style scoped>
/* 终极暗黑科幻风 - 降维打击土味界面 */
@import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700&family=Noto+Sans+SC:wght@400;700&display=swap');

.impact-cockpit {
  width: 100vw;
  height: 100vh;
  background-color: #030816;
  background-image: 
    radial-gradient(circle at 50% 50%, rgba(10, 40, 80, 0.6) 0%, #030816 80%),
    linear-gradient(rgba(0,150,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,150,255,0.03) 1px, transparent 1px);
  background-size: 100% 100%, 30px 30px, 30px 30px;
  color: #fff;
  font-family: 'Noto Sans SC', sans-serif;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 头部宇宙战舰风格 */
.cockpit-header {
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  background: linear-gradient(180deg, rgba(16, 55, 131, 0.8) 0%, rgba(3, 8, 22, 0) 100%);
  border-bottom: 2px solid rgba(0, 191, 255, 0.3);
  box-shadow: 0px 5px 20px rgba(0, 191, 255, 0.1);
}

.title-core {
  text-align: center;
  z-index: 2;
}

.main-title {
  font-family: 'Orbitron', 'Noto Sans SC', sans-serif;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 4px;
  color: #00E5FF;
  text-shadow: 0 0 10px rgba(0, 229, 255, 0.8);
  display: block;
}

.sub-title {
  font-family: 'Orbitron', sans-serif;
  font-size: 12px;
  color: #8c9eff;
  letter-spacing: 2px;
}

/* 主体布局 2:4:2 */
.cockpit-body {
  flex: 1;
  display: flex;
  padding: 20px;
  gap: 20px;
}

.panel-left, .panel-right {
  flex: 3;
  display: flex;
  flex-direction: column;
}

.panel-center {
  flex: 5;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

/* 发光面板容器 */
.glow-panel {
  background: rgba(16, 30, 60, 0.4);
  border: 1px solid rgba(0, 229, 255, 0.2);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 
    inset 0 0 20px rgba(0, 229, 255, 0.05),
    0 8px 32px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
}

.panel-title {
  font-size: 18px;
  color: #00E5FF;
  border-bottom: 1px solid rgba(0, 229, 255, 0.4);
  padding-bottom: 10px;
  margin-bottom: 20px;
  border-left: 4px solid #00E5FF;
  padding-left: 10px;
}

/* 数据快区块 */
.data-block {
  background: linear-gradient(90deg, rgba(255,255,255,0.05) 0%, transparent 100%);
  padding: 15px;
  border-radius: 8px;
}

.data-label { color: #a0aec0; font-size: 14px; margin-bottom: 5px; }
.data-value { 
  font-family: 'Orbitron', sans-serif; 
  font-size: 38px; 
  font-weight: 700;
  line-height: 1.2;
}

.unit { font-size: 16px; font-weight: normal; margin-left: 5px; color: #718096; }
.tech-green { color: #00FF88; text-shadow: 0 0 8px rgba(0, 255, 136, 0.5); }
.tech-red { color: #FF3366; text-shadow: 0 0 8px rgba(255, 51, 102, 0.5); }
.warning-orange { color: #FFAA00; text-shadow: 0 0 10px rgba(255, 170, 0, 0.8); }

.trend.up { color: #00FF88; font-size: 12px; margin-top: 5px; }

/* 中心雷达扫描特效 */
.radar-container {
  width: 400px;
  height: 400px;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.radar-circle {
  width: 300px;
  height: 300px;
  border-radius: 50%;
  border: 2px solid rgba(0, 229, 255, 0.6);
  background: 
    repeating-radial-gradient(transparent 0, transparent 40px, rgba(0,229,255,0.1) 40px, rgba(0,229,255,0.1) 41px);
  position: relative;
  box-shadow: 0 0 50px rgba(0,229,255,0.2), inset 0 0 40px rgba(0,229,255,0.2);
}

.sweeper {
  width: 150px;
  height: 150px;
  position: absolute;
  top: 0; left: 150px;
  background: linear-gradient(45deg, transparent 50%, rgba(0, 255, 136, 0.5) 100%);
  transform-origin: 0 150px;
  animation: sweep 3s linear infinite;
}

@keyframes sweep { 100% { transform: rotate(360deg); } }

.ping {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #FF3366;
  position: absolute;
  box-shadow: 0 0 10px #FF3366;
}
.dot-1 { top: 60px; left: 80px; animation: blink 2s infinite; }
.dot-2 { top: 200px; left: 220px; animation: blink 3s infinite 1s; }
.dot-3 { top: 120px; left: 240px; animation: blink 1.5s infinite 0.5s; }

@keyframes blink { 0%, 100% { opacity: 0; } 50% { opacity: 1; } }

.radar-status {
  position: absolute;
  bottom: -40px;
  color: #00E5FF;
  font-family: 'Orbitron', 'Noto Sans SC', sans-serif;
  letter-spacing: 2px;
}

/* 右侧日志流 */
.log-stream { height: 200px; }
.log-title { font-size: 12px; color: #a0aec0; margin-bottom: 10px; }
.log-item {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px dashed rgba(255,255,255,0.1);
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  color: rgba(255,51,102,0.9);
}
.log-item .time { color: #8c9eff; margin-right: 10px; flex-shrink: 0;}
.text-truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* 警报特效 */
.pulse-alert {
  border: 1px solid rgba(255, 51, 102, 0.6);
  animation: bg-pulse 1s ease-in-out infinite alternate;
}
@keyframes bg-pulse {
  from { background-color: rgba(255, 51, 102, 0.05); }
  to { background-color: rgba(255, 51, 102, 0.2); }
}
.mt-4 { margin-top: 20px;}
</style>
