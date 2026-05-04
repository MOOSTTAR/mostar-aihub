<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'

const router = useRouter()
const currentIndex = ref(0)
const isDarkMode = ref(false)

// 当前使用的 logo（public 目录下的文件）
const logoB = '/image/logoBlack.png'
const whiteLogoB = '/image/logoWhite.png'

// 当前使用的 logo
const currentLogo = ref(logoB)

// 功能列表
const features = [
  { name: 'AI 对话', icon: 'chat' },
  { name: '上下文记忆', icon: 'memory' },
  { name: '个人中心', icon: 'profile' },
  { name: '主题切换', icon: 'theme' },
  { name: '登录鉴权', icon: 'auth' },
]

// 主题切换
const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  document.documentElement.setAttribute('data-theme', isDarkMode.value ? 'dark' : 'light')
  localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light')
  // 切换 logo
  currentLogo.value = isDarkMode.value ? whiteLogoB : logoB
}

const initTheme = () => {
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    isDarkMode.value = true
    document.documentElement.setAttribute('data-theme', 'dark')
    currentLogo.value = whiteLogoB
  } else {
    currentLogo.value = logoB
  }
}

// 进入大厅
const enterLobby = () => {
  router.push('/home')
}

// 计算每个 feature 应该显示的位置
const getItemRole = (index: number) => {
  const len = features.length
  const current = currentIndex.value

  // 计算相对于当前索引的位置
  let diff = (index - current + len) % len

  // 5 个元素循环，显示 3 个（left, center, right），隐藏 2 个
  // diff = 0: center
  // diff = 1: right
  // diff = 2: enter-right（在右边远处等待进入）
  // diff = 3: exit-left（在左边远处已离开）
  // diff = 4: left

  if (diff === 0) return 'center'
  if (diff === 1) return 'right'
  if (diff === 2) return 'enter-right'
  if (diff === 3) return 'exit-left'
  if (diff === 4) return 'left'

  return 'hidden'
}

// 获取元素的位置样式
const getPositionStyle = (index: number) => {
  const role = getItemRole(index)
  const baseOffset = 140 // 中心到两边的距离

  if (role === 'center') {
    return {
      transform: 'translateX(-50%) scale(1.2)',
      opacity: 1,
      zIndex: 10
    }
  } else if (role === 'left') {
    return {
      transform: `translateX(calc(-50% - ${baseOffset}px)) scale(0.75)`,
      opacity: 0.4,
      zIndex: 5
    }
  } else if (role === 'right') {
    return {
      transform: `translateX(calc(-50% + ${baseOffset}px)) scale(0.75)`,
      opacity: 0.4,
      zIndex: 5
    }
  } else if (role === 'exit-left') {
    // 在左边远处，已经离开（缩短移动距离）
    return {
      transform: `translateX(calc(-50% - ${baseOffset * 1.8}px)) scale(0.75)`,
      opacity: 0,
      zIndex: 1
    }
  } else if (role === 'enter-right') {
    // 在右边远处等待进入
    return {
      transform: `translateX(calc(-50% + ${baseOffset * 1.8}px)) scale(0.75)`,
      opacity: 0,
      zIndex: 1
    }
  }
  return { opacity: 0 }
}

// 显示所有 features，由 getPositionStyle 控制位置
const displayItems = features.map((feature, index) => ({
  ...feature,
  uniqueKey: index
}))

// 自动切换
onMounted(() => {
  initTheme()
  setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % features.length
  }, 1500)
})
</script>

<template>
  <div class="intro-page">
    <!-- 主题切换按钮 -->
    <button
      class="theme-toggle"
      @click="toggleTheme"
      :title="isDarkMode ? '切换亮色' : '切换暗色'"
    >
      <svg v-if="isDarkMode" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="5"/>
        <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/>
      </svg>
      <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
      </svg>
    </button>

    <div class="content-wrapper">
      <!-- Logo 和标题 -->
      <div class="logo-section">
        <img :src="currentLogo" alt="Logo" class="brand-logo" />
        <h1 class="brand-name">莫星 AI</h1>
        <p class="brand-tagline">探索智能的无限可能</p>
      </div>

      <!-- 功能展示区域 -->
      <div class="feature-display">
        <div
          v-for="item in displayItems"
          :key="item.uniqueKey"
          class="feature-item"
          :style="getPositionStyle(item.uniqueKey)"
        >
          <div class="icon-wrapper">
            <svg v-if="item.icon === 'chat'" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
            <svg v-else-if="item.icon === 'memory'" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 2v20M2 12h20" />
              <circle cx="12" cy="12" r="10" />
            </svg>
            <svg v-else-if="item.icon === 'profile'" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
            <svg v-else-if="item.icon === 'theme'" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
            </svg>
            <svg v-else-if="item.icon === 'auth'" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
          </div>
          <span class="feature-text">{{ item.name }}</span>
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
  position: relative;
}

/* 主题切换按钮 */
.theme-toggle {
  position: absolute;
  top: 24px;
  right: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border, #E8E6E1);
  border-radius: 10px;
  background: var(--bg-elevated, #FFFFFF);
  color: var(--text-secondary, #6B6B6B);
  cursor: pointer;
  transition: all 0.2s ease;
}

.theme-toggle:hover {
  border-color: var(--accent, #4A7C9B);
  color: var(--accent, #4A7C9B);
  transform: translateY(-1px);
}

.theme-toggle:active {
  transform: scale(0.96);
}

.content-wrapper {
  text-align: center;
  max-width: 400px;
}

/* Logo 区域 */
.logo-section {
  margin-bottom: 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.brand-logo {
  width: 60px;
  height: 60px;
  margin-bottom: 16px;
  object-fit: contain;
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

/* 功能展示区域 */
.feature-display {
  position: relative;
  width: 100%;
  height: 120px;
  margin-bottom: 40px;
}

.feature-item {
  position: absolute;
  left: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.icon-wrapper {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent, #4A7C9B);
}

.feature-text {
  font-size: 13px;
  color: var(--text-primary, #1A1A1A);
  font-weight: 500;
  white-space: nowrap;
  transition: opacity 0.6s cubic-bezier(0.4, 0, 0.2, 1);
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
  --bg-elevated: #1A1A1A;
  --border: #2A2A2A;
}

[data-theme="dark"] .theme-toggle {
  color: #E8E8E8 !important;
  border-color: #3A3A3A !important;
}

[data-theme="dark"] .theme-toggle:hover {
  color: #FFFFFF !important;
  border-color: #FFFFFF !important;
}
</style>