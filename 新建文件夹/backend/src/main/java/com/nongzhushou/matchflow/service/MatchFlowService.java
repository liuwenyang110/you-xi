package com.nongzhushou.matchflow.service;

public interface MatchFlowService {
    Long startMatchFlow(Long demandId);
    Long dispatchNextAttempt(Long demandId);
    boolean ownerAccept(Long attemptId);
    boolean ownerReject(Long attemptId);
    boolean farmerConfirm(Long attemptId);
    boolean farmerReject(Long attemptId);
    boolean ownerTimeout(Long attemptId);
    void handleOwnerTimeout();
    void handleFarmerTimeout();
}
