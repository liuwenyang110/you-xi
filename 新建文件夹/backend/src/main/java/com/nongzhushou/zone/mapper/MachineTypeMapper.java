package com.nongzhushou.zone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.zone.entity.MachineTypeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MachineTypeMapper extends BaseMapper<MachineTypeEntity> {
    @Select("SELECT * FROM v3_machine_type WHERE enabled = 1 ORDER BY sort_no ASC")
    List<MachineTypeEntity> findAllEnabled();

    @Select("SELECT * FROM v3_machine_type WHERE category_id = #{categoryId} AND enabled = 1 ORDER BY sort_no ASC")
    List<MachineTypeEntity> findByCategory(Integer categoryId);
}
