#!/bin/bash

###############################################################################
# AllData数据中台 - 数据库服务器部署脚本
# 服务器: lhins-qwpb0we8 (58.87.103.99)
# 用途: 部署MySQL、PostgreSQL、Redis、Zookeeper
###############################################################################

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

SERVER="alldata-db"
SERVER_IP="58.87.103.99"

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}部署数据库服务器${NC}"
echo -e "${GREEN}服务器: ${SERVER_IP}${NC}"
echo -e "${GREEN}================================${NC}"
echo ""

# 1. 上传配置文件
echo -e "${YELLOW}[1/5] 上传配置文件...${NC}"
scp -r server2-db/* ${SERVER}:/opt/alldata/

# 2. 创建MySQL配置
echo -e "${YELLOW}[2/5] 创建MySQL配置...${NC}"
ssh ${SERVER} 'cat > /opt/alldata/config/mysql.cnf <<EOF
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
max_connections=500
max_allowed_packet=256M
innodb_buffer_pool_size=2G
innodb_log_file_size=256M
innodb_flush_log_at_trx_commit=2
innodb_flush_method=O_DIRECT
slow_query_log=1
slow_query_log_file=/var/log/mysql/slow.log
long_query_time=2

[client]
default-character-set=utf8mb4
EOF'

# 3. 配置防火墙（只允许内网访问）
echo -e "${YELLOW}[3/5] 配置防火墙...${NC}"
ssh ${SERVER} <<'ENDSSH'
# 允许应用服务器访问MySQL
firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.2.4.15" port port="3306" protocol="tcp" accept'
# 允许大数据服务器访问MySQL
firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.2.4.9" port port="3306" protocol="tcp" accept'
# 允许内网访问PostgreSQL
firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.2.0.0/16" port port="5432" protocol="tcp" accept'
# 允许内网访问Redis
firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.2.0.0/16" port port="6379" protocol="tcp" accept'
# 允许内网访问Zookeeper
firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.2.0.0/16" port port="2181" protocol="tcp" accept'
firewall-cmd --reload
ENDSSH

# 4. 启动服务
echo -e "${YELLOW}[4/5] 启动数据库服务...${NC}"
ssh ${SERVER} 'cd /opt/alldata && docker-compose up -d'

# 5. 等待服务启动
echo -e "${YELLOW}[5/5] 等待服务启动...${NC}"
sleep 30

# 检查服务状态
echo ""
echo "检查服务状态..."
ssh ${SERVER} 'docker-compose ps'

# 初始化数据库
echo ""
echo -e "${YELLOW}初始化数据库...${NC}"
ssh ${SERVER} <<'ENDSSH'
# 等待MySQL启动
sleep 10

# 创建数据库和用户
docker exec alldata-mysql mysql -uroot -pAllData@2025 <<EOF
CREATE DATABASE IF NOT EXISTS chat2db DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS dbswitch DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS datacap DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS transport DEFAULT CHARACTER SET utf8mb4;

CREATE USER IF NOT EXISTS 'alldata'@'%' IDENTIFIED BY 'AllData@2025';
GRANT ALL PRIVILEGES ON *.* TO 'alldata'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

SELECT user, host FROM mysql.user;
SHOW DATABASES;
EOF

echo "MySQL初始化完成"

# PostgreSQL初始化
docker exec alldata-postgresql psql -U postgres <<EOF
CREATE DATABASE chat2db;
CREATE DATABASE dbswitch;
CREATE DATABASE datacap;
\l
EOF

echo "PostgreSQL初始化完成"
ENDSSH

echo ""
echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}数据库服务器部署完成！${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "服务信息:"
echo "  MySQL: 10.2.0.16:3306"
echo "  PostgreSQL: 10.2.0.16:5432"
echo "  Redis: 10.2.0.16:6379"
echo "  Zookeeper: 10.2.0.16:2181"
echo ""
echo "默认密码: AllData@2025"
echo ""
echo "下一步: ./deploy-app.sh"
echo ""

