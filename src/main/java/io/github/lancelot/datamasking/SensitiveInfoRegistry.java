package io.github.lancelot.datamasking;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册脱敏信息
 */
public class SensitiveInfoRegistry {

    private static final Map<String,SensitiveType> sensitiveInfoMap = new ConcurrentHashMap<>();

    public static void replaceAll(Map<String, SensitiveType> map) {
        sensitiveInfoMap.clear();
        sensitiveInfoMap.putAll(map);
    }

    public static void putAll(Map<String, SensitiveType> map) {
        sensitiveInfoMap.putAll(map);
    }

    public static void clear() {
        sensitiveInfoMap.clear();
    }

    public static void put(String fieldName,SensitiveType sensitiveType) {
        sensitiveInfoMap.put(fieldName,sensitiveType);
    }

    public static SensitiveType getSensitiveType(String key) {
        return sensitiveInfoMap.get(key);
    }
}
