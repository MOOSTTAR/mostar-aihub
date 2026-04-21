<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = computed(() => authStore.userInfo?.username || '用户')

// 主题切换
const isDarkMode = ref(false)

const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  document.documentElement.setAttribute('data-theme', isDarkMode.value ? 'dark' : 'light')
  localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light')
}

const initTheme = () => {
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    isDarkMode.value = true
    document.documentElement.setAttribute('data-theme', 'dark')
  }
}

onMounted(() => {
  initTheme()
})

const modules = [
  {
    id: 'chat',
    title: '与 AI 对话',
    subtitle: 'Chat with AI',
    description: '智能问答、知识探索、创意灵感',
    icon: 'chat',
    color: '#4A7C9B',
    route: '/chat'
  }
]

const navigateTo = (route: string) => {
  router.push(route)
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="lobby">
    <!-- Header -->
    <header class="lobby-header">
      <div class="header-actions">
        <div class="user-info">
          <span class="username">{{ username }}</span>
        </div>
        <button class="icon-btn logout-btn" @click="handleLogout" title="退出登录">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </button>
        <button class="icon-btn theme-toggle" @click="toggleTheme" :title="isDarkMode ? '切换亮色' : '切换暗色'">
          <svg v-if="isDarkMode" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="5"/>
            <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/>
          </svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
          </svg>
        </button>
      </div>

      <div class="brand">
        <div class="logo-pulse"></div>
        <h1 class="brand-name">莫星 AI</h1>
      </div>
      <p class="brand-tagline">探索智能的无限可能</p>
    </header>

    <!-- Main Content -->
    <main class="lobby-main">
      <section class="modules-section">
        <h2 class="section-title">选择功能</h2>

        <div class="modules-grid">
          <div
            v-for="module in modules"
            :key="module.id"
            class="module-card"
            @click="navigateTo(module.route)"
          >
            <div class="module-icon" :style="{ background: module.color }">
              <svg
                v-if="module.icon === 'chat'"
                width="28"
                height="28"
                viewBox="0 0 24 24"
                fill="none"
                stroke="white"
                stroke-width="2"
              >
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
              </svg>
            </div>

            <div class="module-info">
              <h3 class="module-title">{{ module.title }}</h3>
              <p class="module-subtitle">{{ module.subtitle }}</p>
              <p class="module-desc">{{ module.description }}</p>
            </div>

            <div class="module-arrow">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6" />
              </svg>
            </div>
          </div>
        </div>
      </section>

      <!-- Future sections placeholder -->
      <section class="coming-soon">
        <h2 class="section-title">更多功能</h2>
        <p class="soon-text">敬请期待...</p>
      </section>
    </main>

    <!-- Footer -->
    <footer class="lobby-footer">
      <p>© 2025 莫星 AI · MOstArAI</p>
    </footer>
  </div>
</template>

<style scoped>
/* ============================================
   LOBBY - Hall Entrance Page
   ============================================ */

.lobby {
  min-height: 100vh;
  background: var(--bg-canvas, #FDFBF7);
  color: var(--text-primary, #1A1A1A);
  font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
  display: flex;
  flex-direction: column;
}

/* Header */
.lobby-header {
  position: relative;
  text-align: center;
  padding: 60px 24px 40px;
}

.header-actions {
  position: absolute;
  top: 24px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary, #6B6B6B);
}

.logout-btn:hover {
  border-color: #EF4444;
  color: #EF4444;
}

.icon-btn {
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

.icon-btn:hover {
  border-color: var(--accent, #4A7C9B);
  color: var(--accent, #4A7C9B);
  transform: translateY(-1px);
}

.icon-btn:active {
  transform: scale(0.96);
}

.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}

.logo-pulse {
  width: 12px;
  height: 12px;
  background: var(--accent, #4A7C9B);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.9); }
}

.brand-name {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: var(--text-primary, #1A1A1A);
}

.brand-tagline {
  font-size: 15px;
  color: var(--text-secondary, #6B6B6B);
  letter-spacing: 0.02em;
}

/* Main Content */
.lobby-main {
  flex: 1;
  max-width: 720px;
  width: 100%;
  margin: 0 auto;
  padding: 0 24px 40px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--text-tertiary, #A3A3A3);
  margin-bottom: 16px;
}

/* Modules Grid */
.modules-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.module-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--bg-elevated, #FFFFFF);
  border: 1px solid var(--border, #E8E6E1);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.module-card:hover {
  border-color: var(--accent, #4A7C9B);
  box-shadow: 0 4px 20px rgba(74, 124, 155, 0.1);
  transform: translateY(-2px);
}

.module-card:active {
  transform: scale(0.99);
}

.module-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.module-info {
  flex: 1;
}

.module-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  margin-bottom: 2px;
}

.module-subtitle {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-tertiary, #A3A3A3);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 4px;
}

.module-desc {
  font-size: 13px;
  color: var(--text-secondary, #6B6B6B);
}

.module-arrow {
  color: var(--text-tertiary, #A3A3A3);
  transition: transform 0.2s ease;
}

.module-card:hover .module-arrow {
  transform: translateX(4px);
  color: var(--accent, #4A7C9B);
}

/* Coming Soon */
.coming-soon {
  margin-top: 40px;
  padding-top: 40px;
  border-top: 1px solid var(--border, #E8E6E1);
}

.soon-text {
  font-size: 14px;
  color: var(--text-tertiary, #A3A3A3);
  text-align: center;
  padding: 24px;
  background: var(--bg-subtle, #F5F3EF);
  border-radius: 12px;
  border: 1px dashed var(--border, #E8E6E1);
}

/* Footer */
.lobby-footer {
  text-align: center;
  padding: 24px;
  font-size: 12px;
  color: var(--text-tertiary, #A3A3A3);
  border-top: 1px solid var(--border, #E8E6E1);
}

/* 滚动条样式 */
.lobby::-webkit-scrollbar {
  width: 8px;
}

.lobby::-webkit-scrollbar-track {
  background: transparent;
}

.lobby::-webkit-scrollbar-thumb {
  background: #D0D0D0;
  border-radius: 4px;
}

.lobby::-webkit-scrollbar-thumb:hover {
  background: #B0B0B0;
}

/* 黑暗模式滚动条 */
[data-theme="dark"] .lobby::-webkit-scrollbar {
  width: 8px;
}

[data-theme="dark"] .lobby::-webkit-scrollbar-track {
  background: transparent;
}

[data-theme="dark"] .lobby::-webkit-scrollbar-thumb {
  background: #3A3A3A;
  border-radius: 4px;
}

[data-theme="dark"] .lobby::-webkit-scrollbar-thumb:hover {
  background: #4A4A4A;
}

/* Dark Mode Support */
[data-theme="dark"] .lobby {
  --bg-canvas: #0F0F0F;
  --bg-elevated: #1A1A1A;
  --bg-subtle: #141414;
  --text-primary: #F5F5F5;
  --text-secondary: #A0A0A0;
  --text-tertiary: #6B6B6B;
  --border: #2A2A2A;
  --accent: #6B9BC3;
}

/* Responsive */
@media (max-width: 640px) {
  .lobby-header {
    padding: 40px 20px 32px;
  }

  .brand-name {
    font-size: 24px;
  }

  .lobby-main {
    padding: 0 16px 32px;
  }

  .module-card {
    padding: 16px;
  }

  .module-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
  }
}
</style>
