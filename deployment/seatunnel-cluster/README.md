# 🚀 Apache SeaTunnel + Quartz 数据同步引擎集成方案

## 📋 目录
- [一、方案概述](#一方案概述)
- [二、技术架构](#二技术架构)
- [三、部署步骤](#三部署步骤)
- [四、配置说明](#四配置说明)
- [五、使用指南](#五使用指南)
- [六、监控运维](#六监控运维)

---

## 一、方案概述

### 1.1 核心组件

| 组件 | 版本 | 用途 |
|------|------|------|
| Apache SeaTunnel | 2.3.8 | 数据同步引擎 |
| Quartz Scheduler | 2.3.x | 任务调度 |
| MySQL | 8.0 | 元数据存储 |
| Hazelcast | 5.3 | 集群协调 |
| Zookeeper | 3.8 | 分布式协调 |
| Docker | 24.x | 容器化部署 |

### 1.2 部署架构

```
┌─────────────────────────────────────────────────────────┐
│                     DataHub Frontend                     │
│                     (Vue 3 + Element Plus)               │
└───────────────────────┬─────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│                   DataHub Backend                        │
│            (Spring Boot + Quartz + SeaTunnel Client)     │
└───────────┬─────────────────────────┬───────────────────┘
            │                         │
            ▼                         ▼
┌───────────────────────┐  ┌─────────────────────────────┐
│  Quartz Scheduler     │  │   SeaTunnel Cluster         │
│  ┌─────────────────┐  │  │  ┌──────────────────────┐   │
│  │ Cron Triggers   │  │  │  │ Master (Server1)     │   │
│  │ Task Scheduler  │  │  │  │  - Web UI (5801)     │   │
│  │ Job Management  │  │  │  │  - REST API (8080)   │   │
│  └─────────────────┘  │  │  └──────────────────────┘   │
└───────────────────────┘  │  ┌──────────────────────┐   │
                           │  │ Worker-1 (Server2)   │   │
                           │  │  - Data Processing   │   │
                           │  └──────────────────────┘   │
                           │  ┌──────────────────────┐   │
                           │  │ Worker-2 (Server3)   │   │
                           │  │  - Data Processing   │   │
                           │  └──────────────────────┘   │
                           └─────────────────────────────┘
```

### 1.3 服务器规划

| 服务器 | IP地址 | 部署组件 | 资源配置 |
|--------|--------|----------|----------|
| Server1 | 10.2.0.14 | SeaTunnel Master + DataHub Backend | 8C16G |
| Server2 | 10.2.0.15 | SeaTunnel Worker-1 + MySQL | 8C16G |
| Server3 | 10.2.0.16 | SeaTunnel Worker-2 + Zookeeper | 16C32G |

---

## 二、技术架构

### 2.1 数据流转流程

```
1. 用户创建同步任务 (前端)
   ↓
2. 后端接收任务配置
   ↓
3. 保存到MySQL数据库
   ↓
4. Quartz Scheduler创建定时任务
   ↓
5. 触发执行 → SeaTunnelService
   ↓
6. 生成SeaTunnel配置文件 (HOCON)
   ↓
7. 提交到SeaTunnel Master节点
   ↓
8. Master分配任务给Worker节点
   ↓
9. Worker执行数据同步
   ↓
10. 实时更新任务进度 → MySQL
    ↓
11. 前端轮询获取进度更新
```

### 2.2 关键特性

#### ✅ **高可用**
- SeaTunnel集群模式(1 Master + 2 Workers)
- Quartz集群模式(数据库持久化)
- Hazelcast分布式协调

#### ✅ **高性能**
- 分布式并行处理
- 批量数据传输(1000条/批)
- Worker节点自动负载均衡

#### ✅ **可扩展**
- 支持动态添加Worker节点
- 支持多种数据源类型
- 支持自定义数据转换

#### ✅ **易运维**
- Docker容器化部署
- Web UI可视化管理
- 详细的日志和监控

---

## 三、部署步骤

### 3.1 前置准备

#### 1. 检查服务器环境

```bash
# 三台服务器都需要执行
# 检查操作系统
cat /etc/os-release

# 检查内存
free -h

# 检查磁盘
df -h

# 检查网络
ping 10.2.0.14
ping 10.2.0.15
ping 10.2.0.16
```

#### 2. 配置SSH免密登录(可选)

```bash
# 在本地机器生成密钥
ssh-keygen -t rsa

# 复制到三台服务器
ssh-copy-id root@10.2.0.14
ssh-copy-id root@10.2.0.15
ssh-copy-id root@10.2.0.16
```

### 3.2 一键部署

#### 方式一:使用自动化脚本(推荐)

```bash
cd /Users/liumingze/DataHub/deployment/seatunnel-cluster

# 赋予执行权限
chmod +x deploy.sh

# 执行部署
./deploy.sh
```

脚本会自动完成:
1. ✅ 检查SSH连接
2. ✅ 安装Docker和Docker Compose
3. ✅ 创建部署目录
4. ✅ 上传配置文件
5. ✅ 部署Master节点
6. ✅ 部署Worker节点
7. ✅ 检查集群状态

#### 方式二:手动部署

```bash
# ========== Server1 (Master) ==========
ssh root@10.2.0.14

# 创建目录
mkdir -p /opt/seatunnel-cluster/{config,plugins,logs,jobs}

# 上传文件
# (从本地上传 docker-compose.yml 和 config/* 到服务器)

# 启动服务
cd /opt/seatunnel-cluster
docker-compose up -d seatunnel-master zookeeper hazelcast

# 查看日志
docker-compose logs -f seatunnel-master


# ========== Server2 (Worker-1) ==========
ssh root@10.2.0.15

mkdir -p /opt/seatunnel-cluster/{config,plugins,logs}
cd /opt/seatunnel-cluster

docker-compose up -d seatunnel-worker-1

docker-compose logs -f seatunnel-worker-1


# ========== Server3 (Worker-2) ==========
ssh root@10.2.0.16

mkdir -p /opt/seatunnel-cluster/{config,plugins,logs}
cd /opt/seatunnel-cluster

docker-compose up -d seatunnel-worker-2

docker-compose logs -f seatunnel-worker-2
```

### 3.3 验证部署

#### 1. 检查容器状态

```bash
# Server1
ssh root@10.2.0.14 'docker ps'

# 应该看到:
# - seatunnel-master
# - seatunnel-zookeeper
# - seatunnel-hazelcast


# Server2
ssh root@10.2.0.15 'docker ps'

# 应该看到:
# - seatunnel-worker-1


# Server3
ssh root@10.2.0.16 'docker ps'

# 应该看到:
# - seatunnel-worker-2
```

#### 2. 访问Web UI

```
浏览器访问: http://10.2.0.14:5801
```

应该能看到SeaTunnel管理界面,显示1个Master和2个Worker节点

#### 3. 测试REST API

```bash
# 检查集群状态
curl http://10.2.0.14:8080/hazelcast/rest/cluster

# 应该返回:
{
  "status": "ACTIVE",
  "members": [
    {"address": "seatunnel-master:5801"},
    {"address": "seatunnel-worker-1:5801"},
    {"address": "seatunnel-worker-2:5801"}
  ]
}
```

---

## 四、配置说明

### 4.1 SeaTunnel配置

#### hazelcast.yaml
```yaml
hazelcast:
  cluster-name: seatunnel-cluster
  network:
    port:
      port: 5801
    join:
      tcp-ip:
        enabled: true
        member-list:
          - seatunnel-master:5801
          - seatunnel-worker-1:5801
          - seatunnel-worker-2:5801
```

### 4.2 后端配置

#### application.yml
```yaml
# SeaTunnel配置
seatunnel:
  master:
    host: 10.2.0.14
    port: 8080
  cluster:
    enabled: true

# Quartz配置
spring:
  quartz:
    job-store-type: jdbc
    jdbc:
      initialize-schema: always
    properties:
      org.quartz.scheduler.instanceName: DataHubScheduler
      org.quartz.scheduler.instanceId: AUTO
      org.quartz.threadPool.threadCount: 10
```

### 4.3 数据库配置

#### Quartz表结构(自动创建)
```sql
-- Quartz会自动创建以下表:
- qrtz_job_details
- qrtz_triggers
- qrtz_cron_triggers
- qrtz_fired_triggers
- qrtz_locks
```

---

## 五、使用指南

### 5.1 创建同步任务

#### 前端操作

1. 登录DataHub系统
2. 进入"数据库同步"模块
3. 点击"新建任务"
4. 配置数据源:
   - 选择源数据库
   - 选择目标数据库
   - 选择要同步的表
5. 配置字段映射(使用可视化映射界面)
6. 配置调度:
   - 手动触发
   - Cron表达式(如: `0 0 2 * * ?` 每天凌晨2点)
7. 提交任务

#### 后端处理流程

```java
// 1. SyncTaskController接收请求
@PostMapping
public Result<SyncTask> create(@RequestBody SyncTask syncTask) {
    return Result.success(syncTaskService.create(syncTask));
}

// 2. SyncTaskServiceImpl保存任务并创建调度
public SyncTask create(SyncTask syncTask) {
    // 保存任务
    syncTaskMapper.insert(syncTask);
    
    // 如果有cron表达式,创建定时任务
    if (syncTask.getCronExpression() != null) {
        scheduleService.createJob(syncTask);
    }
    
    return syncTask;
}

// 3. ScheduleService创建Quartz任务
public void createJob(SyncTask syncTask) {
    JobDetail job = JobBuilder.newJob(SyncTaskJob.class)
        .withIdentity("job_" + syncTask.getId())
        .usingJobData("taskId", syncTask.getId())
        .build();
    
    CronTrigger trigger = TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule(
            syncTask.getCronExpression()))
        .build();
    
    scheduler.scheduleJob(job, trigger);
}

// 4. 定时触发 → SyncTaskJob执行
public void execute(JobExecutionContext context) {
    Long taskId = context.getJobDataMap().getLong("taskId");
    syncTaskService.execute(taskId);
}

// 5. SeaTunnelService生成配置并提交
public String submitJob(SyncTask syncTask) {
    String jobConfig = generateJobConfig(syncTask);
    return sendPostRequest(masterUrl + "/submit-job", jobConfig);
}
```

### 5.2 监控任务执行

#### 实时查看进度

```java
// 前端每3秒轮询一次
setInterval(async () => {
  const task = await syncApi.getById(taskId)
  progress.value = task.progress
  status.value = task.status
}, 3000)

// 后端从SeaTunnel获取进度
public Integer getJobProgress(String jobId) {
    String metrics = sendGetRequest(
        masterUrl + "/job-metrics/" + jobId);
    JSONObject result = JSON.parseObject(metrics);
    
    Long writeCount = result.getLong("SinkWriteCount");
    Long totalCount = result.getLong("SourceReceivedCount");
    
    return (int) ((writeCount * 100) / totalCount);
}
```

#### 查看任务日志

```bash
# Master节点日志
ssh root@10.2.0.14 'docker logs -f seatunnel-master'

# Worker节点日志
ssh root@10.2.0.15 'docker logs -f seatunnel-worker-1'
ssh root@10.2.0.16 'docker logs -f seatunnel-worker-2'

# 应用日志
tail -f /opt/seatunnel-cluster/logs/seatunnel-master.log
```

---

## 六、监控运维

### 6.1 健康检查

```bash
# 检查容器健康状态
docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'

# 检查SeaTunnel集群
curl http://10.2.0.14:8080/hazelcast/rest/cluster

# 检查Quartz调度器
curl http://localhost:8080/api/scheduler/status
```

### 6.2 常用运维命令

#### 重启服务

```bash
# 重启Master
ssh root@10.2.0.14 'cd /opt/seatunnel-cluster && docker-compose restart seatunnel-master'

# 重启Worker
ssh root@10.2.0.15 'cd /opt/seatunnel-cluster && docker-compose restart seatunnel-worker-1'
ssh root@10.2.0.16 'cd /opt/seatunnel-cluster && docker-compose restart seatunnel-worker-2'

# 重启所有
./deploy.sh restart
```

#### 查看资源使用

```bash
# CPU和内存
docker stats

# 磁盘空间
df -h /opt/seatunnel-cluster

# 网络流量
iftop
```

### 6.3 故障排查

#### 问题1: Master节点无法启动

```bash
# 检查端口占用
netstat -tunlp | grep 5801
netstat -tunlp | grep 8080

# 查看详细日志
docker logs seatunnel-master

# 检查配置文件
cat /opt/seatunnel-cluster/config/hazelcast.yaml
```

#### 问题2: Worker节点无法连接Master

```bash
# 检查网络连通性
ping seatunnel-master

# 检查Master是否正常
curl http://seatunnel-master:5801/health

# 重启Worker
docker-compose restart seatunnel-worker-1
```

#### 问题3: 任务执行失败

```bash
# 查看任务日志
docker logs seatunnel-worker-1 | grep ERROR

# 检查数据库连接
docker exec -it seatunnel-worker-1 bash
mysql -h 10.2.0.15 -u root -p

# 查看SeaTunnel配置
cat /opt/seatunnel-cluster/jobs/job_xxx.conf
```

---

## 七、性能优化

### 7.1 调整并行度

```yaml
# 修改SeaTunnel配置
env {
  execution.parallelism = 4  # 增加并行度
  job.mode = "BATCH"
}
```

### 7.2 调整批次大小

```yaml
sink {
  Jdbc {
    batch_size = 5000  # 增加批次大小
    batch_interval_ms = 1000
  }
}
```

### 7.3 增加Worker节点

```bash
# 添加新的Worker节点
docker-compose up -d seatunnel-worker-3
```

---

## 八、安全配置

### 8.1 开启认证

```yaml
# hazelcast.yaml
security:
  enabled: true
  client-authentication:
    type: simple
    username: admin
    password: your_password
```

### 8.2 网络隔离

```bash
# 使用Docker网络隔离
docker network create --driver bridge seatunnel-net
```

### 8.3 数据加密

```yaml
# 传输加密
network:
  ssl:
    enabled: true
    factory-class-name: com.hazelcast.nio.ssl.BasicSSLContextFactory
```

---

## 九、总结

### ✅ 已完成
1. ✅ SeaTunnel集群Docker化部署
2. ✅ Quartz定时调度集成
3. ✅ 后端服务API实现
4. ✅ 前端可视化字段映射
5. ✅ 自动化部署脚本
6. ✅ 完整的监控和日志

### 🚀 下一步
1. 添加更多数据源支持(Hive, ClickHouse, Kafka等)
2. 实现增量同步功能
3. 添加数据质量校验
4. 集成Prometheus监控
5. 添加任务失败重试机制

---

**文档版本**: v1.0  
**更新时间**: 2025-01-15  
**作者**: DataHub Team
