# MOstAr AI Hub

基于 Spring Boot + Vue3 的智能聊天应用，集成 LangChain4j AI 框架与 DeepSeek 大模型。

## ✨ 功能特性

### 核心功能

- 💬 **AI 对话** - 流式 SSE 响应，实时打字机效果
- 🧠 **上下文记忆** - 基于 Redis 的会话内容存储，支持多轮对话
- 📋 **会话管理** - 历史会话列表、标题自动生成、批量删除
- 🧹 **一键清空** - `/clear` 指令清空 AI 记忆（保留对话记录）

### 安全认证

- 🔐 **JWT 认证** - 安全的用户登录和 Token 管理
- 🔄 **自动续期** - Token 过期自动续期，无感知体验
- 🛡️ **XSS 防护** - 输入输出双重过滤，防止脚本注入

### 用户体验

- ⏱️ **思考计时** - 显示 AI 已思考时长，3 个跳动圆点动画
- 🎮 **快捷指令** - `/` 唤出指令菜单，快速输入命令
- 🎨 **精美 UI** - Soft UI + Minimalist 设计风格，Bento Grid 布局
- 🌙 **主题切换** - 亮色/暗色模式，平滑过渡
- 📱 **响应式** - 适配移动端，侧边栏可折叠
- 🔐 **安全确认** - 退出登录、删除会话前弹窗确认
- 📋 **代码复制** - 代码块一键复制，复制成功反馈
- ⚠️ **操作保护** - AI 回复中禁止切换会话/退出/删除

### 内容渲染

- 📝 **Markdown** - Typora 风格排版，支持表格、引用、列表
- 🧾 **代码高亮** - 语法高亮、语言标签、一键复制
- 🔢 **公式支持** - MathJax 数学公式渲染（行内/块级）
- 🔗 **链接预览** - 自动识别 URL

### 数据存储

- 💾 **Redis 存储** - 高效的会话和 Token 管理
- 🗄️ **MySQL 持久化** - 用户数据关系型存储

## 🛠️ 技术栈

### 后端

| 技术 | 版本 | 说明 |
| :--- | :--- | :--- |
| Java | 21 | 语言基础 |
| Spring Boot | 3.4.4 | Web 框架 |
| Spring Security | 6.x | 安全认证 |
| LangChain4j | latest | AI 应用框架 |
| MyBatis-Plus | latest | ORM 框架 |
| Redis | 7.x | 缓存/会话存储 |
| MySQL | 8.0+ | 关系数据库 |
| Hutool | latest | 工具库 |
| Lombok | latest | 代码简化 |

### 前端

| 技术 | 版本 | 说明 |
| :--- | :--- | :--- |
| Vue | 3.x | 渐进式框架 |
| TypeScript | 5.x | 类型系统 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP 客户端 |
| Element Plus | latest | UI 组件库 |
| Vite | 7.x | 构建工具 |
| DOMPurify | latest | XSS 防护 |
| MathJax | 3.x | 数学公式渲染 |

### AI 模型

| 模型 | 提供商 | 说明 |
| :--- | :--- | :--- |
| DeepSeek | 深度求索 | 主聊天模型 |

## 📦 项目结构

```
langchain4j-test/
├── front/                          # 前端项目
│   ├── src/
│   │   ├── assets/                 # 静态资源
│   │   ├── components/             # 公共组件
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # Pinia 状态管理
│   │   ├── utils/                  # 工具函数
│   │   └── views/                  # 页面视图
│   ├── index.html
│   ├── package.json
│   └── vite.config.ts
├── src/                            # 后端项目
│   ├── main/java/com/mostar/langchain4jtest/
│   │   ├── aiservice/              # LangChain4j 声明式 AI 服务
│   │   ├── config/                 # 配置类（安全、CORS、密码）
│   │   ├── constants/              # 常量定义
│   │   ├── context/                # 用户上下文
│   │   ├── controller/             # 控制器（聊天、会话、认证、用户）
│   │   ├── entity/                 # 实体类
│   │   │   ├── dto/                # 数据传输对象
│   │   │   ├── po/                 # 持久化对象
│   │   │   └── vo/                 # 视图对象
│   │   ├── exception/              # 自定义异常
│   │   ├── filter/                 # JWT 认证过滤器
│   │   ├── handler/                # 全局异常处理
│   │   ├── mapper/                 # MyBatis Mapper
│   │   ├── repository/             # 数据仓库（Redis/MySQL ChatMemory）
│   │   ├── service/                # 服务层接口
│   │   │   └── impl/               # 服务层实现
│   │   └── utils/                  # 工具类（JWT 等）
│   ├── main/resources/
│   │   ├── application.yml         # 主配置
│   │   ├── application-dev.yml     # 开发环境配置
│   │   ├── application-prod.yml    # 生产环境配置
│   │   ├── sql/                    # SQL 初始化脚本
│   │   └── mapper/                 # MyBatis XML
│   └── test/                       # 测试类
├── .github/                        # GitHub 配置
├── pom.xml                         # Maven 配置
└── README.md
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
- XSS 过滤防护

## 📖 API 接口

### 认证接口

| 接口 | 方法 | 说明 |
| :--- | :--- | :--- |
| /auth/login | POST | 用户登录 |
| /auth/logout | POST | 用户登出 |
| /auth/renew | POST | 续期 Token |
| /auth/register | POST | 用户注册 |
| /auth/test | GET | 测试 Token 有效性 |

### 聊天接口

| 接口 | 方法 | 说明 |
| :--- | :--- | :--- |
| /chat | GET | SSE 流式聊天 |
| /chat/clear | POST | 清空对话记忆 |
| /chat/sessions | GET | 获取会话列表 |
| /chat/sessions/:id | DELETE | 删除会话 |
| /chat/sessions/batch-delete | POST | 批量删除会话 |
| /chat/history/:memoryId | GET | 获取历史消息 |

## 🧪 测试

```bash
# 后端测试
mvn test

# 前端测试
cd front
npm run test
```

## 📄 License

MIT License

## 👨‍💻 作者

MOstAr

---

⭐ 如果这个项目对你有帮助，请给一个 Star！
