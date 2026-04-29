package com.nongzhushou.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JsonSetUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonSetUtils() {
    }

    public static Set<String> toStringSet(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptySet();
        }
        try {
            List<String> list = MAPPER.readValue(json, new TypeReference<List<String>>() {});
            return new HashSet<>(list);
        } catch (Exception ignored) {
            return Collections.emptySet();
        }
    }
}
