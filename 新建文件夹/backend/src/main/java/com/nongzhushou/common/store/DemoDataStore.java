package com.nongzhushou.common.store;

import com.nongzhushou.demand.dto.DemandCreateRequest;
import com.nongzhushou.equipment.dto.EquipmentCreateRequest;
import com.nongzhushou.report.dto.ReportCreateRequest;
import com.nongzhushou.serviceitem.dto.ServiceItemCreateRequest;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class DemoDataStore {

    private final AtomicLong equipmentSeq = new AtomicLong(1000);
    private final AtomicLong serviceItemSeq = new AtomicLong(2000);
    private final AtomicLong demandSeq = new AtomicLong(3000);
    private final AtomicLong matchSeq = new AtomicLong(4000);
    private final AtomicLong contactSeq = new AtomicLong(5000);
    private final AtomicLong orderSeq = new AtomicLong(6000);
    private final AtomicLong reportSeq = new AtomicLong(7000);
    private final AtomicLong auditSeq = new AtomicLong(8000);

    private final Map<Long, Map<String, Object>> users = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> equipmentStore = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> serviceItemStore = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> demandStore = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> matchAttemptStore = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> contactSessionStore = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> orderStore = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> reportStore = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> adminAuditStore = new ConcurrentHashMap<>();

    @PostConstruct
    public synchronized void init() {
        seedUsers();
        if (!serviceItemStore.isEmpty()) {
            return;
        }

        EquipmentCreateRequest equipment = new EquipmentCreateRequest();
        equipment.setMachineTypeId(102L);
        equipment.setEquipmentName("履带式联合收割机");
        equipment.setBrandModel("4LZ-4.0");
        equipment.setQuantity(1);
        equipment.setBaseRegionCode("330106001001");
        equipment.setServiceRadiusKm(15);
        long equipmentId = createEquipment(equipment, 10002L, "PASSED");

        ServiceItemCreateRequest serviceItem = new ServiceItemCreateRequest();
        serviceItem.setServiceCategoryId(3L);
        serviceItem.setServiceSubcategoryId(301L);
        serviceItem.setServiceName("水稻收割服务");
        serviceItem.setMachineBindingType("single");
        serviceItem.setMainEquipmentId(equipmentId);
        serviceItem.setRelatedEquipmentIds(List.of(equipmentId));
        serviceItem.setCropTags(List.of("RICE"));
        serviceItem.setTerrainTags(List.of("PLAIN", "WET_FIELD"));
        serviceItem.setPlotTags(List.of("SMALL_PLOT"));
        serviceItem.setMinAreaMu(BigDecimal.ONE);
        serviceItem.setMaxAreaMu(new BigDecimal("80"));
        serviceItem.setAvailableTimeDesc("今日可作业");
        serviceItem.setServiceRadiusKm(15);
        serviceItem.setIsAcceptingOrders(true);
        createServiceItem(serviceItem, 10002L, "PASSED", "ACTIVE");

        DemandCreateRequest demand = new DemandCreateRequest();
        demand.setServiceCategoryId(3L);
        demand.setServiceSubcategoryId(301L);
        demand.setCropCode("RICE");
        demand.setAreaMu(new BigDecimal("12.5"));
        demand.setScheduleType("today");
        demand.setLat(30.1233);
        demand.setLng(120.1230);
        demand.setVillageName("东河村");
        demand.setVoiceText("今天想收水稻，地里有点湿");
        demand.setRemark("靠河边地块");
        demand.setRequirementJson(Map.of("fieldWet", true, "canLargeMachineEnter", "NO"));
        createDemand(demand, 10001L);
    }

    private void seedUsers() {
        users.put(10001L, user(10001L, "13800000001", "farmer", "normal", "normal"));
        users.put(10002L, user(10002L, "13800000002", "owner", "normal", "normal"));
        users.put(10003L, user(10003L, "13800000003", "admin", "normal", "normal"));
    }

    private Map<String, Object> user(Long userId, String phone, String role, String status, String uiMode) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", userId);
        data.put("phone", phone);
        data.put("currentRole", role);
        data.put("status", status);
        data.put("uiMode", uiMode);
        data.put("realnameStatus", "passed");
        data.put("roles", List.of("farmer", "owner"));
        return data;
    }

    public synchronized Map<String, Object> getUser(Long userId) {
        return copy(users.get(userId));
    }

    public synchronized Map<String, Object> updateUserRole(Long userId, String role) {
        Map<String, Object> user = users.get(userId);
        user.put("currentRole", role.toLowerCase());
        return copy(user);
    }

    public synchronized Map<String, Object> updateUserUiMode(Long userId, String uiMode) {
        Map<String, Object> user = users.get(userId);
        user.put("uiMode", uiMode.toLowerCase());
        return copy(user);
    }

    public synchronized long createEquipment(EquipmentCreateRequest request, Long ownerId, String approveStatus) {
        long id = equipmentSeq.incrementAndGet();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("ownerId", ownerId);
        data.put("machineTypeId", request.getMachineTypeId());
        data.put("equipmentName", request.getEquipmentName());
        data.put("brandModel", request.getBrandModel());
        data.put("quantity", request.getQuantity());
        data.put("baseRegionCode", request.getBaseRegionCode());
        data.put("serviceRadiusKm", request.getServiceRadiusKm());
        data.put("currentStatus", "IDLE");
        data.put("approveStatus", approveStatus);
        data.put("createdAt", LocalDateTime.now().toString());
        equipmentStore.put(id, data);
        return id;
    }

    public synchronized Map<String, Object> updateEquipment(Long id, EquipmentCreateRequest request) {
        Map<String, Object> item = equipmentStore.get(id);
        if (item == null) {
            return Map.of("id", id, "updated", false);
        }
        item.put("machineTypeId", request.getMachineTypeId());
        item.put("equipmentName", request.getEquipmentName());
        item.put("brandModel", request.getBrandModel());
        item.put("quantity", request.getQuantity());
        item.put("baseRegionCode", request.getBaseRegionCode());
        item.put("serviceRadiusKm", request.getServiceRadiusKm());
        item.put("updatedAt", LocalDateTime.now().toString());
        return copy(item);
    }

    public synchronized List<Map<String, Object>> listEquipment() {
        return snapshot(equipmentStore);
    }

    public synchronized long createServiceItem(
            ServiceItemCreateRequest request,
            Long ownerId,
            String approveStatus,
            String status
    ) {
        long id = serviceItemSeq.incrementAndGet();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("ownerId", ownerId);
        data.put("serviceCategoryId", request.getServiceCategoryId());
        data.put("serviceSubcategoryId", request.getServiceSubcategoryId());
        data.put("serviceName", request.getServiceName());
        data.put("machineBindingType", request.getMachineBindingType());
        data.put("mainEquipmentId", request.getMainEquipmentId());
        data.put("relatedEquipmentIds", new ArrayList<>(request.getRelatedEquipmentIds()));
        data.put("cropTags", new ArrayList<>(request.getCropTags()));
        data.put("terrainTags", new ArrayList<>(request.getTerrainTags()));
        data.put("plotTags", new ArrayList<>(request.getPlotTags()));
        data.put("minAreaMu", request.getMinAreaMu());
        data.put("maxAreaMu", request.getMaxAreaMu());
        data.put("availableTimeDesc", request.getAvailableTimeDesc());
        data.put("serviceRadiusKm", request.getServiceRadiusKm());
        data.put("isAcceptingOrders", request.getIsAcceptingOrders());
        data.put("approveStatus", approveStatus);
        data.put("status", status);
        data.put("createdAt", LocalDateTime.now().toString());
        serviceItemStore.put(id, data);
        return id;
    }

    public synchronized Map<String, Object> updateServiceItem(Long id, ServiceItemCreateRequest request) {
        Map<String, Object> item = serviceItemStore.get(id);
        if (item == null) {
            return Map.of("id", id, "updated", false);
        }
        item.put("serviceCategoryId", request.getServiceCategoryId());
        item.put("serviceSubcategoryId", request.getServiceSubcategoryId());
        item.put("serviceName", request.getServiceName());
        item.put("machineBindingType", request.getMachineBindingType());
        item.put("mainEquipmentId", request.getMainEquipmentId());
        item.put("relatedEquipmentIds", new ArrayList<>(request.getRelatedEquipmentIds()));
        item.put("cropTags", new ArrayList<>(request.getCropTags()));
        item.put("terrainTags", new ArrayList<>(request.getTerrainTags()));
        item.put("plotTags", new ArrayList<>(request.getPlotTags()));
        item.put("minAreaMu", request.getMinAreaMu());
        item.put("maxAreaMu", request.getMaxAreaMu());
        item.put("availableTimeDesc", request.getAvailableTimeDesc());
        item.put("serviceRadiusKm", request.getServiceRadiusKm());
        item.put("isAcceptingOrders", request.getIsAcceptingOrders());
        item.put("updatedAt", LocalDateTime.now().toString());
        return copy(item);
    }

    public synchronized List<Map<String, Object>> listServiceItems() {
        return snapshot(serviceItemStore);
    }

    public synchronized long createDemand(DemandCreateRequest request, Long farmerId) {
        long id = demandSeq.incrementAndGet();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("farmerId", farmerId);
        data.put("serviceCategoryId", request.getServiceCategoryId());
        data.put("serviceSubcategoryId", request.getServiceSubcategoryId());
        data.put("cropCode", request.getCropCode());
        data.put("areaMu", request.getAreaMu());
        data.put("scheduleType", request.getScheduleType());
        data.put("expectedDate", request.getExpectedDate());
        data.put("lat", request.getLat());
        data.put("lng", request.getLng());
        data.put("villageName", request.getVillageName());
        data.put("voiceAssetId", request.getVoiceAssetId());
        data.put("voiceText", request.getVoiceText());
        data.put("remark", request.getRemark());
        data.put("requirementJson", request.getRequirementJson());
        data.put("status", "MATCHING");
        data.put("publishedAt", LocalDateTime.now().toString());
        data.put("matchAttemptCount", 0);
        demandStore.put(id, data);
        createMatchAttempt(id);
        return id;
    }

    public synchronized List<Map<String, Object>> listDemands() {
        return snapshot(demandStore);
    }

    public synchronized Map<String, Object> getDemand(Long id) {
        return copy(demandStore.get(id));
    }

    public synchronized List<Map<String, Object>> listMatchTasks() {
        return snapshot(matchAttemptStore);
    }

    public synchronized Map<String, Object> getMatchTask(Long id) {
        return copy(matchAttemptStore.get(id));
    }

    public synchronized Map<String, Object> retryDemandMatch(Long demandId) {
        Map<String, Object> attempt = createMatchAttempt(demandId);
        addAudit("retry_match", "对需求 " + demandId + " 发起人工重试");
        return attempt == null
                ? Map.of("demandId", demandId, "matched", false, "message", "没有可继续派单的农机主")
                : Map.of("demandId", demandId, "matched", true, "currentMatchAttempt", attempt);
    }

    public synchronized Map<String, Object> acceptMatch(Long matchId) {
        Map<String, Object> match = matchAttemptStore.get(matchId);
        if (match == null) {
            return Map.of("id", matchId, "accepted", false);
        }
        match.put("status", "OWNER_ACCEPTED");
        match.put("handledAt", LocalDateTime.now().toString());

        Map<String, Object> demand = demandStore.get((Long) match.get("demandId"));
        demand.put("status", "WAITING_FARMER_CONFIRM");

        long contactId = contactSeq.incrementAndGet();
        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("id", contactId);
        contact.put("demandId", match.get("demandId"));
        contact.put("matchAttemptId", matchId);
        contact.put("ownerId", match.get("ownerId"));
        contact.put("farmerId", demand.get("farmerId"));
        contact.put("status", "WAITING_FARMER_CONFIRM");
        contact.put("activeFlag", true);
        contact.put("maskedPhone", "170****0002");
        contact.put("createdAt", LocalDateTime.now().toString());
        contactSessionStore.put(contactId, contact);

        long orderId = orderSeq.incrementAndGet();
        Map<String, Object> order = new LinkedHashMap<>();
        order.put("id", orderId);
        order.put("demandId", match.get("demandId"));
        order.put("matchAttemptId", matchId);
        order.put("contactSessionId", contactId);
        order.put("ownerId", match.get("ownerId"));
        order.put("farmerId", demand.get("farmerId"));
        order.put("serviceItemId", match.get("serviceItemId"));
        order.put("status", "PENDING_CONTACT");
        order.put("ownerConfirmedFinish", false);
        order.put("farmerConfirmedFinish", false);
        order.put("createdAt", LocalDateTime.now().toString());
        orderStore.put(orderId, order);

        demand.put("currentMatchAttemptId", matchId);
        demand.put("currentContactSessionId", contactId);
        demand.put("currentOrderId", orderId);
        return Map.of("matchAttempt", copy(match), "contactSession", copy(contact), "order", copy(order));
    }

    public synchronized Map<String, Object> rejectMatch(Long matchId) {
        Map<String, Object> match = matchAttemptStore.get(matchId);
        if (match == null) {
            return Map.of("id", matchId, "rejected", false);
        }
        match.put("status", "OWNER_REJECTED");
        match.put("handledAt", LocalDateTime.now().toString());
        Long demandId = (Long) match.get("demandId");
        Map<String, Object> retried = createMatchAttempt(demandId);
        return Map.of("matchAttempt", copy(match), "nextMatchAttempt", retried);
    }

    public synchronized List<Map<String, Object>> listContactSessions() {
        return snapshot(contactSessionStore);
    }

    public synchronized Map<String, Object> confirmContact(Long contactId) {
        Map<String, Object> contact = contactSessionStore.get(contactId);
        if (contact == null) {
            return Map.of("id", contactId, "confirmed", false);
        }
        contact.put("status", "CONFIRMED");
        contact.put("activeFlag", false);
        contact.put("confirmedAt", LocalDateTime.now().toString());

        Long demandId = (Long) contact.get("demandId");
        Map<String, Object> demand = demandStore.get(demandId);
        demand.put("status", "IN_SERVICE");

        Map<String, Object> order = findOrderByContactSession(contactId);
        if (order != null) {
            order.put("status", "SERVING");
            order.put("contactConfirmedAt", LocalDateTime.now().toString());
        }
        return Map.of("contactSession", copy(contact), "order", copy(order), "demand", copy(demand));
    }

    public synchronized List<Map<String, Object>> listOrders() {
        return snapshot(orderStore);
    }

    public synchronized Map<String, Object> getOrder(Long id) {
        return copy(orderStore.get(id));
    }

    public synchronized Map<String, Object> confirmOrderFinish(Long id, String actorRole) {
        Map<String, Object> order = orderStore.get(id);
        if (order == null) {
            return Map.of("id", id, "updated", false);
        }
        String role = actorRole == null ? "" : actorRole.toLowerCase();
        if ("owner".equals(role)) {
            order.put("ownerConfirmedFinish", true);
        }
        if ("farmer".equals(role)) {
            order.put("farmerConfirmedFinish", true);
        }
        boolean done = Boolean.TRUE.equals(order.get("ownerConfirmedFinish"))
                && Boolean.TRUE.equals(order.get("farmerConfirmedFinish"));
        order.put("status", done ? "COMPLETED" : "FINISHED_PENDING_CONFIRM");
        order.put("updatedAt", LocalDateTime.now().toString());

        Map<String, Object> demand = demandStore.get((Long) order.get("demandId"));
        if (demand != null && done) {
            demand.put("status", "COMPLETED");
        }
        return Map.of("order", copy(order), "demand", copy(demand));
    }

    public synchronized long createReport(ReportCreateRequest request, Long reporterId) {
        long id = reportSeq.incrementAndGet();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("reporterId", reporterId);
        data.put("reportedUserId", request.getReportedUserId());
        data.put("orderId", request.getOrderId());
        data.put("demandId", request.getDemandId());
        data.put("reportType", request.getReportType());
        data.put("content", request.getContent());
        data.put("evidenceAssetIds", new ArrayList<>(request.getEvidenceAssetIds()));
        data.put("status", "PENDING");
        data.put("createdAt", LocalDateTime.now().toString());
        reportStore.put(id, data);
        addAudit("create_report", "新增举报 " + id);
        return id;
    }

    public synchronized List<Map<String, Object>> listReports() {
        return snapshot(reportStore);
    }

    public synchronized Map<String, Object> dashboard() {
        long matchSuccessCount = matchAttemptStore.values().stream()
                .filter(item -> Objects.equals(item.get("status"), "OWNER_ACCEPTED"))
                .count();
        long completedOrderCount = orderStore.values().stream()
                .filter(item -> Objects.equals(item.get("status"), "COMPLETED"))
                .count();
        return Map.of(
                "userCount", users.size(),
                "demandCount", demandStore.size(),
                "serviceItemCount", serviceItemStore.size(),
                "matchSuccessCount", matchSuccessCount,
                "completedOrderCount", completedOrderCount,
                "reportCount", reportStore.size()
        );
    }

    public synchronized List<Map<String, Object>> listAdminAudits() {
        return snapshot(adminAuditStore);
    }

    private Map<String, Object> createMatchAttempt(Long demandId) {
        Map<String, Object> demand = demandStore.get(demandId);
        if (demand == null) {
            return null;
        }
        Set<Long> attemptedOwners = new LinkedHashSet<>();
        for (Map<String, Object> item : matchAttemptStore.values()) {
            if (Objects.equals(item.get("demandId"), demandId)) {
                attemptedOwners.add((Long) item.get("ownerId"));
            }
        }

        Optional<Map<String, Object>> candidateOptional = serviceItemStore.values().stream()
                .filter(item -> Objects.equals(item.get("status"), "ACTIVE"))
                .filter(item -> Objects.equals(item.get("approveStatus"), "PASSED"))
                .filter(item -> Boolean.TRUE.equals(item.get("isAcceptingOrders")))
                .filter(item -> Objects.equals(item.get("serviceSubcategoryId"), demand.get("serviceSubcategoryId")))
                .filter(item -> !attemptedOwners.contains((Long) item.get("ownerId")))
                .sorted(Comparator.comparing(item -> (Long) item.get("id")))
                .findFirst();

        if (candidateOptional.isEmpty()) {
            demand.put("status", "CLOSED");
            return null;
        }

        Map<String, Object> candidate = candidateOptional.get();
        long id = matchSeq.incrementAndGet();
        Map<String, Object> match = new LinkedHashMap<>();
        match.put("id", id);
        match.put("demandId", demandId);
        match.put("ownerId", candidate.get("ownerId"));
        match.put("serviceItemId", candidate.get("id"));
        match.put("serviceName", candidate.get("serviceName"));
        match.put("villageName", demand.get("villageName"));
        match.put("distanceLayer", "0-3km");
        match.put("status", "PENDING_RESPONSE");
        match.put("responseDeadlineSeconds", 120);
        match.put("createdAt", LocalDateTime.now().toString());
        matchAttemptStore.put(id, match);

        demand.put("status", "MATCHING");
        demand.put("currentMatchAttemptId", id);
        demand.put("matchAttemptCount", ((Number) demand.get("matchAttemptCount")).intValue() + 1);
        return copy(match);
    }

    private void addAudit(String action, String content) {
        long id = auditSeq.incrementAndGet();
        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("id", id);
        audit.put("action", action);
        audit.put("content", content);
        audit.put("createdAt", LocalDateTime.now().toString());
        adminAuditStore.put(id, audit);
    }

    private Map<String, Object> findOrderByContactSession(Long contactId) {
        return orderStore.values().stream()
                .filter(item -> Objects.equals(item.get("contactSessionId"), contactId))
                .findFirst()
                .orElse(null);
    }

    private List<Map<String, Object>> snapshot(Map<Long, Map<String, Object>> store) {
        return store.values().stream()
                .sorted(Comparator.comparing(item -> ((Number) item.get("id")).longValue()))
                .map(this::copy)
                .toList();
    }

    private Map<String, Object> copy(Map<String, Object> source) {
        if (source == null) {
            return null;
        }
        return new LinkedHashMap<>(source);
    }
}
