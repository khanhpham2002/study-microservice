# ==============================================================================
# STAGE 1: Build ReactJS Frontend với Node.js
# ==============================================================================
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build

# ==============================================================================
# STAGE 2: Build Spring Boot Backend với Maven (Nhúng kèm Frontend vào static/)
# ==============================================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS backend-build
WORKDIR /app/backend

COPY backend/pom.xml ./
COPY backend/src ./src

# Copy sản phẩm build của React (thư mục dist/) vào static/ của Spring Boot
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

RUN mvn clean package -DskipTests

# ==============================================================================
# STAGE 3: Image Runtime siêu nhẹ chỉ chạy file JAR cuối cùng
# ==============================================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=backend-build /app/backend/target/*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]
