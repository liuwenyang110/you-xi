# 农助手 第二、第三阶段最终复跑记录

日期：2026-03-25

## 复跑目标

- 用最新代码重新跑通农户主链
- 确认第二阶段的权限与错误处理仍正常
- 确认第三阶段页面依赖的关键接口状态正常

## 本次复跑链路

1. 使用农户测试账号创建新需求
2. 系统自动启动匹配并生成匹配尝试
3. 使用农机主测试账号接单
4. 农户确认联系
5. 系统生成订单并推进需求到协商中

## 本次新数据

- `demandId = 23`
- `villageName = phase23-final-1774433901`
- `matchAttemptId = 21`
- `contactSessionId = 9`
- `orderId = 3`

## 接口结果

### 1. 发布需求后

- `POST /api/v1/demands`
  - 返回新需求 `23`

- `GET /api/v1/demands/23/match-status`
  - `status = MATCHING`
  - `currentMatchAttemptId = 21`
  - `matchAttemptCount = 1`

### 2. 农机主接单后

- `POST /api/v1/dispatches/21/accept`
  - 成功
  - 创建 `contactSessionId = 9`

- `GET /api/v1/demands/23/match-status`
  - `status = WAIT_FARMER_CONTACT_CONFIRM`
  - `currentContactSessionId = 9`

### 3. 农户确认联系后

- `POST /api/v1/contact-sessions/9/confirm`
  - 成功
  - 创建 `orderId = 3`

- `GET /api/v1/demands/23/match-status`
  - `status = NEGOTIATING`
  - `currentMatchAttemptId = 21`
  - `currentContactSessionId = 9`
  - `currentOrderId = 3`

## 数据库结果

### demand

- `id = 23`
- `status = NEGOTIATING`
- `current_match_attempt_id = 21`
- `current_contact_session_id = 9`
- `current_order_id = 3`
- `match_attempt_count = 1`

### match_task

- `demand_id = 23`
- `current_tier = 1`
- `status = SUCCESS`
- `success_owner_id = 10002`
- `success_service_item_id = 2001`

### match_attempt

- `id = 21`
- `demand_id = 23`
- `owner_id = 10002`
- `status = CONTACT_OPENED`
- `distance_layer = 近距离优先`
- `service_name = Rice Harvest Service`

### contact_session

- `id = 9`
- `demand_id = 23`
- `match_attempt_id = 21`
- `status = CONTACT_ACTIVE`
- `active_flag = 1`
- `confirmed_at = 2026-03-25 18:18:22`

### order_info

- `id = 3`
- `demand_id = 23`
- `contact_session_id = 9`
- `status = WAIT_NEGOTIATION`
- `farmer_confirmed_finish = 0`
- `owner_confirmed_finish = 0`

## 同轮补验

- 农户 token 访问管理员接口：
  - `GET /api/v1/admin/dashboard`
  - 返回 `4003 admin permission required`

- 已登录用户再次进入登录页：
  - 会自动按角色回到对应首页

- 无效订单详情页：
  - 会显示错误信息
  - 会显示返回首页按钮

## 备注

- 历史旧数据里仍有少量早期测试记录的 `contact_session.active_flag = 1` 且 `status = CLOSED`，这是旧脏数据，不影响本轮新链路。
- 本轮新链路创建的数据状态一致，没有出现这个问题。
