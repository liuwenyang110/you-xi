package com.nongzhushou.match.service;

import java.util.List;
import java.util.Map;

public interface MatchService {

    List<Map<String, Object>> listTasks();

    Map<String, Object> getTask(Long id);

    Map<String, Object> accept(Long id);

    Map<String, Object> reject(Long id);

    Map<String, Object> retry(Long demandId);
}
