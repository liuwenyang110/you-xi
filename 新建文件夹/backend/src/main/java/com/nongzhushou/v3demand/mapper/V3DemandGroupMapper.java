package com.nongzhushou.v3demand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.v3demand.entity.V3DemandGroupEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface V3DemandGroupMapper extends BaseMapper<V3DemandGroupEntity> {

    @Select("SELECT * FROM v3_demand_group WHERE zone_id = #{zoneId} AND status = 'OPEN' ORDER BY created_at DESC")
    List<V3DemandGroupEntity> findOpenByZone(Long zoneId);

    @Select("SELECT * FROM v3_demand_group WHERE creator_id = #{creatorId} ORDER BY created_at DESC")
    List<V3DemandGroupEntity> findByCreator(Long creatorId);
}
