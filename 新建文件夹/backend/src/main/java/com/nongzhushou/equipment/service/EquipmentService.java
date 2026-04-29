package com.nongzhushou.equipment.service;

import com.nongzhushou.equipment.dto.EquipmentCreateRequest;
import java.util.List;
import java.util.Map;

public interface EquipmentService {

    Long create(EquipmentCreateRequest request);

    Map<String, Object> update(Long id, EquipmentCreateRequest request);

    List<Map<String, Object>> list();
}
