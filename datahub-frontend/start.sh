#!/bin/bash

# AllData数据中台前端 - 快速启动脚本

echo "======================================"
echo "  AllData数据中台前端 - 启动脚本"
echo "======================================"
echo ""

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo "❌ 错误: 未检测到Node.js，请先安装Node.js (>= 16.x)"
    exit 1
fi

echo "✅ Node.js版本: $(node -v)"
echo "✅ npm版本: $(npm -v)"
echo ""

# 检查是否已安装依赖
if [ ! -d "node_modules" ]; then
    echo "📦 检测到未安装依赖，开始安装..."
    echo ""
    
    # 询问是否使用国内镜像
    read -p "是否使用国内镜像加速? (y/n): " use_mirror
    
    if [ "$use_mirror" = "y" ] || [ "$use_mirror" = "Y" ]; then
        echo "使用淘宝镜像安装..."
        npm install --registry=https://registry.npmmirror.com
    else
        echo "使用默认镜像安装..."
        npm install
    fi
    
    if [ $? -ne 0 ]; then
        echo "❌ 依赖安装失败，请检查网络连接或手动执行: npm install"
        exit 1
    fi
    
    echo ""
    echo "✅ 依赖安装成功！"
    echo ""
else
    echo "✅ 依赖已安装"
    echo ""
fi

# 启动开发服务器
echo "🚀 正在启动开发服务器..."
echo ""
echo "访问地址: http://localhost:5173"
echo "默认账号: admin / admin123"
echo ""
echo "按 Ctrl+C 停止服务器"
echo "======================================"
echo ""

npm run dev

