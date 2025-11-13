# DataHub 后端快速开始

## 🚀 快速启动步骤

### 1. 环境准备

确保已安装以下环境：

- **JDK 11+**
  ```bash
  # 检查Java版本
  java -version
  
  # 如果未安装，macOS可以使用Homebrew安装
  brew install openjdk@11
  ```

- **Maven 3.6+**
  ```bash
  # 检查Maven版本
  mvn -version
  
  # 如果未安装，macOS可以使用Homebrew安装
  brew install maven
  ```

- **MySQL 8.0+**
  ```bash
  # 检查MySQL是否运行
  mysql -u root -p -e "SELECT VERSION();"
  ```

- **Redis 6.x+** (可选，用于缓存)
  ```bash
  # 检查Redis是否运行
  redis-cli ping
  
  # 如果未安装，macOS可以使用Homebrew安装
  brew install redis
  brew services start redis
  ```

### 2. 数据库初始化

```bash
# 进入项目目录
cd /Users/liumingze/DataHub/datahub-backend

# 执行数据库初始化脚本（请替换为你的MySQL密码）
mysql -u root -p < sql/datahub.sql

# 或者登录MySQL后手动执行
mysql -u root -p
source /Users/liumingze/DataHub/datahub-backend/sql/datahub.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库密码：

```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/datahub?...
      username: root
      password: 你的MySQL密码  # 修改这里
  
  redis:
    host: localhost
    port: 6379
    password:  # 如果Redis没有密码，留空
```

### 4. 启动服务

#### 方式一：使用启动脚本（推荐）

```bash
chmod +x start.sh
./start.sh
```

#### 方式二：使用Maven

```bash
# 编译项目
mvn clean package -DskipTests

# 运行项目
mvn spring-boot:run
```

#### 方式三：直接运行JAR包

```bash
# 编译项目
mvn clean package -DskipTests

# 运行JAR包
java -jar target/datahub-backend.jar
```

### 5. 验证服务

服务启动成功后，访问以下地址验证：

- **接口地址**: http://localhost:8080/api
- **Druid监控**: http://localhost:8080/api/druid (admin/admin)
- **测试登录接口**:
  ```bash
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}'
  ```

## 📝 默认账号

- 用户名: `admin`
- 密码: `admin123`

## 🔧 常见问题

### 1. 端口被占用

如果8080端口被占用，修改 `application.yml` 中的端口：

```yaml
server:
  port: 8081  # 修改为其他端口
```

### 2. MySQL连接失败

检查MySQL是否运行：
```bash
mysql -u root -p -e "SELECT 1;"
```

检查数据库是否创建：
```bash
mysql -u root -p -e "SHOW DATABASES LIKE 'datahub';"
```

### 3. Redis连接失败

如果不使用Redis，可以注释掉 `application.yml` 中的Redis配置：

```yaml
# spring:
#   redis:
#     host: localhost
#     port: 6379
```

### 4. 编译失败

清理Maven缓存重新编译：
```bash
mvn clean
rm -rf ~/.m2/repository
mvn package -DskipTests
```

## 📚 下一步

1. 查看 [README.md](README.md) 了解完整的API文档
2. 启动前端项目进行联调测试
3. 开始使用DataHub数据中台！

## 💡 提示

- 开发环境默认开启SQL日志，可以在控制台看到所有执行的SQL
- Druid监控面板可以查看数据源连接池状态和SQL执行情况
- 如果需要修改日志级别，编辑 `application.yml` 中的 `logging` 配置

