# 项目概览
- 前端：`datahub-frontend`（Vue 3 + Vite + Element Plus + Pinia + Vue Router）。
- 后端：`datahub-backend`（Spring Boot + MyBatis-Plus + Druid + Redis + JWT）。
- 入口：后端主类 `datahub-backend/src/main/java/com/datahub/DataHubApplication.java:22`；启动日志含接口与监控地址。
- 代理：前端开发代理将 `/api` 转发至 `http://localhost:8080`（`datahub-frontend/vite.config.ts:27-36`）。
- 前端请求：`axios.create` 使用 `baseURL`=`/api`（`datahub-frontend/src/utils/request.ts:6-12`）。
- 后端端口与路径：`server.port=8080`、`context-path=/api`（`datahub-backend/src/main/resources/application.yml:1-4`）。

# 环境与配置
- 前端环境：`.env.*` 中的 `VITE_API_BASE_URL` 默认 `/api`，无需修改即可联通本地后端。
- 后端环境：`application.yml` 包含数据源与 Redis 等敏感配置（默认指向远程服务），建议本地运行改为本地数据库与缓存。
- 初始化数据：`datahub-backend/sql/datahub.sql`（创建表与默认数据，如管理员账号）。

# 本地运行步骤（macOS）
1. 安装依赖
   - 检查版本：`java -version`（JDK 11+）、`mvn -version`、`node -v`（>=16）、`npm -v`、如需本地 DB：`docker -v`、`docker compose version`。
2. 启动本地数据库（可选）
   - `cd /Users/liumingze/DataHub/deployment/server2-db`
   - `docker compose up -d`
   - 初始化数据库：
     - `mysql -h 127.0.0.1 -P 3306 -u root -p<密码> < /Users/liumingze/DataHub/datahub-backend/sql/datahub.sql`
3. 启动后端
   - `cd /Users/liumingze/DataHub/datahub-backend`
   - `mvn clean package -DskipTests && java -jar target/datahub-backend.jar`
   - 访问接口与文档：`http://localhost:8080/api`、`http://localhost:8080/api/doc.html`。
4. 启动前端
   - `cd /Users/liumingze/DataHub/datahub-frontend`
   - `npm install && npm run dev`
   - 打开：`http://localhost:5173`（默认登录：`admin / admin123`）。

# 验证联通
- 前端至后端：前端以 `/api` 发起请求，经 Vite 代理转发到 `http://localhost:8080`（`vite.config.ts:31-35`）。
- 请求封装：`request.ts` 在有 `token` 时添加 `Authorization: Bearer <token>`（`datahub-frontend/src/utils/request.ts:20-23`）。
- 后端入口与日志：主类输出启动提示（`datahub-backend/src/main/java/com/datahub/DataHubApplication.java:24-28`）。

# 可选：切换后端到本地数据库
- 修改 `application.yml` 的 `spring.datasource.*` 与 `spring.redis.*` 指向本地实例，或以环境变量/启动参数覆盖。
- 重新启动后端并回归验证。

# 下一步
- 在您确认后，我将按上述步骤在本机实际执行：
  - 使用 Docker 启动本地数据库并导入初始化数据；
  - 启动后端与前端；
  - 进行端到端联通验证与登录校验；
  - 必要时调整后端配置以使用本地数据库与 Redis。