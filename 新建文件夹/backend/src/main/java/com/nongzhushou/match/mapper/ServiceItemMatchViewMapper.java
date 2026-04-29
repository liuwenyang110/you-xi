package com.nongzhushou.match.mapper;

import com.nongzhushou.match.model.ServiceItemMatchView;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ServiceItemMatchViewMapper {
    List<ServiceItemMatchView> selectEligibleBasePool(@Param("subcategoryId") Long subcategoryId);
}
