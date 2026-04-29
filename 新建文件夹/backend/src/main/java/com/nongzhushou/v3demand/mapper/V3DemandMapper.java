package com.nongzhushou.v3demand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.v3demand.entity.V3DemandEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface V3DemandMapper extends BaseMapper<V3DemandEntity> {

    @Select("SELECT id, farmer_id, zone_id, work_type_id, crop_id, area_desc, expect_date_start, expect_date_end, location_desc, plot_notes, photos, group_id, status, published_at, created_at, updated_at, first_ask_at, first_ask_reply, second_ask_at, second_ask_reply, final_warn_at, auto_cleaned FROM v3_demand WHERE farmer_id = #{farmerId} ORDER BY created_at DESC")
    List<V3DemandEntity> findByFarmer(Long farmerId);

    @Select("SELECT id, farmer_id, zone_id, work_type_id, crop_id, area_desc, expect_date_start, expect_date_end, location_desc, plot_notes, photos, group_id, status, published_at, created_at, updated_at, first_ask_at, first_ask_reply, second_ask_at, second_ask_reply, final_warn_at, auto_cleaned FROM v3_demand WHERE zone_id = #{zoneId} AND status = 'PUBLISHED' ORDER BY published_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<V3DemandEntity> findActiveByZone(@Param("zoneId") Long zoneId,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM v3_demand WHERE zone_id = #{zoneId} AND status = 'PUBLISHED'")
    int countActiveByZone(Long zoneId);

    // ============ 生命周期定时任务扫描 ============

    /** T+2天：需要发送第一次询问的需求 */
    @Select("SELECT id, farmer_id, zone_id, work_type_id, crop_id, area_desc, expect_date_start, expect_date_end, location_desc, plot_notes, photos, group_id, status, published_at, created_at, updated_at, first_ask_at, first_ask_reply, second_ask_at, second_ask_reply, final_warn_at, auto_cleaned FROM v3_demand WHERE status = 'PUBLISHED' AND published_at <= #{deadline} AND first_ask_at IS NULL")
    List<V3DemandEntity> findNeedFirstAsk(@Param("deadline") LocalDateTime deadline);

    /** T+4天：需要发送第二次询问的需求 */
    @Select("SELECT id, farmer_id, zone_id, work_type_id, crop_id, area_desc, expect_date_start, expect_date_end, location_desc, plot_notes, photos, group_id, status, published_at, created_at, updated_at, first_ask_at, first_ask_reply, second_ask_at, second_ask_reply, final_warn_at, auto_cleaned FROM v3_demand WHERE status = 'PUBLISHED' AND first_ask_at IS NOT NULL AND first_ask_reply IS NULL AND first_ask_at <= #{deadline} AND second_ask_at IS NULL")
    List<V3DemandEntity> findNeedSecondAsk(@Param("deadline") LocalDateTime deadline);

    /** 两次都未回复，需要发最后通牒 */
    @Select("SELECT id, farmer_id, zone_id, work_type_id, crop_id, area_desc, expect_date_start, expect_date_end, location_desc, plot_notes, photos, group_id, status, published_at, created_at, updated_at, first_ask_at, first_ask_reply, second_ask_at, second_ask_reply, final_warn_at, auto_cleaned FROM v3_demand WHERE status = 'PUBLISHED' AND second_ask_at IS NOT NULL AND second_ask_reply IS NULL AND final_warn_at IS NULL")
    List<V3DemandEntity> findNeedFinalWarn();

    /** T+5天：最后通牒已发出1天仍未回复，需要强杀 */
    @Select("SELECT id, farmer_id, zone_id, work_type_id, crop_id, area_desc, expect_date_start, expect_date_end, location_desc, plot_notes, photos, group_id, status, published_at, created_at, updated_at, first_ask_at, first_ask_reply, second_ask_at, second_ask_reply, final_warn_at, auto_cleaned FROM v3_demand WHERE status = 'PUBLISHED' AND final_warn_at IS NOT NULL AND final_warn_at <= #{deadline} AND auto_cleaned = 0")
    List<V3DemandEntity> findNeedAutoClean(@Param("deadline") LocalDateTime deadline);

    // ============ 需求气泡聚合（镇域雷达） ============

    /** 按片区聚合活跃需求统计：每个村的需求数量和总面积 */
    @Select("SELECT d.zone_id AS zoneId, z.name AS zoneName, COUNT(*) AS demandCount " +
            "FROM v3_demand d JOIN v3_zone z ON d.zone_id = z.id " +
            "WHERE d.status = 'PUBLISHED' AND z.township_code = #{townshipCode} " +
            "GROUP BY d.zone_id, z.name")
    List<Map<String, Object>> aggregateByTownship(@Param("townshipCode") String townshipCode);
}
