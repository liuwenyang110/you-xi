package com.nongzhushou.common.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongzhushou.common.util.GeoDistanceUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class MapDistanceService {

    private static final int SEARCH_LIMIT = 8;
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private static final List<BuiltinLocation> BUILTIN_LOCATIONS = List.of(
            new BuiltinLocation("donghe", "Donghe Village", 30.27415, 120.15515),
            new BuiltinLocation("donghevillage", "Donghe Village", 30.27415, 120.15515),
            new BuiltinLocation("\u4E1C\u6CB3\u6751", "Donghe Village", 30.27415, 120.15515),
            new BuiltinLocation("hongqiao", "Hongqiao Demo", 31.19790, 121.32700),
            new BuiltinLocation("hongqiaodemo", "Hongqiao Demo", 31.19790, 121.32700),
            new BuiltinLocation("\u8679\u6865", "Hongqiao Demo", 31.19790, 121.32700),
            new BuiltinLocation("suzhou", "Suzhou", 31.29890, 120.58530),
            new BuiltinLocation("suzhoudemo", "Suzhou Demo", 31.29940, 120.61960),
            new BuiltinLocation("\u82CF\u5DDE", "Suzhou", 31.29890, 120.58530),
            new BuiltinLocation("hangzhou", "Hangzhou", 30.27410, 120.15510),
            new BuiltinLocation("hangzhoueast", "Hangzhou East Station", 30.29000, 120.21200),
            new BuiltinLocation("hangzhoueaststation", "Hangzhou East Station", 30.29000, 120.21200),
            new BuiltinLocation("\u676D\u5DDE", "Hangzhou", 30.27410, 120.15510),
            new BuiltinLocation("\u676D\u5DDE\u4E1C", "Hangzhou East Station", 30.29000, 120.21200),
            new BuiltinLocation("\u676D\u5DDE\u4E1C\u7AD9", "Hangzhou East Station", 30.29000, 120.21200),
            new BuiltinLocation("xiaoshanairport", "Hangzhou Xiaoshan Airport", 30.22950, 120.43450),
            new BuiltinLocation("\u8427\u5C71\u673A\u573A", "Hangzhou Xiaoshan Airport", 30.22950, 120.43450),
            new BuiltinLocation("shanghai", "Shanghai", 31.23040, 121.47370),
            new BuiltinLocation("shanghaihongqiao", "Shanghai Hongqiao", 31.19790, 121.32700),
            new BuiltinLocation("hongqiaostation", "Shanghai Hongqiao Station", 31.19790, 121.32700),
            new BuiltinLocation("hongqiaoairport", "Shanghai Hongqiao Airport", 31.19670, 121.33630),
            new BuiltinLocation("\u4E0A\u6D77", "Shanghai", 31.23040, 121.47370),
            new BuiltinLocation("\u4E0A\u6D77\u8679\u6865", "Shanghai Hongqiao", 31.19790, 121.32700),
            new BuiltinLocation("\u8679\u6865\u7AD9", "Shanghai Hongqiao Station", 31.19790, 121.32700),
            new BuiltinLocation("\u8679\u6865\u673A\u573A", "Shanghai Hongqiao Airport", 31.19670, 121.33630)
    );

    private final MapApiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ConcurrentHashMap<String, CachedValue<Map<String, Object>>> geocodeCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedValue<Map<String, Object>>> distanceCache = new ConcurrentHashMap<>();

    public MapDistanceService(MapApiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> configSnapshot() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("gaodeEnabled", properties.isGaodeEnabled());
        result.put("gaodeConfigured", hasGaodeKey());
        result.put("geocodeUrl", properties.getGeocodeUrl());
        result.put("drivingUrl", properties.getDrivingUrl());
        result.put("inputTipsUrl", properties.getInputTipsUrl());
        result.put("builtinLocationCount", BUILTIN_LOCATIONS.size());

        Map<String, Object> cache = new LinkedHashMap<>();
        cache.put("ttlSeconds", CACHE_TTL.toSeconds());
        cache.put("geocodeCacheSize", geocodeCache.size());
        cache.put("distanceCacheSize", distanceCache.size());
        result.put("cache", cache);
        return result;
    }

    public Map<String, Object> geocode(String address) {
        if (address == null || address.isBlank()) {
            return Map.of("matched", false, "message", "address is blank");
        }
        String normalized = normalize(address);
        Map<String, Object> cached = getCachedMap(geocodeCache, normalized);
        if (cached != null) {
            return cached;
        }

        Map<String, Object> builtinResult = builtinGeocode(address);
        if ((boolean) builtinResult.getOrDefault("matched", false)) {
            putCache(geocodeCache, normalized, builtinResult);
            return builtinResult;
        }

        if (!canCallGaode()) {
            Map<String, Object> disabled = Map.of(
                    "matched", false,
                    "source", "disabled",
                    "message", "gaode map api is not configured"
            );
            putCache(geocodeCache, normalized, disabled);
            return disabled;
        }

        try {
            String url = properties.getGeocodeUrl()
                    + "?key=" + encode(properties.getGaodeApiKey())
                    + "&address=" + encode(address)
                    + "&output=json";
            JsonNode root = getJson(url);
            JsonNode geocodes = root.path("geocodes");
            if (!"1".equals(root.path("status").asText()) || !geocodes.isArray() || geocodes.isEmpty()) {
                Map<String, Object> failed = Map.of(
                        "matched", false,
                        "source", "gaode",
                        "message", root.path("info").asText("gaode geocode failed")
                );
                putCache(geocodeCache, normalized, failed);
                return failed;
            }

            String location = geocodes.get(0).path("location").asText("");
            String[] parts = location.split(",");
            if (parts.length != 2) {
                Map<String, Object> invalid = Map.of(
                        "matched", false,
                        "source", "gaode",
                        "message", "invalid gaode geocode result"
                );
                putCache(geocodeCache, normalized, invalid);
                return invalid;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("matched", true);
            result.put("source", "gaode");
            result.put("address", address);
            result.put("lng", Double.parseDouble(parts[0]));
            result.put("lat", Double.parseDouble(parts[1]));
            result.put("formattedAddress", geocodes.get(0).path("formatted_address").asText(address));
            putCache(geocodeCache, normalized, result);
            return result;
        } catch (Exception ex) {
            Map<String, Object> error = Map.of(
                    "matched", false,
                    "source", "gaode",
                    "message", ex.getMessage()
            );
            putCache(geocodeCache, normalized, error);
            return error;
        }
    }

    public List<Map<String, Object>> searchLocations(String keyword) {
        String normalized = normalize(keyword);
        if (normalized.isBlank()) {
            return List.of();
        }

        List<Map<String, Object>> builtinMatches = searchBuiltinLocations(normalized);
        if (!canCallGaode()) {
            return builtinMatches;
        }

        List<Map<String, Object>> gaodeMatches = searchGaodeLocations(keyword);
        if (builtinMatches.isEmpty()) {
            return gaodeMatches;
        }
        if (gaodeMatches.isEmpty()) {
            return builtinMatches;
        }
        return mergeSearchResult(builtinMatches, gaodeMatches);
    }

    public Map<String, Object> calculateDistance(Double originLat, Double originLng, Double destLat, Double destLng) {
        double straightDistanceKm = GeoDistanceUtils.distanceKm(originLat, originLng, destLat, destLng);
        if (straightDistanceKm == Double.MAX_VALUE) {
            return Map.of("matched", false, "message", "invalid coordinates");
        }

        String cacheKey = buildDistanceCacheKey(originLat, originLng, destLat, destLng);
        Map<String, Object> cached = getCachedMap(distanceCache, cacheKey);
        if (cached != null) {
            return cached;
        }

        if (!canCallGaode()) {
            Map<String, Object> fallback = fallbackDistance(straightDistanceKm, "fallback");
            putCache(distanceCache, cacheKey, fallback);
            return fallback;
        }
        try {
            String origin = originLng + "," + originLat;
            String destination = destLng + "," + destLat;
            String url = properties.getDrivingUrl()
                    + "?key=" + encode(properties.getGaodeApiKey())
                    + "&origin=" + encode(origin)
                    + "&destination=" + encode(destination)
                    + "&extensions=base&output=json";
            JsonNode root = getJson(url);
            JsonNode paths = root.path("route").path("paths");
            if (!"1".equals(root.path("status").asText()) || !paths.isArray() || paths.isEmpty()) {
                Map<String, Object> fallback = fallbackDistance(straightDistanceKm, "fallback");
                putCache(distanceCache, cacheKey, fallback);
                return fallback;
            }
            JsonNode firstPath = paths.get(0);
            double distanceMeters = firstPath.path("distance").asDouble(-1D);
            double durationSeconds = firstPath.path("duration").asDouble(-1D);
            if (distanceMeters < 0 || durationSeconds < 0) {
                Map<String, Object> fallback = fallbackDistance(straightDistanceKm, "fallback");
                putCache(distanceCache, cacheKey, fallback);
                return fallback;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("matched", true);
            result.put("source", "gaode");
            result.put("distanceKm", round(distanceMeters / 1000D));
            result.put("routeDistanceKm", round(distanceMeters / 1000D));
            result.put("durationMinutes", Math.round(durationSeconds / 60D));
            result.put("straightDistanceKm", round(straightDistanceKm));
            putCache(distanceCache, cacheKey, result);
            return result;
        } catch (Exception ex) {
            Map<String, Object> fallback = fallbackDistance(straightDistanceKm, "fallback");
            fallback.put("message", ex.getMessage());
            putCache(distanceCache, cacheKey, fallback);
            return fallback;
        }
    }

    private boolean canCallGaode() {
        return properties.isGaodeEnabled() && hasGaodeKey();
    }

    private boolean hasGaodeKey() {
        return properties.getGaodeApiKey() != null && !properties.getGaodeApiKey().isBlank();
    }

    private JsonNode getJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return objectMapper.readTree(response.body());
    }

    private Map<String, Object> fallbackDistance(double straightDistanceKm, String source) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("matched", true);
        result.put("source", source);
        result.put("distanceKm", round(straightDistanceKm * 1.2D));
        result.put("routeDistanceKm", round(straightDistanceKm * 1.2D));
        result.put("durationMinutes", Math.round((straightDistanceKm * 1.2D / 35D) * 60D));
        result.put("straightDistanceKm", round(straightDistanceKm));
        return result;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private double round(double value) {
        return Math.round(value * 10D) / 10D;
    }

    private Map<String, Object> builtinGeocode(String address) {
        String normalized = normalize(address);
        if (normalized.isBlank()) {
            return Map.of("matched", false, "message", "address is blank");
        }
        List<BuiltinLocation> candidates = rankBuiltinLocations(normalized);
        if (candidates.isEmpty()) {
            return Map.of("matched", false, "source", "builtin", "message", "builtin geocode not matched");
        }
        BuiltinLocation matched = candidates.get(0);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("matched", true);
        result.put("source", "builtin");
        result.put("address", address);
        result.put("lng", matched.lng());
        result.put("lat", matched.lat());
        result.put("formattedAddress", matched.formattedAddress());
        return result;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim().toLowerCase();
        return trimmed.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\p{IsIdeographic}]", "");
    }

    private List<Map<String, Object>> searchBuiltinLocations(String normalizedKeyword) {
        List<BuiltinLocation> candidates = rankBuiltinLocations(normalizedKeyword);
        List<Map<String, Object>> items = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();
        for (BuiltinLocation matched : candidates) {
            String dedupeKey = matched.formattedAddress() + "|" + matched.lat() + "|" + matched.lng();
            if (!seenKeys.add(dedupeKey)) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", matched.formattedAddress());
            item.put("address", matched.formattedAddress());
            item.put("lat", matched.lat());
            item.put("lng", matched.lng());
            item.put("source", "builtin");
            item.put("tag", "Builtin");
            items.add(item);
            if (items.size() >= SEARCH_LIMIT) {
                break;
            }
        }
        return items;
    }

    private List<Map<String, Object>> searchGaodeLocations(String keyword) {
        try {
            String url = properties.getInputTipsUrl()
                    + "?key=" + encode(properties.getGaodeApiKey())
                    + "&keywords=" + encode(keyword)
                    + "&datatype=poi&citylimit=false&output=json";
            JsonNode root = getJson(url);
            JsonNode tips = root.path("tips");
            if (!"1".equals(root.path("status").asText()) || !tips.isArray() || tips.isEmpty()) {
                return List.of();
            }
            List<Map<String, Object>> items = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (JsonNode node : tips) {
                String location = node.path("location").asText("");
                String[] parts = location.split(",");
                if (parts.length != 2) {
                    continue;
                }
                double lng = parseDouble(parts[0]);
                double lat = parseDouble(parts[1]);
                if (lat == Double.MAX_VALUE || lng == Double.MAX_VALUE) {
                    continue;
                }
                String name = node.path("name").asText(keyword);
                String address = buildTipAddress(node, name);
                String dedupeKey = String.format(Locale.ROOT, "%.5f|%.5f|%s", lat, lng, name);
                if (!seen.add(dedupeKey)) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", name);
                item.put("address", address);
                item.put("lat", lat);
                item.put("lng", lng);
                item.put("source", "gaode");
                item.put("tag", "Map Search");
                items.add(item);
                if (items.size() >= SEARCH_LIMIT) {
                    break;
                }
            }
            return items;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String buildTipAddress(JsonNode node, String fallbackName) {
        String district = node.path("district").asText("");
        String address = node.path("address").asText("");
        StringBuilder full = new StringBuilder();
        if (district != null && !district.isBlank()) {
            full.append(district.trim());
        }
        if (address != null && !address.isBlank()) {
            if (!full.isEmpty()) {
                full.append(" ");
            }
            full.append(address.trim());
        }
        if (full.isEmpty()) {
            return fallbackName;
        }
        return full.toString();
    }

    private List<Map<String, Object>> mergeSearchResult(List<Map<String, Object>> builtin, List<Map<String, Object>> gaode) {
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Map<String, Object> item : builtin) {
            result.add(item);
            seen.add(buildSearchKey(item));
            if (result.size() >= SEARCH_LIMIT) {
                return result;
            }
        }
        for (Map<String, Object> item : gaode) {
            String key = buildSearchKey(item);
            if (!seen.add(key)) {
                continue;
            }
            result.add(item);
            if (result.size() >= SEARCH_LIMIT) {
                break;
            }
        }
        return result;
    }

    private String buildSearchKey(Map<String, Object> item) {
        return String.valueOf(item.get("lat")) + "|" + item.get("lng") + "|" + item.get("name");
    }

    private List<BuiltinLocation> rankBuiltinLocations(String normalized) {
        List<BuiltinLocation> exactCandidates = new ArrayList<>();
        List<BuiltinLocation> fuzzyCandidates = new ArrayList<>();
        for (BuiltinLocation item : BUILTIN_LOCATIONS) {
            String alias = normalize(item.alias());
            String name = normalize(item.formattedAddress());
            if (normalized.equals(alias) || normalized.equals(name)) {
                exactCandidates.add(item);
                continue;
            }
            if (normalized.contains(alias) || alias.contains(normalized)) {
                fuzzyCandidates.add(item);
            }
        }
        exactCandidates.sort((left, right) -> Integer.compare(right.alias().length(), left.alias().length()));
        fuzzyCandidates.sort((left, right) -> Integer.compare(right.alias().length(), left.alias().length()));
        return !exactCandidates.isEmpty() ? exactCandidates : fuzzyCandidates;
    }

    private String buildDistanceCacheKey(Double originLat, Double originLng, Double destLat, Double destLng) {
        return String.format(
                Locale.ROOT,
                "%.5f,%.5f->%.5f,%.5f",
                originLat,
                originLng,
                destLat,
                destLng
        );
    }

    private double parseDouble(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (Exception ex) {
            return Double.MAX_VALUE;
        }
    }

    private Map<String, Object> getCachedMap(ConcurrentHashMap<String, CachedValue<Map<String, Object>>> cache, String key) {
        CachedValue<Map<String, Object>> cachedValue = cache.get(key);
        if (cachedValue == null) {
            return null;
        }
        if (cachedValue.expiresAtMillis() < System.currentTimeMillis()) {
            cache.remove(key);
            return null;
        }
        return new LinkedHashMap<>(cachedValue.value());
    }

    private void putCache(ConcurrentHashMap<String, CachedValue<Map<String, Object>>> cache, String key, Map<String, Object> value) {
        if (key == null || key.isBlank() || value == null || value.isEmpty()) {
            return;
        }
        long expiresAt = System.currentTimeMillis() + CACHE_TTL.toMillis();
        cache.put(key, new CachedValue<>(new LinkedHashMap<>(value), expiresAt));
    }

    private record BuiltinLocation(String alias, String formattedAddress, double lat, double lng) {
    }

    private record CachedValue<T>(T value, long expiresAtMillis) {
    }
}
