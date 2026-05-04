import { createRouter, createWebHistory } from 'vue-router'
import IntroView from '../views/IntroView.vue'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'intro',
      component: IntroView
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { guestOnly: true }
    },
    {
      path: '/chat',
      name: 'chat',
      component: () => import('../views/ChatView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true, title: '修改资料' }
    }
  ],
})

// 路由守卫 - 认证检查
router.beforeEach((to, _from, next) => {
  // 直接从 localStorage 检查 token，避免 Pinia 状态同步问题
  const token = localStorage.getItem('token')
  const isLoggedIn = !!token

  // 根路径 - 不做跳转，让用户点击"开始体验"按钮
  if (to.path === '/') {
    next()
    return
  }

  // 需要登录的页面
  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 仅限未登录用户的页面（如登录页）
  if (to.meta.guestOnly && isLoggedIn) {
    next('/')
    return
  }

  next()
})

export default router
