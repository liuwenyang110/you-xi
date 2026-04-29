package com.nongzhushou.matchflow.mapper;

import com.nongzhushou.matchflow.model.ContactSessionLite;
import com.nongzhushou.matchflow.model.MatchAttemptLite;
import com.nongzhushou.matchflow.model.MatchTaskLite;
import com.nongzhushou.matchflow.model.OrderLite;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MatchTaskFlowMapper {
    MatchTaskLite selectTaskByDemandId(@Param("demandId") Long demandId);
    int insertMatchTask(MatchTaskLite task);
    int updateTaskTierAndStatus(@Param("taskId") Long taskId, @Param("tierNo") Integer tierNo, @Param("status") String status);
    int markTaskSuccess(@Param("taskId") Long taskId, @Param("ownerId") Long ownerId, @Param("serviceItemId") Long serviceItemId);
    int markTaskFailed(@Param("taskId") Long taskId, @Param("failReason") String failReason);
    int insertMatchAttempt(MatchAttemptLite attempt);
    MatchAttemptLite selectAttemptById(@Param("attemptId") Long attemptId);
    int updateAttemptStatus(@Param("attemptId") Long attemptId, @Param("status") String status);
    int closeAttempt(@Param("attemptId") Long attemptId, @Param("status") String status);
    int insertContactSession(ContactSessionLite session);
    ContactSessionLite selectContactSessionByAttemptId(@Param("attemptId") Long attemptId);
    int updateContactSessionStatus(@Param("sessionId") Long sessionId, @Param("status") String status);
    int insertOrder(OrderLite order);
    int updateDemandStatus(@Param("demandId") Long demandId, @Param("status") String status);
    Long selectFarmerIdByDemandId(@Param("demandId") Long demandId);
    List<Long> selectOwnerTimeoutAttemptIds();
    List<Long> selectFarmerTimeoutAttemptIds();
}
