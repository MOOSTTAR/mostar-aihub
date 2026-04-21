# MOstAr AI Hub

基于 Spring Boot + Vue3 的智能聊天应用，集成 LangChain4j AI 框架。

## ✨ 功能特性

- 🔐 **JWT 认证** - 安全的用户登录和 Token 管理
- 💬 **AI 对话** - 流式 SSE 响应，实时打字机效果
- 📋 **会话管理** - 历史会话列表、标题自动生成、一键删除
- 🧹 **一键清空** - `/clear` 指令清空对话和 AI 记忆
- ⏱️ **思考计时** - 显示 AI 已思考时长
- 🎮 **快捷指令** - 点击指令按钮快速输入命令
- 🎨 **精美 UI** - Soft UI + Minimalist 设计风格
- 🌙 **主题切换** - 亮色/暗色模式
- 📱 **响应式** - 适配移动端
- 📝 **Markdown** - Typora 风格排版
- 🔢 **公式支持** - MathJax 数学公式渲染
- 💾 **Redis 存储** - 高效的会话和 Token 管理

## 🛠️ 技术栈

### 后端
- Java 21
- Spring Boot 3.4.4
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
├── front/                      # 前端项目
│   ├── src/
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 公共组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面视图
│   └── package.json
├── src/                        # 后端项目
│   ├── main/java/com/mostar/langchain4jtest/
│   │   ├── aiservice/          # LangChain4j 声明式 AI 服务
│   │   ├── config/             # 配置类（安全、CORS、密码）
│   │   ├── constants/          # 常量定义
│   │   ├── context/            # 用户上下文
│   │   ├── controller/         # 控制器（聊天、会话、认证、用户）
│   │   ├── entity/             # 实体类
│   │   │   ├── dto/            # 数据传输对象
│   │   │   ├── po/             # 持久化对象
│   │   │   └── vo/             # 视图对象
│   │   ├── exception/          # 自定义异常
│   │   ├── filter/             # JWT 认证过滤器
│   │   ├── handler/            # 全局异常处理
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── repository/         # 数据仓库（Redis/MySQL ChatMemory）
│   │   ├── service/            # 服务层接口
│   │   │   └── impl/           # 服务层实现
│   │   └── utils/              # 工具类（JWT 等）
│   ├── main/resources/
│   │   ├── application.yml         # 主配置
│   │   ├── application-dev.yml     # 开发环境配置
│   │   ├── application-prod.yml    # 生产环境配置
│   │   └── mapper/                 # MyBatis XML
│   └── test/                       # 测试类
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
# 1. 创建数据库并初始化
# 使用 MySQL 客户端执行初始化脚本
mysql -u root -p < src/main/resources/sql/init.sql

# 2. 配置数据库（application.yml）
# 修改 MySQL 和 Redis 连接信息

# 3. 配置 DeepSeek API Key
# 设置环境变量：export DEEPSEEK_API_KEY=your_api_key
# 或在 application.yml 中直接填写

# 4. 启动后端
mvn spring-boot:run

# 后端运行在 http://localhost:8080
```

**默认测试账号：**
- 用户名：`test`
- 密码：`test123456`

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
