package com.nongzhushou.match.service.impl;

import com.nongzhushou.common.map.MapDistanceService;
import com.nongzhushou.common.util.JsonSetUtils;
import com.nongzhushou.match.mapper.MatchRuleQueryMapper;
import com.nongzhushou.match.model.DemandMatchView;
import com.nongzhushou.match.model.OwnerCandidate;
import com.nongzhushou.match.model.ServiceItemMatchView;
import com.nongzhushou.match.service.MatchRuleService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MatchRuleServiceImpl implements MatchRuleService {

    private final MatchRuleQueryMapper mapper;
    private final MapDistanceService mapDistanceService;

    public MatchRuleServiceImpl(MatchRuleQueryMapper mapper, MapDistanceService mapDistanceService) {
        this.mapper = mapper;
        this.mapDistanceService = mapDistanceService;
    }

    @Override
    public List<OwnerCandidate> findEligibleCandidates(Long demandId) {
        if (mapper.countActiveContactsByDemand(demandId) > 0) {
            return List.of();
        }
        DemandMatchView demand = mapper.selectDemandMatchView(demandId);
        if (demand == null) {
            return List.of();
        }
        demand.setTerrainTags(JsonSetUtils.toStringSet(extractJsonArray(demand.getRequirementJson(), "terrainTags")));
        demand.setPlotTags(JsonSetUtils.toStringSet(extractJsonArray(demand.getRequirementJson(), "plotTags")));

        List<ServiceItemMatchView> pool = mapper.selectEligibleBasePool(demand.getServiceSubcategoryId());
        if (pool == null || pool.isEmpty()) {
            return List.of();
        }

        List<OwnerCandidate> raw = new ArrayList<>();
        for (ServiceItemMatchView item : pool) {
            item.setCropTags(parseFlexibleTags(item.getCropTagsRaw()));
            item.setTerrainTags(parseFlexibleTags(item.getTerrainTagsRaw()));
            item.setPlotTags(parseFlexibleTags(item.getPlotTagsRaw()));

            if (!Boolean.TRUE.equals(item.getRealnamePassed())) continue;
            if (!Boolean.TRUE.equals(item.getOwnerAccepting())) continue;
            if (!Boolean.TRUE.equals(item.getServiceItemActive())) continue;
            if (!Boolean.TRUE.equals(item.getEquipmentIdle())) continue;
            if (mapper.countActiveOrdersByOwner(item.getOwnerId()) > 0) continue;
            if (mapper.countTriedOwnerByDemand(demandId, item.getOwnerId()) > 0) continue;
            if (!item.getCropTags().isEmpty() && demand.getCropCode() != null && !item.getCropTags().contains(demand.getCropCode())) continue;
            if (!tagIntersect(demand.getTerrainTags(), item.getTerrainTags())) continue;
            if (!tagIntersect(demand.getPlotTags(), item.getPlotTags())) continue;
            if (!matchArea(demand.getAreaMu(), item.getMinAreaMu(), item.getMaxAreaMu())) continue;

            Map<String, Object> distanceResult = mapDistanceService.calculateDistance(
                    demand.getLat(), demand.getLng(), item.getOwnerLat(), item.getOwnerLng()
            );
            double distanceKm = extractDistanceKm(distanceResult);
            if (distanceKm == Double.MAX_VALUE) continue;
            if (item.getServiceRadiusKm() != null && distanceKm > item.getServiceRadiusKm()) continue;

            int score = 50;
            score += intersectCount(demand.getTerrainTags(), item.getTerrainTags()) * 10;
            score += intersectCount(demand.getPlotTags(), item.getPlotTags()) * 10;
            score -= (int) Math.round(distanceKm);

            OwnerCandidate candidate = new OwnerCandidate();
            candidate.setOwnerId(item.getOwnerId());
            candidate.setServiceItemId(item.getServiceItemId());
            candidate.setDistanceKm(distanceKm);
            candidate.setStraightDistanceKm(extractOptionalDouble(distanceResult, "straightDistanceKm"));
            candidate.setDurationMinutes(extractOptionalLong(distanceResult, "durationMinutes"));
            candidate.setDistanceSource(String.valueOf(distanceResult.getOrDefault("source", "fallback")));
            candidate.setScore(score);
            candidate.setReason(buildCandidateReason(demand, item, candidate));
            raw.add(candidate);
        }

        Map<Long, OwnerCandidate> bestByOwner = raw.stream().collect(Collectors.toMap(
                OwnerCandidate::getOwnerId,
                c -> c,
                (a, b) -> {
                    if (!Objects.equals(a.getScore(), b.getScore())) {
                        return a.getScore() > b.getScore() ? a : b;
                    }
                    return a.getDistanceKm() <= b.getDistanceKm() ? a : b;
                }
        ));
        return new ArrayList<>(bestByOwner.values());
    }

    @Override
    public Map<Integer, List<OwnerCandidate>> buildTierPool(Long demandId) {
        List<OwnerCandidate> all = findEligibleCandidates(demandId);
        Map<Integer, List<OwnerCandidate>> tierPool = new LinkedHashMap<>();
        tierPool.put(1, new ArrayList<>());
        tierPool.put(2, new ArrayList<>());
        tierPool.put(3, new ArrayList<>());
        for (OwnerCandidate candidate : all) {
            int tier = com.nongzhushou.match.model.GeoDistanceUtils.calcTier(candidate.getDistanceKm());
            if (tierPool.containsKey(tier)) {
                tierPool.get(tier).add(candidate);
            }
        }
        return tierPool;
    }

    private Set<String> parseFlexibleTags(String raw) {
        Set<String> jsonSet = JsonSetUtils.toStringSet(raw);
        if (!jsonSet.isEmpty()) {
            return jsonSet;
        }
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toSet());
    }

    private String extractJsonArray(String raw, String key) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        int keyIndex = raw.indexOf("\"" + key + "\"");
        if (keyIndex < 0) {
            return null;
        }
        int start = raw.indexOf('[', keyIndex);
        int end = raw.indexOf(']', start);
        if (start < 0 || end < 0) {
            return null;
        }
        return raw.substring(start, end + 1);
    }

    private boolean tagIntersect(Set<String> demand, Set<String> service) {
        if (demand == null || demand.isEmpty()) return true;
        if (service == null || service.isEmpty()) return false;
        return demand.stream().anyMatch(service::contains);
    }

    private int intersectCount(Set<String> a, Set<String> b) {
        if (a == null || b == null) return 0;
        int count = 0;
        for (String item : a) {
            if (b.contains(item)) {
                count++;
            }
        }
        return count;
    }

    private boolean matchArea(BigDecimal demandArea, BigDecimal minArea, BigDecimal maxArea) {
        if (demandArea == null) return false;
        if (minArea != null && demandArea.compareTo(minArea) < 0) return false;
        if (maxArea != null && demandArea.compareTo(maxArea) > 0) return false;
        return true;
    }

    private double extractDistanceKm(Map<String, Object> distanceResult) {
        Object matched = distanceResult.get("matched");
        if (matched instanceof Boolean matchedBool && !matchedBool) {
            return Double.MAX_VALUE;
        }
        Double distance = extractOptionalDouble(distanceResult, "distanceKm");
        return distance == null ? Double.MAX_VALUE : distance;
    }

    private Double extractOptionalDouble(Map<String, Object> data, String key) {
        Object raw = data.get(key);
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        return null;
    }

    private Long extractOptionalLong(Map<String, Object> data, String key) {
        Object raw = data.get(key);
        if (raw instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private String buildCandidateReason(DemandMatchView demand, ServiceItemMatchView item, OwnerCandidate candidate) {
        int terrainHit = intersectCount(demand.getTerrainTags(), item.getTerrainTags());
        int plotHit = intersectCount(demand.getPlotTags(), item.getPlotTags());
        String areaBand = formatAreaBand(item.getMinAreaMu(), item.getMaxAreaMu());
        String radius = item.getServiceRadiusKm() == null ? "unlimited" : formatDouble(item.getServiceRadiusKm().doubleValue()) + "km";
        String routeDistance = formatDouble(candidate.getDistanceKm());
        String straightDistance = formatDouble(candidate.getStraightDistanceKm());
        String eta = candidate.getDurationMinutes() == null ? "na" : candidate.getDurationMinutes() + "m";

        return "eligible"
                + "|map=" + defaultString(candidate.getDistanceSource(), "fallback")
                + "|distanceKm=" + routeDistance
                + "|straightKm=" + straightDistance
                + "|eta=" + eta
                + "|radiusKm=" + radius
                + "|tier=" + com.nongzhushou.match.model.GeoDistanceUtils.calcTier(candidate.getDistanceKm())
                + "|terrainHit=" + terrainHit
                + "|plotHit=" + plotHit
                + "|areaBand=" + areaBand;
    }

    private String formatAreaBand(BigDecimal minArea, BigDecimal maxArea) {
        String min = minArea == null ? "0" : minArea.stripTrailingZeros().toPlainString();
        String max = maxArea == null ? "inf" : maxArea.stripTrailingZeros().toPlainString();
        return min + "-" + max;
    }

    private String formatDouble(Double value) {
        if (value == null) {
            return "na";
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private String defaultString(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
