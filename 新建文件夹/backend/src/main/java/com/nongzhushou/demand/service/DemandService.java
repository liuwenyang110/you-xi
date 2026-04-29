package com.nongzhushou.demand.service;

import com.nongzhushou.demand.dto.DemandCreateRequest;
import java.util.List;
import java.util.Map;

public interface DemandService {

    Long createDemand(DemandCreateRequest request);

    List<Map<String, Object>> listDemands();

    Map<String, Object> detail(Long id);

    Map<String, Object> matchStatus(Long id);

    Map<String, Object> cancelDemand(Long id);
}
