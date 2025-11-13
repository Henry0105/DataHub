# 🚀 AllData数据中台 - 部署指南

## 📋 服务器信息

### 服务器1 - 应用服务器
```
实例ID: lhins-otsq8f9m
公网IP: 82.156.36.157
内网IP: 10.2.4.15
配置: 4核8GB + 120GB SSD
用途: 前端 + 后端服务
```

### 服务器2 - 数据库服务器
```
实例ID: lhins-qwpb0we8
公网IP: 58.87.103.99
内网IP: 10.2.0.16
配置: 4核8GB + 120GB SSD
用途: MySQL + PostgreSQL + Redis
```

### 服务器3 - 大数据服务器
```
实例ID: lhins-b3sguy1u
公网IP: 192.144.173.136
内网IP: 10.2.4.9
配置: 4核8GB + 120GB SSD
用途: 数据集成 + 消息队列
```

---

## 🚀 快速开始

### 第1步：配置本地SSH

```bash
# 创建SSH配置文件
cat > ~/.ssh/config <<EOF
Host alldata-app
    HostName 82.156.36.157
    User root
    IdentityFile ~/.ssh/lhkp-5k5yya5o.pem
    ServerAliveInterval 60

Host alldata-db
    HostName 58.87.103.99
    User root
    IdentityFile ~/.ssh/lhkp-5k5yya5o.pem
    ServerAliveInterval 60

Host alldata-bigdata
    HostName 192.144.173.136
    User root
    IdentityFile ~/.ssh/lhkp-5k5yya5o.pem
    ServerAliveInterval 60
EOF

# 修改私钥权限
chmod 400 ~/.ssh/lhkp-5k5yya5o.pem

# 测试连接
ssh alldata-app "echo '应用服务器连接成功'"
ssh alldata-db "echo '数据库服务器连接成功'"
ssh alldata-bigdata "echo '大数据服务器连接成功'"
```

### 第2步：初始化所有服务器

```bash
# 在本地执行
cd /Users/liumingze/DataHub/deployment

# 一键初始化所有服务器
./init-all-servers.sh
```

### 第3步：部署服务

```bash
# 部署应用服务器
./deploy-app.sh

# 部署数据库服务器
./deploy-db.sh

# 部署大数据服务器
./deploy-bigdata.sh
```

### 第4步：访问系统

```
前端地址: http://82.156.36.157
后端API: http://82.156.36.157:8080
Chat2DB: http://82.156.36.157:10824

默认账号: admin
默认密码: admin123
```

---

## 📂 文件说明

```
deployment/
├── README.md                    # 本文件
├── init-all-servers.sh          # 初始化所有服务器
├── deploy-app.sh                # 部署应用服务器
├── deploy-db.sh                 # 部署数据库服务器
├── deploy-bigdata.sh            # 部署大数据服务器
├── backup-to-cos.sh             # 备份到COS
├── server1-app/                 # 应用服务器配置
│   ├── docker-compose.yml
│   ├── nginx.conf
│   └── init.sh
├── server2-db/                  # 数据库服务器配置
│   ├── docker-compose.yml
│   ├── mysql.cnf
│   ├── postgresql.conf
│   └── init.sh
├── server3-bigdata/             # 大数据服务器配置
│   ├── docker-compose.yml
│   └── init.sh
└── monitoring/                  # 监控配置
    ├── prometheus.yml
    └── grafana-dashboard.json
```

---

## 🔧 详细部署步骤

### 阶段1：服务器初始化（10分钟）

每台服务器都需要执行：
- 系统更新
- 安装Docker和Docker Compose
- 配置防火墙
- 优化内核参数
- 配置Swap
- 配置时间同步

### 阶段2：部署数据库（15分钟）

在服务器2上部署：
- MySQL 8.0
- PostgreSQL 14
- Redis 6.x
- Zookeeper

### 阶段3：部署应用（20分钟）

在服务器1上部署：
- Nginx（前端）
- 后端API
- Chat2DB
- DBSwitch
- DataCap

### 阶段4：部署数据处理（15分钟）

在服务器3上部署：
- Kafka
- DataX/Transport
- TIS
- SeaTunnel

---

## 📊 服务端口规划

### 服务器1 - 应用服务器
```
80     - Nginx (前端)
443    - Nginx (HTTPS)
8080   - 后端API
10824  - Chat2DB
9088   - DBSwitch
9999   - DataCap
```

### 服务器2 - 数据库服务器
```
3306   - MySQL
5432   - PostgreSQL
6379   - Redis
2181   - Zookeeper
```

### 服务器3 - 大数据服务器
```
9092   - Kafka
8888   - DataX Web
8080   - TIS
8081   - SeaTunnel
```

---

## 🔒 安全配置

### 防火墙规则

**服务器1（应用服务器）**
```bash
# 开放端口
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --permanent --add-port=8080/tcp
firewall-cmd --permanent --add-port=10824/tcp
firewall-cmd --reload
```

**服务器2（数据库服务器）**
```bash
# 只允许内网访问
firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.2.4.15" port port="3306" protocol="tcp" accept'
firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.2.4.9" port port="3306" protocol="tcp" accept'
firewall-cmd --reload
```

**服务器3（大数据服务器）**
```bash
# 内网互通
firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.2.0.0/16" accept'
firewall-cmd --reload
```

---

## 💾 备份策略

### 自动备份到COS

```bash
# 每天凌晨2点自动备份
crontab -e

# 添加定时任务
0 2 * * * /root/deployment/backup-to-cos.sh
```

### 备份内容
- MySQL数据库
- PostgreSQL数据库
- Redis数据
- 配置文件
- 应用日志

---

## 📈 监控配置

### Prometheus + Grafana

访问地址：
```
Grafana: http://82.156.36.157:3000
默认账号: admin
默认密码: admin
```

监控指标：
- CPU使用率
- 内存使用率
- 磁盘使用率
- 网络流量
- 服务状态
- 数据库连接数

---

## 🆘 故障排查

### 常见问题

**1. 无法连接服务器**
```bash
# 检查SSH密钥权限
ls -la ~/.ssh/lhkp-5k5yya5o.pem
# 应该显示: -r-------- (400)

# 如果不对，修改权限
chmod 400 ~/.ssh/lhkp-5k5yya5o.pem
```

**2. Docker服务未启动**
```bash
ssh alldata-app
systemctl status docker
systemctl start docker
systemctl enable docker
```

**3. 容器无法启动**
```bash
# 查看容器日志
docker logs <container_name>

# 查看容器状态
docker ps -a

# 重启容器
docker-compose restart
```

**4. 内存不足**
```bash
# 查看内存使用
free -h

# 查看Swap
swapon --show

# 如果没有Swap，创建
./create-swap.sh
```

---

## 📞 技术支持

如有问题，请查看：
- 部署日志：`/var/log/alldata/`
- Docker日志：`docker logs <container>`
- 系统日志：`journalctl -xe`

---

**部署时间**：2025-01-15  
**版本**：v1.0  
**状态**：准备就绪 ✅

