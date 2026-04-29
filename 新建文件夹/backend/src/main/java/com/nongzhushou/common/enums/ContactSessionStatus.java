package com.nongzhushou.common.enums;

public enum ContactSessionStatus {
    // === 原有状态（保留向后兼容） ===
    WAIT_FARMER_CONFIRM("待农户确认", "等待农户确认是否建立联系"),
    WAITING_FARMER_CONFIRM("待农户确认", "等待农户确认是否继续联系"),
    CONTACT_ACTIVE("联系已建立", "双方已获得联系方式"),
    CONFIRMED("已确认", "联系已确认完成"),
    CLOSED("已关闭", "联系会话已关闭"),
    EXPIRED("已过期", "联系会话已过期"),

    // === 新增：联系平台简化状态 ===
    CONTACTED("已发起联系", "农户已发起联系请求"),
    SERVING("服务进行中", "农机主正在提供服务"),
    FEEDBACK_GIVEN("已评价", "农户已完成服务评价");

    private final String label;
    private final String description;

    ContactSessionStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "未知状态";
        }
        try {
            return valueOf(code).getLabel();
        } catch (IllegalArgumentException ignored) {
            return code;
        }
    }

    public static String descriptionOf(String code) {
        if (code == null || code.isBlank()) {
            return "状态未知";
        }
        try {
            return valueOf(code).getDescription();
        } catch (IllegalArgumentException ignored) {
            return code;
        }
    }
}
