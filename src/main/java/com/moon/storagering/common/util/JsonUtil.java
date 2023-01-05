package com.moon.storagering.common.util;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * @author Chanmoey
 * @date 2023年01月05日
 */
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }

    public static <T> T fromJson(Class<T> clazz, String json) throws IOException {
        return objectMapper.readValue(json, clazz);
    }
}
