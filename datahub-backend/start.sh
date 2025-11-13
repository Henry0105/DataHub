#!/bin/bash

echo "======================================"
echo "  DataHub 后端服务启动脚本"
echo "======================================"

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 未检测到Java环境，请先安装JDK 11+"
    exit 1
fi

echo "✓ Java版本:"
java -version

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "❌ 未检测到Maven环境，请先安装Maven 3.6+"
    exit 1
fi

echo ""
echo "✓ Maven版本:"
mvn -version | head -n 1

echo ""
echo "======================================"
echo "  开始编译项目..."
echo "======================================"

mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ 编译失败，请检查错误信息"
    exit 1
fi

echo ""
echo "======================================"
echo "  启动后端服务..."
echo "======================================"

java -jar target/datahub-backend.jar

