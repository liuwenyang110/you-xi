package com.nongzhushou.common.enums;

public enum OrderStatus {
    PENDING_CONTACT("待建立联系", "等待双方建立联系"),
    WAIT_NEGOTIATION("待协商", "等待确认价格、时间等作业细节"),
    SERVING("服务中", "作业进行中"),
    FINISHED_PENDING_CONFIRM("待完工确认", "作业已完成，等待双方确认"),
    COMPLETED("已完成", "联系与服务记录已完成"),
    FAILED("已失败", "订单未能继续执行"),
    CANCELLED("已取消", "订单已取消");

    private final String label;
    private final String description;

    OrderStatus(String label, String description) {
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
