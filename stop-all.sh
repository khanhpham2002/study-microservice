#!/usr/bin/env bash

# ==============================================================================
# Script dừng toàn bộ hệ sinh thái Microservices & giải phóng RAM
# ==============================================================================

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m' # No Color

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="$BASE_DIR/.service_pids"

echo -e "${BOLD}${YELLOW}====================================================================${NC}"
echo -e "${BOLD}${YELLOW}   🛑 DỪNG TOÀN BỘ HỆ THỐNG MICROSERVICES & GIẢI PHÓNG TÀI NGUYÊN   ${NC}"
echo -e "${BOLD}${YELLOW}====================================================================${NC}\n"

# 1. Dừng các tiến trình Java từ file PID nếu có
if [ -f "$PID_FILE" ]; then
    echo -e "${BLUE}[1/3] Đang tắt các Microservice Java (từ PID file)...${NC}"
    while IFS= read -r pid; do
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            echo -e "   Tắt process PID: $pid"
            kill "$pid" 2>/dev/null || true
        fi
    done < "$PID_FILE"
    rm -f "$PID_FILE"
fi

# 2. Quét dọn triệt để các tiến trình đang chiếm port của 5 microservices
echo -e "${BLUE}[2/3] Quét sạch các tiến trình còn sót lại trên ports (8761, 8080, 8081, 8082, 8084)...${NC}"
PORTS=(8761 8080 8081 8082 8084)
for port in "${PORTS[@]}"; do
    PIDS=$(lsof -ti :$port 2>/dev/null || true)
    if [ -n "$PIDS" ]; then
        echo -e "   Phát hiện process đang giữ port $port: $PIDS ➡️ Đang force kill..."
        echo "$PIDS" | xargs kill -9 2>/dev/null || true
    fi
done
echo -e "${GREEN}   ✔ Tất cả 5 Microservices Java đã được dừng hoàn toàn!${NC}\n"

# 3. Dừng các container Docker
echo -e "${BLUE}[3/3] Dừng các container Docker (Kafka, Redis, Kafka UI)...${NC}"
docker compose stop > /dev/null 2>&1
echo -e "${GREEN}   ✔ Đã tạm dừng các container Docker.${NC}\n"

echo -e "${BOLD}${GREEN}====================================================================${NC}"
echo -e "${BOLD}${GREEN}   ✅ HOÀN TẤT! TOÀN BỘ RAM VÀ PORT ĐÃ ĐƯỢC GIẢI PHÓNG SẠCH SẼ.     ${NC}"
echo -e "${BOLD}${GREEN}====================================================================${NC}\n"
