package com.nongzhushou.match.mapper;

import com.nongzhushou.match.model.DemandMatchView;
import com.nongzhushou.match.model.ServiceItemMatchView;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MatchRuleQueryMapper {
    DemandMatchView selectDemandMatchView(@Param("demandId") Long demandId);
    List<ServiceItemMatchView> selectEligibleBasePool(@Param("subcategoryId") Long subcategoryId);
    int countActiveOrdersByOwner(@Param("ownerId") Long ownerId);
    int countActiveContactsByDemand(@Param("demandId") Long demandId);
    int countTriedOwnerByDemand(@Param("demandId") Long demandId, @Param("ownerId") Long ownerId);
}
