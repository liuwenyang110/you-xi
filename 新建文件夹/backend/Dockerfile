# ================================================
# 农助手后端 Dockerfile (多阶段构建)
# ================================================

# --- 阶段1: 构建 ---
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# --- 阶段2: 运行 ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 添加非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/target/nong-helper-*.jar app.jar

# 安全：以非 root 用户运行
USER appuser

# JVM 优化参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
