package com.nongzhushou.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nongzhushou.common.enums.DemandStatus;
import com.nongzhushou.common.enums.OrderStatus;
import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import com.nongzhushou.common.security.AuthContext;
import com.nongzhushou.demand.entity.DemandEntity;
import com.nongzhushou.demand.mapper.DemandMapper;
import com.nongzhushou.order.entity.OrderInfoEntity;
import com.nongzhushou.order.mapper.OrderInfoMapper;
import com.nongzhushou.order.service.OrderService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderInfoMapper orderInfoMapper;
    private final DemandMapper demandMapper;

    public OrderServiceImpl(OrderInfoMapper orderInfoMapper, DemandMapper demandMapper) {
        this.orderInfoMapper = orderInfoMapper;
        this.demandMapper = demandMapper;
    }

    @Override
    public List<Map<String, Object>> list() {
        Long currentUserId = AuthContext.currentUserId();
        return orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfoEntity>()
                        .eq(OrderInfoEntity::getFarmerId, currentUserId)
                        .orderByDesc(OrderInfoEntity::getId))
                .stream()
                .map(this::toMap)
                .toList();
    }

    @Override
    public Map<String, Object> detail(Long id) {
        OrderInfoEntity entity = orderInfoMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "order not found");
        }
        if (!AuthContext.currentUserId().equals(entity.getFarmerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看该订单");
        }
        return toMap(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> confirmFinish(Long id, String actorRole) {
        OrderInfoEntity order = orderInfoMapper.selectById(id);
        if (order == null) {
            log.warn("确认完成操作失败：订单ID[{}]不存在", id);
            throw new BizException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        if (OrderStatus.COMPLETED.name().equals(order.getStatus())) {
            DemandEntity completedDemand = demandMapper.selectById(order.getDemandId());
            return Map.of("order", toMap(order), "demandId", completedDemand == null ? null : completedDemand.getId());
        }
        String role = actorRole == null ? "" : actorRole.toLowerCase();
        Long currentUserId = AuthContext.currentUserId();
        if ("owner".equals(role)) {
            order.setOwnerConfirmedFinish(1);
            log.info("农机手[{}]确认了订单[{}]完成", currentUserId, id);
        } else if ("farmer".equals(role)) {
            if (!currentUserId.equals(order.getFarmerId())) {
                log.warn("越权操作：用户ID[{}]试图确认他人订单[{}]", currentUserId, id);
                throw new BizException(ErrorCode.FORBIDDEN, "无权确认该订单");
            }
            order.setFarmerConfirmedFinish(1);
            log.info("农户[{}]确认了订单[{}]完成", currentUserId, id);
        } else {
            throw new BizException(ErrorCode.INVALID_REQUEST, "角色参数无效");
        }
        boolean done = Integer.valueOf(1).equals(order.getOwnerConfirmedFinish())
                && Integer.valueOf(1).equals(order.getFarmerConfirmedFinish());
        
        if (done) {
            log.info("订单[{}]双方已确认，状态更新为 COMPLETED", id);
        }
        
        order.setStatus(done ? OrderStatus.COMPLETED.name() : OrderStatus.FINISHED_PENDING_CONFIRM.name());
        orderInfoMapper.updateById(order);

        DemandEntity demand = demandMapper.selectById(order.getDemandId());
        if (demand != null) {
            demand.setStatus(done ? DemandStatus.COMPLETED.name() : DemandStatus.FINISHED_PENDING_CONFIRM.name());
            demandMapper.updateById(demand);
        }
        return Map.of("order", toMap(order), "demandId", demand == null ? null : demand.getId());
    }

    private Map<String, Object> toMap(OrderInfoEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("demandId", entity.getDemandId());
        map.put("matchAttemptId", entity.getMatchAttemptId());
        map.put("contactSessionId", entity.getContactSessionId());
        map.put("ownerId", entity.getOwnerId());
        map.put("farmerId", entity.getFarmerId());
        map.put("serviceItemId", entity.getServiceItemId());
        map.put("status", entity.getStatus());
        map.put("statusLabel", OrderStatus.labelOf(entity.getStatus()));
        map.put("statusDescription", OrderStatus.descriptionOf(entity.getStatus()));
        map.put("ownerConfirmedFinish", entity.getOwnerConfirmedFinish());
        map.put("farmerConfirmedFinish", entity.getFarmerConfirmedFinish());
        map.put("contactConfirmedAt", entity.getContactConfirmedAt());
        return map;
    }
}
