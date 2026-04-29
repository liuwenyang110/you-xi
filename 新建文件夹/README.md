# 🌾 农助手 (Nong Helper)

> 面向中国市场的农业机械便民对接平台 —— 让带着泥巴的拖拉机手在田头顺畅接单

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-blue)](https://vuejs.org/)
[![Uni-app](https://img.shields.io/badge/Uni--app-cross%20platform-purple)](https://uniapp.dcloud.net.cn/)

---

## 📖 项目简介

农助手是一个连接**农户**和**农机主**的信息对接平台，核心解决"找农机难、联系不顺、偏远地区断联"三大痛点。平台只提供信息撮合、联系方式确认和进度留痕，不参与线上收款、结算或资金托管。

### 核心功能
- 🚜 **一键叫车**：滴滴风格地图首页，农户选机型→选田亩→一键呼叫
- 🤖 **智能匹配引擎**：基于距离、设备能力、时间窗口的多维匹配算法
- 📐 **GPS 绕场测亩**：机手到田头后沿边界行走，自动计算真实面积
- 📡 **SMS 离线兜底**：偏远无网地区通过短信指令完成闭环
- 🌦️ **气象灾害调度**：接入和风天气 API，暴雨红色预警时自动切换救灾模式
- 👴 **适老模式**：一键切换大字体、高对比度界面
- 🏛️ **政府监管大盘**：为地方农业部门提供数字化管理驾驶舱

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────┐
│                   客户端层                        │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────┐ │
│  │ 农户/机主端   │  │ 管理后台     │  │ 政府大盘  │ │
│  │ Uni-app+Vue3 │  │ Vue3+ElemUI │  │ Vue3     │ │
│  └──────┬──────┘  └──────┬──────┘  └────┬─────┘ │
└─────────┼────────────────┼───────────────┼───────┘
          │     RESTful API (JSON)         │
┌─────────┼────────────────┼───────────────┼───────┐
│         ▼                ▼               ▼       │
│  ┌──────────────────────────────────────────┐    │
│  │        Spring Boot 3.3.5 + Java 17       │    │
│  │  Security · JWT · Validation · Flyway    │    │
│  └──────┬───────────────┬────────────┬──────┘    │
│         ▼               ▼            ▼           │
│  ┌──────────┐  ┌──────────┐  ┌────────────┐     │
│  │ MySQL 8  │  │ Redis 7  │  │ 高德/和风API│     │
│  └──────────┘  └──────────┘  └────────────┘     │
└──────────────────────────────────────────────────┘
```

## 🚀 快速开始

### 环境要求

| 工具 | 最低版本 |
|------|---------|
| Java | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 7.0+ |
| Maven | 3.8+ |

### 1. 克隆项目

```bash
git clone <仓库地址>
cd nong-helper
```

### 2. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env，填写数据库密码、JWT 密钥、高德 API Key 等
```

### 3. 启动后端

```bash
cd backend
# 首次运行会自动执行 Flyway 数据库迁移
mvn spring-boot:run
# 服务启动在 http://localhost:8080
```

### 4. 启动前端（H5 模式）

```bash
cd frontend-uniapp
npm install
npm run dev:h5
# 服务启动在 http://localhost:5173
```

### 5. 启动管理后台

```bash
cd admin-web
npm install
npm run dev
```

## 📁 项目结构

```
nong-helper/
├── backend/                  # Spring Boot 后端
│   └── src/main/java/com/nongzhushou/
│       ├── auth/             # 认证（短信登录、JWT）
│       ├── demand/           # 农户需求管理
│       ├── match/            # 智能匹配引擎
│       ├── matchflow/        # 匹配流程编排
│       ├── order/            # 订单管理
│       ├── contact/          # 联系会话
│       ├── equipment/        # 农机设备
│       ├── track/            # GPS 轨迹与测亩
│       ├── volunteer/        # 志愿者互助
│       ├── community/        # 村社社区
│       ├── rental/           # 农机租赁
│       ├── report/           # 报表统计
│       └── common/           # 公共模块（安全、异常、枚举）
├── frontend-uniapp/          # Uni-app 跨端前端
│   └── src/
│       ├── pages/            # 页面（farmer/owner/admin/common）
│       ├── components/       # 公共组件
│       ├── api/              # HTTP 请求模块
│       ├── styles/           # 模块化样式系统
│       └── store/            # 状态管理
├── admin-web/                # Vue3 管理后台
├── docs/                     # 产品 & 技术文档
│   ├── product/              # PRD、匹配规则、角色流程
│   └── backend/              # API 规格、数据库设计
└── integration/              # 集成测试与演示文档
```

## 📚 核心文档

| 文档 | 说明 |
|------|------|
| [PROJECT_OVERVIEW.md](./PROJECT_OVERVIEW.md) | 项目规划与阶段状态 |
| [docs/product/PRD.md](./docs/product/PRD.md) | 产品需求文档 |
| [docs/backend/API_SPEC.md](./docs/backend/API_SPEC.md) | API 接口规范 & 状态字典 |
| [docs/backend/DB_SCHEMA.sql](./docs/backend/DB_SCHEMA.sql) | 数据库表结构 |

## 🔐 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `MYSQL_HOST` | MySQL 主机 | `127.0.0.1` |
| `MYSQL_PORT` | MySQL 端口 | `3306` |
| `MYSQL_DB` | 数据库名 | `nongzhushou` |
| `MYSQL_USER` | 数据库用户 | `root` |
| `MYSQL_PASSWORD` | 数据库密码 | (必填) |
| `REDIS_HOST` | Redis 主机 | `127.0.0.1` |
| `JWT_SECRET` | JWT 签名密钥 | (必填) |
| `GAODE_MAP_API_KEY` | 高德地图 API Key | (可选) |

## 📄 License

本项目为私有项目，版权所有。
