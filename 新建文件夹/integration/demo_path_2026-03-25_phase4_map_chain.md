# 第四阶段演示链路记录（地图定位驱动）

## 演示目标

验证“地点定位 -> 带坐标发布需求 -> 匹配派单 -> 农机主接单 -> 农户确认联系 -> 订单生成”这条链路已经真实可用。

## 本次演示数据

- `demandId = 28`
- `matchAttemptId = 24`
- `contactSessionId = 10`
- `orderId = 4`

## 演示步骤

1. 农户登录，进入“发布需求”页。
2. 通过地点定位填写服务地点，发布一条带坐标的需求。
3. 系统创建需求后进入“智能匹配”页。
4. 后端根据位置和规则生成匹配尝试：
   - `match_attempt 24`
   - 状态 `PENDING_RESPONSE`
5. 农机主登录，在“派单详情”页处理该派单。
6. 农机主点击“确认接单”后：
   - `demand.status = WAIT_FARMER_CONTACT_CONFIRM`
   - `match_attempt.status = WAIT_FARMER_CONFIRM`
   - `contact_session 10` 创建成功
7. 农户进入“确认联系”页，点击“同意联系并生成订单”。
8. 系统自动创建订单：
   - `order_info 4`
   - `contact_session.status = CONTACT_ACTIVE`
   - `match_task.status = SUCCESS`
   - `match_attempt.status = CONTACT_OPENED`
   - `demand.status = NEGOTIATING`

## 数据库最终状态

- `demand(28).status = NEGOTIATING`
- `demand(28).current_match_attempt_id = 24`
- `demand(28).current_contact_session_id = 10`
- `demand(28).current_order_id = 4`
- `match_task(demand_id=28).status = SUCCESS`
- `match_attempt(24).status = CONTACT_OPENED`
- `contact_session(10).status = CONTACT_ACTIVE`
- `order_info(4).status = WAIT_NEGOTIATION`

## 结论

第四阶段当前已经完成一条真实的地图定位驱动主链路，不再只是后端调试接口可用，而是已经能够直接支撑 App 页面联调与演示。
