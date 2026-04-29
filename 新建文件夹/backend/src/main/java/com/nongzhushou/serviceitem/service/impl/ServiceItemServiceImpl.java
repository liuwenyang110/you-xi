package com.nongzhushou.serviceitem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nongzhushou.common.enums.ApproveStatus;
import com.nongzhushou.common.enums.ServiceItemStatus;
import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import com.nongzhushou.common.security.AuthContext;
import com.nongzhushou.serviceitem.entity.ServiceItemEntity;
import com.nongzhushou.serviceitem.mapper.ServiceItemMapper;
import com.nongzhushou.serviceitem.dto.ServiceItemCreateRequest;
import com.nongzhushou.serviceitem.service.ServiceItemService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ServiceItemServiceImpl implements ServiceItemService {

    private final ServiceItemMapper serviceItemMapper;

    public ServiceItemServiceImpl(ServiceItemMapper serviceItemMapper) {
        this.serviceItemMapper = serviceItemMapper;
    }

    @Override
    public Long create(ServiceItemCreateRequest request) {
        Long ownerId = AuthContext.currentUserId();
        ServiceItemEntity entity = new ServiceItemEntity();
        entity.setOwnerId(ownerId);
        entity.setServiceCategoryId(request.getServiceCategoryId());
        entity.setServiceSubcategoryId(request.getServiceSubcategoryId());
        entity.setServiceName(request.getServiceName());
        entity.setMachineBindingType(request.getMachineBindingType());
        entity.setMainEquipmentId(request.getMainEquipmentId());
        entity.setRelatedEquipmentIds(joinLongs(request.getRelatedEquipmentIds()));
        entity.setCropTags(joinStrings(request.getCropTags()));
        entity.setTerrainTags(joinStrings(request.getTerrainTags()));
        entity.setPlotTags(joinStrings(request.getPlotTags()));
        entity.setMinAreaMu(request.getMinAreaMu());
        entity.setMaxAreaMu(request.getMaxAreaMu());
        entity.setAvailableTimeDesc(request.getAvailableTimeDesc());
        entity.setServiceRadiusKm(request.getServiceRadiusKm());
        entity.setIsAcceptingOrders(Boolean.TRUE.equals(request.getIsAcceptingOrders()));
        entity.setApproveStatus(ApproveStatus.PENDING.name());
        entity.setStatus(ServiceItemStatus.ACTIVE.name());
        serviceItemMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public Map<String, Object> update(Long id, ServiceItemCreateRequest request) {
        ServiceItemEntity entity = serviceItemMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "service item not found");
        }
        Long ownerId = AuthContext.currentUserId();
        if (!ownerId.equals(entity.getOwnerId())) {
            throw new BizException(ErrorCode.FORBIDDEN, "no permission to access service item");
        }
        entity.setServiceCategoryId(request.getServiceCategoryId());
        entity.setServiceSubcategoryId(request.getServiceSubcategoryId());
        entity.setServiceName(request.getServiceName());
        entity.setMachineBindingType(request.getMachineBindingType());
        entity.setMainEquipmentId(request.getMainEquipmentId());
        entity.setRelatedEquipmentIds(joinLongs(request.getRelatedEquipmentIds()));
        entity.setCropTags(joinStrings(request.getCropTags()));
        entity.setTerrainTags(joinStrings(request.getTerrainTags()));
        entity.setPlotTags(joinStrings(request.getPlotTags()));
        entity.setMinAreaMu(request.getMinAreaMu());
        entity.setMaxAreaMu(request.getMaxAreaMu());
        entity.setAvailableTimeDesc(request.getAvailableTimeDesc());
        entity.setServiceRadiusKm(request.getServiceRadiusKm());
        entity.setIsAcceptingOrders(Boolean.TRUE.equals(request.getIsAcceptingOrders()));
        serviceItemMapper.updateById(entity);
        return toMap(entity);
    }

    @Override
    public List<Map<String, Object>> list() {
        Long ownerId = AuthContext.currentUserId();
        return serviceItemMapper.selectList(new LambdaQueryWrapper<ServiceItemEntity>()
                        .eq(ServiceItemEntity::getOwnerId, ownerId)
                        .orderByAsc(ServiceItemEntity::getId))
                .stream()
                .map(this::toMap)
                .toList();
    }

    private String joinLongs(List<Long> values) {
        return values == null || values.isEmpty()
                ? null
                : values.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(null);
    }

    private String joinStrings(List<String> values) {
        return values == null || values.isEmpty() ? null : String.join(",", values);
    }

    private Map<String, Object> toMap(ServiceItemEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("ownerId", entity.getOwnerId());
        map.put("serviceCategoryId", entity.getServiceCategoryId());
        map.put("serviceSubcategoryId", entity.getServiceSubcategoryId());
        map.put("serviceName", entity.getServiceName());
        map.put("machineBindingType", entity.getMachineBindingType());
        map.put("mainEquipmentId", entity.getMainEquipmentId());
        map.put("relatedEquipmentIds", entity.getRelatedEquipmentIds());
        map.put("cropTags", entity.getCropTags());
        map.put("terrainTags", entity.getTerrainTags());
        map.put("plotTags", entity.getPlotTags());
        map.put("minAreaMu", entity.getMinAreaMu());
        map.put("maxAreaMu", entity.getMaxAreaMu());
        map.put("availableTimeDesc", entity.getAvailableTimeDesc());
        map.put("serviceRadiusKm", entity.getServiceRadiusKm());
        map.put("isAcceptingOrders", entity.getIsAcceptingOrders());
        map.put("approveStatus", entity.getApproveStatus());
        map.put("status", entity.getStatus());
        return map;
    }
}
