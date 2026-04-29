class AudioEngine {
  private ctx: AudioContext | null = null;
  
  public init() {
    if (!this.ctx) {
      this.ctx = new (window.AudioContext || (window as any).webkitAudioContext)();
    }
    if (this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  // 物理建模合成"木棋子击打木棋盘"声
  private playWoodClick(pitch = 600, duration = 0.08, volume = 0.6) {
    if (!this.ctx) return;
    const t = this.ctx.currentTime;
    
    // 主体音：频率快速下降的正弦波 → 模拟木头被敲击后的钝而短的共振
    const osc = this.ctx.createOscillator();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(pitch, t);
    osc.frequency.exponentialRampToValueAtTime(pitch * 0.15, t + duration);
    
    const gain = this.ctx.createGain();
    gain.gain.setValueAtTime(volume, t);
    gain.gain.exponentialRampToValueAtTime(0.001, t + duration);
    
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    
    // 带通白噪声 → 木材质感
    const bufferSize = Math.floor(this.ctx.sampleRate * duration);
    const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
    const data = buffer.getChannelData(0);
    for (let i = 0; i < bufferSize; i++) {
      data[i] = Math.random() * 2 - 1;
    }
    const noise = this.ctx.createBufferSource();
    noise.buffer = buffer;
    
    const noiseFilter = this.ctx.createBiquadFilter();
    noiseFilter.type = 'bandpass';
    noiseFilter.frequency.value = pitch * 1.5;
    noiseFilter.Q.value = 1.5;

    const noiseGain = this.ctx.createGain();
    noiseGain.gain.setValueAtTime(volume * 0.35, t);
    noiseGain.gain.exponentialRampToValueAtTime(0.001, t + duration * 0.7);
    
    noise.connect(noiseFilter);
    noiseFilter.connect(noiseGain);
    noiseGain.connect(this.ctx.destination);
    
    osc.start(t);
    osc.stop(t + duration);
    noise.start(t);
    noise.stop(t + duration);
  }

  // 落子声：低沉浑厚的「啪」
  public playMove() {
    this.init();
    this.playWoodClick(350, 0.1, 0.7);
  }

  // 吃子声：两声碰撞「啪咔」
  public playCapture() {
    this.init();
    this.playWoodClick(500, 0.07, 0.9);
    setTimeout(() => this.playWoodClick(800, 0.05, 0.5), 50);
  }

  // 将军声：合成一个中国风「将！军！」的音效
  // 用合成的庄严和弦代替浏览器TTS（TTS音色不稳定且很多浏览器不支持中文）
  public playCheck() {
    this.init();
    if (!this.ctx) return;
    const t = this.ctx.currentTime;

    // 第一声：低沉的铜锣「将！」
    const gong1 = this.ctx.createOscillator();
    gong1.type = 'sine';
    gong1.frequency.setValueAtTime(220, t);
    gong1.frequency.exponentialRampToValueAtTime(180, t + 0.4);
    const gong1Gain = this.ctx.createGain();
    gong1Gain.gain.setValueAtTime(0.5, t);
    gong1Gain.gain.exponentialRampToValueAtTime(0.001, t + 0.4);
    gong1.connect(gong1Gain);
    gong1Gain.connect(this.ctx.destination);
    gong1.start(t);
    gong1.stop(t + 0.4);

    // 伴随的金属泛音
    const metal1 = this.ctx.createOscillator();
    metal1.type = 'triangle';
    metal1.frequency.setValueAtTime(660, t);
    metal1.frequency.exponentialRampToValueAtTime(440, t + 0.3);
    const metal1Gain = this.ctx.createGain();
    metal1Gain.gain.setValueAtTime(0.15, t);
    metal1Gain.gain.exponentialRampToValueAtTime(0.001, t + 0.3);
    metal1.connect(metal1Gain);
    metal1Gain.connect(this.ctx.destination);
    metal1.start(t);
    metal1.stop(t + 0.3);

    // 第二声：更高亢的铜锣「军！」延迟0.35s
    const delay = 0.38;
    const gong2 = this.ctx.createOscillator();
    gong2.type = 'sine';
    gong2.frequency.setValueAtTime(330, t + delay);
    gong2.frequency.exponentialRampToValueAtTime(260, t + delay + 0.5);
    const gong2Gain = this.ctx.createGain();
    gong2Gain.gain.setValueAtTime(0.6, t + delay);
    gong2Gain.gain.exponentialRampToValueAtTime(0.001, t + delay + 0.5);
    gong2.connect(gong2Gain);
    gong2Gain.connect(this.ctx.destination);
    gong2.start(t + delay);
    gong2.stop(t + delay + 0.5);

    const metal2 = this.ctx.createOscillator();
    metal2.type = 'triangle';
    metal2.frequency.setValueAtTime(880, t + delay);
    metal2.frequency.exponentialRampToValueAtTime(550, t + delay + 0.4);
    const metal2Gain = this.ctx.createGain();
    metal2Gain.gain.setValueAtTime(0.2, t + delay);
    metal2Gain.gain.exponentialRampToValueAtTime(0.001, t + delay + 0.4);
    metal2.connect(metal2Gain);
    metal2Gain.connect(this.ctx.destination);
    metal2.start(t + delay);
    metal2.stop(t + delay + 0.4);
  }

  // 游戏结束：悠扬的结束音
  public playGameOver() {
    this.init();
    if (!this.ctx) return;
    const t = this.ctx.currentTime;
    
    const notes = [262, 330, 392, 523]; // C4-E4-G4-C5
    notes.forEach((freq, i) => {
      const osc = this.ctx!.createOscillator();
      osc.type = 'sine';
      osc.frequency.value = freq;
      const gain = this.ctx!.createGain();
      gain.gain.setValueAtTime(0, t + i * 0.25);
      gain.gain.linearRampToValueAtTime(0.3, t + i * 0.25 + 0.05);
      gain.gain.exponentialRampToValueAtTime(0.001, t + i * 0.25 + 0.8);
      osc.connect(gain);
      gain.connect(this.ctx!.destination);
      osc.start(t + i * 0.25);
      osc.stop(t + i * 0.25 + 0.8);
    });
  }
}

export const audioEngine = new AudioEngine();
