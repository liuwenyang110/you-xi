package com.nongzhushou.order.service;

import java.util.List;
import java.util.Map;

public interface OrderService {

    List<Map<String, Object>> list();

    Map<String, Object> detail(Long id);

    Map<String, Object> confirmFinish(Long id, String actorRole);
}
