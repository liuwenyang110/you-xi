package com.nongzhushou.demand.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongzhushou.contact.mapper.ContactSessionMapper;
import com.nongzhushou.demand.entity.DemandEntity;
import com.nongzhushou.demand.mapper.DemandMapper;
import com.nongzhushou.match.entity.MatchAttemptEntity;
import com.nongzhushou.match.mapper.MatchAttemptMapper;
import com.nongzhushou.matchflow.entity.MatchTaskEntity;
import com.nongzhushou.matchflow.mapper.MatchTaskMapper;
import com.nongzhushou.matchflow.service.MatchFlowService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemandServiceImplTest {

    @Mock
    private DemandMapper demandMapper;

    @Mock
    private MatchFlowService matchFlowService;

    @Mock
    private MatchAttemptMapper matchAttemptMapper;

    @Mock
    private ContactSessionMapper contactSessionMapper;

    @Mock
    private MatchTaskMapper matchTaskMapper;

    private DemandServiceImpl demandService;

    @BeforeEach
    void setUp() {
        demandService = new DemandServiceImpl(
                demandMapper,
                matchFlowService,
                new ObjectMapper(),
                matchAttemptMapper,
                contactSessionMapper,
                matchTaskMapper
        );
    }

    @Test
    void matchStatus_returnsChineseStatusFields() {
        DemandEntity demand = new DemandEntity();
        demand.setId(1L);
        demand.setFarmerId(10001L);
        demand.setStatus("MATCHING");
        demand.setCurrentMatchAttemptId(10L);

        MatchTaskEntity task = new MatchTaskEntity();
        task.setId(20L);
        task.setDemandId(1L);
        task.setStatus("MATCHING");

        MatchAttemptEntity attempt = new MatchAttemptEntity();
        attempt.setId(10L);
        attempt.setDemandId(1L);
        attempt.setStatus("PENDING_RESPONSE");

        when(demandMapper.selectById(1L)).thenReturn(demand);
        when(matchTaskMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(task);
        when(matchAttemptMapper.selectById(10L)).thenReturn(attempt);
        when(matchAttemptMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(attempt));

        var result = demandService.matchStatus(1L);
        var explain = (java.util.Map<?, ?>) result.get("explain");
        var currentAttempt = (java.util.Map<?, ?>) result.get("currentAttempt");
        var matchTask = (java.util.Map<?, ?>) result.get("matchTask");

        assertEquals("匹配中", result.get("statusLabel"));
        assertEquals("系统正在为您匹配合适的农机主", result.get("statusDescription"));
        assertEquals("匹配中", explain.get("statusText"));
        assertEquals("待机主响应", currentAttempt.get("statusLabel"));
        assertEquals("待匹配", matchTask.get("statusLabel"));
    }
}
