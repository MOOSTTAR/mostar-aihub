<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = ref({
  username: '',
  phonenum: '',
  email: '',
  bio: '来点酷酷的签名~',
  birthday: '' as string | Date | null,
  gender: 0,
  githubUrl: ''
})

// 头像 URL（使用 DiceBear 生成默认头像）
const avatarUrl = computed(() => {
  const seed = authStore.userInfo?.userId || 'default'
  return `https://api.dicebear.com/9.x/avataaars/svg?seed=${seed}`
})

// 加载状态
const loading = ref(false)

// 验证规则
const rules = computed<FormRules>(() => ({
  phonenum: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  bio: [
    { max: 200, message: '个性签名不能超过 200 个字符', trigger: 'blur' }
  ],
  githubUrl: [
    { type: 'url', message: '请输入正确的 URL 格式', trigger: 'blur' }
  ]
}))

// 禁用未来日期
const disableFutureDate = (date: Date) => date.getTime() > Date.now()

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
      formData.value = {
        username: result.data.username || '',
        phonenum: result.data.phonenum || '',
        email: result.data.email || '',
        bio: result.data.bio || '',
        birthday: result.data.birthday ? new Date(result.data.birthday) : null,
        gender: result.data.gender ?? 0,
        githubUrl: result.data.githubUrl || ''
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const response = await fetch('/api/user/info', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          phonenum: formData.value.phonenum,
          email: formData.value.email,
          bio: formData.value.bio,
          birthday: formData.value.birthday ? new Date(formData.value.birthday).toISOString().split('T')[0] : null,
          gender: formData.value.gender,
          githubUrl: formData.value.githubUrl
        })
      })
      const result = await response.json()
      if (result.success) {
        ElMessage.success('保存成功')
        router.push('/')
      } else {
        ElMessage.error(result.message || '保存失败')
      }
    } catch (error) {
      console.error('保存失败:', error)
      ElMessage.error('保存失败，请重试')
    } finally {
      loading.value = false
    }
  })
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
        <h1 class="page-title">修改资料</h1>
      </header>

      <!-- 表单内容 -->
      <div class="profile-content">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="100px"
          label-position="top"
          class="profile-form"
        >
          <!-- 头像 -->
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

          <!-- 手机号 -->
          <el-form-item label="手机号" prop="phonenum">
            <el-input v-model="formData.phonenum" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>

          <!-- 邮箱 -->
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" />
          </el-form-item>

          <!-- 个性签名 -->
          <el-form-item label="个性签名" prop="bio">
            <el-input
              v-model="formData.bio"
              type="textarea"
              :rows="3"
              placeholder="来点酷酷的签名~"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <!-- 生日 -->
          <el-form-item label="生日" prop="birthday">
            <el-date-picker
              v-model="formData.birthday"
              type="date"
              placeholder="选择生日"
              :disabled-date="disableFutureDate"
            />
          </el-form-item>

          <!-- 性别 -->
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="formData.gender">
              <el-radio :value="0">未知</el-radio>
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- GitHub 链接 -->
          <el-form-item label="GitHub" prop="githubUrl">
            <el-input v-model="formData.githubUrl" placeholder="https://github.com/yourname" />
          </el-form-item>

          <!-- 提交按钮 -->
          <div class="form-actions">
            <el-button @click="goBack">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="loading">
              保存
            </el-button>
          </div>
        </el-form>
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
  max-width: 600px;
  margin: 0 auto;
}

/* Header */
.profile-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
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
  margin-bottom: 32px;
  text-align: center;
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

/* Form */
.profile-form {
  max-width: 100%;
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.profile-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary, #1a1a1a);
  margin-bottom: 8px;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: var(--bg-canvas, #f5f5f5);
}

.profile-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  background: var(--bg-canvas, #f5f5f5);
}

.profile-form :deep(.el-radio-group) {
  display: flex;
  gap: 16px;
}

/* Actions */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border, #e8e6e1);
}

/* Dark Mode */
[data-theme="dark"] .profile-page {
  --bg-canvas: #0f0f0f;
  --bg-elevated: #1a1a1a;
  --bg-subtle: #141414;
  --text-primary: #f5f5f5;
  --text-secondary: #a0a0a0;
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

  .profile-form :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}
</style>
