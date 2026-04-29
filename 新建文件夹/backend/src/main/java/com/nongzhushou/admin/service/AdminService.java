package com.nongzhushou.admin.service;

import java.util.List;
import java.util.Map;

public interface AdminService {

    Map<String, Object> dashboard();

    List<Map<String, Object>> matchTasks();

    List<Map<String, Object>> demands();

    List<Map<String, Object>> orders();

    List<Map<String, Object>> users();

    List<Map<String, Object>> equipment();

    List<Map<String, Object>> reports();

    List<Map<String, Object>> audits();

    Map<String, Object> collabSummary();

    List<Map<String, Object>> collabSessions();
}
