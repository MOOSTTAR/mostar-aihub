<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
}

/* Custom Confirm Dialog Style - 强优先级 */
body .el-message-box__wrapper {
  backdrop-filter: blur(4px) !important;
}

/* 亮色模式弹窗背景 */
body .el-message-box {
  background-color: #FFFFFF !important;
  background: #FFFFFF !important;
  border: 1px solid #E8E6E1 !important;
  border-radius: 12px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12) !important;
}

/* 暗色模式弹窗背景 */
[data-theme="dark"] body .el-message-box {
  background-color: #1A1A1A !important;
  background: #1A1A1A !important;
  border: 1px solid #2A2A2A !important;
}

body .el-message-box__header {
  padding: 20px 24px 12px !important;
}

body .el-message-box__title {
  font-size: 16px !important;
  font-weight: 600 !important;
  color: #1A1A1A !important;
}

[data-theme="dark"] body .el-message-box__title {
  color: #F5F5F5 !important;
}

body .el-message-box__content {
  padding: 12px 24px 24px !important;
}

body .el-message-box__message {
  font-size: 14px !important;
  color: #6B6B6B !important;
  line-height: 1.6 !important;
}

[data-theme="dark"] body .el-message-box__message {
  color: #A0A0A0 !important;
}

body .el-message-box__btns {
  padding: 0 24px 20px !important;
  display: flex;
  gap: 12px;
  justify-content: flex-end !important;
}

body .el-message-box__cancel-btn {
  padding: 8px 20px !important;
  font-size: 14px !important;
  color: #6B6B6B !important;
  background: transparent !important;
  border: 1px solid #E8E6E1 !important;
  border-radius: 8px !important;
  transition: all 0.2s ease !important;
}

[data-theme="dark"] body .el-message-box__cancel-btn {
  color: #A0A0A0 !important;
  border: 1px solid #2A2A2A !important;
}

body .el-message-box__cancel-btn:hover {
  background: #F5F3EF !important;
  border-color: #E8E6E1 !important;
}

[data-theme="dark"] body .el-message-box__cancel-btn:hover {
  background: #141414 !important;
  border-color: #2A2A2A !important;
}

body .el-message-box__confirm-btn {
  padding: 8px 20px !important;
  font-size: 14px !important;
  color: #ef4444 !important;
  background: transparent !important;
  border: 1px solid #ef4444 !important;
  border-radius: 8px !important;
  transition: all 0.2s ease !important;
}

body .el-message-box__confirm-btn:hover {
  background: rgba(239, 68, 68, 0.08) !important;
  transform: translateY(-1px) !important;
}

/* 自定义按钮类名 - 更强优先级 */
body .custom-confirm-btn {
  background-color: transparent !important;
  border-color: #ef4444 !important;
  color: #ef4444 !important;
}

body .custom-confirm-btn:hover {
  background-color: rgba(239, 68, 68, 0.08) !important;
  border-color: #ef4444 !important;
  color: #ef4444 !important;
}

body .custom-cancel-btn {
  background-color: transparent !important;
  border-color: #E8E6E1 !important;
  color: #6B6B6B !important;
}

[data-theme="dark"] body .custom-cancel-btn {
  background-color: transparent !important;
  border-color: #2A2A2A !important;
  color: #A0A0A0 !important;
}

body .custom-cancel-btn:hover {
  background-color: #F5F3EF !important;
  border-color: #E8E6E1 !important;
}

[data-theme="dark"] body .custom-cancel-btn:hover {
  background-color: #141414 !important;
  border-color: #2A2A2A !important;
}
</style>
