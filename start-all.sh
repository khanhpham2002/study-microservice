#!/usr/bin/env bash

# ==============================================================================
# Script khởi chạy toàn bộ hệ sinh thái Microservices (Hạ tầng + 5 Service)
# ==============================================================================

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m' # No Color

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$BASE_DIR/logs"
PID_FILE="$BASE_DIR/.service_pids"

mkdir -p "$LOG_DIR"

echo -e "${BOLD}${CYAN}====================================================================${NC}"
echo -e "${BOLD}${CYAN}   🚀 KHỞI CHẠY HỆ THỐNG MICROSERVICES (SPRING CLOUD + KAFKA + REDIS)  ${NC}"
echo -e "${BOLD}${CYAN}====================================================================${NC}\n"

# 1. Kiểm tra Docker
echo -e "${BLUE}[1/4] Kiểm tra hạ tầng Docker...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker chưa được bật! Vui lòng khởi động Docker Desktop trước.${NC}"
    exit 1
fi

echo -e "   Đang khởi động Redis, Kafka (KRaft), Kafka UI..."
docker compose up -d > /dev/null 2>&1
echo -e "${GREEN}   ✔ Hạ tầng Docker đã sẵn sàng!${NC}\n"

# 2. Kiểm tra file JAR đóng gói
echo -e "${BLUE}[2/4] Kiểm tra các bản build JAR...${NC}"
SERVICES=("discovery-service" "api-gateway" "product-service" "order-service" "notification-service")
NEED_BUILD=false

for s in "${SERVICES[@]}"; do
    if [ ! -f "$BASE_DIR/$s/target/$s-1.0.0-SNAPSHOT.jar" ]; then
        NEED_BUILD=true
        break
    fi
done

if [ "$NEED_BUILD" = true ]; then
    echo -e "${YELLOW}   Chưa tìm thấy một số file JAR, đang biên dịch nhanh (mvn package -DskipTests)...${NC}"
    mvn package -DskipTests -q
    echo -e "${GREEN}   ✔ Đã build xong toàn bộ JAR!${NC}\n"
else
    echo -e "${GREEN}   ✔ Đã có sẵn file JAR cho tất cả 5 services.${NC}\n"
fi

# Xóa PID file cũ và dọn sạch các tiến trình cũ nếu còn chạy trên port
rm -f "$PID_FILE"
PORTS=(8761 8080 8081 8082 8084)
for port in "${PORTS[@]}"; do
    PIDS=$(lsof -ti :$port 2>/dev/null || true)
    if [ -n "$PIDS" ]; then
        echo "$PIDS" | xargs kill -9 2>/dev/null || true
    fi
done

# Hàm khởi động 1 microservice bằng JAR (tiết kiệm RAM, khởi động cực nhanh)
start_service() {
    local name=$1
    local jar="$BASE_DIR/$name/target/$name-1.0.0-SNAPSHOT.jar"
    local log="$LOG_DIR/$name.log"

    echo -ne "   Starting ${BOLD}$name${NC}..."
    nohup java -jar "$jar" > "$log" 2>&1 &
    local pid=$!
    echo "$pid" >> "$PID_FILE"
    echo -e " ${GREEN}[PID: $pid]${NC} (Log: logs/$name.log)"
}

# 3. Khởi động Discovery Service (Eureka) trước tiên
echo -e "${BLUE}[3/4] Khởi chạy Eureka Server (Service Registry)...${NC}"
start_service "discovery-service"

echo -ne "   Đang đợi Eureka Server sẵn sàng tại port 8761"
for i in {1..30}; do
    if lsof -i :8761 > /dev/null 2>&1 && curl -s http://localhost:8761 > /dev/null 2>&1; then
        echo -e " ${GREEN}OK!${NC}\n"
        break
    fi
    sleep 1
    echo -ne "."
done

# 4. Khởi động các Microservice còn lại
echo -e "${BLUE}[4/4] Khởi chạy các Microservices nghiệp vụ & Gateway...${NC}"
start_service "product-service"
start_service "order-service"
start_service "notification-service"
start_service "api-gateway"

echo -e "\n${BOLD}${GREEN}====================================================================${NC}"
echo -e "${BOLD}${GREEN}   ✅ TOÀN BỘ HỆ THỐNG ĐÃ ĐƯỢC KHỞI CHẠY THÀNH CÔNG!                ${NC}"
echo -e "${BOLD}${GREEN}====================================================================${NC}\n"

echo -e "${BOLD}📌 CÁC ĐƯỜNG DẪN TRUY CẬP TRỰC QUAN:${NC}"
echo -e "   - ${CYAN}Kafka UI Dashboard:${NC}    http://localhost:8085"
echo -e "   - ${CYAN}Eureka Server Dashboard:${NC} http://localhost:8761"
echo -e "   - ${CYAN}API Gateway (Entrypoint):${NC}http://localhost:8080"
echo -e "   - ${CYAN}Product Service direct:${NC}  http://localhost:8081/api/products"
echo -e "   - ${CYAN}Order Service direct:${NC}    http://localhost:8082/api/orders"
echo -e "\n${BOLD}📁 THEO DÕI LOG:${NC}"
echo -e "   - Xem log của service bất kỳ:  ${YELLOW}tail -f logs/<tên-service>.log${NC}"
echo -e "   - Ví dụ xem log gửi email:     ${YELLOW}tail -f logs/notification-service.log${NC}"
echo -e "\n${BOLD}🛑 ĐỂ DỪNG TOÀN BỘ HỆ THỐNG:${NC}"
echo -e "   - Chạy lệnh:                   ${YELLOW}./stop-all.sh${NC}\n"
