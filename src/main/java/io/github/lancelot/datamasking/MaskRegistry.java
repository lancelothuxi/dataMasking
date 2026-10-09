package io.github.lancelot.datamasking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可实例化的敏感字段注册表。
 *
 * 兼容旧的静态注册表 API，且允许业务方为不同场景创建独立规则集合。
 */
public class MaskRegistry {

    private final Map<String, SensitiveType> sensitiveInfoMap = new ConcurrentHashMap<>();

    public void replaceAll(Map<String, SensitiveType> map) {
        sensitiveInfoMap.clear();
        if (map != null) {
            sensitiveInfoMap.putAll(map);
        }
    }

    public void putAll(Map<String, SensitiveType> map) {
        if (map != null) {
            sensitiveInfoMap.putAll(map);
        }
    }

    public void clear() {
        sensitiveInfoMap.clear();
    }

    public void put(String fieldName, SensitiveType sensitiveType) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            return;
        }
        sensitiveInfoMap.put(fieldName, sensitiveType);
    }

    public SensitiveType get(String key) {
        return sensitiveInfoMap.get(key);
    }

    public Map<String, SensitiveType> snapshot() {
        return new ConcurrentHashMap<>(sensitiveInfoMap);
    }
}
