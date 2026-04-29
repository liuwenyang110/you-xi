package com.nongzhushou.admin.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.nongzhushou.admin.mapper.AdminAuditMapper;
import com.nongzhushou.collab.mapper.CollabMessageMapper;
import com.nongzhushou.collab.mapper.CollabSessionEventMapper;
import com.nongzhushou.contact.mapper.ContactSessionMapper;
import com.nongzhushou.demand.mapper.DemandMapper;
import com.nongzhushou.equipment.mapper.EquipmentMapper;
import com.nongzhushou.match.mapper.MatchAttemptMapper;
import com.nongzhushou.order.mapper.OrderInfoMapper;
import com.nongzhushou.report.mapper.ReportRecordMapper;
import com.nongzhushou.serviceitem.mapper.ServiceItemMapper;
import com.nongzhushou.user.mapper.UserAccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplCollabTest {

    @Mock private DemandMapper demandMapper;
    @Mock private EquipmentMapper equipmentMapper;
    @Mock private UserAccountMapper userAccountMapper;
    @Mock private ServiceItemMapper serviceItemMapper;
    @Mock private MatchAttemptMapper matchAttemptMapper;
    @Mock private OrderInfoMapper orderInfoMapper;
    @Mock private ReportRecordMapper reportRecordMapper;
    @Mock private AdminAuditMapper adminAuditMapper;
    @Mock private ContactSessionMapper contactSessionMapper;
    @Mock private CollabMessageMapper collabMessageMapper;
    @Mock private CollabSessionEventMapper collabSessionEventMapper;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(
                demandMapper,
                equipmentMapper,
                userAccountMapper,
                serviceItemMapper,
                matchAttemptMapper,
                orderInfoMapper,
                reportRecordMapper,
                adminAuditMapper,
                contactSessionMapper,
                collabMessageMapper,
                collabSessionEventMapper
        );
    }

    @Test
    void dashboard_includesCollabGovernanceMetrics() {
        when(demandMapper.selectCount(null)).thenReturn(6L);
        when(equipmentMapper.selectCount(null)).thenReturn(4L);
        when(serviceItemMapper.selectCount(null)).thenReturn(8L);
        when(reportRecordMapper.selectCount(null)).thenReturn(2L);
        when(contactSessionMapper.selectCount(null)).thenReturn(5L);
        when(collabMessageMapper.selectCount(null)).thenReturn(12L);
        when(collabSessionEventMapper.selectCount(null)).thenReturn(7L);

        var result = adminService.dashboard();

        assertEquals(5L, result.get("collabSessionCount"));
        assertEquals(12L, result.get("collabMessageCount"));
        assertEquals(7L, result.get("collabEventCount"));
    }
}
