package com.nongzhushou.common.map;

import com.nongzhushou.common.api.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/map")
public class MapController {

    private final MapDistanceService mapDistanceService;

    public MapController(MapDistanceService mapDistanceService) {
        this.mapDistanceService = mapDistanceService;
    }

    @PostMapping("/geocode")
    public Result<Map<String, Object>> geocode(@Valid @RequestBody GeocodeRequest request) {
        return Result.ok(mapDistanceService.geocode(request.getAddress()));
    }

    @GetMapping("/geocode")
    public Result<Map<String, Object>> geocodeGet(
            @RequestParam("address")
            @NotBlank(message = "address can not be blank")
            @Size(max = 200, message = "address is too long") String address
    ) {
        return Result.ok(mapDistanceService.geocode(address));
    }

    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.ok(mapDistanceService.configSnapshot());
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

    @GetMapping("/distance")
    public Result<Map<String, Object>> distanceGet(
            @RequestParam("originLat")
            @DecimalMin(value = "-90.0", message = "originLat out of range")
            @DecimalMax(value = "90.0", message = "originLat out of range") Double originLat,
            @RequestParam("originLng")
            @DecimalMin(value = "-180.0", message = "originLng out of range")
            @DecimalMax(value = "180.0", message = "originLng out of range") Double originLng,
            @RequestParam("destLat")
            @DecimalMin(value = "-90.0", message = "destLat out of range")
            @DecimalMax(value = "90.0", message = "destLat out of range") Double destLat,
            @RequestParam("destLng")
            @DecimalMin(value = "-180.0", message = "destLng out of range")
            @DecimalMax(value = "180.0", message = "destLng out of range") Double destLng
    ) {
        return Result.ok(mapDistanceService.calculateDistance(originLat, originLng, destLat, destLng));
    }

    @PostMapping("/search")
    public Result<Map<String, Object>> search(@Valid @RequestBody GeocodeRequest request) {
        return Result.ok(buildSearchResult(request.getAddress()));
    }

    @GetMapping("/search")
    public Result<Map<String, Object>> searchGet(
            @RequestParam("keyword")
            @NotBlank(message = "keyword can not be blank")
            @Size(max = 200, message = "keyword is too long") String keyword
    ) {
        return Result.ok(buildSearchResult(keyword));
    }

    private Map<String, Object> buildSearchResult(String keyword) {
        List<Map<String, Object>> items = mapDistanceService.searchLocations(keyword);
        long gaodeCount = items.stream().filter(item -> "gaode".equalsIgnoreCase(String.valueOf(item.get("source")))).count();
        long builtinCount = items.stream().filter(item -> "builtin".equalsIgnoreCase(String.valueOf(item.get("source")))).count();

        Map<String, Object> sourceBreakdown = new LinkedHashMap<>();
        sourceBreakdown.put("gaode", gaodeCount);
        sourceBreakdown.put("builtin", builtinCount);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("keyword", keyword);
        result.put("count", items.size());
        result.put("sourceBreakdown", sourceBreakdown);
        result.put("items", items);
        return result;
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
