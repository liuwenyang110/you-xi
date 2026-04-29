package com.nongzhushou.zone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.zone.entity.RegionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface RegionMapper extends BaseMapper<RegionEntity> {

    @Select("SELECT * FROM v3_region WHERE level = #{level} ORDER BY sort_no ASC")
    List<RegionEntity> findByLevel(int level);

    @Select("SELECT * FROM v3_region WHERE parent_code = #{parentCode} ORDER BY sort_no ASC")
    List<RegionEntity> findByParentCode(String parentCode);

    @Select("SELECT * FROM v3_region WHERE code = #{code} LIMIT 1")
    RegionEntity findByCode(String code);
}
