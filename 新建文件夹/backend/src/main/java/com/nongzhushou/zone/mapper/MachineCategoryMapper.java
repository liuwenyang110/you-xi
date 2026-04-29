package com.nongzhushou.zone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.zone.entity.MachineCategoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MachineCategoryMapper extends BaseMapper<MachineCategoryEntity> {
    @Select("SELECT * FROM v3_machine_category WHERE enabled = 1 ORDER BY sort_no ASC")
    List<MachineCategoryEntity> findAllEnabled();
}
