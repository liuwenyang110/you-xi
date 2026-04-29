package com.nongzhushou.match.mapper;

import com.nongzhushou.match.model.DemandMatchView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DemandMatchViewMapper {
    DemandMatchView selectDemandMatchView(@Param("demandId") Long demandId);
}
