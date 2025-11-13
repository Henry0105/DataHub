#!/bin/bash

# ===========================================
# SeaTunnel 集群部署脚本
# 用于在三台Docker服务器上部署SeaTunnel
# ===========================================

set -e

echo "=========================================="
echo "   SeaTunnel 集群部署脚本"
echo "=========================================="

# 服务器配置
SERVER1="datahub-app"      # 应用服务器 - Master
SERVER2="datahub-db"       # 数据库服务器 - Worker1  
SERVER3="datahub-bigdata"  # 大数据服务器 - Worker2

SSH_USER="root"
SSH_PORT="22"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查SSH连接
check_ssh() {
    local server=$1
    echo -e "${YELLOW}检查SSH连接: ${server}${NC}"
    if ssh -p ${SSH_PORT} ${SSH_USER}@${server} "echo 'SSH连接成功'" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ ${server} SSH连接正常${NC}"
        return 0
    else
        echo -e "${RED}✗ ${server} SSH连接失败${NC}"
        return 1
    fi
}

# 安装Docker和Docker Compose
install_docker() {
    local server=$1
    echo -e "${YELLOW}在 ${server} 上安装Docker...${NC}"
    
    ssh -p ${SSH_PORT} ${SSH_USER}@${server} << 'EOF'
        # 检查是否已安装Docker
        if ! command -v docker &> /dev/null; then
            echo "安装Docker..."
            curl -fsSL https://get.docker.com | bash
            systemctl enable docker
            systemctl start docker
        else
            echo "Docker已安装"
        fi
        
        # 检查Docker Compose
        if ! command -v docker-compose &> /dev/null; then
            echo "安装Docker Compose..."
            curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
            chmod +x /usr/local/bin/docker-compose
        else
            echo "Docker Compose已安装"
        fi
        
        docker --version
        docker-compose --version
EOF
    
    echo -e "${GREEN}✓ ${server} Docker安装完成${NC}"
}

# 创建部署目录
create_directories() {
    local server=$1
    echo -e "${YELLOW}在 ${server} 上创建部署目录...${NC}"
    
    ssh -p ${SSH_PORT} ${SSH_USER}@${server} << 'EOF'
        mkdir -p /opt/seatunnel-cluster/{config,plugins,logs,jobs}
        chmod -R 755 /opt/seatunnel-cluster
EOF
    
    echo -e "${GREEN}✓ ${server} 目录创建完成${NC}"
}

# 上传配置文件
upload_configs() {
    local server=$1
    echo -e "${YELLOW}上传配置文件到 ${server}...${NC}"
    
    scp -P ${SSH_PORT} -r ./config/* ${SSH_USER}@${server}:/opt/seatunnel-cluster/config/
    scp -P ${SSH_PORT} ./docker-compose.yml ${SSH_USER}@${server}:/opt/seatunnel-cluster/
    
    echo -e "${GREEN}✓ ${server} 配置文件上传完成${NC}"
}

# 部署Master节点
deploy_master() {
    echo -e "${YELLOW}部署SeaTunnel Master节点到 ${SERVER1}...${NC}"
    
    ssh -p ${SSH_PORT} ${SSH_USER}@${SERVER1} << 'EOF'
        cd /opt/seatunnel-cluster
        
        # 停止旧容器
        docker-compose down 2>/dev/null || true
        
        # 只启动Master和Zookeeper
        docker-compose up -d seatunnel-master zookeeper hazelcast
        
        echo "等待Master节点启动..."
        sleep 30
        
        # 检查容器状态
        docker-compose ps
        
        # 查看日志
        docker-compose logs --tail=50 seatunnel-master
EOF
    
    echo -e "${GREEN}✓ Master节点部署完成${NC}"
}

# 部署Worker节点
deploy_worker() {
    local server=$1
    local worker_name=$2
    echo -e "${YELLOW}部署SeaTunnel ${worker_name}节点到 ${server}...${NC}"
    
    ssh -p ${SSH_PORT} ${SSH_USER}@${server} << EOF
        cd /opt/seatunnel-cluster
        
        # 停止旧容器
        docker-compose down 2>/dev/null || true
        
        # 启动Worker节点
        docker-compose up -d ${worker_name}
        
        echo "等待Worker节点启动..."
        sleep 20
        
        # 检查容器状态
        docker-compose ps
        
        # 查看日志
        docker-compose logs --tail=50 ${worker_name}
EOF
    
    echo -e "${GREEN}✓ ${worker_name}节点部署完成${NC}"
}

# 检查集群状态
check_cluster() {
    echo -e "${YELLOW}检查SeaTunnel集群状态...${NC}"
    
    echo "Master节点状态:"
    ssh -p ${SSH_PORT} ${SSH_USER}@${SERVER1} "docker exec seatunnel-master curl -s http://localhost:5801/hazelcast/rest/cluster" || echo "Master节点未就绪"
    
    echo ""
    echo "集群容器列表:"
    echo "Server1 (Master):"
    ssh -p ${SSH_PORT} ${SSH_USER}@${SERVER1} "docker ps --format 'table {{.Names}}\t{{.Status}}'"
    
    echo ""
    echo "Server2 (Worker1):"
    ssh -p ${SSH_PORT} ${SSH_USER}@${SERVER2} "docker ps --format 'table {{.Names}}\t{{.Status}}'"
    
    echo ""
    echo "Server3 (Worker2):"
    ssh -p ${SSH_PORT} ${SSH_USER}@${SERVER3} "docker ps --format 'table {{.Names}}\t{{.Status}}'"
}

# 主部署流程
main() {
    echo "开始部署SeaTunnel集群..."
    
    # 1. 检查SSH连接
    echo ""
    echo "步骤 1/6: 检查服务器连接..."
    check_ssh ${SERVER1}
    check_ssh ${SERVER2}
    check_ssh ${SERVER3}
    
    # 2. 安装Docker
    echo ""
    echo "步骤 2/6: 安装Docker环境..."
    install_docker ${SERVER1}
    install_docker ${SERVER2}
    install_docker ${SERVER3}
    
    # 3. 创建目录
    echo ""
    echo "步骤 3/6: 创建部署目录..."
    create_directories ${SERVER1}
    create_directories ${SERVER2}
    create_directories ${SERVER3}
    
    # 4. 上传配置
    echo ""
    echo "步骤 4/6: 上传配置文件..."
    upload_configs ${SERVER1}
    upload_configs ${SERVER2}
    upload_configs ${SERVER3}
    
    # 5. 部署节点
    echo ""
    echo "步骤 5/6: 部署SeaTunnel节点..."
    deploy_master
    deploy_worker ${SERVER2} "seatunnel-worker-1"
    deploy_worker ${SERVER3} "seatunnel-worker-2"
    
    # 6. 检查状态
    echo ""
    echo "步骤 6/6: 检查集群状态..."
    sleep 10
    check_cluster
    
    echo ""
    echo -e "${GREEN}=========================================="
    echo "   SeaTunnel 集群部署完成!"
    echo "==========================================${NC}"
    echo ""
    echo "访问地址:"
    echo "  Master Web UI: http://${SERVER1}:5801"
    echo "  REST API: http://${SERVER1}:8080"
    echo ""
    echo "管理命令:"
    echo "  查看日志: ssh ${SSH_USER}@${SERVER1} 'cd /opt/seatunnel-cluster && docker-compose logs -f'"
    echo "  重启集群: ssh ${SSH_USER}@${SERVER1} 'cd /opt/seatunnel-cluster && docker-compose restart'"
    echo "  停止集群: ssh ${SSH_USER}@${SERVER1} 'cd /opt/seatunnel-cluster && docker-compose down'"
}

# 运行主流程
main
