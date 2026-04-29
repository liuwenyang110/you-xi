package com.nongzhushou.match.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MatchConflictMapper {
    int countActiveOrdersByOwner(@Param("ownerId") Long ownerId);
    int countActiveContactsByDemand(@Param("demandId") Long demandId);
    int countTriedOwnerByDemand(@Param("demandId") Long demandId, @Param("ownerId") Long ownerId);
}
