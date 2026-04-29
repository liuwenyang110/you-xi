package com.nongzhushou.match.service;

public interface MatchConflictService {
    boolean hasActiveOrderConflict(Long ownerId);
    boolean hasActiveContactConflict(Long demandId);
    boolean hasAlreadyTried(Long demandId, Long ownerId);
}
