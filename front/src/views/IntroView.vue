<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

// 功能列表 - 4 个唯一功能
const features = [
  { name: 'AI 对话', icon: 'chat' },
  { name: '上下文记忆', icon: 'memory' },
  { name: '个人中心', icon: 'profile' },
  { name: '主题切换', icon: 'theme' },
]

// 进入大厅
const enterLobby = () => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/home')
  } else {
    router.push('/login')
  }
}
</script>

<template>
  <div class="intro-page">
    <div class="content-wrapper">
      <!-- Logo 和标题 -->
      <div class="logo-section">
        <div class="logo-pulse"></div>
        <h1 class="brand-name">莫星 AI</h1>
        <p class="brand-tagline">探索智能的无限可能</p>
      </div>

      <!-- 左右滚动动画：展示项目功能 -->
      <div class="carousel-wrapper">
        <div class="feature-carousel">
          <!-- 4 个功能 + 4 个重复功能 = 8 个，实现无缝循环 -->
          <div v-for="(feature, idx) in [...features, ...features]" :key="idx" class="carousel-item">
            <div class="icon-wrapper">
              <!-- AI 对话 -->
              <svg v-if="feature.icon === 'chat'" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
              </svg>
              <!-- 上下文记忆 -->
              <svg v-else-if="feature.icon === 'memory'" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 2v20M2 12h20" />
                <circle cx="12" cy="12" r="10" />
              </svg>
              <!-- 个人中心 -->
              <svg v-else-if="feature.icon === 'profile'" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <!-- 主题切换 -->
              <svg v-else-if="feature.icon === 'theme'" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
              </svg>
            </div>
            <span class="feature-text">{{ feature.name }}</span>
          </div>
        </div>
      </div>

      <!-- 开始体验按钮 -->
      <button class="start-btn" @click="enterLobby">
        <span>开始体验</span>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M5 12h14M12 5l7 7-7 7" />
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.intro-page {
  min-height: 100vh;
  background: var(--bg-canvas, #FDFBF7);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.content-wrapper {
  text-align: center;
  max-width: 400px;
}

/* Logo 区域 */
.logo-section {
  margin-bottom: 48px;
}

.logo-pulse {
  width: 60px;
  height: 60px;
  margin: 0 auto 20px;
  border-radius: 50%;
  background: var(--accent, #4A7C9B);
  animation: logoPulse 2s ease-in-out infinite;
}

@keyframes logoPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.9;
  }
  50% {
    transform: scale(1.08);
    opacity: 1;
  }
}

.brand-name {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin-bottom: 8px;
}

.brand-tagline {
  font-size: 14px;
  color: var(--text-secondary, #6B6B6B);
  letter-spacing: 0.05em;
}

/* 滚动动画区域 */
.carousel-wrapper {
  position: relative;
  width: 100%;
  height: 120px;
  margin-bottom: 40px;
  overflow: hidden;
  mask-image: linear-gradient(to right, transparent, black 15%, black 85%, transparent);
  -webkit-mask-image: linear-gradient(to right, transparent, black 15%, black 85%, transparent);
}

.feature-carousel {
  display: flex;
  gap: 100px;
  position: absolute;
  left: 0;
  animation: carouselScroll 8s linear infinite;
}

@keyframes carouselScroll {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(calc(-140px * 4));
  }
}

.carousel-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 140px;
  flex-shrink: 0;
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent, #4A7C9B);
  /* 默认状态：小且透明 */
  transform: scale(0.75);
  opacity: 0.25;
}

.feature-text {
  font-size: 13px;
  color: var(--text-primary, #1A1A1A);
  font-weight: 500;
  white-space: nowrap;
  opacity: 0.3;
}

/* 使用 CSS 动画让经过中心的图标高亮 - 每个图标在 25%-37.5% 时间段高亮 */
.carousel-item:nth-child(1),
.carousel-item:nth-child(5) {
  animation: itemHighlight 8s linear infinite;
  animation-delay: 0s;
}

.carousel-item:nth-child(2),
.carousel-item:nth-child(6) {
  animation: itemHighlight 8s linear infinite;
  animation-delay: 2s;
}

.carousel-item:nth-child(3),
.carousel-item:nth-child(7) {
  animation: itemHighlight 8s linear infinite;
  animation-delay: 4s;
}

.carousel-item:nth-child(4),
.carousel-item:nth-child(8) {
  animation: itemHighlight 8s linear infinite;
  animation-delay: 6s;
}

@keyframes itemHighlight {
  0%, 12.5%, 50%, 100% {
    transform: translateX(0) scale(0.75);
  }
  25%, 37.5% {
    transform: translateX(0) scale(1.15);
  }
}

.carousel-item:nth-child(1) .icon-wrapper,
.carousel-item:nth-child(5) .icon-wrapper {
  animation: iconPulse 8s linear infinite;
  animation-delay: 0s;
}

.carousel-item:nth-child(2),
.carousel-item:nth-child(6) {
  animation: iconPulse 8s linear infinite;
  animation-delay: 2s;
}

.carousel-item:nth-child(3),
.carousel-item:nth-child(7) {
  animation: iconPulse 8s linear infinite;
  animation-delay: 4s;
}

.carousel-item:nth-child(4),
.carousel-item:nth-child(8) {
  animation: iconPulse 8s linear infinite;
  animation-delay: 6s;
}

@keyframes iconPulse {
  0%, 12.5%, 50%, 100% {
    opacity: 0.25;
  }
  25%, 37.5% {
    opacity: 1;
  }
}

.carousel-item:nth-child(1) .feature-text,
.carousel-item:nth-child(5) .feature-text {
  animation: textPulse 8s linear infinite;
  animation-delay: 0s;
}

.carousel-item:nth-child(2) .feature-text,
.carousel-item:nth-child(6) .feature-text {
  animation: textPulse 8s linear infinite;
  animation-delay: 2s;
}

.carousel-item:nth-child(3) .feature-text,
.carousel-item:nth-child(7) .feature-text {
  animation: textPulse 8s linear infinite;
  animation-delay: 4s;
}

.carousel-item:nth-child(4) .feature-text,
.carousel-item:nth-child(8) .feature-text {
  animation: textPulse 8s linear infinite;
  animation-delay: 6s;
}

@keyframes textPulse {
  0%, 12.5%, 50%, 100% {
    opacity: 0.3;
  }
  25%, 37.5% {
    opacity: 1;
  }
}

/* 开始体验按钮 */
.start-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 36px;
  background: var(--accent, #4A7C9B);
  border: none;
  border-radius: 10px;
  color: #ffffff;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.start-btn:hover {
  background: #3d6882;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(74, 124, 155, 0.3);
}

.start-btn:active {
  transform: translateY(0);
}

/* 暗色模式 */
[data-theme="dark"] .intro-page {
  --bg-canvas: #0F0F0F;
  --text-primary: #F5F5F5;
  --text-secondary: #A0A0A0;
  --accent: #6B9BC3;
}
</style>
