package com.nongzhushou.zone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.zone.entity.ZoneEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
import java.util.Map;

@Mapper
public interface ZoneMapper extends BaseMapper<ZoneEntity> {

    @Select("SELECT * FROM v3_zone WHERE township_code = #{townshipCode} AND status = 'ACTIVE' ORDER BY id ASC")
    List<ZoneEntity> findByTownship(String townshipCode);

    @Select("SELECT * FROM v3_zone WHERE county_code = #{countyCode} AND status = 'ACTIVE' ORDER BY id ASC")
    List<ZoneEntity> findByCounty(String countyCode);

    @Select("SELECT * FROM v3_zone WHERE id = #{id} LIMIT 1")
    ZoneEntity findById(Long id);

    // 更新片区农机手数量（注册/退出时调用）
    @Update("UPDATE v3_zone SET operator_count = (" +
            "SELECT COUNT(*) FROM v3_user WHERE zone_id = #{zoneId} AND role = 'OPERATOR' AND status = 'NORMAL'" +
            "), machinery_count = (" +
            "SELECT COUNT(*) FROM v3_machinery WHERE zone_id = #{zoneId} AND status = 'ACTIVE'" +
            ") WHERE id = #{zoneId}")
    void refreshStats(@Param("zoneId") Long zoneId);

    /** 区管员查询县区下所有片区（含停用） */
    @Select("SELECT * FROM v3_zone WHERE county_code = #{countyCode} ORDER BY status ASC, id ASC")
    List<ZoneEntity> findAllByCounty(@Param("countyCode") String countyCode);

    /** 镇域运力统计：每个镇的活跃需求数和机手数 */
    @Select("SELECT z.township_code AS townshipCode, " +
            "COUNT(DISTINCT d.id) AS demandCount, " +
            "SUM(z.operator_count) AS operatorCount " +
            "FROM v3_zone z " +
            "LEFT JOIN v3_demand d ON d.zone_id = z.id AND d.status = 'PUBLISHED' " +
            "WHERE z.status = 'ACTIVE' " +
            "GROUP BY z.township_code")
    List<Map<String, Object>> townshipCapacityStats();
}
