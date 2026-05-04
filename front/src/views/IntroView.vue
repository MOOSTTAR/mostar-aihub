<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 动画状态
const showAnimation = ref(true)
const showEnterButton = ref(false)
const logoScale = ref(0)
const logoOpacity = ref(0)
const pulseOpacity = ref(0)

// 进入大厅
const enterLobby = () => {
  // 检查是否已登录
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/home')
  } else {
    router.push('/login')
  }
}

onMounted(() => {
  // Logo 放大动画
  setTimeout(() => {
    logoScale.value = 1
    logoOpacity.value = 1
  }, 100)

  // 光晕脉动动画
  setTimeout(() => {
    pulseOpacity.value = 1
  }, 500)

  // 动画结束后显示进入按钮
  setTimeout(() => {
    showAnimation.value = false
    showEnterButton.value = true
  }, 2000)
})
</script>

<template>
  <div class="intro-page">
    <!-- 开场动画 -->
    <div v-if="showAnimation" class="intro-animation">
      <div class="logo-container" :style="{
        transform: `scale(${logoScale})`,
        opacity: logoOpacity
      }">
        <div class="logo-pulse"></div>
        <div class="logo-core"></div>
      </div>
      <h1 class="brand-name">莫星 AI</h1>
      <p class="brand-tagline">探索智能的无限可能</p>
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
  background: linear-gradient(135deg, #0f0f0f 0%, #1a1a2e 50%, #16213e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* 开场动画区域 */
.intro-animation {
  text-align: center;
  animation: fadeOut 0.5s ease forwards;
}

@keyframes fadeOut {
  to {
    opacity: 0;
    transform: scale(1.1);
  }
}

/* Logo 容器 */
.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  margin: 0 auto 24px;
  transition: all 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 外圈光晕 */
.logo-pulse {
  position: absolute;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(107, 155, 195, 0.6) 0%, transparent 70%);
  animation: pulse 2s ease-in-out infinite;
  opacity: 0;
  transition: opacity 0.5s ease;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}

/* 核心光点 */
.logo-core {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: radial-gradient(circle, #6b9bc3 0%, #4a7c9b 100%);
  box-shadow: 0 0 30px rgba(107, 155, 195, 0.8);
  animation: glow 1.5s ease-in-out infinite alternate;
}

@keyframes glow {
  from {
    box-shadow: 0 0 20px rgba(107, 155, 195, 0.6);
  }
  to {
    box-shadow: 0 0 40px rgba(107, 155, 195, 1);
  }
}

/* 品牌名称 */
.brand-name {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 8px;
  letter-spacing: 0.05em;
  text-shadow: 0 2px 20px rgba(107, 155, 195, 0.5);
}

/* 标语 */
.brand-tagline {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 0.1em;
}

/* 进入按钮区域 */
.enter-section {
  text-align: center;
  animation: fadeIn 0.8s ease forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.enter-btn {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 16px 40px;
  background: linear-gradient(135deg, #6b9bc3 0%, #4a7c9b 100%);
  border: none;
  border-radius: 12px;
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(107, 155, 195, 0.4);
}

.enter-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(107, 155, 195, 0.6);
}

.enter-btn:active {
  transform: translateY(-1px);
}

.enter-btn span {
  letter-spacing: 0.05em;
}
</style>
