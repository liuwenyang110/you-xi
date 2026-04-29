package com.nongzhushou.zone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nongzhushou.zone.entity.CropEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CropMapper extends BaseMapper<CropEntity> {
    @Select("SELECT * FROM v3_crop WHERE enabled = 1 ORDER BY sort_no ASC")
    List<CropEntity> findAllEnabled();
}
