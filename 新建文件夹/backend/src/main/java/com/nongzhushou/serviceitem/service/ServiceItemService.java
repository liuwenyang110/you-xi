package com.nongzhushou.serviceitem.service;

import com.nongzhushou.serviceitem.dto.ServiceItemCreateRequest;
import java.util.List;
import java.util.Map;

public interface ServiceItemService {

    Long create(ServiceItemCreateRequest request);

    Map<String, Object> update(Long id, ServiceItemCreateRequest request);

    List<Map<String, Object>> list();
}
