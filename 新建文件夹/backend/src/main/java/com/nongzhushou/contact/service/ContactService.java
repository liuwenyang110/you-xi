package com.nongzhushou.contact.service;

import java.util.List;
import java.util.Map;

/**
 * 联系对接服务接口（V10 升级为平台核心服务）
 */
public interface ContactService {

    /** 获取当前用户的联系记录列表 */
    List<Map<String, Object>> list();

    /** 确认联系会话 */
    Map<String, Object> confirm(Long id);

    /** 拒绝联系会话 */
    Map<String, Object> reject(Long id);

    // === V10 新增：联系平台核心方法 ===

    /**
     * 农户主动发起联系（浏览模式）
     * 不经过匹配系统，直接创建联系会话
     *
     * @param ownerId 目标农机主ID
     * @param serviceItemId 关联服务项ID（可选）
     * @param contactType 联系方式: PHONE / WECHAT / VISIT
     * @param discoverySource 发现来源: BROWSE / POST / SHARE
     * @return 联系会话信息
     */
    Map<String, Object> initiateContact(Long ownerId, Long serviceItemId, String contactType, String discoverySource);

    /**
     * 提交服务反馈与评价
     *
     * @param id 联系会话ID
     * @param feedback 反馈文字
     * @param rating 评分 1-5
     * @return 更新后的联系会话
     */
    Map<String, Object> submitFeedback(Long id, String feedback, Integer rating);

    /**
     * 标记服务已完成
     *
     * @param id 联系会话ID
     * @return 更新后的联系会话
     */
    Map<String, Object> markServiceCompleted(Long id);
}
