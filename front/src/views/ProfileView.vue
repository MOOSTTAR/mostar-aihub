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

// 正在编辑的字段名
const editingField = ref<string | null>(null)

// 编辑中的临时值
const editValue = ref('')

// 头像 URL（使用 DiceBear 生成默认头像）
const avatarUrl = computed(() => {
  const seed = authStore.userInfo?.userId || 'default'
  return `https://api.dicebear.com/9.x/avataaars/svg?seed=${seed}`
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
    if (result.success && result.data) {
      userInfo.value = {
        username: result.data.username || '',
        phonenum: result.data.phonenum || '',
        email: result.data.email || '',
        bio: result.data.bio || '',
        birthday: result.data.birthday ? result.data.birthday.toString() : null,
        gender: result.data.gender ?? 0,
        githubUrl: result.data.githubUrl || ''
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 开始编辑字段
const startEdit = (field: keyof typeof userInfo.value, currentValue: string | number) => {
  editingField.value = field
  editValue.value = String(currentValue || '')
}

// 取消编辑
const cancelEdit = () => {
  editingField.value = null
  editValue.value = ''
}

// 保存编辑 - 单个字段更新
const saveEdit = async (field: keyof typeof userInfo.value) => {
  loading.value = true
  try {
    // 准备提交的数据
    const submitData: Record<string, any> = {}

    if (field === 'birthday') {
      submitData[field] = editValue.value || null
    } else if (field === 'gender') {
      submitData[field] = parseInt(editValue.value) || 0
    } else {
      submitData[field] = editValue.value
    }

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
      if (field === 'birthday') {
        userInfo.value[field] = editValue.value || null
      } else if (field === 'gender') {
        userInfo.value[field] = parseInt(editValue.value) || 0
      } else {
        userInfo.value[field] = editValue.value
      }
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    loading.value = false
    editingField.value = null
    editValue.value = ''
  }
}

// 处理回车保存
const handleEnter = (field: keyof typeof userInfo.value) => {
  saveEdit(field)
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
  return String(value) || '未设置'
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
  { key: 'phonenum', label: '手机号', editable: true, type: 'text', placeholder: '请输入手机号' },
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

onMounted(() => {
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
              'editable': field.editable,
              'editing': editingField === field.key
            }"
            @mouseenter="field.editable && startEdit(field.key, userInfo[field.key] || 0)"
            @mouseleave="editingField !== field.key && cancelEdit()"
          >
            <div class="info-label">{{ field.label }}</div>

            <!-- 展示模式 -->
            <div v-if="editingField !== field.key" class="info-value">
              <span :class="{ 'placeholder': !userInfo[field.key] && field.key !== 'gender' }">
                {{ getDisplayValue(field.key) }}
              </span>
              <span v-if="field.editable" class="edit-hint">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
              </span>
            </div>

            <!-- 编辑模式 -->
            <div v-else class="info-edit">
              <!-- 文本输入 -->
              <el-input
                v-if="field.type === 'text' || !field.type"
                v-model="editValue"
                size="small"
                :placeholder="field.placeholder"
                @keyup.enter="handleEnter(field.key)"
                ref="editInput"
              />
              <!-- 日期选择 -->
              <el-date-picker
                v-else-if="field.type === 'date'"
                v-model="editValue"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                size="small"
                style="width: 100%"
              />
              <!-- 下拉选择 -->
              <el-select
                v-else-if="field.type === 'select'"
                v-model="editValue"
                size="small"
                style="width: 100%"
              >
                <el-option
                  v-for="opt in field.options"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>

              <div class="edit-actions">
                <button class="edit-btn save" @click="saveEdit(field.key)" :loading="loading">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </button>
                <button class="edit-btn cancel" @click="cancelEdit">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
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
  max-width: 700px;
  margin: 0 auto;
}

/* Header */
.profile-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
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
}

.info-item {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.info-item.editable:hover {
  background: var(--bg-subtle, #f5f3ef);
}

.info-item.editing {
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
  justify-content: space-between;
  font-size: 15px;
  color: var(--text-primary, #1a1a1a);
}

.info-value .placeholder {
  color: var(--text-tertiary, #a3a3a3);
  font-style: italic;
}

.edit-hint {
  opacity: 0;
  color: var(--accent, #4a7c9b);
  transition: opacity 0.2s ease;
}

.info-item.editable:hover .edit-hint {
  opacity: 1;
}

.info-edit {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.edit-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.edit-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.edit-btn.save {
  background: var(--accent, #4a7c9b);
  color: white;
}

.edit-btn.save:hover {
  background: #3d6882;
}

.edit-btn.cancel {
  background: transparent;
  color: var(--text-secondary, #6b6b6b);
  border: 1px solid var(--border, #e0e0e0);
}

.edit-btn.cancel:hover {
  background: var(--bg-elevated, #ffffff);
  color: var(--text-primary, #1a1a1a);
}

/* Dark Mode */
[data-theme="dark"] .profile-page {
  --bg-canvas: #0f0f0f;
  --bg-elevated: #1a1a1a;
  --bg-subtle: #2a2a2a;
  --text-primary: #f5f5f5;
  --text-secondary: #a0a0a0;
  --text-tertiary: #6b6b6b;
  --border: #2a2a2a;
  --accent: #6b9bc3;
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
</style>
