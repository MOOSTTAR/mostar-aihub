<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

import defaultAvatar from '../assets/1728101288756.jpg'

const username = computed(() => authStore.userInfo?.username || '用户')

// 头像 URL
const avatarUrl = computed(() => {
  return defaultAvatar
})

// 用户下拉菜单
const isUserMenuOpen = ref(false)
const userMenuRef = ref<HTMLElement | null>(null)

const toggleUserMenu = () => {
  isUserMenuOpen.value = !isUserMenuOpen.value
}

const closeUserMenu = () => {
  isUserMenuOpen.value = false
}

const goToProfile = () => {
  closeUserMenu()
  router.push('/profile')
}

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

  // 点击外部关闭菜单
  document.addEventListener('click', (event) => {
    if (userMenuRef.value && !userMenuRef.value.contains(event.target as Node)) {
      closeUserMenu()
    }
  })
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
  const result = await ElMessageBox.confirm('是否确认退出登录？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    confirmButtonClass: 'custom-confirm-btn',
    cancelButtonClass: 'custom-cancel-btn',
  })

  if (result) {
    await authStore.logout()
    router.push('/login')
  }
}
</script>

<template>
  <div class="lobby">
    <!-- Header -->
    <header class="lobby-header">
      <div class="header-actions">
        <!-- 主题切换按钮 -->
        <button class="icon-btn theme-toggle" @click="toggleTheme" :title="isDarkMode ? '切换亮色' : '切换暗色'">
          <svg v-if="isDarkMode" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="5"/>
            <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/>
          </svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
          </svg>
        </button>

        <!-- 用户下拉菜单 -->
        <div class="user-menu" :class="{ open: isUserMenuOpen }" ref="userMenuRef">
          <div class="user-menu-trigger" @click="toggleUserMenu">
            <img :src="avatarUrl" alt="头像" class="user-avatar" />
            <span class="username">{{ username }}</span>
            <svg
              class="dropdown-arrow"
              :class="{ rotated: isUserMenuOpen }"
              width="12"
              height="12"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M6 9l6 6 6-6" />
            </svg>
          </div>

          <div class="user-menu-dropdown" v-show="isUserMenuOpen">
            <div class="menu-item" @click="goToProfile">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <span>个人中心</span>
            </div>
            <div class="menu-item" @click="handleLogout">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                <polyline points="16 17 21 12 16 7" />
                <line x1="21" y1="12" x2="9" y2="12" />
              </svg>
              <span>退出登录</span>
            </div>
          </div>
        </div>
      </div>

      <div class="brand">
        <div class="logo-pulse"></div>
        <h1 class="brand-name">莫星 AI</h1>
        <a
          class="icon-btn github-btn"
          href="https://github.com/MOOSTTAR/mostar-aihub"
          target="_blank"
          rel="noopener noreferrer"
          title="访问 GitHub 项目"
        >
          <svg
            width="22"
            height="22"
            viewBox="0 0 24 24"
            fill="currentColor"
          >
            <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
          </svg>
        </a>
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
      <p>© 2026 莫星 AI · MOstArAI</p>
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

/* GitHub 按钮 */
.github-btn {
  color: var(--text-primary, #1A1A1A);
}

.github-btn:hover {
  color: #181717;
  border-color: #181717;
}

/* 用户下拉菜单 */
.user-menu {
  position: relative;
}

.user-menu-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px 6px 6px;
  height: 40px;
  background: var(--bg-elevated, #FFFFFF);
  border: 1px solid var(--border, #E8E6E1);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  background: var(--bg-subtle, #f0f0f0);
}

.user-menu-trigger:hover {
  border-color: var(--accent, #4A7C9B);
}

.user-menu-trigger .username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary, #1A1A1A);
}

.dropdown-arrow {
  color: var(--text-tertiary, #A3A3A3);
  transition: transform 0.2s ease;
}

.dropdown-arrow.rotated {
  transform: rotate(180deg);
}

.user-menu-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 160px;
  background: var(--bg-elevated, #FFFFFF);
  border: 1px solid var(--border, #E8E6E1);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  padding: 6px;
  z-index: 1000;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-primary, #1A1A1A);
}

.menu-item:hover {
  background: var(--bg-subtle, #F5F3EF);
}

.menu-item svg {
  color: var(--text-secondary, #6B6B6B);
}

.menu-item span {
  font-size: 14px;
  font-weight: 500;
}

/* 暗色模式 */
[data-theme="dark"] .github-btn {
  color: #F5F5F5;
}

[data-theme="dark"] .github-btn:hover {
  color: #FFFFFF;
  border-color: #FFFFFF;
}

[data-theme="dark"] .user-menu-trigger {
  background: #1A1A1A;
  border-color: #2A2A2A;
}

[data-theme="dark"] .user-menu-trigger .username {
  color: #F5F5F5;
}

[data-theme="dark"] .user-menu-dropdown {
  background: #1A1A1A;
  border-color: #2A2A2A;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

[data-theme="dark"] .menu-item:hover {
  background: #2A2A2A;
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

/* 暗色模式下主题切换按钮滑块为浅白色 */
[data-theme="dark"] .theme-toggle {
  color: #E8E8E8 !important;
  border-color: #3A3A3A !important;
}

[data-theme="dark"] .theme-toggle:hover {
  color: #FFFFFF !important;
  border-color: #FFFFFF !important;
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
