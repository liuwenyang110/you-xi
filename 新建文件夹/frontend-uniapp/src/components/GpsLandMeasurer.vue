<template>
  <view class="land-measurer">
    <view class="header-card">
      <view class="top-info">
        <text class="title">实地测亩仪 (防扯皮系统)</text>
        <text class="status" :class="isRecording ? 'recording' : ''">
          {{ isRecording ? '📡 正在测算录轨中...' : '📍 GPS 已就绪' }}
        </text>
      </view>
      <view class="area-display">
        <text class="label">当前预估面积</text>
        <view class="value-row">
          <text class="number">{{ calculatedMu }}</text>
          <text class="unit">亩</text>
        </view>
      </view>
    </view>

    <!-- 模拟地图图层区域 -->
    <view class="map-container">
      <view class="map-placeholder">
        <text class="map-tip">高德地图基线层渲染中...</text>
        <!-- 轨迹连线渲染 -->
        <view class="polygon-svg" v-if="points.length > 0">
          <text>打卡节点数: {{ points.length }}</text>
        </view>
      </view>
    </view>

    <!-- 控制面板 -->
    <view class="control-panel">
      <view class="tips">请手持手机沿田地边缘步行</view>
      <view class="btn-group">
        <button v-if="!isRecording" class="btn-start" @click="startMeasure">▶ 开始测田</button>
        <template v-else>
          <button class="btn-mark" @click="markCorner">📌 标记拐点</button>
          <button class="btn-stop" @click="stopMeasure">⏹ 完成测算</button>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const isRecording = ref(false);
const calculatedMu = ref('0.00');
const points = ref<any[]>([]);
let simulateInterval: any = null;

const startMeasure = () => {
  isRecording.value = true;
  points.value = [];
  calculatedMu.value = '0.12';
  // 模拟 GPS 位移更新面积
  simulateInterval = setInterval(() => {
    calculatedMu.value = (parseFloat(calculatedMu.value) + Math.random() * 0.2).toFixed(2);
  }, 3000);
};

const markCorner = () => {
  uni.showToast({ title: '拐点标记成功', icon: 'success' });
  points.value.push({ lat: 30.123, lng: 120.456 }); // 伪代码点
};

const stopMeasure = () => {
  isRecording.value = false;
  clearInterval(simulateInterval);
  uni.showModal({
    title: '实地测权完成',
    content: `系统判定面积为：${calculatedMu.value} 亩。是否将此面积作为本次作业记录和线下沟通参考？`,
    confirmText: '确认记录',
    success: (res) => {
      // 提交到后端计算确权
    }
  });
};
</script>

<style scoped>
.land-measurer { height: 100vh; display: flex; flex-direction: column; background: #f5f6fa; }

.header-card {
  background: #fff; padding: 20px; border-radius: 0 0 20px 20px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05); z-index: 10;
}
.top-info { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;}
.title { font-size: 18px; font-weight: bold; color: #2C3E50; }
.recording { color: #E74C3C; animation: blink 1.5s infinite; }
@keyframes blink { 50% { opacity: 0.5; } }

.area-display { text-align: center; }
.label { color: #7F8C8D; font-size: 12px; }
.value-row { display: flex; justify-content: center; align-items: baseline; margin-top: 5px; }
.number { font-size: 48px; font-weight: 900; color: #27AE60; font-family: 'Courier New', Courier, monospace; }
.unit { font-size: 16px; font-weight: bold; margin-left: 5px; }

.map-container { flex: 1; background: #e0e6ed; position: relative; }
.map-placeholder { width: 100%; height: 100%; display: flex; flex-direction: column; justify-content: center; align-items: center; color: #95a5a6; }

.control-panel { background: #fff; padding: 20px; box-shadow: 0 -4px 15px rgba(0,0,0,0.05); }
.tips { text-align: center; color: #34495E; margin-bottom: 15px; font-size: 14px; }
.btn-group { display: flex; gap: 15px; }
button { flex: 1; border-radius: 12px; font-weight: bold; display: flex; justify-content: center; align-items: center; }
.btn-start { background: #27AE60; color: #fff; }
.btn-mark { background: #F39C12; color: #fff; }
.btn-stop { background: #E74C3C; color: #fff; }
</style>
