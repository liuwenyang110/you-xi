package com.nongzhushou.order.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.order.entity.OrderInfoEntity;
import com.nongzhushou.order.mapper.OrderInfoMapper;
import com.nongzhushou.demand.mapper.DemandMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderInfoMapper orderInfoMapper;

    @Mock
    private DemandMapper demandMapper;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderInfoMapper, demandMapper);
    }

    @Test
    void detail_returnsStatusLabelAndDescription() {
        OrderInfoEntity entity = new OrderInfoEntity();
        entity.setId(1L);
        entity.setFarmerId(10001L);
        entity.setStatus("COMPLETED");
        when(orderInfoMapper.selectById(1L)).thenReturn(entity);

        var result = orderService.detail(1L);

        assertEquals("COMPLETED", result.get("status"));
        assertEquals("已完成", result.get("statusLabel"));
        assertEquals("订单已完成", result.get("statusDescription"));
    }

    @Test
    void detail_throwsWhenOrderMissing() {
        when(orderInfoMapper.selectById(99L)).thenReturn(null);

        assertThrows(BizException.class, () -> orderService.detail(99L));
    }
}
