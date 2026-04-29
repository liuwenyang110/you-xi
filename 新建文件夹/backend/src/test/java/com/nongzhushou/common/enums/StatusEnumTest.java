package com.nongzhushou.common.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatusEnumTest {

    @Test
    void demandStatus_returnsChineseLabelAndDescriptionForAllKeyStates() {
        assertEquals("草稿", DemandStatus.labelOf("DRAFT"));
        assertEquals("待匹配", DemandStatus.labelOf("PUBLISHED"));
        assertEquals("匹配中", DemandStatus.labelOf("MATCHING"));
        assertEquals("待确认联系", DemandStatus.labelOf("WAIT_FARMER_CONTACT_CONFIRM"));
        assertEquals("待农户确认", DemandStatus.labelOf("WAITING_FARMER_CONFIRM"));
        assertEquals("协商中", DemandStatus.labelOf("NEGOTIATING"));
        assertEquals("联系中", DemandStatus.labelOf("CONTACTING"));
        assertEquals("服务中", DemandStatus.labelOf("IN_SERVICE"));
        assertEquals("待完工确认", DemandStatus.labelOf("FINISHED_PENDING_CONFIRM"));
        assertEquals("已完成", DemandStatus.labelOf("COMPLETED"));
        assertEquals("匹配失败", DemandStatus.labelOf("MATCH_FAILED"));
        assertEquals("已取消", DemandStatus.labelOf("CANCELLED"));
        assertEquals("已关闭", DemandStatus.labelOf("CLOSED"));
        assertEquals("系统正在为您匹配合适的农机主", DemandStatus.descriptionOf("MATCHING"));
    }

    @Test
    void orderStatus_returnsChineseLabelAndDescriptionForAllKeyStates() {
        assertEquals("待建立联系", OrderStatus.labelOf("PENDING_CONTACT"));
        assertEquals("待协商", OrderStatus.labelOf("WAIT_NEGOTIATION"));
        assertEquals("服务中", OrderStatus.labelOf("SERVING"));
        assertEquals("待完工确认", OrderStatus.labelOf("FINISHED_PENDING_CONFIRM"));
        assertEquals("已完成", OrderStatus.labelOf("COMPLETED"));
        assertEquals("已失败", OrderStatus.labelOf("FAILED"));
        assertEquals("已取消", OrderStatus.labelOf("CANCELLED"));
        assertEquals("订单未能继续执行", OrderStatus.descriptionOf("FAILED"));
    }

    @Test
    void contactAndMatchStatus_returnChineseTextForAllKeyStates() {
        assertEquals("待农户确认", ContactSessionStatus.labelOf("WAIT_FARMER_CONFIRM"));
        assertEquals("待农户确认", ContactSessionStatus.labelOf("WAITING_FARMER_CONFIRM"));
        assertEquals("联系已建立", ContactSessionStatus.labelOf("CONTACT_ACTIVE"));
        assertEquals("已确认", ContactSessionStatus.labelOf("CONFIRMED"));
        assertEquals("已关闭", ContactSessionStatus.labelOf("CLOSED"));
        assertEquals("已过期", ContactSessionStatus.labelOf("EXPIRED"));
        assertEquals("等待农户确认是否继续联系", ContactSessionStatus.descriptionOf("WAITING_FARMER_CONFIRM"));

        assertEquals("待机主响应", MatchAttemptStatus.labelOf("PENDING_RESPONSE"));
        assertEquals("机主已接单", MatchAttemptStatus.labelOf("OWNER_ACCEPTED"));
        assertEquals("待农户确认", MatchAttemptStatus.labelOf("WAIT_FARMER_CONFIRM"));
        assertEquals("已建立联系", MatchAttemptStatus.labelOf("CONTACT_OPENED"));
        assertEquals("机主已拒绝", MatchAttemptStatus.labelOf("OWNER_REJECTED"));
        assertEquals("农户已拒绝", MatchAttemptStatus.labelOf("FARMER_REJECTED"));
        assertEquals("机主超时", MatchAttemptStatus.labelOf("OWNER_TIMEOUT"));
        assertEquals("农户超时", MatchAttemptStatus.labelOf("FARMER_TIMEOUT"));
        assertEquals("需求已取消", MatchAttemptStatus.labelOf("DEMAND_CANCELLED"));
        assertEquals("已过期", MatchAttemptStatus.labelOf("EXPIRED"));
        assertEquals("已向农机主派单，等待响应", MatchAttemptStatus.descriptionOf("PENDING_RESPONSE"));
    }

    @Test
    void statusEnums_returnFallbackForBlankAndUnknownCode() {
        assertEquals("未知状态", DemandStatus.labelOf(""));
        assertEquals("状态未知", DemandStatus.descriptionOf(null));
        assertEquals("UNKNOWN_ORDER", OrderStatus.labelOf("UNKNOWN_ORDER"));
        assertEquals("UNKNOWN_CONTACT", ContactSessionStatus.descriptionOf("UNKNOWN_CONTACT"));
        assertEquals("UNKNOWN_MATCH", MatchAttemptStatus.labelOf("UNKNOWN_MATCH"));
    }
}
