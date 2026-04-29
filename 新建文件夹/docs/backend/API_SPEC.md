# API 规格（V10 — 农业机械联系平台）

## 面向中国市场的接口约定

- 所有用户可见状态都必须提供中文展示能力，前端页面禁止直接裸显英文状态码。
- 后端保留 `status` 作为系统状态码，同时在关键业务响应中补充：
  - `statusLabel`: 面向页面展示的中文短标签
  - `statusDescription`: 面向说明文案的中文描述
- 地图与地址相关接口、字段应优先适配中国市场使用习惯：高德地图、乡镇/村地址表达、弱网降级提示。
- 适老化模式下，前端应优先使用 `statusLabel` 与简洁说明，不展示技术术语。

## 平台定位变更说明（V10）

> **从"交易闭环平台"转型为"联系对接平台"**
>
> - 去除支付、订单交易等复杂闭环
> - `contact_session` 升级为核心实体
> - 新增农户主动联系（不经匹配系统）流程
> - 新增隐私合规接口（PIPL）
> - 新增信誉等级体系（老把式认证）

## 核心状态字典

### DemandStatus

| 状态码 | 中文标签 | 说明 |
| --- | --- | --- |
| `DRAFT` | 草稿 | 待完善需求内容 |
| `PUBLISHED` | 待匹配 | 需求已发布，等待系统开始匹配 |
| `MATCHING` | 匹配中 | 系统正在为您匹配合适的农机主 |
| `WAIT_FARMER_CONTACT_CONFIRM` | 待确认联系 | 已有农机主接单，等待农户确认是否建立联系 |
| `WAITING_FARMER_CONFIRM` | 待农户确认 | 已有农机主响应，等待农户确认 |
| `NEGOTIATING` | 协商中 | 双方正在沟通作业细节 |
| `CONTACTING` | 联系中 | 双方已建立联系 |
| `IN_SERVICE` | 服务中 | 作业已开始执行 |
| `FINISHED_PENDING_CONFIRM` | 待完工确认 | 作业已完成，等待双方确认 |
| `COMPLETED` | 已完成 | 需求已完成闭环 |
| `MATCH_FAILED` | 匹配失败 | 暂未匹配到合适农机主，可稍后重试 |
| `CANCELLED` | 已取消 | 需求已取消 |
| `CLOSED` | 已关闭 | 需求已关闭，不再继续流转 |

### ContactSessionStatus（⭐ V10 核心）

| 状态码 | 中文标签 | 说明 |
| --- | --- | --- |
| `CONTACTED` | 已发起联系 | 农户已发起联系请求 |
| `WAIT_FARMER_CONFIRM` | 待农户确认 | 等待农户确认是否建立联系 |
| `WAITING_FARMER_CONFIRM` | 待农户确认 | 等待农户确认是否继续联系 |
| `CONTACT_ACTIVE` | 联系已建立 | 双方已获得联系方式 |
| `SERVING` | 服务进行中 | 农机主正在提供服务 |
| `CONFIRMED` | 已确认 | 联系已确认完成 |
| `FEEDBACK_GIVEN` | 已评价 | 农户已完成服务评价 |
| `CLOSED` | 已关闭 | 联系会话已关闭 |
| `EXPIRED` | 已过期 | 联系会话已过期 |

### ReputationLevel（V10 新增：老把式信誉等级）

| 等级码 | 中文名 | 星级 | 条件 |
| --- | --- | --- | --- |
| `NEWBIE` | 新手机手 | ⭐ | 注册即有 |
| `RELIABLE` | 靠谱机手 | ⭐⭐ | 完成 5 次 + 好评率 80%+ |
| `VETERAN` | 老把式 | ⭐⭐⭐ | 完成 20 次 + 好评率 95%+ |
| `GOLD` | 金牌机手 | ⭐⭐⭐⭐ | 完成 50 次 + 好评率 98%+ |
| `MODEL` | 村级模范 | ⭐⭐⭐⭐⭐ | 完成 100 次 + 参与救灾 |

## 认证

- `POST /api/v1/auth/sms/send`
- `POST /api/v1/auth/login/sms`
- `POST /api/v1/auth/realname/submit`
- `POST /api/v1/auth/role/switch`
- `GET /api/v1/auth/me`

### `GET /api/v1/auth/me` 响应补充

- `status`: 账户状态码
- `statusLabel`: 账户状态中文标签
- `wxOpenid`: 微信小程序 openid（V10 新增）
- `nickname`: 用户昵称（V10 新增）
- `avatarUrl`: 用户头像（V10 新增）

## 分类

- `GET /api/v1/catalog/service-categories`
- `GET /api/v1/catalog/machine-types`
- `GET /api/v1/catalog/demand-form/{subcategoryId}`
- `GET /api/v1/catalog/equipment-form/{machineTypeId}`

## 农户端

### 需求管理
- `POST /api/v1/demands`
- `GET /api/v1/demands`
- `GET /api/v1/demands/{id}`
- `GET /api/v1/demands/{id}/match-status`
- `POST /api/v1/demands/{id}/cancel`

### 联系对接（⭐ V10 核心）
- `GET /api/v1/contact-sessions` — 获取联系记录列表
- `POST /api/v1/contact-sessions/initiate` — ⭐ 农户主动发起联系
- `POST /api/v1/contact-sessions/{id}/confirm` — 确认联系
- `POST /api/v1/contact-sessions/{id}/reject` — 拒绝/关闭联系
- `POST /api/v1/contact-sessions/{id}/feedback` — ⭐ 提交服务评价
- `POST /api/v1/contact-sessions/{id}/complete` — ⭐ 标记服务完成

#### `POST /api/v1/contact-sessions/initiate` 请求参数

| 参数 | 类型 | 必须 | 说明 |
| --- | --- | --- | --- |
| `ownerId` | Long | 是 | 目标农机主ID |
| `serviceItemId` | Long | 否 | 关联服务项ID |
| `contactType` | String | 否 | `PHONE` / `WECHAT` / `VISIT`，默认 `PHONE` |
| `discoverySource` | String | 否 | `BROWSE` / `POST` / `SHARE`，默认 `BROWSE` |

#### `POST /api/v1/contact-sessions/{id}/feedback` 请求参数

| 参数 | 类型 | 必须 | 说明 |
| --- | --- | --- | --- |
| `feedback` | String | 否 | 文字反馈 |
| `rating` | Integer | 否 | 1-5 星评分 |

### 订单记录（保留兼容）
- `GET /api/v1/orders`
- `GET /api/v1/orders/{id}`

### 关键响应字段约定

#### Contact Session 响应

```json
{
  "id": 1,
  "ownerId": 10002,
  "farmerId": 10001,
  "status": "CONTACTED",
  "statusLabel": "已发起联系",
  "statusDescription": "农户已发起联系请求",
  "contactType": "PHONE",
  "discoverySource": "BROWSE",
  "maskedPhone": "138****0002",
  "farmerRating": null,
  "farmerFeedback": null,
  "serviceCompleted": 0
}
```

## 农机主端

- `POST /api/v1/equipment`
- `PUT /api/v1/equipment/{id}`
- `GET /api/v1/equipment`
- `GET /api/v1/match-tasks`（保留兼容）
- `GET /api/v1/match-tasks/{id}`
- `POST /api/v1/match-tasks/{id}/accept`
- `POST /api/v1/match-tasks/{id}/reject`

## 隐私合规（V10 新增 — PIPL）

- `POST /api/v1/privacy/consent` — 记录用户同意
- `GET /api/v1/privacy/consent/check?consentType=PRIVACY_POLICY` — 检查是否已同意
- `GET /api/v1/privacy/consents` — 获取用户所有同意记录
- `POST /api/v1/privacy/consent/revoke` — 撤回同意

### `POST /api/v1/privacy/consent` 请求参数

| 参数 | 类型 | 必须 | 说明 |
| --- | --- | --- | --- |
| `consentType` | String | 是 | `PRIVACY_POLICY` / `LOCATION` / `PHONE_DISPLAY` |
| `consentVersion` | String | 否 | 版本号，默认 `1.0` |
