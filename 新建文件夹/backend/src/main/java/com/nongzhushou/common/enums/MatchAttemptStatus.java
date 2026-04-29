package com.nongzhushou.common.enums;

public enum MatchAttemptStatus {
    PENDING_RESPONSE("待机主响应", "已向农机主派单，等待响应"),
    OWNER_ACCEPTED("机主已接单", "农机主已接受派单"),
    WAIT_FARMER_CONFIRM("待农户确认", "等待农户确认是否继续联系"),
    CONTACT_OPENED("已建立联系", "双方已获得联系方式"),
    OWNER_REJECTED("机主已拒绝", "当前农机主已拒绝，系统将继续匹配"),
    FARMER_REJECTED("农户已拒绝", "农户未确认当前匹配结果"),
    OWNER_TIMEOUT("机主超时", "农机主未在规定时间内响应"),
    FARMER_TIMEOUT("农户超时", "农户未在规定时间内确认"),
    DEMAND_CANCELLED("需求已取消", "农户已取消当前需求"),
    EXPIRED("已过期", "当前匹配尝试已过期");

    private final String label;
    private final String description;

    MatchAttemptStatus(String label, String description) {
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
