package com.nongzhushou.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nongzhushou.common.enums.ApproveStatus;
import com.nongzhushou.common.enums.EquipmentStatus;
import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import com.nongzhushou.common.security.AuthContext;
import com.nongzhushou.equipment.entity.EquipmentEntity;
import com.nongzhushou.equipment.dto.EquipmentCreateRequest;
import com.nongzhushou.equipment.mapper.EquipmentMapper;
import com.nongzhushou.equipment.service.EquipmentService;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentMapper equipmentMapper;

    public EquipmentServiceImpl(EquipmentMapper equipmentMapper) {
        this.equipmentMapper = equipmentMapper;
    }

    @Override
    public Long create(EquipmentCreateRequest request) {
        Long ownerId = AuthContext.currentUserId();
        EquipmentEntity entity = new EquipmentEntity();
        entity.setOwnerId(ownerId);
        entity.setMachineTypeId(request.getMachineTypeId());
        entity.setEquipmentName(request.getEquipmentName());
        entity.setBrandModel(request.getBrandModel());
        entity.setQuantity(request.getQuantity());
        entity.setBaseRegionCode(request.getBaseRegionCode());
        entity.setServiceRadiusKm(request.getServiceRadiusKm());
        entity.setCurrentLat(request.getCurrentLat() == null ? null : BigDecimal.valueOf(request.getCurrentLat()));
        entity.setCurrentLng(request.getCurrentLng() == null ? null : BigDecimal.valueOf(request.getCurrentLng()));
        entity.setCurrentStatus(EquipmentStatus.IDLE.name());
        entity.setApproveStatus(ApproveStatus.PENDING.name());
        equipmentMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public Map<String, Object> update(Long id, EquipmentCreateRequest request) {
        EquipmentEntity entity = equipmentMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "equipment not found");
        }
        Long ownerId = AuthContext.currentUserId();
        if (!ownerId.equals(entity.getOwnerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "no permission to access equipment");
        }
        entity.setMachineTypeId(request.getMachineTypeId());
        entity.setEquipmentName(request.getEquipmentName());
        entity.setBrandModel(request.getBrandModel());
        entity.setQuantity(request.getQuantity());
        entity.setBaseRegionCode(request.getBaseRegionCode());
        entity.setServiceRadiusKm(request.getServiceRadiusKm());
        entity.setCurrentLat(request.getCurrentLat() == null ? null : BigDecimal.valueOf(request.getCurrentLat()));
        entity.setCurrentLng(request.getCurrentLng() == null ? null : BigDecimal.valueOf(request.getCurrentLng()));
        equipmentMapper.updateById(entity);
        return toMap(entity);
    }

    @Override
    public List<Map<String, Object>> list() {
        Long ownerId = AuthContext.currentUserId();
        return equipmentMapper.selectList(new LambdaQueryWrapper<EquipmentEntity>()
                        .eq(EquipmentEntity::getOwnerId, ownerId)
                        .orderByAsc(EquipmentEntity::getId))
                .stream()
                .map(this::toMap)
                .toList();
    }

    private Map<String, Object> toMap(EquipmentEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("ownerId", entity.getOwnerId());
        map.put("machineTypeId", entity.getMachineTypeId());
        map.put("equipmentName", entity.getEquipmentName());
        map.put("brandModel", entity.getBrandModel());
        map.put("quantity", entity.getQuantity());
        map.put("baseRegionCode", entity.getBaseRegionCode());
        map.put("serviceRadiusKm", entity.getServiceRadiusKm());
        map.put("currentLat", entity.getCurrentLat());
        map.put("currentLng", entity.getCurrentLng());
        map.put("currentStatus", entity.getCurrentStatus());
        map.put("approveStatus", entity.getApproveStatus());
        return map;
    }
}
