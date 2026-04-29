package com.nongzhushou.match.service.impl;

import com.nongzhushou.match.mapper.MatchConflictMapper;
import com.nongzhushou.match.service.MatchConflictService;
import org.springframework.stereotype.Service;

@Service
public class MatchConflictServiceImpl implements MatchConflictService {

    private final MatchConflictMapper matchConflictMapper;

    public MatchConflictServiceImpl(MatchConflictMapper matchConflictMapper) {
        this.matchConflictMapper = matchConflictMapper;
    }

    @Override
    public boolean hasActiveOrderConflict(Long ownerId) {
        return matchConflictMapper.countActiveOrdersByOwner(ownerId) > 0;
    }

    @Override
    public boolean hasActiveContactConflict(Long demandId) {
        return matchConflictMapper.countActiveContactsByDemand(demandId) > 0;
    }

    @Override
    public boolean hasAlreadyTried(Long demandId, Long ownerId) {
        return matchConflictMapper.countTriedOwnerByDemand(demandId, ownerId) > 0;
    }
}
