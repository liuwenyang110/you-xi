package com.nongzhushou.common.map;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.match.model.OwnerCandidate;
import com.nongzhushou.match.service.MatchEngineBridgeService;
import com.nongzhushou.match.service.MatchRuleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@RestController
@Validated
@RequestMapping("/api/v1/debug/map")
public class MapDebugController {

    private final MapDistanceService mapDistanceService;
    private final MatchRuleService matchRuleService;
    private final MatchEngineBridgeService matchEngineBridgeService;

    public MapDebugController(
            MapDistanceService mapDistanceService,
            MatchRuleService matchRuleService,
            MatchEngineBridgeService matchEngineBridgeService
    ) {
        this.mapDistanceService = mapDistanceService;
        this.matchRuleService = matchRuleService;
        this.matchEngineBridgeService = matchEngineBridgeService;
    }

    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.ok(mapDistanceService.configSnapshot());
    }

    @PostMapping("/geocode")
    public Result<Map<String, Object>> geocode(@Valid @RequestBody GeocodeRequest request) {
        return Result.ok(mapDistanceService.geocode(request.getAddress()));
    }

    @PostMapping("/distance")
    public Result<Map<String, Object>> distance(@Valid @RequestBody DistanceRequest request) {
        return Result.ok(mapDistanceService.calculateDistance(
                request.getOriginLat(),
                request.getOriginLng(),
                request.getDestLat(),
                request.getDestLng()
        ));
    }

    @GetMapping("/demand/{demandId}/match-preview")
    public Result<Map<String, Object>> matchPreview(@PathVariable @Positive(message = "demandId must be greater than 0") Long demandId) {
        List<OwnerCandidate> eligible = matchRuleService.findEligibleCandidates(demandId);
        Map<Integer, List<OwnerCandidate>> tierPool = matchEngineBridgeService.buildTierPool(demandId);

        long gaodeCount = eligible.stream().filter(item -> "gaode".equalsIgnoreCase(item.getDistanceSource())).count();
        long builtinCount = eligible.stream().filter(item -> "builtin".equalsIgnoreCase(item.getDistanceSource())).count();
        long fallbackCount = eligible.stream().filter(item -> "fallback".equalsIgnoreCase(item.getDistanceSource())).count();

        List<OwnerCandidate> topCandidates = eligible.stream()
                .sorted(Comparator
                        .comparing((OwnerCandidate c) -> c.getScore() == null ? Integer.MIN_VALUE : c.getScore())
                        .reversed()
                        .thenComparing(c -> c.getDistanceKm() == null ? Double.MAX_VALUE : c.getDistanceKm()))
                .limit(10)
                .toList();

        Map<String, Object> tierCounts = new LinkedHashMap<>();
        tierCounts.put("tier1", tierPool.getOrDefault(1, List.of()).size());
        tierCounts.put("tier2", tierPool.getOrDefault(2, List.of()).size());
        tierCounts.put("tier3", tierPool.getOrDefault(3, List.of()).size());

        Map<String, Object> sourceBreakdown = new LinkedHashMap<>();
        sourceBreakdown.put("gaode", gaodeCount);
        sourceBreakdown.put("builtin", builtinCount);
        sourceBreakdown.put("fallback", fallbackCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("demandId", demandId);
        result.put("mapConfig", mapDistanceService.configSnapshot());
        result.put("eligibleCount", eligible.size());
        result.put("sourceBreakdown", sourceBreakdown);
        result.put("tierCounts", tierCounts);
        result.put("topCandidates", topCandidates);
        result.put("tiers", tierPool);
        return Result.ok(result);
    }

    public static class GeocodeRequest {
        @NotBlank(message = "address can not be blank")
        private String address;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class DistanceRequest {
        @DecimalMin(value = "-90.0", message = "originLat out of range")
        @DecimalMax(value = "90.0", message = "originLat out of range")
        private Double originLat;

        @DecimalMin(value = "-180.0", message = "originLng out of range")
        @DecimalMax(value = "180.0", message = "originLng out of range")
        private Double originLng;

        @DecimalMin(value = "-90.0", message = "destLat out of range")
        @DecimalMax(value = "90.0", message = "destLat out of range")
        private Double destLat;

        @DecimalMin(value = "-180.0", message = "destLng out of range")
        @DecimalMax(value = "180.0", message = "destLng out of range")
        private Double destLng;

        public Double getOriginLat() {
            return originLat;
        }

        public void setOriginLat(Double originLat) {
            this.originLat = originLat;
        }

        public Double getOriginLng() {
            return originLng;
        }

        public void setOriginLng(Double originLng) {
            this.originLng = originLng;
        }

        public Double getDestLat() {
            return destLat;
        }

        public void setDestLat(Double destLat) {
            this.destLat = destLat;
        }

        public Double getDestLng() {
            return destLng;
        }

        public void setDestLng(Double destLng) {
            this.destLng = destLng;
        }
    }
}
