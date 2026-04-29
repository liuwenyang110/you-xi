package com.nongzhushou.v3machinery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.v3machinery.entity.V3MachineryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface V3MachineryMapper extends BaseMapper<V3MachineryEntity> {

    @Select("SELECT id, operator_id, zone_id, machine_type_id, plate_number, owner_name, contact_phone, status, is_cross_region, created_at, updated_at FROM v3_machinery WHERE operator_id = #{operatorId} AND status = 'ACTIVE' ORDER BY id ASC")
    List<V3MachineryEntity> findByOperator(Long operatorId);

    @Select("SELECT id, operator_id, zone_id, machine_type_id, plate_number, owner_name, contact_phone, status, is_cross_region, created_at, updated_at FROM v3_machinery WHERE zone_id = #{zoneId} AND status = 'ACTIVE' ORDER BY id ASC")
    List<V3MachineryEntity> findByZone(Long zoneId);

    /** 按片区统计各大类农机数量 */
    @Select("SELECT mt.category_id, mc.name AS categoryName, mc.icon, COUNT(*) AS cnt " +
            "FROM v3_machinery m " +
            "JOIN v3_machine_type mt ON m.machine_type_id = mt.id " +
            "JOIN v3_machine_category mc ON mt.category_id = mc.id " +
            "WHERE m.zone_id = #{zoneId} AND m.status = 'ACTIVE' " +
            "GROUP BY mt.category_id, mc.name, mc.icon ORDER BY mc.sort_no ASC")
    List<Map<String, Object>> countByZoneAndCategory(@Param("zoneId") Long zoneId);

    /** 查询片区内提供跨区作业的农机 */
    @Select("SELECT id, operator_id, zone_id, machine_type_id, plate_number, owner_name, contact_phone, status, is_cross_region, created_at, updated_at FROM v3_machinery WHERE zone_id = #{zoneId} AND is_cross_region = 1 AND status = 'ACTIVE'")
    List<V3MachineryEntity> findCrossRegionByZone(Long zoneId);
}
