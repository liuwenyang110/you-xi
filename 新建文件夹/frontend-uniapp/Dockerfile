# ================================================
# 农助手前端 Dockerfile (H5 构建 + Nginx 部署)
# ================================================

# --- 阶段1: 构建 ---
FROM node:18-alpine AS builder
WORKDIR /build
COPY package*.json ./
RUN npm ci --production=false
COPY . .
RUN npm run build:h5

# --- 阶段2: Nginx 部署 ---
FROM nginx:alpine
COPY --from=builder /build/dist/build/h5 /usr/share/nginx/html

# 自定义 Nginx 配置（支持 Vue Router history 模式）
RUN echo 'server { \
    listen 80; \
    server_name _; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
    location /api/ { \
        proxy_pass http://backend:8080; \
        proxy_set_header Host $host; \
        proxy_set_header X-Real-IP $remote_addr; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost/ || exit 1
