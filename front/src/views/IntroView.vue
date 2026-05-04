<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 动画状态
const showAnimation = ref(true)
const showEnterButton = ref(false)
const logoOpacity = ref(0)
const logoY = ref(20)
const textOpacity = ref(0)
const textY = ref(15)

// 进入大厅
const enterLobby = () => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/home')
  } else {
    router.push('/login')
  }
}

onMounted(() => {
  // Logo 淡入上浮
  setTimeout(() => {
    logoOpacity.value = 1
    logoY.value = 0
  }, 100)

  // 文字淡入上浮
  setTimeout(() => {
    textOpacity.value = 1
    textY.value = 0
  }, 400)

  // 动画结束后显示进入按钮
  setTimeout(() => {
    showAnimation.value = false
    showEnterButton.value = true
  }, 1200)
})
</script>

<template>
  <div class="intro-page">
    <!-- 开场动画 -->
    <div v-if="showAnimation" class="intro-animation">
      <div class="logo-container" :style="{
        opacity: logoOpacity,
        transform: `translateY(${logoY}px)`
      }">
        <div class="logo-pulse"></div>
      </div>
      <h1 class="brand-name" :style="{
        opacity: textOpacity,
        transform: `translateY(${textY}px)`
      }">莫星 AI</h1>
      <p class="brand-tagline" :style="{
        opacity: textOpacity,
        transform: `translateY(${textY}px)`
      }">探索智能的无限可能</p>
    </div>

    <!-- 进入按钮 -->
    <div v-if="showEnterButton" class="enter-section">
      <button class="enter-btn" @click="enterLobby">
        <span>进入</span>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
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
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: background 0.3s ease;
}

/* 开场动画区域 */
.intro-animation {
  text-align: center;
  transition: opacity 0.5s ease;
}

/* Logo 容器 */
.logo-container {
  width: 80px;
  height: 80px;
  margin: 0 auto 32px;
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 光晕效果 */
.logo-pulse {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: var(--accent, #4A7C9B);
  opacity: 0.9;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.9;
  }
  50% {
    transform: scale(1.05);
    opacity: 1;
  }
}

/* 品牌名称 */
.brand-name {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin-bottom: 8px;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 标语 */
.brand-tagline {
  font-size: 14px;
  color: var(--text-secondary, #6B6B6B);
  letter-spacing: 0.05em;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 进入按钮区域 */
.enter-section {
  text-align: center;
  animation: fadeIn 0.6s ease forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.enter-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  background: var(--accent, #4A7C9B);
  border: none;
  border-radius: 10px;
  color: #ffffff;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.enter-btn:hover {
  background: #3d6882;
  transform: translateY(-1px);
}

.enter-btn:active {
  transform: translateY(0);
}

/* 暗色模式支持 */
[data-theme="dark"] .intro-page {
  --bg-canvas: #0F0F0F;
  --text-primary: #F5F5F5;
  --text-secondary: #A0A0A0;
  --accent: #6B9BC3;
}
</style>
