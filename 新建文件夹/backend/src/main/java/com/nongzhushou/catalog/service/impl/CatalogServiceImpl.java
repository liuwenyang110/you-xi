package com.nongzhushou.catalog.service.impl;

import com.nongzhushou.catalog.service.CatalogService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CatalogServiceImpl implements CatalogService {

    @Override
    public List<Map<String, Object>> serviceCategories() {
        return List.of(
                Map.of("id", 1, "name", "Tillage", "code", "TILL"),
                Map.of("id", 2, "name", "Sowing", "code", "SOW"),
                Map.of("id", 3, "name", "Harvest", "code", "HARVEST")
        );
    }

    @Override
    public List<Map<String, Object>> machineTypes() {
        return List.of(
                Map.of("id", 101, "name", "Wheel Tractor", "horsepowerRange", "80-120"),
                Map.of("id", 102, "name", "Combine Harvester", "horsepowerRange", "120-200")
        );
    }

    @Override
    public List<Map<String, Object>> demandForm(Long subcategoryId) {
        return List.of(
                Map.of("fieldKey", "cropCode", "fieldLabel", "Crop", "fieldType", "text", "required", true),
                Map.of("fieldKey", "areaMu", "fieldLabel", "Area (mu)", "fieldType", "number", "required", true),
                Map.of("fieldKey", "scheduleType", "fieldLabel", "Schedule", "fieldType", "select", "required", true)
        );
    }

    @Override
    public List<Map<String, Object>> equipmentForm(Long machineTypeId) {
        return List.of(
                Map.of("fieldKey", "equipmentName", "fieldLabel", "Equipment Name", "fieldType", "text", "required", true),
                Map.of("fieldKey", "brandModel", "fieldLabel", "Brand Model", "fieldType", "text", "required", false),
                Map.of("fieldKey", "serviceRadiusKm", "fieldLabel", "Service Radius", "fieldType", "number", "required", true)
        );
    }
}
