package com.nongzhushou.match.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongzhushou.common.util.GeoDistanceUtils;
import com.nongzhushou.match.mapper.DemandMatchViewMapper;
import com.nongzhushou.match.mapper.ServiceItemMatchViewMapper;
import com.nongzhushou.match.model.DemandMatchView;
import com.nongzhushou.match.model.MatchRuleInput;
import com.nongzhushou.match.model.OwnerCandidate;
import com.nongzhushou.match.model.ServiceItemMatchView;
import com.nongzhushou.match.service.MatchConflictService;
import com.nongzhushou.match.service.MatchEligibilityService;
import com.nongzhushou.match.service.MatchTagService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class MatchEligibilityServiceImpl implements MatchEligibilityService {

    private final DemandMatchViewMapper demandMatchViewMapper;
    private final ServiceItemMatchViewMapper serviceItemMatchViewMapper;
    private final MatchTagService matchTagService;
    private final MatchConflictService matchConflictService;
    private final ObjectMapper objectMapper;

    public MatchEligibilityServiceImpl(
            DemandMatchViewMapper demandMatchViewMapper,
            ServiceItemMatchViewMapper serviceItemMatchViewMapper,
            MatchTagService matchTagService,
            MatchConflictService matchConflictService,
            ObjectMapper objectMapper
    ) {
        this.demandMatchViewMapper = demandMatchViewMapper;
        this.serviceItemMatchViewMapper = serviceItemMatchViewMapper;
        this.matchTagService = matchTagService;
        this.matchConflictService = matchConflictService;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<OwnerCandidate> findEligibleCandidates(Long demandId) {
        DemandMatchView demand = demandMatchViewMapper.selectDemandMatchView(demandId);
        if (demand == null) {
            return List.of();
        }
        demand.setTerrainTags(splitTags(readRequirementTag(demand.getRequirementJson(), "terrainTags")));
        demand.setPlotTags(splitTags(readRequirementTag(demand.getRequirementJson(), "plotTags")));

        List<ServiceItemMatchView> basePool = serviceItemMatchViewMapper.selectEligibleBasePool(demand.getServiceSubcategoryId());
        List<OwnerCandidate> candidates = new ArrayList<>();
        for (ServiceItemMatchView item : basePool) {
            item.setCropTags(splitTags(item.getCropTagsRaw()));
            item.setTerrainTags(splitTags(item.getTerrainTagsRaw()));
            item.setPlotTags(splitTags(item.getPlotTagsRaw()));

            Double distanceKm = GeoDistanceUtils.distanceKm(
                    demand.getLat(),
                    demand.getLng(),
                    item.getOwnerLat(),
                    item.getOwnerLng()
            );
            if (distanceKm == Double.MAX_VALUE) {
                distanceKm = item.getServiceRadiusKm() == null ? 1.0D : Math.min(item.getServiceRadiusKm().doubleValue(), 1.0D);
            }

            MatchRuleInput input = new MatchRuleInput();
            input.setDemandId(demandId);
            input.setOwnerId(item.getOwnerId());
            input.setServiceItemId(item.getServiceItemId());
            input.setCropCode(demand.getCropCode());
            input.setAreaMu(demand.getAreaMu());
            input.setDemandLat(demand.getLat());
            input.setDemandLng(demand.getLng());
            input.setTierNo(resolveTier(distanceKm));
            input.setMaxDistanceKm(item.getServiceRadiusKm());
            input.setTerrainTags(demand.getTerrainTags());
            input.setPlotTags(demand.getPlotTags());
            input.setOwnerAccepting(item.getOwnerAccepting());
            input.setServiceItemActive(item.getServiceItemActive());
            input.setRealnamePassed(item.getRealnamePassed());
            input.setEquipmentIdle(item.getEquipmentIdle());

            boolean baseEligible = isEligible(input);
            boolean cropOk = matchTagService.matchCrop(demand.getCropCode(), item.getCropTags());
            boolean terrainOk = matchTagService.matchTerrain(demand.getTerrainTags(), item.getTerrainTags());
            boolean plotOk = matchTagService.matchPlot(demand.getPlotTags(), item.getPlotTags());
            boolean areaOk = matchTagService.matchArea(demand.getAreaMu(), item.getMinAreaMu(), item.getMaxAreaMu());
            boolean distanceOk = item.getServiceRadiusKm() == null || distanceKm <= item.getServiceRadiusKm();
            if (baseEligible && cropOk && terrainOk && plotOk && areaOk && distanceOk) {
                OwnerCandidate candidate = new OwnerCandidate();
                candidate.setOwnerId(item.getOwnerId());
                candidate.setServiceItemId(item.getServiceItemId());
                candidate.setDistanceKm(distanceKm);
                candidate.setScore(buildScore(distanceKm, cropOk, terrainOk, plotOk));
                candidate.setReason("eligible");
                candidates.add(candidate);
            }
        }
        return deduplicateOwners(candidates);
    }

    @Override
    public boolean isEligible(MatchRuleInput input) {
        if (input == null) {
            return false;
        }
        if (!Boolean.TRUE.equals(input.getRealnamePassed())) {
            return false;
        }
        if (!Boolean.TRUE.equals(input.getOwnerAccepting())) {
            return false;
        }
        if (!Boolean.TRUE.equals(input.getServiceItemActive())) {
            return false;
        }
        if (!Boolean.TRUE.equals(input.getEquipmentIdle())) {
            return false;
        }
        if (matchConflictService.hasActiveOrderConflict(input.getOwnerId())) {
            return false;
        }
        if (matchConflictService.hasAlreadyTried(input.getDemandId(), input.getOwnerId())) {
            return false;
        }
        return true;
    }

    private List<OwnerCandidate> deduplicateOwners(List<OwnerCandidate> candidates) {
        return candidates.stream()
                .collect(java.util.stream.Collectors.toMap(
                        OwnerCandidate::getOwnerId,
                        candidate -> candidate,
                        (left, right) -> left.getScore() >= right.getScore() ? left : right))
                .values()
                .stream()
                .sorted(Comparator.comparing(OwnerCandidate::getDistanceKm, Comparator.nullsLast(Double::compareTo))
                        .thenComparing(OwnerCandidate::getScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private int buildScore(Double distanceKm, boolean cropOk, boolean terrainOk, boolean plotOk) {
        int score = 100;
        if (cropOk) {
            score += 30;
        }
        if (terrainOk) {
            score += 20;
        }
        if (plotOk) {
            score += 10;
        }
        score -= (int) Math.round(distanceKm == null ? 999 : distanceKm);
        return score;
    }

    private int resolveTier(Double distanceKm) {
        if (distanceKm == null || distanceKm <= 3) {
            return 1;
        }
        if (distanceKm <= 8) {
            return 2;
        }
        return 3;
    }

    private Set<String> splitTags(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        Set<String> tags = new HashSet<>();
        for (String item : csv.split(",")) {
            String tag = item.trim();
            if (!tag.isEmpty()) {
                tags.add(tag);
            }
        }
        return tags;
    }

    private String readRequirementTag(String raw, String key) {
        if (raw == null || raw.isBlank() || !raw.contains(key)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode node = root.path(key);
            if (node.isMissingNode() || node.isNull()) {
                return null;
            }
            if (node.isArray()) {
                List<String> values = new ArrayList<>();
                for (JsonNode item : node) {
                    if (item != null && !item.isNull() && !item.asText().isBlank()) {
                        values.add(item.asText().trim());
                    }
                }
                return String.join(",", values);
            }
            String value = node.asText();
            return value == null || value.isBlank() ? null : value;
        } catch (Exception ignored) {
            return null;
        }
    }
}
