package io.github.lancelot.datamasking;

import java.util.EnumMap;
import java.util.Map;

/**
 * 内置脱敏策略工厂（内部实现）。
 *
 * <p>将 {@link MaskType} 适配为 {@link MaskStrategy}，供
 * {@link MaskEngine#register(String, MaskType)} 在注册内置类型时完成适配。
 * 非公共 API，不保证向后兼容。</p>
 */
public final class BasicMaskStrategies {

    private static final Map<MaskType, MaskStrategy> STRATEGIES = build();

    private BasicMaskStrategies() {
    }

    /**
     * 获取内置类型对应的脱敏策略。
     *
     * @param type 敏感类型，null 返回 null
     * @return 脱敏策略；类型未知时返回 null
     */
    public static MaskStrategy forType(MaskType type) {
        if (type == null) {
            return null;
        }
        return STRATEGIES.get(type);
    }

    private static Map<MaskType, MaskStrategy> build() {
        Map<MaskType, MaskStrategy> map = new EnumMap<>(MaskType.class);
        for (final MaskType type : MaskType.values()) {
            map.put(type, value -> Maskers.mask(type, value));
        }
        return map;
    }
}