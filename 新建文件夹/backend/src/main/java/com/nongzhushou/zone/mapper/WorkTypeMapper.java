package com.nongzhushou.zone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.zone.entity.WorkTypeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface WorkTypeMapper extends BaseMapper<WorkTypeEntity> {
    @Select("SELECT * FROM v3_work_type WHERE enabled = 1 ORDER BY sort_no ASC")
    List<WorkTypeEntity> findAllEnabled();

    @Select("SELECT * FROM v3_work_type WHERE category_id = #{categoryId} AND enabled = 1 ORDER BY sort_no ASC")
    List<WorkTypeEntity> findByCategory(Integer categoryId);
}
