<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const rememberMe = ref(false)
const errorMsg = ref('')
const isLoading = ref(false)

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

  // 如果已登录，跳转到首页
  if (authStore.isLoggedIn) {
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  }
})

const handleLogin = async () => {
  errorMsg.value = ''

  if (!username.value.trim() || !password.value.trim()) {
    errorMsg.value = '请输入用户名和密码'
    return
  }

  isLoading.value = true

  const success = await authStore.login({
    username: username.value.trim(),
    password: password.value,
    rememberMe: rememberMe.value
  })

  isLoading.value = false

  if (success) {
    ElMessage.success('登录成功')
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } else {
    errorMsg.value = '用户名或密码错误'
  }
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    handleLogin()
  }
}
</script>

<template>
  <div class="login-page">
    <!-- Theme Toggle -->
    <button class="theme-toggle" @click="toggleTheme" :title="isDarkMode ? '切换亮色' : '切换暗色'">
      <svg v-if="isDarkMode" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="5"/>
        <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/>
      </svg>
      <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
      </svg>
    </button>

    <!-- Login Card -->
    <div class="login-card">
      <!-- Brand -->
      <div class="brand">
        <div class="logo-pulse"></div>
        <h1 class="brand-name">莫星 AI</h1>
      </div>
      <p class="brand-tagline">欢迎回来</p>

      <!-- Login Form -->
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <div class="input-wrapper">
            <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <input
              v-model="username"
              type="text"
              placeholder="请输入用户名"
              class="form-input"
              @keydown="handleKeydown"
            />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">密码</label>
          <div class="input-wrapper">
            <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              class="form-input"
              @keydown="handleKeydown"
            />
          </div>
        </div>

        <!-- Remember Me -->
        <div class="form-options">
          <label class="checkbox-wrapper">
            <input v-model="rememberMe" type="checkbox" class="checkbox-input" />
            <span class="checkbox-custom"></span>
            <span class="checkbox-label">记住我</span>
          </label>
        </div>

        <!-- Error Message -->
        <div v-if="errorMsg" class="error-message">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          {{ errorMsg }}
        </div>

        <!-- Login Button -->
        <button
          type="submit"
          class="login-btn"
          :disabled="isLoading || !username.trim() || !password.trim()"
          :class="{ 'is-loading': isLoading }"
        >
          <span v-if="isLoading" class="loading-spinner"></span>
          <span>{{ isLoading ? '登录中...' : '登录' }}</span>
        </button>
      </form>

      <!-- Footer -->
      <div class="login-footer">
        <p>© 2025 莫星 AI · MOstArAI</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ============================================
   LOGIN PAGE - Soft UI + Minimalist Fusion
   ============================================ */

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-canvas, #FDFBF7);
  padding: 24px;
  position: relative;
}

/* Theme Toggle */
.theme-toggle {
  position: absolute;
  top: 24px;
  right: 24px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border, #E8E6E1);
  border-radius: 12px;
  background: var(--bg-elevated, #FFFFFF);
  color: var(--text-secondary, #6B6B6B);
  cursor: pointer;
  transition: all 0.2s ease;
}

.theme-toggle:hover {
  border-color: var(--accent, #4A7C9B);
  color: var(--accent, #4A7C9B);
  transform: translateY(-2px);
}

/* Login Card */
.login-card {
  width: 100%;
  max-width: 400px;
  padding: 48px 40px;
  background: var(--bg-elevated, #FFFFFF);
  border: 1px solid var(--border, #E8E6E1);
  border-radius: 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
}

/* Brand */
.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 8px;
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
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: var(--text-primary, #1A1A1A);
}

.brand-tagline {
  text-align: center;
  font-size: 15px;
  color: var(--text-secondary, #6B6B6B);
  margin-bottom: 32px;
}

/* Form */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary, #1A1A1A);
  letter-spacing: 0.02em;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  color: var(--text-tertiary, #A3A3A3);
  pointer-events: none;
}

.form-input {
  width: 100%;
  height: 48px;
  padding: 0 16px 0 44px;
  font-size: 15px;
  color: var(--text-primary, #1A1A1A);
  background: var(--bg-canvas, #FDFBF7);
  border: 1px solid var(--border, #E8E6E1);
  border-radius: 12px;
  outline: none;
  transition: all 0.2s ease;
}

.form-input::placeholder {
  color: var(--text-tertiary, #A3A3A3);
}

.form-input:focus {
  border-color: var(--accent, #4A7C9B);
  box-shadow: 0 0 0 3px rgba(74, 124, 155, 0.08);
}

/* Options */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-input {
  display: none;
}

.checkbox-custom {
  width: 18px;
  height: 18px;
  border: 1px solid var(--border, #E8E6E1);
  border-radius: 5px;
  background: var(--bg-canvas, #FDFBF7);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.checkbox-custom::after {
  content: '';
  width: 10px;
  height: 10px;
  background: var(--accent, #4A7C9B);
  border-radius: 2px;
  opacity: 0;
  transform: scale(0);
  transition: all 0.2s ease;
}

.checkbox-input:checked + .checkbox-custom {
  border-color: var(--accent, #4A7C9B);
}

.checkbox-input:checked + .checkbox-custom::after {
  opacity: 1;
  transform: scale(1);
}

.checkbox-label {
  font-size: 13px;
  color: var(--text-secondary, #6B6B6B);
}

/* Error Message */
.error-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(239, 68, 68, 0.08);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 10px;
  font-size: 13px;
  color: #EF4444;
}

/* Login Button */
.login-btn {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: white;
  background: var(--accent, #4A7C9B);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.login-btn:hover:not(:disabled) {
  background: var(--accent-hover, #3D6A85);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(74, 124, 155, 0.3);
}

.login-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.login-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.login-btn.is-loading {
  opacity: 0.8;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Footer */
.login-footer {
  margin-top: 32px;
  text-align: center;
}

.login-footer p {
  font-size: 12px;
  color: var(--text-tertiary, #A3A3A3);
}

/* Dark Mode Support */
[data-theme="dark"] .login-page {
  --bg-canvas: #0F0F0F;
  --bg-elevated: #1A1A1A;
  --text-primary: #F5F5F5;
  --text-secondary: #A0A0A0;
  --text-tertiary: #6B6B6B;
  --border: #2A2A2A;
  --accent: #6B9BC3;
  --accent-hover: #8AB4D4;
}

/* Responsive */
@media (max-width: 480px) {
  .login-card {
    padding: 36px 24px;
  }

  .brand-name {
    font-size: 22px;
  }

  .theme-toggle {
    top: 16px;
    right: 16px;
  }
}
</style>
