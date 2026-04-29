package com.nongzhushou.common.enums;

public enum DemandStatus {
    DRAFT("草稿", "待完善求助内容"),
    PUBLISHED("待帮扶", "需求已发布，等待有人联系您"),
    CONTACTED("已联系", "已建立联系，正在沟通中"),
    IN_SERVICE("作业中", "机手正在作业中"),
    COMPLETED("已完成", "帮扶圆满完成"),
    CANCELLED("已取消", "求助已取消"),
    CLOSED("已关闭", "需求已关闭");

    private final String label;
    private final String description;

    DemandStatus(String label, String description) {
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
