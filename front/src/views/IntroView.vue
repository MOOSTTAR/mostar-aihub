<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

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

      <!-- 3 秒循环动画：展示项目功能 -->
      <div class="feature-animation">
        <!-- 场景 1：AI 对话（0-1s, 1-2s 淡出） -->
        <div class="feature-scene scene-chat">
          <div class="chat-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
          </div>
          <span class="feature-text">AI 对话</span>
        </div>

        <!-- 场景 2：上下文记忆（1-2s, 2-3s 淡出） -->
        <div class="feature-scene scene-memory">
          <div class="memory-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 2v20M2 12h20" />
              <circle cx="12" cy="12" r="10" />
            </svg>
          </div>
          <span class="feature-text">上下文记忆</span>
        </div>

        <!-- 场景 3：个人中心（2-3s, 3-4s 淡出） -->
        <div class="feature-scene scene-profile">
          <div class="profile-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </div>
          <span class="feature-text">个人中心</span>
        </div>

        <!-- 场景 4：主题切换（3-4s, 4-5s 淡出） -->
        <div class="feature-scene scene-theme">
          <div class="theme-icon-wrapper">
            <svg class="sun-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="12" cy="12" r="5" />
              <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
            </svg>
            <svg class="moon-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
            </svg>
          </div>
          <span class="feature-text">主题切换</span>
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

/* 功能动画区域 */
.feature-animation {
  position: relative;
  height: 120px;
  margin-bottom: 40px;
}

.feature-scene {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  opacity: 0;
}

.feature-scene .feature-icon,
.feature-scene .theme-icon-wrapper {
  color: var(--accent, #4A7C9B);
}

.feature-text {
  font-size: 15px;
  color: var(--text-primary, #1A1A1A);
  font-weight: 500;
}

/* 场景动画 - 3 秒循环 */
.scene-chat {
  animation: sceneCycle 4s ease-in-out infinite;
}

.scene-memory {
  animation: sceneCycle 4s ease-in-out infinite 1s;
}

.scene-profile {
  animation: sceneCycle 4s ease-in-out infinite 2s;
}

.scene-theme {
  animation: sceneCycle 4s ease-in-out infinite 3s;
}

@keyframes sceneCycle {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }
  15% {
    opacity: 1;
    transform: translateY(0);
  }
  75% {
    opacity: 1;
    transform: translateY(0);
  }
  90%, 100% {
    opacity: 0;
    transform: translateY(-10px);
  }
}

/* 主题切换图标特殊动画 */
.theme-icon-wrapper {
  position: relative;
  width: 48px;
  height: 48px;
}

.sun-icon {
  position: absolute;
  opacity: 1;
  animation: sunFade 4s ease-in-out infinite;
}

.moon-icon {
  position: absolute;
  opacity: 0;
  animation: moonFade 4s ease-in-out infinite;
}

@keyframes sunFade {
  0%, 100% { opacity: 0; }
  25%, 75% { opacity: 1; }
}

@keyframes moonFade {
  0%, 50% { opacity: 0; }
  65%, 95% { opacity: 1; }
  100% { opacity: 0; }
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
