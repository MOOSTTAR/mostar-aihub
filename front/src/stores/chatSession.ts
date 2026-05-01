import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../utils/request'

export interface ChatSession {
  memoryId: string
  title: string
  createTime: number
}

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
}

export const useChatSessionStore = defineStore('chatSession', () => {
  // State
  const sessions = ref<ChatSession[]>([])
  const loading = ref(false)

  // Actions
  const fetchSessions = async () => {
    loading.value = true
    try {
      const response = await request.get<ChatSession[]>('/chat/sessions')
      sessions.value = response.data
    } catch (error) {
      console.error('[ChatSessionStore] 获取会话列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  const deleteSession = async (memoryId: string) => {
    try {
      await request.delete(`/chat/sessions/${memoryId}`)
      sessions.value = sessions.value.filter(s => s.memoryId !== memoryId)
      return true
    } catch (error) {
      console.error('删除会话失败:', error)
      return false
    }
  }

  const deleteSessions = async (memoryIds: string[]) => {
    try {
      await request.post('/chat/sessions/batch-delete', memoryIds)
      sessions.value = sessions.value.filter(s => !memoryIds.includes(s.memoryId))
      return true
    } catch (error) {
      console.error('批量删除失败:', error)
      return false
    }
  }

  const addSession = (memoryId: string, title: string) => {
    sessions.value.unshift({
      memoryId,
      title,
      createTime: Date.now()
    })
  }

  const loadHistory = async (memoryId: string): Promise<ChatMessage[]> => {
    try {
      const response = await request.get<ChatMessage[]>(`/chat/history/${memoryId}`)
      return response.data
    } catch (error) {
      console.error('加载历史消息失败:', error)
      return []
    }
  }

  return {
    sessions,
    loading,
    fetchSessions,
    deleteSession,
    deleteSessions,
    addSession,
    loadHistory
  }
})
