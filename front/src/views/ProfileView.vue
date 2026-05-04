<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

// 用户信息
const userInfo = ref({
  username: '',
  phonenum: '',
  email: '',
  bio: '',
  birthday: '' as string | null,
  gender: 0,
  githubUrl: ''
})

// 加载状态
const loading = ref(false)

// 是否处于编辑模式
const isEditing = ref(false)

// 编辑中的临时值 - 存储所有可编辑字段的值
const editValues = ref<Record<string, any>>({})

import defaultAvatar from '../assets/1728101288756.jpg'

// 头像 URL
const avatarUrl = computed(() => {
  return defaultAvatar
})

// 性别文本映射
const genderText = computed(() => {
  const map: Record<number, string> = { 0: '未知', 1: '男', 2: '女' }
  return map[userInfo.value.gender] || '未知'
})

// 获取用户信息
const fetchUserInfo = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/user/info', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    const result = await response.json()
    console.log('后端返回的用户信息:', result)
    if (result.success && result.data) {
      userInfo.value = {
        username: result.data.username || '',
        phonenum: result.data.phonenum || '',
        email: result.data.email || '',
        bio: result.data.bio ?? '',
        birthday: result.data.birthday ? result.data.birthday.toString() : null,
        gender: result.data.gender ?? 0,
        githubUrl: result.data.githubUrl ?? ''
      }
      console.log('前端解析后的用户信息:', userInfo.value)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 开始整体编辑模式
const startEditMode = () => {
  // 初始化所有可编辑字段的临时值
  const editableFields = fields.filter(f => f.editable).map(f => f.key)
  editableFields.forEach(field => {
    const value = userInfo.value[field]
    if (field === 'birthday' && value) {
      editValues.value[field] = value
    } else if (field === 'gender') {
      editValues.value[field] = value
    } else {
      editValues.value[field] = value || ''
    }
  })
  isEditing.value = true
}

// 取消编辑模式
const cancelEditMode = () => {
  isEditing.value = false
  editValues.value = {}
}

// 保存所有编辑
const saveAllEdits = async () => {
  loading.value = true
  try {
    const submitData: Record<string, any> = {}

    fields.filter(f => f.editable).forEach(field => {
      const key = field.key
      let value = editValues.value[key]

      if (key === 'birthday') {
        submitData[key] = value || null
      } else if (key === 'gender') {
        submitData[key] = parseInt(value) || 0
      } else {
        submitData[key] = value
      }
    })

    const response = await fetch('/api/user/info', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(submitData)
    })
    const result = await response.json()

    if (result.success) {
      // 更新本地数据
      fields.filter(f => f.editable).forEach(field => {
        const key = field.key
        let value = editValues.value[key]

        if (key === 'birthday') {
          userInfo.value[key] = value || null
        } else if (key === 'gender') {
          userInfo.value[key] = parseInt(value) || 0
        } else {
          userInfo.value[key] = value
        }
      })
      ElMessage.success('保存成功')
      isEditing.value = false
      editValues.value = {}
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    loading.value = false
  }
}

// 返回大厅
const goBack = () => {
  router.push('/')
}

// 点击头像提示
const handleAvatarClick = () => {
  ElMessageBox.alert('头像修改功能尚未开发，敬请期待~', '提示', {
    confirmButtonText: '知道了'
  })
}

// 获取字段显示值
const getDisplayValue = (field: keyof typeof userInfo.value): string => {
  const value = userInfo.value[field]
  if (field === 'gender') {
    return genderText.value
  }
  if (field === 'birthday' && value) {
    return new Date(value).toLocaleDateString('zh-CN')
  }
  // 用户名字段特殊处理，有值显示值，无值显示'未设置'
  if (field === 'username') {
    return value ? String(value) : '未设置'
  }
  return value ? String(value) : '未设置'
}

// 字段配置
interface FieldConfig {
  key: keyof typeof userInfo.value
  label: string
  editable: boolean
  placeholder?: string
  type?: 'text' | 'date' | 'select'
  options?: { value: number; label: string }[]
}

const fields: FieldConfig[] = [
  { key: 'username', label: '用户名', editable: false },
  { key: 'phonenum', label: '手机号', editable: false },
  { key: 'email', label: '邮箱', editable: true, type: 'text', placeholder: '请输入邮箱' },
  { key: 'bio', label: '个性签名', editable: true, type: 'text', placeholder: '来点酷酷的签名~' },
  { key: 'birthday', label: '生日', editable: true, type: 'date' },
  { key: 'gender', label: '性别', editable: true, type: 'select', options: [
    { value: 0, label: '未知' },
    { value: 1, label: '男' },
    { value: 2, label: '女' }
  ]},
  { key: 'githubUrl', label: 'GitHub', editable: true, type: 'text', placeholder: 'https://github.com/yourname' }
]

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
  fetchUserInfo()
})
</script>

<template>
  <div class="profile-page">
    <div class="profile-container">
      <!-- 头部 -->
      <header class="profile-header">
        <button class="back-btn" @click="goBack">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7" />
          </svg>
          返回
        </button>
        <h1 class="page-title">个人中心</h1>
        <div class="header-actions">
          <div
            class="theme-switch"
            :class="isDarkMode ? 'dark' : 'light'"
            @click="toggleTheme"
            :title="isDarkMode ? '切换亮色' : '切换暗色'"
          >
            <div class="theme-switch-track">
              <span class="theme-icon light-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="5" />
                  <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
                </svg>
              </span>
              <span class="theme-icon dark-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
                </svg>
              </span>
            </div>
            <div class="theme-switch-thumb"></div>
          </div>
        </div>
      </header>

      <!-- 内容区域 -->
      <div class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-label">头像</div>
          <div class="avatar-wrapper" @click="handleAvatarClick">
            <img :src="avatarUrl" alt="头像" class="avatar" />
            <div class="avatar-overlay">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z" />
                <circle cx="12" cy="13" r="4" />
              </svg>
              <span>修改头像</span>
            </div>
          </div>
        </div>

        <!-- 信息列表 -->
        <div class="info-list">
          <div
            v-for="field in fields"
            :key="field.key"
            class="info-item"
            :class="{
              'editable': field.editable && isEditing,
              'readonly': !field.editable
            }"
          >
            <div class="info-label">{{ field.label }}</div>

            <!-- 展示模式 / 编辑模式 -->
            <div class="info-value">
              <span v-if="!isEditing || !field.editable" :class="{ 'placeholder': !userInfo[field.key] && field.key !== 'gender' }">
                {{ getDisplayValue(field.key) }}
              </span>
              <template v-else>
                <!-- 文本输入 -->
                <el-input
                  v-if="field.type === 'text' || !field.type"
                  v-model="editValues[field.key]"
                  size="small"
                  :placeholder="field.placeholder"
                  style="flex: 1"
                />
                <!-- 日期选择 -->
                <el-date-picker
                  v-else-if="field.type === 'date'"
                  v-model="editValues[field.key]"
                  type="date"
                  placeholder="选择日期"
                  value-format="YYYY-MM-DD"
                  size="small"
                  style="flex: 1"
                />
                <!-- 下拉选择 -->
                <el-select
                  v-else-if="field.type === 'select'"
                  v-model="editValues[field.key]"
                  size="small"
                  class="profile-select"
                  style="flex: 1"
                >
                  <el-option
                    v-for="opt in field.options"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </template>
            </div>
          </div>
        </div>

        <!-- 底部操作按钮 -->
        <div class="action-buttons">
          <template v-if="!isEditing">
            <button class="action-btn edit-btn" @click="startEditMode">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
              修改
            </button>
          </template>
          <template v-else>
            <button class="action-btn save-btn" @click="saveAllEdits" :disabled="loading">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12" />
              </svg>
              保存
            </button>
            <button class="action-btn cancel-btn" @click="cancelEditMode">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
              取消
            </button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--bg-canvas, #f5f5f5);
  padding: 24px;
}

.profile-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

/* Header */
.profile-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  position: relative;
  flex-shrink: 0;
}

.header-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--bg-elevated, #ffffff);
  border: 1px solid var(--border, #e0e0e0);
  border-radius: 10px;
  color: var(--text-primary, #1a1a1a);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.back-btn:hover {
  border-color: var(--accent, #4a7c9b);
  color: var(--accent, #4a7c9b);
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary, #1a1a1a);
  margin: 0;
}

/* Content */
.profile-content {
  background: var(--bg-elevated, #ffffff);
  border-radius: 16px;
  padding: 32px;
  border: 1px solid var(--border, #e8e6e1);
  flex-shrink: 0;
}

/* Avatar Section */
.avatar-section {
  margin-bottom: 40px;
  text-align: center;
  padding-bottom: 32px;
  border-bottom: 1px solid var(--border, #e8e6e1);
}

.avatar-label {
  font-size: 14px;
  color: var(--text-secondary, #6b6b6b);
  margin-bottom: 16px;
}

.avatar-wrapper {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: var(--bg-subtle, #f0f0f0);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
  font-size: 12px;
}

/* Info List */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.info-item.editable {
  background: var(--bg-subtle, #f5f3ef);
}

.info-label {
  width: 100px;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary, #6b6b6b);
}

.info-value {
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 15px;
  color: var(--text-primary, #1a1a1a);
}

.info-value .placeholder {
  color: var(--text-tertiary, #a3a3a3);
  font-style: italic;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border, #e8e6e1);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn.edit-btn {
  background: var(--accent, #4a7c9b);
  color: white;
}

.action-btn.edit-btn:hover {
  background: #3d6882;
}

.action-btn.save-btn {
  background: var(--accent, #4a7c9b);
  color: white;
}

.action-btn.save-btn:hover {
  background: #3d6882;
}

.action-btn.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.action-btn.cancel-btn {
  background: transparent;
  color: var(--text-secondary, #6b6b6b);
  border: 1px solid var(--border, #e0e0e0);
}

.action-btn.cancel-btn:hover {
  background: var(--bg-elevated, #ffffff);
  color: var(--text-primary, #1a1a1a);
}

/* Dark Mode */
[data-theme='dark'] .profile-page {
  --bg-canvas: #0f0f0f;
  --bg-elevated: #1a1a1a;
  --bg-subtle: #2a2a2a;
  --text-primary: #f5f5f5;
  --text-secondary: #a0a0a0;
  --text-tertiary: #6b6b6b;
  --border: #2a2a2a;
  --accent: #6b9bc3;
}

/* 暗色模式下输入框样式覆盖 - 使用:deep() 穿透 scoped */
.profile-page:deep(.el-input__wrapper) {
  background-color: var(--bg-elevated, #ffffff);
}

[data-theme='dark'] .profile-page:deep(.el-input__wrapper),
[data-theme='dark'] .profile-page:deep(.el-select .el-input__wrapper),
[data-theme='dark'] .profile-page:deep(.el-date-picker .el-input__wrapper) {
  background-color: #1a1a1a !important;
  box-shadow: 0 0 0 1px #2a2a2a inset !important;
}

[data-theme='dark'] .profile-page:deep(.el-input__inner),
[data-theme='dark'] .profile-page:deep(.el-select .el-input__inner) {
  color: #f5f5f5 !important;
}

[data-theme='dark'] .profile-page:deep(.el-textarea__inner) {
  background-color: #1a1a1a !important;
  color: #f5f5f5 !important;
}

[data-theme='dark'] .profile-page:deep(.el-input__wrapper:hover),
[data-theme='dark'] .profile-page:deep(.el-select .el-input__wrapper:hover),
[data-theme='dark'] .profile-page:deep(.el-date-picker .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #6b9bc3 inset !important;
}

/* Theme Switch Toggle */
.theme-switch {
  width: 70px;
  height: 40px;
  position: relative;
  cursor: pointer;
  user-select: none;
}

.theme-switch-track {
  width: 100%;
  height: 100%;
  border-radius: 10px;
  background: var(--bg-elevated, #ffffff);
  border: 1px solid var(--border, #e0e0e0);
  position: relative;
  overflow: hidden;
  transition: all var(--duration-fast, 200ms) var(--ease-smooth, cubic-bezier(0.4, 0, 0.2, 1));
}

.theme-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  transition: opacity var(--duration-fast, 200ms) var(--ease-smooth, cubic-bezier(0.4, 0, 0.2, 1));
}

.theme-icon.light-icon {
  color: #f59e0b;
  opacity: 1;
  left: 8px;
}

.theme-icon.dark-icon {
  color: #60a5fa;
  opacity: 0.5;
  right: 8px;
}

.theme-switch.dark .theme-icon.light-icon {
  opacity: 0.5;
}

.theme-switch.dark .theme-icon.dark-icon {
  opacity: 1;
}

.theme-switch.light .theme-icon.light-icon {
  opacity: 1;
}

.theme-switch.light .theme-icon.dark-icon {
  opacity: 0.5;
}

.theme-switch-thumb {
  position: absolute;
  top: 4px;
  left: 4px;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #e0e0e0;
  border: 1px solid var(--border, #e0e0e0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  transition: all var(--duration-normal, 300ms) var(--ease-smooth, cubic-bezier(0.4, 0, 0.2, 1));
  z-index: 2;
}

.theme-switch.dark .theme-switch-thumb {
  transform: translateX(32px);
  background: var(--accent);
  border-color: var(--accent);
}

.theme-switch.light .theme-switch-thumb {
  background: #e0e0e0;
  border-color: var(--border);
}

/* 暗色模式下滑块为浅白色 */
[data-theme='dark'] .theme-switch-thumb {
  background: #C0C0C0 !important;
  border-color: #C0C0C0 !important;
  border-radius: 8px !important;
}

[data-theme='dark'] .theme-switch:hover .theme-switch-thumb {
  background: #D0D0D0 !important;
}

.theme-switch:hover .theme-switch-thumb {
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

.theme-switch.light:hover .theme-switch-thumb {
  background: #b0b0b0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
}

.theme-switch.dark:hover .theme-switch-thumb {
  background: var(--accent-hover);
  border-color: var(--accent-hover);
}

/* Responsive */
@media (max-width: 640px) {
  .profile-page {
    padding: 16px;
  }

  .profile-content {
    padding: 24px 16px;
  }

  .page-title {
    font-size: 20px;
  }

  .avatar-wrapper {
    width: 100px;
    height: 100px;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .info-label {
    width: 100%;
  }
}

/* 暗色模式下输入框样式覆盖 - 使用:deep() 穿透 scoped */
.profile-page:deep(.el-input__wrapper) {
  background-color: var(--bg-elevated, #ffffff);
}

[data-theme='dark'] .profile-page:deep(.el-input__wrapper) {
  background-color: #1a1a1a !important;
  box-shadow: 0 0 0 1px #2a2a2a inset !important;
}

[data-theme='dark'] .profile-page:deep(.el-input__inner) {
  color: #f5f5f5 !important;
}

[data-theme='dark'] .profile-page:deep(.el-textarea__inner) {
  background-color: #1a1a1a !important;
  color: #f5f5f5 !important;
}

[data-theme='dark'] .profile-page:deep(.el-select .el-input__wrapper) {
  background-color: #1a1a1a !important;
}

[data-theme='dark'] .profile-page:deep(.el-date-picker .el-input__wrapper) {
  background-color: #1a1a1a !important;
}

[data-theme='dark'] .profile-page:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #6b9bc3 inset !important;
}

[data-theme='dark'] .profile-page:deep(.el-select-dropdown) {
  background-color: #1a1a1a !important;
  border-color: #2a2a2a !important;
}

[data-theme='dark'] .profile-page:deep(.el-select-dropdown__item) {
  color: #f5f5f5 !important;
  background-color: #1a1a1a !important;
}

[data-theme='dark'] .profile-page:deep(.el-select-dropdown__item:hover) {
  background-color: #2a2a2a !important;
}

[data-theme='dark'] .profile-page:deep(.el-select-dropdown__item.selected) {
  color: #6b9bc3 !important;
}

[data-theme='dark'] .profile-page:deep(.el-date-picker__header) {
  color: #f5f5f5 !important;
}

[data-theme='dark'] .profile-page:deep(.el-date-picker__header-label) {
  color: #f5f5f5 !important;
}

[data-theme='dark'] .profile-page:deep(.el-date-picker-table td) {
  color: #f5f5f5 !important;
}

[data-theme='dark'] .profile-page:deep(.el-date-picker-table td.selected) {
  background-color: #6b9bc3 !important;
}

[data-theme='dark'] .profile-page:deep(.el-date-picker-table td.today) {
  color: #6b9bc3 !important;
}

[data-theme='dark'] .profile-page:deep(.el-date-picker__time-header) {
  color: #f5f5f5 !important;
  border-color: #2a2a2a !important;
}

[data-theme='dark'] .profile-page:deep(.popper__arrow) {
  background: #1a1a1a !important;
}

/* 性别选择框暗色模式 - 使用类名直接覆盖 */
.profile-select :deep(.el-input__wrapper) {
  background-color: var(--bg-elevated, #ffffff);
}

[data-theme='dark'] .profile-select :deep(.el-input__wrapper) {
  background-color: #1a1a1a !important;
  box-shadow: 0 0 0 1px #2a2a2a inset !important;
}

[data-theme='dark'] .profile-select :deep(.el-input__inner) {
  color: #f5f5f5 !important;
}
</style>
