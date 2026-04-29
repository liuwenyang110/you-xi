package com.nongzhushou.catalog.service;

import java.util.List;
import java.util.Map;

public interface CatalogService {

    List<Map<String, Object>> serviceCategories();

    List<Map<String, Object>> machineTypes();

    List<Map<String, Object>> demandForm(Long subcategoryId);

    List<Map<String, Object>> equipmentForm(Long machineTypeId);
}
