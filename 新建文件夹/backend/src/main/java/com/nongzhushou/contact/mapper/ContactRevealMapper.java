package com.nongzhushou.contact.mapper;

import com.nongzhushou.contact.entity.ContactRevealLogEntity;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 公益联系行为日志 MyBatis Mapper
 * 对应表：contact_reveal_log / platform_daily_stats
 */
@Mapper
public interface ContactRevealMapper {

    /** 插入一条联系行为记录 */
    @Insert("""
        INSERT INTO contact_reveal_log (farmer_id, operator_id, zone_id, source)
        VALUES (#{farmerId}, #{operatorId}, #{zoneId}, #{source})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ContactRevealLogEntity log);

    /** 查询某农户今日联系次数 */
    @Select("""
        SELECT COUNT(*) FROM contact_reveal_log
        WHERE farmer_id = #{farmerId} AND DATE(created_at) = #{today}
        """)
    int countTodayByFarmer(@Param("farmerId") Long farmerId, @Param("today") LocalDate today);

    /** 查询片区某日期范围内的联系总次数（zone_id=null 时统计全平台） */
    @Select("""
        SELECT COUNT(*) FROM contact_reveal_log
        WHERE (#{zoneId} IS NULL OR zone_id = #{zoneId})
          AND DATE(created_at) BETWEEN #{from} AND #{to}
        """)
    int countByZoneAndDate(@Param("zoneId") Long zoneId,
                           @Param("from") LocalDate from,
                           @Param("to") LocalDate to);

    /** 查询片区历史累计联系次数 */
    @Select("SELECT COUNT(*) FROM contact_reveal_log WHERE zone_id = #{zoneId}")
    int countTotalByZone(@Param("zoneId") Long zoneId);

    /** 查询片区今日被联系的不同服务者数 */
    @Select("""
        SELECT COUNT(DISTINCT operator_id) FROM contact_reveal_log
        WHERE zone_id = #{zoneId} AND DATE(created_at) = #{today}
        """)
    int countUniqueOperatorsByZone(@Param("zoneId") Long zoneId, @Param("today") LocalDate today);

    /** 从每日快照表读取当日缓存 */
    @Select("""
        SELECT total_demands AS todayDemands,
               total_connected AS todayConnected,
               total_operators AS totalOperators,
               covered_zones AS coveredZones
        FROM platform_daily_stats
        WHERE stat_date = #{date} AND zone_id IS NULL
        LIMIT 1
        """)
    Map<String, Object> findDailyStats(@Param("date") LocalDate date);

    /** 统计今日新增需求数 */
    @Select("SELECT COUNT(*) FROM v3_demand WHERE DATE(created_at) = #{today}")
    int countTodayDemands(@Param("today") LocalDate today);

    /** 统计已注册服务者总数 */
    @Select("SELECT COUNT(*) FROM user_account WHERE role = 'OPERATOR' AND status = 'NORMAL'")
    int countTotalOperators();

    /** 统计今日有联系记录的片区数（覆盖村镇） */
    @Select("""
        SELECT COUNT(DISTINCT zone_id) FROM contact_reveal_log
        WHERE DATE(created_at) = #{today} AND zone_id IS NOT NULL
        """)
    int countCoveredZones(@Param("today") LocalDate today);
}
