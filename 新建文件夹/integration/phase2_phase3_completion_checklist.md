# 农助手 第二、第三阶段完成清单

日期：2026-03-25

## 第二阶段：工程规范化

- 已完成：统一错误码与统一返回结构
  - `backend/src/main/java/com/nongzhushou/common/exception/ErrorCode.java`
  - `backend/src/main/java/com/nongzhushou/common/exception/BizException.java`
  - `backend/src/main/java/com/nongzhushou/common/exception/GlobalExceptionHandler.java`
  - `backend/src/main/java/com/nongzhushou/common/api/Result.java`

- 已完成：核心接口参数校验
  - 需求、联系、派单、订单、设备、服务项、举报、角色切换、模式切换等入口均已补 `@Validated` 或 DTO 约束。

- 已完成：状态枚举与常量收口
  - `backend/src/main/java/com/nongzhushou/common/enums`

- 已完成：请求日志与流程日志基础
  - `backend/src/main/java/com/nongzhushou/common/logging/RequestLogInterceptor.java`
  - `backend/src/main/java/com/nongzhushou/common/config/WebConfig.java`

- 已完成：数据归属与角色权限控制
  - 农户侧需求、联系单、订单已按当前登录农户隔离。
  - 农机主侧设备、服务项、派单列表与详情已按当前登录农机主隔离。
  - 管理员接口已强制 `ADMIN` 角色。

- 已完成：主要 service 语义收口
  - 常见 `not found`、`invalid state`、`forbidden` 等场景已统一返回明确错误码，而不是模糊布尔值。

## 第三阶段：前端真实联调

- 已完成：测试账号一键登录
  - 农户、农机主、管理员三种入口已接好。
  - token、userId、role、uiMode 已本地持久化。

- 已完成：登录后自动分流
  - 已登录访问登录页时，会自动按角色返回对应首页。

- 已完成：页面守卫
  - 农户、农机主、管理员核心页面均已接入登录与角色校验。
  - 未登录或角色不匹配时，会自动回正确入口。

- 已完成：农户核心链路联调
  - 农户首页
  - 发布需求
  - 智能匹配
  - 联系确认
  - 订单详情

- 已完成：农机主核心链路联调
  - 农机主首页
  - 派单详情
  - 我的农机
  - 我的服务项

- 已完成：管理员移动端占位入口
  - `frontend-uniapp/pages/admin/entry.vue`

- 已完成：老年模式最小实现
  - 全局字体、按钮尺寸、对比度已区分正常模式与老年模式。

- 已完成：页面状态收口
  - 主要页面已补 loading / error / empty 状态。
  - 详情页错误态和空态已补返回首页或返回上一步入口。

- 已完成：H5 直达路径与返回链路修正
  - H5 下角色串页会被自动拉回正确首页。
  - 详情页返回与首页跳转已统一到稳定工具。

## 本轮实测结论

- 已验证：未登录直接打开保护页面，会回登录页。
- 已验证：农户登录后直接打开农机主页面，会自动回农户首页。
- 已验证：农机主登录后直接打开农户页面，会自动回农机主首页。
- 已验证：已登录状态再次进入登录页，会自动分流回当前角色首页。
- 已验证：无效订单详情页会显示错误信息，并提供返回首页按钮。

## 进入下一阶段前仍可继续优化的细项

- 可继续优化：页面文案与导航标题的终端乱码观感
  - 浏览器实际渲染正常，但终端抓取仍会显示编码乱码。

- 可继续优化：更完整的前端操作成功/失败提示统一
  - 当前已基本中文化，但仍可再做一次全文案扫尾。

- 可继续优化：对核心页面做一次完整录屏式联调复跑
  - 用于进入第四阶段前留档。
