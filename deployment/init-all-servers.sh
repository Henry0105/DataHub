#!/bin/bash

###############################################################################
# AllData数据中台 - 服务器初始化脚本
# 用途: 初始化所有3台服务器
# 作者: AllData团队
# 日期: 2025-01-15
###############################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 服务器列表
SERVERS=(
    "alldata-app|82.156.36.157|10.2.4.15|应用服务器"
    "alldata-db|58.87.103.99|10.2.0.16|数据库服务器"
    "alldata-bigdata|192.144.173.136|10.2.4.9|大数据服务器"
)

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}AllData数据中台 - 服务器初始化${NC}"
echo -e "${GREEN}================================${NC}"
echo ""

# 初始化单台服务器的函数
init_server() {
    local server_alias=$1
    local server_ip=$2
    local server_desc=$3
    
    echo -e "${YELLOW}正在初始化: ${server_desc} (${server_ip})${NC}"
    
    # 上传初始化脚本
    ssh ${server_alias} 'bash -s' <<'ENDSSH'
#!/bin/bash

set -e

echo "========================================="
echo "开始初始化服务器"
echo "========================================="

# 1. 系统更新
echo "[1/8] 更新系统..."
yum update -y

# 2. 安装基础工具
echo "[2/8] 安装基础工具..."
yum install -y wget curl vim git net-tools telnet

# 3. 安装Docker
echo "[3/8] 安装Docker..."
if ! command -v docker &> /dev/null; then
    yum install -y yum-utils
    yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
    yum install -y docker-ce docker-ce-cli containerd.io
    systemctl start docker
    systemctl enable docker
    
    # 配置Docker镜像加速
    mkdir -p /etc/docker
    cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://docker.mirrors.ustc.edu.cn"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
EOF
    systemctl daemon-reload
    systemctl restart docker
else
    echo "Docker已安装"
fi

# 4. 安装Docker Compose
echo "[4/8] 安装Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose
else
    echo "Docker Compose已安装"
fi

# 5. 配置防火墙
echo "[5/8] 配置防火墙..."
systemctl start firewalld
systemctl enable firewalld

# 6. 配置Swap（4GB）
echo "[6/8] 配置Swap..."
if [ ! -f /swapfile ]; then
    fallocate -l 4G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
    
    # 优化Swap使用
    echo 'vm.swappiness=10' >> /etc/sysctl.conf
    echo 'vm.vfs_cache_pressure=50' >> /etc/sysctl.conf
    sysctl -p
else
    echo "Swap已配置"
fi

# 7. 优化内核参数
echo "[7/8] 优化内核参数..."
cat >> /etc/sysctl.conf <<EOF

# AllData优化参数
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_fin_timeout = 30
net.ipv4.tcp_keepalive_time = 1200
net.ipv4.ip_local_port_range = 10000 65000
net.ipv4.tcp_max_syn_backlog = 8192
net.ipv4.tcp_max_tw_buckets = 5000
net.core.somaxconn = 1024
fs.file-max = 65535
EOF
sysctl -p

# 8. 配置时间同步
echo "[8/8] 配置时间同步..."
yum install -y ntpdate
ntpdate ntp.aliyun.com
echo "0 */1 * * * /usr/sbin/ntpdate ntp.aliyun.com > /dev/null 2>&1" | crontab -

# 创建工作目录
mkdir -p /opt/alldata/{data,logs,config,backup}

echo "========================================="
echo "服务器初始化完成！"
echo "========================================="

# 显示系统信息
echo ""
echo "系统信息:"
echo "  主机名: $(hostname)"
echo "  内核版本: $(uname -r)"
echo "  Docker版本: $(docker --version)"
echo "  Docker Compose版本: $(docker-compose --version)"
echo "  内存: $(free -h | grep Mem | awk '{print $2}')"
echo "  Swap: $(free -h | grep Swap | awk '{print $2}')"
echo "  磁盘: $(df -h / | tail -1 | awk '{print $2}')"
echo ""

ENDSSH

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ ${server_desc} 初始化成功${NC}"
    else
        echo -e "${RED}✗ ${server_desc} 初始化失败${NC}"
        return 1
    fi
    
    echo ""
}

# 检查SSH连接
echo "检查服务器连接..."
for server in "${SERVERS[@]}"; do
    IFS='|' read -r alias ip internal desc <<< "$server"
    if ssh -o ConnectTimeout=5 ${alias} "echo '连接成功'" &> /dev/null; then
        echo -e "${GREEN}✓ ${desc} (${ip}) 连接正常${NC}"
    else
        echo -e "${RED}✗ ${desc} (${ip}) 连接失败${NC}"
        echo -e "${RED}请检查SSH配置和网络连接${NC}"
        exit 1
    fi
done

echo ""
read -p "确认开始初始化所有服务器？(y/n) " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "已取消"
    exit 0
fi

echo ""
echo "开始初始化..."
echo ""

# 初始化所有服务器
for server in "${SERVERS[@]}"; do
    IFS='|' read -r alias ip internal desc <<< "$server"
    init_server "${alias}" "${ip}" "${desc}"
done

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}所有服务器初始化完成！${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "下一步："
echo "  1. 部署数据库服务器: ./deploy-db.sh"
echo "  2. 部署应用服务器: ./deploy-app.sh"
echo "  3. 部署大数据服务器: ./deploy-bigdata.sh"
echo ""

