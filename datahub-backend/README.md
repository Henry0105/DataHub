# DataHub 数据中台后端服务

## 项目简介

DataHub 是一个企业级数据中台系统，提供数据源管理、数据库同步、数据查询、数据集管理等功能。

## 技术栈

- **框架**: Spring Boot 2.7.18
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.x
- **连接池**: Druid 1.2.20
- **认证**: JWT
- **工具类**: Hutool 5.8.23

## 项目结构

```
datahub-backend/
├── src/main/java/com/datahub/
│   ├── DataHubApplication.java          # 启动类
│   ├── common/                          # 通用类
│   │   ├── Result.java                  # 统一响应结果
│   │   └── PageResult.java              # 分页响应结果
│   ├── config/                          # 配置类
│   │   ├── CorsConfig.java              # 跨域配置
│   │   └── MybatisPlusConfig.java       # MyBatis Plus配置
│   ├── controller/                      # 控制器
│   │   ├── AuthController.java          # 认证控制器
│   │   ├── DashboardController.java     # 首页看板控制器
│   │   ├── DatasourceController.java    # 数据源控制器
│   │   ├── DatacapController.java       # 数据中枢控制器
│   │   └── SyncTaskController.java      # 同步任务控制器
│   ├── dto/                             # 数据传输对象
│   │   ├── LoginRequest.java            # 登录请求
│   │   ├── LoginResponse.java           # 登录响应
│   │   ├── QueryRequest.java            # 查询请求
│   │   ├── QueryResult.java             # 查询结果
│   │   └── DashboardStats.java          # 统计数据
│   ├── entity/                          # 实体类
│   │   ├── SysUser.java                 # 用户实体
│   │   ├── Datasource.java              # 数据源实体
│   │   ├── SyncTask.java                # 同步任务实体
│   │   ├── QueryHistory.java            # 查询历史实体
│   │   └── Dataset.java                 # 数据集实体
│   ├── exception/                       # 异常处理
│   │   ├── BusinessException.java       # 业务异常
│   │   └── GlobalExceptionHandler.java  # 全局异常处理器
│   ├── mapper/                          # Mapper接口
│   │   ├── SysUserMapper.java
│   │   ├── DatasourceMapper.java
│   │   ├── SyncTaskMapper.java
│   │   ├── QueryHistoryMapper.java
│   │   └── DatasetMapper.java
│   ├── service/                         # 服务接口
│   │   ├── AuthService.java
│   │   ├── DatasourceService.java
│   │   ├── DatacapService.java
│   │   └── SyncTaskService.java
│   ├── service/impl/                    # 服务实现
│   │   ├── AuthServiceImpl.java
│   │   ├── DatasourceServiceImpl.java
│   │   ├── DatacapServiceImpl.java
│   │   └── SyncTaskServiceImpl.java
│   └── util/                            # 工具类
│       └── JwtUtil.java                 # JWT工具类
├── src/main/resources/
│   ├── application.yml                  # 开发环境配置
│   └── application-prod.yml             # 生产环境配置
├── sql/
│   └── datahub.sql                      # 数据库初始化脚本
├── pom.xml                              # Maven配置
└── README.md                            # 项目说明
```

## 快速开始

### 1. 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.x+

### 2. 数据库初始化

```bash
# 执行数据库初始化脚本
mysql -u root -p < sql/datahub.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库和Redis连接信息：

```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/datahub?...
      username: root
      password: your_password
  
  redis:
    host: localhost
    port: 6379
    password: your_password
```

### 4. 编译运行

```bash
# 编译项目
mvn clean package

# 运行项目
java -jar target/datahub-backend.jar

# 或者使用Maven运行
mvn spring-boot:run
```

### 5. 访问接口

- 接口地址: http://localhost:8080/api
- Druid监控: http://localhost:8080/api/druid (admin/admin)

## API 接口文档

### 认证接口

#### 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

### 数据源管理

#### 分页查询数据源
```
GET /api/datasource/page?current=1&size=10&keyword=MySQL
```

#### 创建数据源
```
POST /api/datasource
Content-Type: application/json

{
  "name": "测试MySQL",
  "type": "MySQL",
  "host": "localhost",
  "port": 3306,
  "databaseName": "test",
  "username": "root",
  "password": "root",
  "description": "测试数据库"
}
```

#### 测试数据源连接
```
POST /api/datasource/{id}/test
```

### 数据中枢

#### 执行SQL查询
```
POST /api/datacap/query
Content-Type: application/json

{
  "datasourceId": 1,
  "sqlContent": "SELECT * FROM users LIMIT 10"
}
```

#### 查询历史
```
GET /api/datacap/history/page?current=1&size=10
```

#### 数据集管理
```
GET /api/datacap/dataset/page?current=1&size=10
POST /api/datacap/dataset
PUT /api/datacap/dataset/{id}
DELETE /api/datacap/dataset/{id}
POST /api/datacap/dataset/{id}/refresh
```

### 数据库同步

#### 分页查询同步任务
```
GET /api/sync/page?current=1&size=10
```

#### 创建同步任务
```
POST /api/sync
Content-Type: application/json

{
  "taskName": "用户数据同步",
  "sourceDatasourceId": 1,
  "targetDatasourceId": 2,
  "sourceTable": "users",
  "targetTable": "users_backup",
  "syncMode": "full"
}
```

#### 执行同步任务
```
POST /api/sync/{id}/execute
```

#### 停止同步任务
```
POST /api/sync/{id}/stop
```

### 首页看板

#### 获取统计数据
```
GET /api/dashboard/stats
```

## 默认账号

- 用户名: admin
- 密码: admin123

## 支持的数据库类型

- MySQL
- PostgreSQL
- Oracle
- 达梦(DM)
- 人大金仓(KingBase)

## 开发说明

### 添加新的数据库类型

1. 在 `pom.xml` 中添加对应的JDBC驱动依赖
2. 在 `DatasourceServiceImpl` 和 `DatacapServiceImpl` 的 `buildJdbcUrl` 方法中添加对应的JDBC URL构建逻辑

### 自定义业务逻辑

1. 在 `service` 包下创建服务接口
2. 在 `service/impl` 包下创建服务实现
3. 在 `controller` 包下创建控制器

## 生产部署

### 1. 修改生产配置

编辑 `src/main/resources/application-prod.yml`

### 2. 打包

```bash
mvn clean package -DskipTests
```

### 3. 运行

```bash
java -jar -Dspring.profiles.active=prod target/datahub-backend.jar
```

## 许可证

MIT License

## 联系方式

- 项目地址: https://github.com/yourusername/datahub
- 问题反馈: https://github.com/yourusername/datahub/issues

