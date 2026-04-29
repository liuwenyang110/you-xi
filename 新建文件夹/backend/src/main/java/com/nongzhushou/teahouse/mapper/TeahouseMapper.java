package com.nongzhushou.teahouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.teahouse.entity.TeahouseEntity;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TeahouseMapper extends BaseMapper<TeahouseEntity> {

    @Select("SELECT * FROM v3_zone_teahouse WHERE zone_id = #{zoneId}")
    TeahouseEntity findByZoneId(@Param("zoneId") Long zoneId);

    /** 扫描需要发送7天预警的茶馆 */
    @Select("SELECT * FROM v3_zone_teahouse WHERE status = 'OPEN' AND last_demand_at <= #{deadline}")
    List<TeahouseEntity> findNeedWarn(@Param("deadline") LocalDateTime deadline);

    /** 扫描需要强制关闭的茶馆（连续15天无新需求） */
    @Select("SELECT * FROM v3_zone_teahouse WHERE status IN ('OPEN','CLOSING_WARN','EXTENDED') AND last_demand_at <= #{deadline}")
    List<TeahouseEntity> findNeedForceClose(@Param("deadline") LocalDateTime deadline);
}
