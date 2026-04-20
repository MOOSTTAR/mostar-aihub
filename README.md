# MoStar AI Hub

基于 Spring Boot + Vue3 的智能聊天应用，集成 LangChain4j AI 框架。

## ✨ 功能特性

- 🔐 **JWT 认证** - 安全的用户登录和 Token 管理
- 💬 **AI 对话** - 流式 SSE 响应，实时打字机效果
- 📋 **会话管理** - 历史会话列表、标题自动生成、一键删除
- 🎨 **精美 UI** - Soft UI + Minimalist 设计风格
- 🌙 **主题切换** - 亮色/暗色模式
- 📱 **响应式** - 适配移动端
- 📝 **Markdown** - Typora 风格排版
- 🔢 **公式支持** - MathJax 数学公式渲染
- 💾 **Redis 存储** - 高效的会话和 Token 管理

## 🛠️ 技术栈

### 后端
- Java 21
- Spring Boot 3.4.11
- Spring Security + JWT
- LangChain4j (DeepSeek AI)
- MyBatis-Plus
- Redis
- MySQL

### 前端
- Vue 3 + TypeScript
- Pinia 状态管理
- Vue Router
- Axios
- Element Plus
- Vite

## 📦 项目结构

```
mostar-aihub/
├── front/                  # 前端项目
│   ├── src/
│   │   ├── components/     # 组件
│   │   ├── router/         # 路由
│   │   ├── stores/         # Pinia 状态
│   │   ├── views/          # 页面视图
│   │   └── utils/          # 工具函数
│   └── package.json
├── src/                    # 后端项目
│   ├── main/java/com/mostar/langchain4jtest/
│   │   ├── config/         # 配置类
│   │   ├── controller/     # 控制器
│   │   ├── entity/         # 实体类
│   │   ├── mapper/         # MyBatis Mapper
│   │   ├── repository/     # 数据仓库
│   │   ├── service/        # 服务层
│   │   └── utils/          # 工具类
│   └── main/resources/
│       └── application.yml # 配置文件
└── pom.xml
```

## 🚀 快速开始

### 环境要求

- JDK 21+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+

### 后端启动

```bash
# 1. 配置数据库（application.yml）
# 修改 MySQL 和 Redis 连接信息

# 2. 创建数据库
CREATE DATABASE langchain4j_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. 启动后端
mvn spring-boot:run

# 后端运行在 http://localhost:8081
```

### 前端启动

```bash
# 1. 进入前端目录
cd front

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 前端运行在 http://localhost:5173
```

## 🔧 配置说明

### application.yml

```yaml
# MySQL 配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/langchain4j_test
    username: your_username
    password: your_password

# Redis 配置
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password

# DeepSeek API Key
langchain4j:
  open-ai:
    api-key: your_deepseek_api_key
```

## 📸 截图

### 登录页面
### 对话主界面
### 历史会话列表

## 🔒 安全特性

- JWT Token 认证，无状态会话
- Token 自动续期机制
- 密码 BCrypt 加密存储
- Spring Security 权限控制

## 📄 License

MIT License

## 👨‍💻 作者

MOstAr

---

⭐ 如果这个项目对你有帮助，请给一个 Star！
