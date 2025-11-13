# AllData数据中台前端 - 启动指南

## 🚀 快速启动

### 1. 安装依赖

```bash
cd /Users/liumingze/DataHub/alldata-frontend
npm install
```

如果安装速度慢，可以使用国内镜像：

```bash
npm install --registry=https://registry.npmmirror.com
```

或者使用pnpm（推荐）：

```bash
# 安装pnpm
npm install -g pnpm

# 使用pnpm安装依赖
pnpm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

启动成功后，访问：http://localhost:5173

### 3. 登录系统

默认账号信息：
- 用户名：`admin`
- 密码：`admin123`

## 📁 项目结构说明

```
alldata-frontend/
├── src/
│   ├── views/              # 页面组件
│   │   ├── login/          # 登录页
│   │   ├── dashboard/      # 首页看板
│   │   ├── datasource/     # 数据源平台
│   │   ├── dbswitch/       # 数据库同步
│   │   ├── datacap/        # 数据中枢
│   │   ├── transport/      # 数据集成
│   │   ├── tis/            # 数据集成平台
│   │   ├── seatunnel/      # 数据同步平台
│   │   ├── datasophon/     # 数据平台
│   │   ├── streampark/     # 实时开发
│   │   ├── compare/        # 数据比对
│   │   └── standard/       # 数据标准
│   ├── layouts/            # 布局组件
│   ├── router/             # 路由配置
│   ├── store/              # 状态管理
│   ├── utils/              # 工具函数
│   └── api/                # API接口
├── public/                 # 静态资源
└── package.json            # 项目配置
```

## 🎯 已实现功能

### ✅ 基础框架
- [x] Vue 3 + TypeScript + Vite 开发环境
- [x] Element Plus UI组件库集成
- [x] 路由系统 (Vue Router)
- [x] 状态管理 (Pinia)
- [x] HTTP请求封装 (Axios)

### ✅ 核心页面
- [x] 登录页面
- [x] 主框架布局（侧边栏 + 顶部导航）
- [x] 首页看板（统计数据 + 快捷入口）
- [x] 404错误页面

### ✅ 功能模块入口
- [x] 数据源平台 (Chat2DB)
- [x] 数据库同步 (DBSwitch)
- [x] 数据中枢 (DataCap)
- [x] 数据集成 (Transport)
- [x] 数据集成平台 (Tis)
- [x] 数据同步平台 (SeaTunnel)
- [x] 数据平台 (DataSophon)
- [x] 实时开发 (StreamPark)
- [x] 数据比对管理
- [x] 数据标准管理

## 🔧 开发命令

```bash
# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 构建并指定生产环境
npm run build:prod

# 预览构建结果
npm run preview

# 代码检查
npm run lint
```

## 📝 下一步开发建议

### 1. 完善各模块功能
每个模块目前只有基础页面框架，需要根据实际需求开发具体功能：

- **数据源平台**: 数据源连接、SQL查询、可视化等
- **数据库同步**: 任务配置、执行监控、日志查看等
- **数据中枢**: 数据集管理、查询历史、报表等

### 2. 对接后端API
- 在 `src/api/` 目录下创建各模块的API接口文件
- 使用 `src/utils/request.ts` 中封装的请求方法
- 更新各页面组件，调用真实API

### 3. 完善用户系统
- 实现真实的登录认证
- 添加权限控制
- 完善用户信息管理

### 4. 优化UI/UX
- 添加加载动画
- 完善错误提示
- 优化响应式布局
- 添加主题切换功能

### 5. 添加更多功能
- 数据可视化图表
- 实时数据监控
- 任务调度管理
- 日志查看系统

## 🐛 常见问题

### 1. 依赖安装失败
```bash
# 清除缓存后重新安装
rm -rf node_modules package-lock.json
npm install
```

### 2. 端口被占用
修改 `vite.config.ts` 中的端口配置：
```typescript
server: {
  port: 5174, // 改为其他端口
}
```

### 3. 类型错误
```bash
# 重新生成类型声明文件
npm run dev
```

## 📞 技术支持

如有问题，请查看：
- 项目文档：`README.md`
- 参考文档：`数据中台.md`

---

**祝开发顺利！** 🎉

