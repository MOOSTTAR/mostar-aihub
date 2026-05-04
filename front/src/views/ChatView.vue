<script setup lang="ts">
import { useChatSessionStore, type ChatMessage, type ChatSession } from '../stores/chatSession'
import DOMPurify from 'dompurify'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import defaultAvatar from '../assets/1728101288756.jpg'

// 头像 URL
const avatarUrl = computed(() => {
  return defaultAvatar
})

// MathJax 类型声明
declare global {
  interface Window {
    MathJax?: {
      tex?: {
        inlineMath?: string[][]
        displayMath?: string[][]
        processEscapes?: boolean
        packages?: string[]
      }
      svg?: {
        fontCache?: string
      }
      startup?: {
        pageReady?: () => Promise<void>
        defaultPageReady?: () => Promise<void>
      }
      typesetPromise?: (elements?: HTMLElement[]) => Promise<void>
    }
    __codeBlocks?: Record<string, string>
  }
}

// Taste Skill: Soft UI + Minimalist Fusion
// Parameters: DESIGN_VARIANCE=5 | MOTION_INTENSITY=7 | VISUAL_DENSITY=3

// 动态加载 MathJax
const loadMathJax = () => {
  if (window.MathJax) return Promise.resolve()

  return new Promise<void>((resolve) => {
    const script = document.createElement('script')
    script.src = 'https://cdn.jsdelivr.net/npm/mathjax@3.2.2/es5/tex-mml-chtml.min.js'
    script.async = true
    window.MathJax = {
      tex: {
        inlineMath: [
          ['$', '$'],
          ['\\(', '\\)'],
        ],
        displayMath: [
          ['$$', '$$'],
          ['\\[', '\\]'],
        ],
        processEscapes: true,
        packages: ['base', 'ams', 'noerrors', 'noundefined', 'mhchem'],
      },
      svg: { fontCache: 'global' },
      startup: {
        pageReady: () => {
          resolve()
          return window.MathJax!.startup!.defaultPageReady!()
        },
      },
    }
    document.head.appendChild(script)
  })
}

const renderMathJax = async () => {
  await loadMathJax()
  const MathJax = window.MathJax
  if (MathJax && MathJax.typesetPromise) {
    await MathJax.typesetPromise()
  }
}

interface Message {
  role: 'user' | 'assistant'
  content: string
  id: string
}

// 复制代码 - 通过事件委托处理
const copyCode = async (codeId: string, buttonEl: HTMLElement) => {
  const code = window.__codeBlocks?.[codeId]
  if (!code) return

  try {
    await navigator.clipboard.writeText(code)
    const originalHTML = buttonEl.innerHTML
    buttonEl.innerHTML = `
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="20 6 9 17 4 12"/>
      </svg>
      <span class="copy-text">已复制!</span>
    `
    buttonEl.classList.add('copied')
    setTimeout(() => {
      buttonEl.innerHTML = originalHTML
      buttonEl.classList.remove('copied')
    }, 2000)
  } catch (err) {
    console.error('复制失败:', err)
  }
}

// 在消息容器上委托处理复制按钮点击
const handleCopyButtonClick = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  const copyBtn = target.closest('.copy-code-btn') as HTMLElement
  if (copyBtn) {
    const codeId = copyBtn.getAttribute('data-code-id')
    if (codeId) {
      copyCode(codeId, copyBtn)
    }
  }
}

// 自动检测代码语言
const detectLanguage = (code: string): string => {
  const lines = code.split('\n').filter((line) => line.trim())
  const firstLines = lines.slice(0, 5).join('\n')
  const allCode = code

  // Java
  if (
    /\b(public|private|protected)\s+class\b/.test(allCode) ||
    /\bimport\s+java\./.test(firstLines) ||
    /\bpublic\s+static\s+void\s+main\b/.test(allCode) ||
    /\bMap\.Entry\b/.test(allCode) ||
    /\bentrySet\(\)\b/.test(allCode) ||
    /\bkeySet\(\)\b/.test(allCode) ||
    /\bSystem\.out\.println\b/.test(allCode)
  ) {
    return 'Java'
  }

  // Python
  if (
    /^import\s+\w+/m.test(firstLines) ||
    /^from\s+\w+\s+import/.test(firstLines) ||
    /^def\s+\w+\s*\(/m.test(allCode) ||
    /^class\s+\w+\s*\(/m.test(allCode) ||
    /^print\s*\(/.test(firstLines)
  ) {
    return 'Python'
  }

  // JavaScript
  if (
    /\bconst\b/.test(allCode) ||
    /\blet\b/.test(allCode) ||
    /\bfunction\s*\(/.test(allCode) ||
    /\b=>\s*{/.test(allCode) ||
    /\bconsole\.log\b/.test(allCode) ||
    /\bdocument\./.test(allCode) ||
    /\brequire\(/.test(allCode)
  ) {
    return 'JavaScript'
  }

  // TypeScript
  if (
    /\binterface\s+\w+/.test(allCode) ||
    /\btype\s+\w+\s*=/.test(allCode) ||
    /:\s*(string|number|boolean|any)\b/.test(allCode) ||
    /\bas\s+\w+/.test(allCode)
  ) {
    return 'TypeScript'
  }

  // HTML
  if (
    /^<!DOCTYPE html>/i.test(firstLines) ||
    /^<html/i.test(firstLines) ||
    /^<div/i.test(firstLines) ||
    /^<head>/i.test(firstLines)
  ) {
    return 'HTML'
  }

  // CSS
  if (/{\s*\w+\s*:/.test(allCode) && /[\.#]\w+\s*{/.test(allCode)) {
    return 'CSS'
  }

  // SQL
  if (
    /^SELECT\s+/i.test(firstLines) ||
    /^INSERT\s+INTO/i.test(firstLines) ||
    /^UPDATE\s+/i.test(firstLines) ||
    /^DELETE\s+FROM/i.test(firstLines) ||
    /^CREATE\s+TABLE/i.test(firstLines)
  ) {
    return 'SQL'
  }

  // Redis 命令
  if (
    /^(SET|GET|DEL|INCR|DECR|LPUSH|RPUSH|LPOP|RPOP|BLPOP|BRPOP|SADD|SMEMBERS|ZADD|ZINCRBY|ZREVRANGE|ZREVRANK|EXPIRE|TTL|HSET|HGET|HGETALL|NX|EX|SETNX)\s+/i.test(
      firstLines,
    ) ||
    /^HMSET\s+/i.test(firstLines) ||
    /^ZCARD\s+/i.test(firstLines) ||
    /^SCAN\s+/i.test(firstLines)
  ) {
    return 'Redis'
  }

  // Shell/Bash
  if (
    /^#!/.test(firstLines) ||
    /^\$\s*/.test(firstLines) ||
    /^echo\s+/m.test(allCode) ||
    /^cd\s+/m.test(allCode) ||
    /^\w+=/.test(firstLines)
  ) {
    return 'Shell'
  }

  // JSON
  if (/^{\s*"/.test(allCode.trim()) || /^\[\s*{/.test(allCode.trim())) {
    return 'JSON'
  }

  // XML
  if (/^<\?xml/.test(firstLines) || /^<\w+:\w+/.test(firstLines)) {
    return 'XML'
  }

  // Markdown
  if (/^#\s+/m.test(allCode) || /^##\s+/m.test(allCode) || /^\*\*.*\*\*/.test(allCode)) {
    return 'Markdown'
  }

  // Go
  if (
    /^package\s+\w+/m.test(firstLines) ||
    /\bfunc\s+\w+\(/.test(allCode) ||
    /\b:=\s*/.test(allCode)
  ) {
    return 'Go'
  }

  // Rust
  if (
    /^fn\s+\w+\s*\(/m.test(allCode) && /\blet\s+mut\b/.test(allCode) ||
    /->\s*impl\s+\w+/.test(allCode) ||
    /\bmatch\s+\w+\s*{/.test(allCode) ||
    /\bOption<|Result</.test(allCode)
  ) {
    return 'Rust'
  }

  // C#
  if (
    /^using\s+\w+;/m.test(firstLines) ||
    /\bnamespace\s+\w+/.test(allCode) ||
    /\bConsole\.WriteLine\b/.test(allCode)
  ) {
    return 'C#'
  }

  // C/C++
  if (
    /#include\s*</.test(firstLines) ||
    /\bint\s+main\s*\(/.test(allCode) ||
    /\bstd::/.test(allCode)
  ) {
    return 'C++'
  }

  // PHP
  if (/^<\?php/.test(firstLines) || /^\$/.test(firstLines) || /\becho\s+\$/.test(allCode)) {
    return 'PHP'
  }

  // Ruby
  if (
    /^require\s+/.test(firstLines) ||
    /^def\s+\w+/.test(firstLines) ||
    /^puts\s+/.test(firstLines) ||
    (lines.length > 0 && /^end$/.test(lines[lines.length - 1]!))
  ) {
    return 'Ruby'
  }

  return 'Code'
}

const renderContent = (content: string) => {
  if (!content) return ''

  // 直接使用 AI 返回的 HTML，只用 DOMPurify 过滤 XSS
  const sanitized = DOMPurify.sanitize(content, {
    ALLOWED_TAGS: [
      'p',
      'br',
      'strong',
      'em',
      'i',
      'code',
      'pre',
      'ul',
      'ol',
      'li',
      'h1',
      'h2',
      'h3',
      'h4',
      'h5',
      'h6',
      'blockquote',
      'a',
      'img',
      'table',
      'thead',
      'tbody',
      'tr',
      'th',
      'td',
      'div',
      'span',
      'hr',
      'button',
    ],
    ALLOWED_ATTR: [
      'href',
      'target',
      'rel',
      'src',
      'alt',
      'title',
      'class',
      'style',
      'border',
      'cellspacing',
      'cellpadding',
      'data-lang',
      'data-code-id',
    ],
    FORBID_TAGS: ['script', 'iframe', 'object', 'embed', 'form', 'input', 'textarea'],
    FORBID_ATTR: ['onerror', 'onclick', 'onload', 'onmouseover', 'onfocus', 'onblur'],
  })

  // 为代码块添加复制按钮和语言标签
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = sanitized
  const pres = tempDiv.querySelectorAll('pre')
  pres.forEach((pre, index) => {
    // 优先使用 data-lang 属性，否则自动检测
    let lang: string = pre.getAttribute('data-lang') || ''
    const code = pre.querySelector('code')?.textContent || ''

    if (!lang) {
      lang = detectLanguage(code)
    }

    const codeId = `code-${Date.now()}-${index}`
    window.__codeBlocks = window.__codeBlocks || {}
    window.__codeBlocks[codeId] = code

    // 添加复制按钮
    const button = document.createElement('button')
    button.className = 'copy-code-btn'
    button.setAttribute('data-code-id', codeId)
    button.innerHTML = `
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
        <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
      </svg>
      <span class="copy-text">复制</span>
    `
    ;(pre as HTMLElement).style.position = 'relative'
    pre.appendChild(button)

    // 添加语言标签
    const langBadge = document.createElement('span')
    langBadge.className = 'code-lang-badge'
    langBadge.textContent = lang
    pre.insertBefore(langBadge, button)
  })

  return tempDiv.innerHTML
}

const messages = ref<Message[]>([])
const inputMessage = ref('')
const loading = ref(false)
const isSwitchingSession = ref(false)
const thinkingSeconds = ref(0)
const thinkingTimer = ref<number | null>(null)
const chatContainer = ref<HTMLDivElement>()
const isTyping = ref(false)
const sidebarCollapsed = ref(false)

// 跟踪正在进行的 AI 响应所属的会话
let streamingMemoryId: string | null = null

// 批量删除相关
const isBatchDeleteMode = ref(false)
const selectedSessions = ref<Set<string>>(new Set())

// 生成唯一ID
const generateId = () => Math.random().toString(36).substring(2, 9)

// 路由
const router = useRouter()
const authStore = useAuthStore()
const sessionStore = useChatSessionStore()

// 返回大厅
const goBack = () => {
  // AI 回复期间禁止返回
  if (streamingMemoryId !== null) {
    ElMessage.warning('AI 正在回复中，请稍后再返回大厅')
    return
  }
  router.push('/home')
}

// 会话 Memory ID - 每次新建对话时生成，同一会话保持不变
const sessionMemoryId = ref(Date.now().toString())

// 新建对话 - 清空消息并生成新的 memoryId
const clearChat = () => {
  // AI 回复期间禁止新建对话
  if (streamingMemoryId !== null) {
    ElMessage.warning('AI 正在回复中，请稍后再新建对话')
    return
  }
  messages.value = []
  sessionMemoryId.value = Date.now().toString()
}

// 切换到指定会话
const switchSession = async (memoryId: string) => {
  // AI 回复期间禁止切换
  if (streamingMemoryId !== null) {
    ElMessage.warning('AI 正在回复中，请稍后再切换')
    return
  }

  if (memoryId === sessionMemoryId.value) return

  // 先设置加载状态，防止界面闪烁
  isSwitchingSession.value = true
  sessionMemoryId.value = memoryId
  messages.value = []

  // 加载历史消息
  const history = await sessionStore.loadHistory(memoryId)
  messages.value = history.map((msg: ChatMessage, index: number) => ({
    role: msg.role === 'user' ? 'user' : ('assistant' as 'user' | 'assistant'),
    content: msg.content,
    id: generateId() + index,
  }))

  isSwitchingSession.value = false
  await scrollToBottom()
}

// 删除会话
const handleDeleteSession = async (memoryId: string, event: Event) => {
  event.stopPropagation()
  try {
    await ElMessageBox.confirm('确定要删除这个会话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      confirmButtonClass: 'custom-confirm-btn',
      cancelButtonClass: 'custom-cancel-btn',
    })
    const success = await sessionStore.deleteSession(memoryId)
    if (success) {
      ElMessage.success('删除成功')
      // 如果删除的是当前会话，新建一个
      if (memoryId === sessionMemoryId.value) {
        clearChat()
      }
    }
  } catch {
    // 用户取消
  }
}

// 批量删除相关
const toggleBatchDeleteMode = () => {
  isBatchDeleteMode.value = !isBatchDeleteMode.value
  selectedSessions.value.clear()
}

const toggleSessionSelection = (memoryId: string, event: Event) => {
  event.stopPropagation()
  if (selectedSessions.value.has(memoryId)) {
    selectedSessions.value.delete(memoryId)
  } else {
    selectedSessions.value.add(memoryId)
  }
}

// 全选 / 取消全选
const toggleSelectAll = () => {
  if (selectedSessions.value.size === sessionStore.sessions.length) {
    // 取消全选
    selectedSessions.value.clear()
  } else {
    // 全选
    sessionStore.sessions.forEach((session: { memoryId: string }) => {
      selectedSessions.value.add(session.memoryId)
    })
  }
}

// 是否全选
const isAllSelected = computed(() => {
  return (
    sessionStore.sessions.length > 0 && selectedSessions.value.size === sessionStore.sessions.length
  )
})

const batchDeleteSessions = async () => {
  if (selectedSessions.value.size === 0) {
    ElMessage.warning('请选择要删除的会话')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedSessions.value.size} 个会话吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        confirmButtonClass: 'custom-confirm-btn',
        cancelButtonClass: 'custom-cancel-btn',
      },
    )

    const success = await sessionStore.deleteSessions(Array.from(selectedSessions.value))
    if (success) {
      ElMessage.success('批量删除成功')
      // 如果当前会话被删除了，新建一个
      if (selectedSessions.value.has(sessionMemoryId.value)) {
        clearChat()
      }
      isBatchDeleteMode.value = false
      selectedSessions.value.clear()
    }
  } catch {
    // 用户取消
  }
}

const cancelBatchDelete = () => {
  isBatchDeleteMode.value = false
  selectedSessions.value.clear()
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTo({
      top: chatContainer.value.scrollHeight,
      behavior: 'smooth',
    })
  }
  await renderMathJax()
}

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || loading.value) return

  // 检查是否是新会话（不在侧边栏列表中）
  const existingSession = sessionStore.sessions.find(
    (s: { memoryId: string }) => s.memoryId === sessionMemoryId.value,
  )
  if (!existingSession) {
    // 立即在侧边栏添加新会话
    sessionStore.addSession(sessionMemoryId.value, message.substring(0, 20))
  }

  // 添加用户消息
  messages.value.push({ role: 'user', content: message, id: generateId() })
  inputMessage.value = ''

  // 标记当前会话正在接收 AI 响应（在请求之前设置，防止思考期间可以点击）
  const currentMemoryId = sessionMemoryId.value
  streamingMemoryId = currentMemoryId

  loading.value = true
  isTyping.value = true
  startThinkingTimer()
  await scrollToBottom()

  // 使用当前会话的 memoryId（同一会话保持不变）
  const aiMessageId = generateId()

  try {
    // 获取 token
    const token = localStorage.getItem('token')
    if (!token) {
      authStore.logout()
      router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
      return
    }

    // 使用 fetch 读取 SSE 流
    const response = await fetch(
      `/api/chat?message=${encodeURIComponent(message)}&memoryId=${currentMemoryId}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: 'text/event-stream',
        },
      },
    )

    // 处理 401 错误
    if (response.status === 401) {
      authStore.logout()
      router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
      return
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    if (!reader) throw new Error('无法获取响应')

    let fullContent = ''

    // 如果当前会话正在接收 AI 响应，更新已有消息；否则创建新消息
    const existingMsg = messages.value.find((m) => m.id === aiMessageId && m.role === 'assistant')
    if (!existingMsg) {
      messages.value.push({ role: 'assistant', content: '', id: aiMessageId })
    }

    // 读取 SSE 流并实时显示
    let buffer = ''
    let receivedDone = false
    let emptyReadCount = 0 // 记录空读取次数
    let hasReceivedFirstChunk = false // 是否已收到第一个响应

    while (true) {
      // 使用 Promise.race 实现超时控制
      const readPromise = reader.read()
      const timeoutPromise = new Promise<ReadableStreamReadResult<Uint8Array>>((resolve) => {
        setTimeout(() => {
          resolve({ done: true, value: undefined })
        }, 3000) // 3 秒超时
      })

      const { done, value } = await Promise.race([readPromise, timeoutPromise])

      if (done) {
        break
      }

      const chunk = decoder.decode(value, { stream: true })

      // 如果 chunk 为空，增加空读取计数
      if (!chunk || chunk.length === 0) {
        emptyReadCount++
        if (emptyReadCount >= 3) {
          break
        }
        continue
      }

      emptyReadCount = 0 // 重置空读取计数

      buffer += chunk

      // 处理 SSE 格式：每行 data:xxx
      const lines = buffer.split(/\r?\n/)
      buffer = lines.pop() || '' // 保留最后不完整的一行

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          if (data === '[DONE]') {
            receivedDone = true
            break
          }
          if (data) {
            fullContent += data

            // 收到第一个有效响应时，关闭 loading 状态
            if (!hasReceivedFirstChunk) {
              hasReceivedFirstChunk = true
              isTyping.value = false
            }
          }
        }
      }

      if (receivedDone) {
        break
      }

      // 实时更新消息内容（无论当前是否在该会话）
      const msg = messages.value.find((m) => m.id === aiMessageId)
      if (msg) {
        msg.content = fullContent
        // 只有在当前会话才滚动
        if (sessionMemoryId.value === currentMemoryId) {
          await scrollToBottom()
        }
      }
    }

    // 处理缓冲区中剩余的内容
    if (buffer.trim().startsWith('data:')) {
      const data = buffer.trim().slice(5).trim()
      if (data && data !== '[DONE]') {
        fullContent += data
      }
    }

    // SSE 完成，清除所有状态
    streamingMemoryId = null
    loading.value = false
    isTyping.value = false
    stopThinkingTimer()

    // 刷新会话列表（如果是新会话）
    await sessionStore.fetchSessions()

    // 只有在当前会话才渲染 MathJax
    if (sessionMemoryId.value === currentMemoryId) {
      await renderMathJax()
    }
  } catch (error) {
    console.error('聊天出错:', error)
    // 出错时清除所有状态
    streamingMemoryId = null
    loading.value = false
    isTyping.value = false
    stopThinkingTimer()

    const msg = messages.value.find((m) => m.id === aiMessageId)
    if (msg) msg.content = '抱歉，发生了错误，请稍后重试。'
    else
      messages.value.push({
        role: 'assistant',
        content: '抱歉，发生了错误，请稍后重试。',
        id: generateId(),
      })
  }
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleCommandOrSend()
  }
}

// 处理命令或发送消息
const handleCommandOrSend = async () => {
  const message = inputMessage.value.trim()
  if (!message || loading.value) return

  // 检查是否是/clear 命令
  if (message === '/clear') {
    inputMessage.value = ''
    await clearChatWithApi()
    return
  }

  await sendMessage()
}

// 清空对话
const clearChatWithApi = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      authStore.logout()
      router.push('/login')
      return
    }

    const response = await fetch(`/api/chat/clear?memoryId=${sessionMemoryId.value}`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: 'application/json',
      },
    })

    if (response.status === 401) {
      authStore.logout()
      router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
      return
    }

    if (response.ok) {
      const result = await response.json()
      if (result.code === 200) {
        // 不清空消息列表，保留历史对话记录
        // 添加一条 AI 友好提示消息
        messages.value.push({
          role: 'assistant',
          content: result.data || '已清空对话记忆，接下来我将没有之前的上下文记忆哦～',
          id: generateId()
        })
        // 刷新会话列表（标题可能已更新为"新对话"）
        await sessionStore.fetchSessions()
        ElMessage.success('已清空对话记忆')
        await scrollToBottom()
      } else {
        ElMessage.error(result.msg || '清空对话失败')
      }
    } else {
      ElMessage.error('清空对话失败')
    }
  } catch (error) {
    console.error('清空对话失败:', error)
    ElMessage.error('清空对话失败')
  }
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

// 启动思考计时器
const startThinkingTimer = () => {
  thinkingSeconds.value = 0
  if (thinkingTimer.value) {
    clearInterval(thinkingTimer.value)
  }
  thinkingTimer.value = window.setInterval(() => {
    thinkingSeconds.value++
  }, 1000)
}

// 停止思考计时器
const stopThinkingTimer = () => {
  if (thinkingTimer.value) {
    clearInterval(thinkingTimer.value)
    thinkingTimer.value = null
  }
  thinkingSeconds.value = 0 // 重置思考时间
}

// 输入框焦点动画
const isInputFocused = ref(false)
const showCommands = ref(false)

// 可用指令列表
const commands = [{ name: '/clear', description: '清空当前对话记录和 AI 记忆' }]

// 插入指令到输入框
const insertCommand = (cmd: string) => {
  inputMessage.value = cmd
  showCommands.value = false
}

// 三个点菜单相关状态
const menuVisible = ref<string | null>(null)  // 当前显示菜单的会话 memoryId
const menuPosition = ref<{ top: number, left: number } | null>(null)  // 菜单位置
const editingSessionId = ref<string | null>(null)  // 正在编辑的会话 ID
const editingTitle = ref("")  // 编辑中的标题

// 分组展开/收起状态
const expandedGroups = ref({
  pinned: true,
  today: true,
  week: true,
  other: true
})

// 切换分组展开/收起
const toggleGroup = (group: keyof typeof expandedGroups.value) => {
  expandedGroups.value[group] = !expandedGroups.value[group]
}

// 按时间分组 computed
const groupedSessions = computed(() => {
  const groups: {
    pinned: ChatSession[]
    today: ChatSession[]
    week: ChatSession[]
    other: ChatSession[]
  } = {
    pinned: [],
    today: [],
    week: [],
    other: []
  }

  const now = Date.now()
  const todayStart = new Date(new Date().setHours(0, 0, 0, 0)).getTime()
  const weekAgo = now - 7 * 24 * 60 * 60 * 1000

  sessionStore.sessions.forEach(session => {
    if (session.isPinned) {
      groups.pinned.push(session)
    } else if (session.createTime >= todayStart) {
      groups.today.push(session)
    } else if (session.createTime >= weekAgo) {
      groups.week.push(session)
    } else {
      groups.other.push(session)
    }
  })

  return groups
})

// 打开/关闭三个点菜单
const toggleMenu = (memoryId: string, event: Event) => {
  event.stopPropagation()
  if (menuVisible.value === memoryId) {
    menuVisible.value = null
    menuPosition.value = null
  } else {
    menuVisible.value = memoryId
    // 计算菜单位置 - 在按钮右侧显示
    const target = event.currentTarget as HTMLElement
    const rect = target.getBoundingClientRect()
    menuPosition.value = {
      top: rect.top,
      left: rect.right + 4
    }
  }
}

// 点击外部关闭菜单
const closeMenu = () => {
  menuVisible.value = null
  menuPosition.value = null
}

// 处理置顶/取消置顶
const handleTogglePin = async (session: ChatSession, event: Event) => {
  event.stopPropagation()
  const success = await sessionStore.togglePinSession(session.memoryId, !session.isPinned)
  if (success) {
    ElMessage.success(session.isPinned ? "已取消置顶" : "已置顶")
  }
  closeMenu()
}

// 处理重命名 - 进入编辑模式
const handleRename = (session: ChatSession, event: Event) => {
  event.stopPropagation()
  editingSessionId.value = session.memoryId
  editingTitle.value = session.title
  closeMenu()
}

// 确认重命名
const confirmRename = async () => {
  if (!editingSessionId.value) return

  const trimmedTitle = editingTitle.value.trim()
  if (!trimmedTitle) {
    ElMessage.warning("标题不能为空")
    return
  }

  const success = await sessionStore.renameSession(editingSessionId.value, trimmedTitle)
  if (success) {
    ElMessage.success("重命名成功")
  }
  editingSessionId.value = null
  editingTitle.value = ""
}

// 取消重命名
const cancelRename = () => {
  editingSessionId.value = null
  editingTitle.value = ""
}

// 按 Enter 确认重命名
const handleRenameKeydown = (e: KeyboardEvent) => {
  if (e.key === "Enter") {
    e.preventDefault()
    confirmRename()
  } else if (e.key === "Escape") {
    cancelRename()
  }
}

// 点击外部关闭指令菜单
const handleOutsideClick = (e: MouseEvent) => {
  const target = e.target as HTMLElement
  if (!target.closest('.input-wrapper') && showCommands.value) {
    showCommands.value = false
  }
}

// 点击外部关闭菜单
const handleDocumentClick = (e: MouseEvent) => {
  closeMenu()
  handleOutsideClick(e)
}

onMounted(async () => {
  initTheme()
  sessionStore.fetchSessions()
  document.addEventListener('click', handleDocumentClick)
  document.addEventListener('scroll', closeMenu, true)  // 滚动时关闭菜单
  bindCopyButtonEvent()
})

// 绑定复制按钮事件
const bindCopyButtonEvent = async () => {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.addEventListener('click', handleCopyButtonClick)
  }
}

watch(
  messages,
  () => {
    nextTick(() => {
      bindCopyButtonEvent()
    })
  },
  { deep: true },
)

// 监听会话切换，处理正在进行的流式响应
watch(sessionMemoryId, (newMemoryId, oldMemoryId) => {
  if (!oldMemoryId) return

  // 如果有正在进行的流式响应，切换回来时恢复显示
  if (streamingMemoryId === newMemoryId && loading.value) {
    // 消息已经在后台接收，不需要额外操作
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleDocumentClick)
  document.removeEventListener('scroll', closeMenu, true)
  chatContainer.value?.removeEventListener('click', handleCopyButtonClick)
})
</script>

<template>
  <div class="chat-layout">
    <!-- Sidebar - Session List -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <button
          v-if="!isBatchDeleteMode"
          class="new-chat-btn-sidebar"
          @click="clearChat"
          :disabled="streamingMemoryId !== null"
          :style="{
            opacity: streamingMemoryId !== null ? '0.5' : '1',
            cursor: streamingMemoryId !== null ? 'not-allowed' : 'pointer',
          }"
        >
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          <span>新对话</span>
        </button>
        <button
          v-if="!isBatchDeleteMode && sessionStore.sessions.length > 0"
          class="batch-delete-btn-sidebar"
          @click="toggleBatchDeleteMode"
          :disabled="streamingMemoryId !== null"
          :style="{
            opacity: streamingMemoryId !== null ? '0.5' : '1',
            cursor: streamingMemoryId !== null ? 'not-allowed' : 'pointer',
          }"
        >
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <polyline points="3 6 5 6 21 6" />
            <path
              d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"
            />
          </svg>
        </button>
        <button
          v-if="isBatchDeleteMode"
          class="batch-action-btn batch-select-all-btn"
          @click="toggleSelectAll"
          :title="isAllSelected ? '取消全选' : '全选'"
        >
          <svg
            v-if="isAllSelected"
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <rect x="3" y="3" width="18" height="18" rx="2" fill="var(--accent)" stroke="none" />
            <polyline points="9 12 11 14 15 10" stroke="white" stroke-width="2.5" />
          </svg>
          <svg
            v-else
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <rect x="3" y="3" width="18" height="18" rx="2" />
          </svg>
        </button>
        <button
          v-if="isBatchDeleteMode"
          class="batch-action-btn batch-confirm-btn"
          @click="batchDeleteSessions"
          :disabled="selectedSessions.size === 0"
        >
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <polyline points="20 6 9 17 4 12" />
          </svg>
          <span>一键删除</span>
        </button>
        <button
          v-if="isBatchDeleteMode"
          class="batch-action-btn batch-cancel-btn"
          @click="cancelBatchDelete"
        >
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
          <span>取消</span>
        </button>
      </div>

      <div class="sidebar-content">
        <div v-if="sessionStore.loading" class="loading-text">加载中...</div>
        <div v-else-if="sessionStore.sessions.length === 0" class="empty-sessions">
          暂无历史会话
        </div>
        <div v-else class="session-list">
          <!-- 置顶分组 -->
          <div class="session-group" v-if="groupedSessions.pinned.length > 0">
            <div class="group-header" @click="toggleGroup('pinned')">
              <span class="group-label">置顶</span>
              <span class="group-toggle" :class="{ collapsed: !expandedGroups.pinned }">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </span>
            </div>
            <div class="group-content" v-show="expandedGroups.pinned">
              <div
                v-for="session in groupedSessions.pinned"
                :key="session.memoryId"
                class="session-item"
                :class="{
                  active: session.memoryId === sessionMemoryId,
                  selected: selectedSessions.has(session.memoryId),
                  disabled: streamingMemoryId !== null,
                }"
                :style="{
                  'pointer-events':
                    streamingMemoryId !== null && session.memoryId !== sessionMemoryId
                      ? 'none'
                      : 'auto',
                  opacity:
                    streamingMemoryId !== null && session.memoryId !== sessionMemoryId ? '0.5' : '1',
                }"
                @click="switchSession(session.memoryId)"
              >
                <div
                  v-if="isBatchDeleteMode"
                  class="session-checkbox"
                  @click="toggleSessionSelection(session.memoryId, $event)"
                >
                  <svg
                    v-if="selectedSessions.has(session.memoryId)"
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="3" fill="var(--accent)" />
                    <polyline points="9 12 11 14 15 10" stroke="white" stroke-width="2.5" />
                  </svg>
                  <svg
                    v-else
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="3" />
                  </svg>
                </div>
                <div v-else class="session-icon">
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                  </svg>
                </div>
                <!-- 会话标题 - 支持编辑模式 -->
                <template v-if="editingSessionId === session.memoryId">
                  <input
                    v-model="editingTitle"
                    class="session-title-input"
                    @keydown="handleRenameKeydown"
                    @blur="confirmRename"
                    @click.stop
                    autofocus
                  />
                </template>
                <template v-else>
                  <div class="session-title">
                    <!-- 置顶图标 -->
                    <svg
                      v-if="session.isPinned"
                      class="pin-icon"
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="currentColor"
                      style="margin-right: 4px; flex-shrink: 0;"
                    >
                      <!-- 圆形钉帽 -->
                      <circle cx="12" cy="8" r="4"/>
                      <!-- 尖锐三角形钉针 -->
                      <path d="M12 12l-3 10h6L12 12z"/>
                    </svg>
                    {{ session.title }}
                  </div>
                </template>
                <!-- 三个点菜单按钮（非批量模式下显示） -->
                <div
                  v-if="!isBatchDeleteMode"
                  class="menu-btn"
                  @click="toggleMenu(session.memoryId, $event)"
                >
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                  >
                    <circle cx="5" cy="12" r="2"/>
                    <circle cx="12" cy="12" r="2"/>
                    <circle cx="19" cy="12" r="2"/>
                  </svg>
                </div>
                <!-- 三个点弹出菜单 -->
                <div
                  v-if="menuVisible === session.memoryId"
                  class="session-menu"
                  :style="menuPosition ? { top: menuPosition.top + 'px', left: menuPosition.left + 'px' } : {}"
                  @click.stop
                >
                  <div class="menu-item" @click="handleTogglePin(session, $event)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M16 4v6l2 4v2H6v-2l2-4V4h8zm-2 6V6h-4v4l-2 4h8l-2-4z"/>
                    </svg>
                    <span>{{ session.isPinned ? "取消置顶" : "置顶" }}</span>
                  </div>
                  <div class="menu-item" @click="handleRename(session, $event)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                    <span>重命名</span>
                  </div>
                  <div class="menu-item delete" @click="handleDeleteSession(session.memoryId, $event)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                    </svg>
                    <span>删除</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 今天分组 -->
          <div class="session-group" v-if="groupedSessions.today.length > 0">
            <div class="group-header" @click="toggleGroup('today')">
              <span class="group-label">今天</span>
              <span class="group-toggle" :class="{ collapsed: !expandedGroups.today }">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </span>
            </div>
            <div class="group-content" v-show="expandedGroups.today">
              <div
                v-for="session in groupedSessions.today"
                :key="session.memoryId"
                class="session-item"
                :class="{
                  active: session.memoryId === sessionMemoryId,
                  selected: selectedSessions.has(session.memoryId),
                  disabled: streamingMemoryId !== null,
                }"
                :style="{
                  'pointer-events':
                    streamingMemoryId !== null && session.memoryId !== sessionMemoryId
                      ? 'none'
                      : 'auto',
                  opacity:
                    streamingMemoryId !== null && session.memoryId !== sessionMemoryId ? '0.5' : '1',
                }"
                @click="switchSession(session.memoryId)"
              >
                <div
                  v-if="isBatchDeleteMode"
                  class="session-checkbox"
                  @click="toggleSessionSelection(session.memoryId, $event)"
                >
                  <svg
                    v-if="selectedSessions.has(session.memoryId)"
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="3" fill="var(--accent)" />
                    <polyline points="9 12 11 14 15 10" stroke="white" stroke-width="2.5" />
                  </svg>
                  <svg
                    v-else
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="3" />
                  </svg>
                </div>
                <div v-else class="session-icon">
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                  </svg>
                </div>
                <!-- 会话标题 - 支持编辑模式 -->
                <template v-if="editingSessionId === session.memoryId">
                  <input
                    v-model="editingTitle"
                    class="session-title-input"
                    @keydown="handleRenameKeydown"
                    @blur="confirmRename"
                    @click.stop
                    autofocus
                  />
                </template>
                <template v-else>
                  <div class="session-title">
                    <svg
                      v-if="session.isPinned"
                      class="pin-icon"
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="currentColor"
                      style="margin-right: 4px; flex-shrink: 0;"
                    >
                      <circle cx="12" cy="8" r="4"/>
                      <path d="M12 12l-3 10h6L12 12z"/>
                    </svg>
                    {{ session.title }}
                  </div>
                </template>
                <!-- 三个点菜单按钮 -->
                <div
                  v-if="!isBatchDeleteMode"
                  class="menu-btn"
                  @click="toggleMenu(session.memoryId, $event)"
                >
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                  >
                    <circle cx="5" cy="12" r="2"/>
                    <circle cx="12" cy="12" r="2"/>
                    <circle cx="19" cy="12" r="2"/>
                  </svg>
                </div>
                <!-- 三个点弹出菜单 -->
                <div
                  v-if="menuVisible === session.memoryId"
                  class="session-menu"
                  :style="menuPosition ? { top: menuPosition.top + 'px', left: menuPosition.left + 'px' } : {}"
                  @click.stop
                >
                  <div class="menu-item" @click="handleTogglePin(session, $event)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M16 4v6l2 4v2H6v-2l2-4V4h8zm-2 6V6h-4v4l-2 4h8l-2-4z"/>
                    </svg>
                    <span>{{ session.isPinned ? "取消置顶" : "置顶" }}</span>
                  </div>
                  <div class="menu-item" @click="handleRename(session, $event)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                    <span>重命名</span>
                  </div>
                  <div class="menu-item delete" @click="handleDeleteSession(session.memoryId, $event)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                    </svg>
                    <span>删除</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 7 天内分组 -->
          <div class="session-group" v-if="groupedSessions.week.length > 0">
            <div class="group-header" @click="toggleGroup('week')">
              <span class="group-label">7 天内</span>
              <span class="group-toggle" :class="{ collapsed: !expandedGroups.week }">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </span>
            </div>
            <div class="group-content" v-show="expandedGroups.week">
              <div
                v-for="session in groupedSessions.week"
                :key="session.memoryId"
                class="session-item"
                :class="{
                  active: session.memoryId === sessionMemoryId,
                  selected: selectedSessions.has(session.memoryId),
                  disabled: streamingMemoryId !== null,
                }"
                :style="{
                  'pointer-events':
                    streamingMemoryId !== null && session.memoryId !== sessionMemoryId
                      ? 'none'
                      : 'auto',
                  opacity:
                    streamingMemoryId !== null && session.memoryId !== sessionMemoryId ? '0.5' : '1',
                }"
                @click="switchSession(session.memoryId)"
              >
                <div
                  v-if="isBatchDeleteMode"
                  class="session-checkbox"
                  @click="toggleSessionSelection(session.memoryId, $event)"
                >
                  <svg
                    v-if="selectedSessions.has(session.memoryId)"
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="3" fill="var(--accent)" />
                    <polyline points="9 12 11 14 15 10" stroke="white" stroke-width="2.5" />
                  </svg>
                  <svg
                    v-else
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="3" />
                  </svg>
                </div>
                <div v-else class="session-icon">
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                  </svg>
                </div>
                <!-- 会话标题 - 支持编辑模式 -->
                <template v-if="editingSessionId === session.memoryId">
                  <input
                    v-model="editingTitle"
                    class="session-title-input"
                    @keydown="handleRenameKeydown"
                    @blur="confirmRename"
                    @click.stop
                    autofocus
                  />
                </template>
                <template v-else>
                  <div class="session-title">
                    <svg
                      v-if="session.isPinned"
                      class="pin-icon"
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="currentColor"
                      style="margin-right: 4px; flex-shrink: 0;"
                    >
                      <circle cx="12" cy="8" r="4"/>
                      <path d="M12 12l-3 10h6L12 12z"/>
                    </svg>
                    {{ session.title }}
                  </div>
                </template>
                <!-- 三个点菜单按钮 -->
                <div
                  v-if="!isBatchDeleteMode"
                  class="menu-btn"
                  @click="toggleMenu(session.memoryId, $event)"
                >
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                  >
                    <circle cx="5" cy="12" r="2"/>
                    <circle cx="12" cy="12" r="2"/>
                    <circle cx="19" cy="12" r="2"/>
                  </svg>
                </div>
                <!-- 三个点弹出菜单 -->
                <div
                  v-if="menuVisible === session.memoryId"
                  class="session-menu"
                  :style="menuPosition ? { top: menuPosition.top + 'px', left: menuPosition.left + 'px' } : {}"
                  @click.stop
                >
                  <div class="menu-item" @click="handleTogglePin(session, $event)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M16 4v6l2 4v2H6v-2l2-4V4h8zm-2 6V6h-4v4l-2 4h8l-2-4z"/>
                    </svg>
                    <span>{{ session.isPinned ? "取消置顶" : "置顶" }}</span>
                  </div>
                  <div class="menu-item" @click="handleRename(session, $event)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                    <span>重命名</span>
                  </div>
                  <div class="menu-item delete" @click="handleDeleteSession(session.memoryId, $event)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                    </svg>
                    <span>删除</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 其它分组 -->
          <div class="session-group" v-if="groupedSessions.other.length > 0">
            <div class="group-header" @click="toggleGroup('other')">
              <span class="group-label">其它</span>
              <span class="group-toggle" :class="{ collapsed: !expandedGroups.other }">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </span>
            </div>
            <div class="group-content" v-show="expandedGroups.other">
              <div
                v-for="session in groupedSessions.other"
                :key="session.memoryId"
                class="session-item"
                :class="{
                  active: session.memoryId === sessionMemoryId,
                  selected: selectedSessions.has(session.memoryId),
                  disabled: streamingMemoryId !== null,
                }"
                :style="{
                  'pointer-events':
                    streamingMemoryId !== null && session.memoryId !== sessionMemoryId
                      ? 'none'
                      : 'auto',
                  opacity:
                    streamingMemoryId !== null && session.memoryId !== sessionMemoryId ? '0.5' : '1',
                }"
                @click="switchSession(session.memoryId)"
              >
                <div
                  v-if="isBatchDeleteMode"
                  class="session-checkbox"
                  @click="toggleSessionSelection(session.memoryId, $event)"
                >
                  <svg
                    v-if="selectedSessions.has(session.memoryId)"
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="3" fill="var(--accent)" />
                    <polyline points="9 12 11 14 15 10" stroke="white" stroke-width="2.5" />
                  </svg>
                  <svg
                    v-else
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="3" width="18" height="18" rx="3" />
                  </svg>
                </div>
                <div v-else class="session-icon">
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                  </svg>
                </div>
                <!-- 会话标题 - 支持编辑模式 -->
                <template v-if="editingSessionId === session.memoryId">
                  <input
                    v-model="editingTitle"
                    class="session-title-input"
                    @keydown="handleRenameKeydown"
                    @blur="confirmRename"
                    @click.stop
                    autofocus
                  />
                </template>
                <template v-else>
                  <div class="session-title">
                    <svg
                      v-if="session.isPinned"
                      class="pin-icon"
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="currentColor"
                      style="margin-right: 4px; flex-shrink: 0;"
                    >
                      <circle cx="12" cy="8" r="4"/>
                      <path d="M12 12l-3 10h6L12 12z"/>
                    </svg>
                    {{ session.title }}
                  </div>
                </template>
                <!-- 三个点菜单按钮 -->
                <div
                  v-if="!isBatchDeleteMode"
                  class="menu-btn"
                  @click="toggleMenu(session.memoryId, $event)"
                >
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                  >
                    <circle cx="5" cy="12" r="2"/>
                    <circle cx="12" cy="12" r="2"/>
                    <circle cx="19" cy="12" r="2"/>
                  </svg>
                </div>
                <!-- 三个点弹出菜单 -->
                <div
                  v-if="menuVisible === session.memoryId"
                  class="session-menu"
                  :style="menuPosition ? { top: menuPosition.top + 'px', left: menuPosition.left + 'px' } : {}"
                  @click.stop
                >
                  <div class="menu-item" @click="handleTogglePin(session, $event)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M16 4v6l2 4v2H6v-2l2-4V4h8zm-2 6V6h-4v4l-2 4h8l-2-4z"/>
                    </svg>
                    <span>{{ session.isPinned ? "取消置顶" : "置顶" }}</span>
                  </div>
                  <div class="menu-item" @click="handleRename(session, $event)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                    <span>重命名</span>
                  </div>
                  <div class="menu-item delete" @click="handleDeleteSession(session.memoryId, $event)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                    </svg>
                    <span>删除</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Chat Area -->
    <div class="chat-app">
      <!-- Bento Grid Header -->
      <header class="bento-header">
        <div class="brand-cell">
          <button
            class="icon-btn sidebar-toggle-mobile"
            @click="sidebarCollapsed = !sidebarCollapsed"
            :title="sidebarCollapsed ? '展开边栏' : '收起边栏'"
          >
            <svg
              v-if="sidebarCollapsed"
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <!-- 边栏收起：外框 + 分隔线 + 右箭头 -->
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <line x1="9" y1="3" x2="9" y2="21" />
              <path d="M11 8l5 4-5 4V8z" fill="currentColor" stroke="none" />
            </svg>
            <svg
              v-else
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <!-- 边栏展开：外框 + 分隔线 + 左箭头 -->
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <line x1="9" y1="3" x2="9" y2="21" />
              <path d="M19 8l-5 4 5 4V8z" fill="currentColor" stroke="none" />
            </svg>
          </button>
          <button
            class="icon-btn back-btn"
            @click="goBack"
            title="返回大厅"
            :disabled="streamingMemoryId !== null"
            :style="{
              opacity: streamingMemoryId !== null ? '0.5' : '1',
              cursor: streamingMemoryId !== null ? 'not-allowed' : 'pointer',
            }"
          >
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
              <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
          </button>
          <div class="logo-pulse"></div>
          <h1 class="brand-title">莫星AI</h1>
        </div>

        <div class="action-cell">
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

          <div class="username-display">
            <img :src="avatarUrl" alt="头像" class="user-avatar-small" />
            <span>{{ authStore.userInfo?.username || '用户' }}</span>
          </div>
        </div>
      </header>

      <!-- Main Chat Area -->
      <main class="chat-stage">
        <!-- Empty State -->
        <div v-if="isSwitchingSession" class="empty-bento">
          <div class="welcome-card">
            <div class="loading-spinner">加载中...</div>
          </div>
        </div>
        <div v-else-if="messages.length === 0" class="empty-bento">
          <div class="welcome-card">
            <div class="welcome-icon">
              <svg
                width="48"
                height="48"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
              >
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
              </svg>
            </div>
            <h2 class="welcome-title">开始对话</h2>
            <p class="welcome-subtitle">输入问题，与 AI 助手探索知识的边界</p>
            <div class="suggestion-chips">
              <button class="chip" @click="inputMessage = '解释量子计算原理'">量子计算</button>
              <button class="chip" @click="inputMessage = '帮我写一段Python代码'">写代码</button>
              <button class="chip" @click="inputMessage = '翻译这段英文'">翻译</button>
            </div>
          </div>
        </div>

        <!-- Message Stream -->
        <div v-else ref="chatContainer" class="message-stream">
          <div
            v-for="(msg, index) in messages"
            :key="msg.id"
            class="message-bento"
            :class="[msg.role, { 'stagger-enter': true }]"
            :style="{ animationDelay: `${index * 80}ms` }"
          >
            <!-- Avatar -->
            <div class="avatar-wrapper">
              <div class="avatar" :class="msg.role">
                <svg
                  v-if="msg.role === 'user'"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
                <svg
                  v-else
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
              </div>
            </div>

            <!-- Content -->
            <div class="content-wrapper">
              <div class="meta-label">{{ msg.role === 'user' ? '我' : '莫星 AI' }}</div>
              <div class="message-bubble" v-html="renderContent(msg.content)"></div>
            </div>
          </div>

          <!-- Loading State -->
          <div v-if="isTyping" class="message-bento assistant loading-enter">
            <div class="avatar-wrapper">
              <div class="avatar assistant">
                <svg
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
              </div>
            </div>
            <div class="content-wrapper">
              <div class="meta-label">莫星 AI</div>
              <div class="message-bubble thinking">
                <span class="thinking-dot"></span>
                <span class="thinking-dot"></span>
                <span class="thinking-dot"></span>
              </div>
              <div class="thinking-time">已思考 {{ thinkingSeconds }} 秒</div>
            </div>
          </div>
        </div>
      </main>

      <!-- Input Area -->
      <footer class="input-dock">
        <div class="input-wrapper">
          <!-- 指令按钮和弹窗 -->
          <div class="commands-wrapper">
            <button class="command-btn" @click="showCommands = !showCommands" title="查看指令">
              <span style="font-size: 18px; font-weight: 600; font-family: monospace">/</span>
            </button>
            <!-- 指令列表弹窗 -->
            <div v-if="showCommands" class="commands-dropdown">
              <div
                v-for="cmd in commands"
                :key="cmd.name"
                class="command-item"
                @click="insertCommand(cmd.name)"
              >
                <span class="command-name">{{ cmd.name }}</span>
                <span class="command-desc">{{ cmd.description }}</span>
              </div>
            </div>
          </div>

          <div class="input-bento" :class="{ 'is-focused': isInputFocused }">
            <textarea
              v-model="inputMessage"
              :rows="2"
              placeholder="输入消息，按 Enter 发送..."
              @keydown="handleKeydown"
              @focus="isInputFocused = true"
              @blur="isInputFocused = false"
              class="message-input"
            />
            <button
              class="send-btn"
              :disabled="!inputMessage.trim() || loading"
              @click="sendMessage"
              :class="{ 'has-content': inputMessage.trim() }"
            >
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <line x1="22" y1="2" x2="11" y2="13" />
                <polygon points="22 2 15 22 11 13 2 9 22 2" />
              </svg>
            </button>
          </div>
        </div>
        <div class="input-hint">Enter 发送 · Shift+Enter 换行</div>
      </footer>
    </div>
  </div>
</template>

<style>
/* ============================================
   CHAT LAYOUT - With Sidebar
   ============================================ */

.chat-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 260px;
  background: var(--bg-elevated);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
}

.sidebar.collapsed {
  width: 0;
  border-right: none;
  overflow: hidden;
}

.sidebar-header {
  padding: 12px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.new-chat-btn-sidebar {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  background: var(--accent);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.new-chat-btn-sidebar:hover {
  background: var(--accent-hover);
  transform: translateY(-1px);
}

/* 批量删除按钮 */
.batch-delete-btn-sidebar {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: 10px;
  color: #ef4444;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.batch-delete-btn-sidebar:hover {
  background: rgba(239, 68, 68, 0.1);
  border-color: #ef4444;
}

/* 批量操作按钮组 */
.batch-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  min-width: 36px;
}

.batch-confirm-btn {
  flex: 1;
  background: #ef4444;
  color: white;
}

.batch-confirm-btn:hover:not(:disabled) {
  background: #dc2626;
  transform: translateY(-1px);
}

.batch-confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.batch-cancel-btn {
  background: var(--bg-subtle);
  color: var(--text-secondary);
  border: 1px solid var(--border);
}

.batch-cancel-btn:hover {
  background: var(--border);
}

.batch-select-all-btn {
  background: var(--bg-subtle);
  color: var(--text-primary);
  border: 1px solid var(--border);
}

.batch-select-all-btn:hover {
  background: var(--border);
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.loading-text,
.empty-sessions {
  padding: 24px 16px;
  text-align: center;
  font-size: 13px;
  color: var(--text-tertiary);
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 分组样式 */
.session-group {
  margin-bottom: 12px;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px;
  cursor: pointer;
  color: var(--text-tertiary);
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  border-radius: 6px;
  transition: all 0.15s ease;
  user-select: none;
}

.group-header:hover {
  color: var(--text-secondary);
  background: var(--bg-subtle);
}

.group-label {
  flex: 1;
}

.group-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;
  color: var(--text-tertiary);
}

.group-toggle.collapsed {
  transform: rotate(-90deg);
}

.group-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-left: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.session-item:hover {
  background: var(--bg-subtle);
}

.session-item.active {
  background: var(--accent-soft);
  border: 1px solid var(--accent);
}

.session-item.selected {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid #ef4444;
}

.session-checkbox {
  color: var(--text-tertiary);
  flex-shrink: 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.session-icon {
  color: var(--text-tertiary);
  flex-shrink: 0;
}

.session-item.active .session-icon {
  color: var(--accent);
}

.session-title {
  flex: 1;
  font-size: 13px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
}

/* 三个点菜单按钮 */
.menu-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.session-item:hover .menu-btn {
  opacity: 1;
}

.menu-btn:hover {
  background: var(--bg-subtle);
  color: var(--text-primary);
}

/* 弹出菜单 */
.session-menu {
  position: fixed;
  min-width: 120px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 6px;
  z-index: 1000;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease;
  color: var(--text-primary);
  font-size: 13px;
}

.menu-item:hover {
  background: var(--bg-subtle);
}

.menu-item.delete {
  color: #ef4444;
}

.menu-item.delete:hover {
  background: rgba(239, 68, 68, 0.1);
}

/* 置顶图标 */
.pin-icon {
  color: var(--accent);
  flex-shrink: 0;
}

/* 编辑中的标题输入框 */
.session-title-input {
  flex: 1;
  padding: 4px 8px;
  font-size: 13px;
  border: 1px solid var(--accent);
  border-radius: 6px;
  background: var(--bg-canvas);
  color: var(--text-primary);
  outline: none;
}

.session-title-input:focus {
  box-shadow: 0 0 0 2px var(--accent-soft);
}

/* Mobile Sidebar Toggle */
.sidebar-toggle-mobile {
  display: none;
}

@media (max-width: 768px) {
  .sidebar {
    position: absolute;
    z-index: 100;
    height: 100%;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  }

  .sidebar.collapsed {
    width: 0;
    box-shadow: none;
  }

  .sidebar-toggle-mobile {
    display: flex;
  }
}

/* ============================================
   CHAT APP - Original Styles
   ============================================ */

.chat-app {
  flex: 1;
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: 0;
  background: var(--bg-canvas);
  overflow: hidden;
}

/* CSS Custom Properties - Soft Monochrome Palette */
:root {
  --bg-canvas: #fdfbf7;
  --bg-elevated: #ffffff;
  --bg-subtle: #f5f3ef;
  --text-primary: #1a1a1a;
  --text-secondary: #6b6b6b;
  --text-tertiary: #a3a3a3;
  --accent: #4a7c9b;
  --accent-soft: rgba(74, 124, 155, 0.08);
  --accent-hover: #3d6a85;
  --border: #e8e6e1;
  --border-subtle: rgba(0, 0, 0, 0.06);
  --ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);
  --ease-smooth: cubic-bezier(0.16, 1, 0.3, 1);
  --duration-fast: 150ms;
  --duration-normal: 300ms;
  --duration-slow: 500ms;
}

[data-theme='dark'] {
  --bg-canvas: #0f0f0f;
  --bg-elevated: #1a1a1a;
  --bg-subtle: #141414;
  --text-primary: #f5f5f5;
  --text-secondary: #a0a0a0;
  --text-tertiary: #6b6b6b;
  --accent: #6b9bc3;
  --accent-soft: rgba(107, 155, 195, 0.12);
  --accent-hover: #8ab4d4;
  --border: #2a2a2a;
  --border-subtle: rgba(255, 255, 255, 0.06);
}

/* 亮色模式滚动条 */
.sidebar-content::-webkit-scrollbar {
  width: 6px;
}

.sidebar-content::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-content::-webkit-scrollbar-thumb {
  background: #d0d0d0;
  border-radius: 3px;
}

.sidebar-content::-webkit-scrollbar-thumb:hover {
  background: #b0b0b0;
}

/* 黑暗模式滚动条 */
[data-theme='dark'] .sidebar-content::-webkit-scrollbar {
  width: 6px;
}

[data-theme='dark'] .sidebar-content::-webkit-scrollbar-track {
  background: transparent;
}

[data-theme='dark'] .sidebar-content::-webkit-scrollbar-thumb {
  background: #3a3a3a;
  border-radius: 3px;
}

[data-theme='dark'] .sidebar-content::-webkit-scrollbar-thumb:hover {
  background: #4a4a4a;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  font-family:
    'Plus Jakarta Sans',
    -apple-system,
    BlinkMacSystemFont,
    sans-serif;
  background: var(--bg-canvas);
  color: var(--text-primary);
  height: 100%;
  overflow: hidden;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Header */
.bento-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  margin: 16px 16px 8px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 20px;
  box-shadow: 0 1px 3px var(--border-subtle);
}

.brand-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-pulse {
  width: 10px;
  height: 10px;
  background: var(--accent);
  border-radius: 50%;
  animation: pulse-soft 2s ease-in-out infinite;
}

@keyframes pulse-soft {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(0.9);
  }
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--text-primary);
}

.action-cell {
  display: flex;
  gap: 8px;
  align-items: center;
}

.username-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px 6px 6px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 10px;
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  height: 40px;
}

.user-avatar-small {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  background: var(--bg-subtle);
}

.username-display svg {
  color: var(--text-secondary);
}

.icon-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: var(--bg-canvas);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-smooth);
}

.icon-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
  transform: translateY(-1px);
}

.icon-btn:active {
  transform: scale(0.96);
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
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 8px;
  box-sizing: border-box;
  position: relative;
  z-index: 1;
}

.theme-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  transition: opacity var(--duration-fast) var(--ease-smooth);
}

.theme-icon.light-icon {
  color: #f59e0b;
  opacity: 1;
}

.theme-icon.dark-icon {
  color: #60a5fa;
  opacity: 0.5;
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
  background: var(--bg-canvas);
  border: 1px solid var(--border);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  transition: all var(--duration-normal) var(--ease-smooth);
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

/* Chat Stage */
.chat-stage {
  overflow-y: auto;
  padding: 8px 16px;
  scroll-behavior: smooth;
}

.chat-stage::-webkit-scrollbar {
  width: 6px;
}

.chat-stage::-webkit-scrollbar-track {
  background: transparent;
}

.chat-stage::-webkit-scrollbar-thumb {
  background: var(--border);
  border-radius: 3px;
}

/* Empty State */
.empty-bento {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fade-up 0.6s var(--ease-spring);
}

.welcome-card {
  text-align: center;
  padding: 60px 48px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 24px;
  max-width: 420px;
  box-shadow: 0 2px 8px var(--border-subtle);
}

.welcome-icon {
  color: var(--accent);
  margin-bottom: 24px;
  opacity: 0.8;
}

.loading-spinner {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.loading-spinner::before {
  content: '';
  width: 16px;
  height: 16px;
  border: 2px solid var(--border);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.welcome-title {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.03em;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.welcome-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 32px;
  line-height: 1.5;
}

.suggestion-chips {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.chip {
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  border-radius: 20px;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-smooth);
}

.chip:hover {
  background: var(--accent-soft);
  border-color: var(--accent);
  color: var(--accent);
  transform: translateY(-1px);
}

/* Message Stream */
.message-stream {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 16px;
}

.message-bento {
  display: flex;
  gap: 12px;
  max-width: 85%;
  animation: message-enter 0.4s var(--ease-spring);
}

.message-bento.user {
  flex-direction: row-reverse;
  margin-left: auto;
}

@keyframes message-enter {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.loading-enter {
  animation: fade-in 0.3s ease;
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.avatar-wrapper {
  flex-shrink: 0;
}

.avatar {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 14px;
}

.avatar.user {
  background: var(--bg-subtle);
  color: var(--text-secondary);
  border: 1px solid var(--border);
}

.avatar.assistant {
  background: var(--accent);
  color: white;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: calc(100% - 44px);
}

.meta-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 0 4px;
}

.message-bubble {
  padding: 16px 20px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 16px;
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.65;
  word-break: break-word;
  transition: all var(--duration-fast) var(--ease-smooth);
}

.message-bubble strong {
  font-weight: 900 !important;
}

.message-bubble h3 {
  font-size: 28px !important;
  font-weight: 700 !important;
  margin: 24px 0 14px;
  color: var(--text-primary);
}

.message-bubble h4 {
  font-size: 22px !important;
  font-weight: 700 !important;
  margin: 20px 0 12px;
  color: var(--text-primary);
}

.message-bento.user .message-bubble {
  background: var(--accent);
  border-color: var(--accent);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-bento.assistant .message-bubble {
  background: var(--bg-elevated);
  border-bottom-left-radius: 4px;
}

.thinking {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 18px 24px;
  min-width: 80px;
}

.thinking-dot {
  width: 6px;
  height: 6px;
  background: var(--text-tertiary);
  border-radius: 50%;
  animation: thinking-bounce 1.4s ease-in-out infinite;
}

.thinking-dot:nth-child(2) {
  animation-delay: 0.2s;
}
.thinking-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes thinking-bounce {
  0%,
  80%,
  100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-6px);
  }
}

.thinking-time {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 8px;
  text-align: center;
}

/* Input Dock */
.input-dock {
  padding: 16px;
  background: linear-gradient(to top, var(--bg-canvas) 80%, transparent);
}

.input-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.commands-wrapper {
  position: relative;
  display: flex;
  justify-content: flex-start;
}

.command-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  color: var(--text-secondary);
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.command-btn:hover {
  color: var(--accent);
  border-color: var(--accent);
  background: var(--accent-soft);
}

.commands-dropdown {
  position: absolute;
  bottom: 100%;
  left: 0;
  margin-bottom: 8px;
  min-width: 260px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 12px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  padding: 8px;
  z-index: 100;
}

.command-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.command-item:hover {
  background: var(--bg-subtle);
}

.command-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--accent);
}

.command-desc {
  font-size: 12px;
  color: var(--text-tertiary);
}

.input-bento {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 20px;
  box-shadow: 0 2px 12px var(--border-subtle);
  transition: all var(--duration-fast) var(--ease-smooth);
  flex: 1;
}

.input-bento.is-focused {
  border-color: var(--accent);
  box-shadow:
    0 0 0 3px var(--accent-soft),
    0 4px 20px var(--border-subtle);
  transform: translateY(-1px);
}

.message-input {
  flex: 1;
  min-height: 24px;
  max-height: 120px;
  padding: 6px 0;
  font-family: inherit;
  font-size: 15px;
  line-height: 1.5;
  color: var(--text-primary);
  background: transparent;
  border: none;
  resize: none;
  outline: none;
}

.message-input::placeholder {
  color: var(--text-tertiary);
}

.send-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  border-radius: 12px;
  color: var(--text-tertiary);
  cursor: not-allowed;
  transition: all var(--duration-fast) var(--ease-spring);
}

.send-btn.has-content {
  background: var(--accent);
  border-color: var(--accent);
  color: white;
  cursor: pointer;
}

.send-btn.has-content:hover {
  background: var(--accent-hover);
  transform: scale(1.05);
}

.input-hint {
  text-align: center;
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-tertiary);
  letter-spacing: 0.02em;
}

/* Markdown Styling - Typora-like */
.message-bubble p {
  margin: 0 0 16px;
}
.message-bubble p:last-child {
  margin-bottom: 0;
}

/* 旧代码块容器样式（保留兼容性） */
.code-content-wrapper {
  display: flex;
  align-items: stretch;
}

.message-bubble code {
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 0.9em;
}

.message-bubble p code {
  background: var(--accent-soft);
  color: var(--accent);
  padding: 3px 8px;
  border-radius: 6px;
  font-weight: 500;
  font-size: 0.95em;
}

/* 列表样式 - 白点和文字都向右缩进 */
.message-bubble ul,
.message-bubble ol {
  margin: 12px 0 !important;
  padding-left: 20px !important;
}

.message-bubble ul {
  list-style-type: disc !important;
  list-style-position: outside !important;
}

.message-bubble ul ul {
  list-style-type: disc !important;
}

.message-bubble ol {
  list-style-type: decimal !important;
  list-style-position: outside !important;
}

.message-bubble li {
  margin: 8px 0 !important;
  line-height: 1.7 !important;
  padding-left: 8px !important;
}

.message-bubble li::marker {
  color: var(--accent) !important;
  font-weight: 500 !important;
}

/* Headers - Typora-like */
.message-bubble h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 24px 0 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--border);
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.message-bubble h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 24px 0 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border);
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

/* Blockquote */
.message-bubble blockquote {
  margin: 16px 0;
  padding: 12px 20px;
  border-left: 4px solid var(--accent);
  background: var(--bg-subtle);
  border-radius: 8px 0 0 8px;
  color: var(--text-secondary);
  font-style: italic;
}

.message-bubble blockquote p:last-child {
  margin-bottom: 0;
}

/* Table */
.message-bubble table {
  border-collapse: collapse !important;
  width: 100%;
  margin: 16px 0;
  font-size: 14px;
}

.message-bubble th,
.message-bubble td {
  border: 1px solid var(--border) !important;
  padding: 12px 16px;
  text-align: left;
}

.message-bubble th {
  background: var(--bg-subtle);
  font-weight: 600;
  color: var(--text-primary);
}

.message-bubble tr:nth-child(even) {
  background: var(--bg-subtle);
}

/* Code blocks - 专业代码块样式 */
.message-bubble pre {
  background: #f5f5f5; /* 亮色模式：浅灰色背景 */
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 0;
  overflow-x: auto;
  margin: 16px 0;
  white-space: pre-wrap !important;
  word-break: break-all;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 黑暗模式代码块背景 - 深色 */
[data-theme='dark'] .message-bubble pre {
  background: #1e1e1e;
  border: 1px solid #3a3a3a;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

/* 代码块头部区域 */
.message-bubble pre::before {
  content: '';
  display: block;
  height: 36px;
  background: rgba(0, 0, 0, 0.04);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px 12px 0 0;
}

/* 黑暗模式代码块头部 */
[data-theme='dark'] .message-bubble pre::before {
  background: rgba(255, 255, 255, 0.05);
  border-bottom-color: rgba(255, 255, 255, 0.1);
}

/* 语言标签 */
.message-bubble .code-lang-badge {
  position: absolute;
  top: 9px;
  left: 14px;
  font-size: 11px;
  font-weight: 600;
  color: #666;
  text-transform: uppercase;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  pointer-events: none;
  letter-spacing: 0.05em;
}

/* 黑暗模式语言标签 */
[data-theme='dark'] .message-bubble .code-lang-badge {
  color: #999;
}

/* 复制按钮样式 - 无边框无背景 */
.message-bubble .copy-code-btn {
  position: absolute;
  top: 6px;
  right: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 6px;
  font-size: 11px;
  font-weight: 500;
  color: #666;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.message-bubble .copy-code-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #333;
}

.message-bubble .copy-code-btn:active {
  transform: translateY(0);
}

.message-bubble .copy-code-btn.copied {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border-color: rgba(16, 185, 129, 0.3);
}

.message-bubble .copy-code-btn .copy-text {
  font-size: 11px;
  opacity: 0.9;
}

.message-bubble .copy-code-btn svg {
  width: 14px;
  height: 14px;
}

/* 黑暗模式复制按钮 */
[data-theme='dark'] .message-bubble .copy-code-btn {
  color: #999;
  background: transparent;
  border: none;
}

[data-theme='dark'] .message-bubble .copy-code-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

/* 代码内容区域 */
.message-bubble pre code {
  display: block;
  padding: 16px 20px;
  padding-top: 16px;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.7;
  color: #333; /* 亮色模式：深色文字 */
  white-space: pre-wrap !important;
  background: transparent;
}

/* 黑暗模式代码文字颜色 */
[data-theme='dark'] .message-bubble pre code {
  color: #e0e0e0;
}

/* Inline code */
.message-bubble p code {
  background: var(--accent-soft);
  color: var(--accent);
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 0.9em;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
}

/* Horizontal Rule */
.message-bubble hr {
  border: none;
  border-top: 2px solid var(--border);
  margin: 24px 0;
}

/* Image */
.message-bubble img {
  max-width: 100%;
  border-radius: 12px;
  margin: 12px 0;
}

/* Link */
.message-bubble a {
  color: var(--accent);
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s ease;
}

.message-bubble a:hover {
  border-bottom-color: var(--accent);
}

/* Bold and Italic */
.message-bubble strong {
  font-weight: 900 !important;
  color: var(--text-primary);
}

.message-bubble em {
  font-style: italic;
  color: var(--text-primary);
}

/* Checkbox / Task List */
.message-bubble input[type='checkbox'] {
  margin-right: 8px;
}

.message-bubble .task-list-item {
  list-style: none;
  margin-left: -20px;
}

@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stagger-enter {
  animation: message-enter 0.4s var(--ease-spring) backwards;
}

@media (max-width: 640px) {
  .bento-header {
    margin: 12px 12px 8px;
    padding: 16px 20px;
  }

  .welcome-card {
    margin: 0 16px;
    padding: 40px 32px;
  }

  .message-bento {
    max-width: 92%;
  }

  .input-dock {
    padding: 12px;
  }
}
</style>