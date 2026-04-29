package com.nongzhushou.match.service;

import com.nongzhushou.match.entity.MatchAttemptEntity;

public interface MatchEngineService {

    MatchAttemptEntity tryDispatch(Long demandId);
}
