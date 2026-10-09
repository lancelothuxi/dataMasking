package io.github.lancelot.datamasking;

public interface MaskStrategy {

    /**
     * 处理单个字段值，返回脱敏后的值。
     *
     * @param value 原始字段值
     * @return 脱敏后的字段值
     */
    String mask(String value);

    /**
     * 返回策略标识，便于在规则注册表中定位与扩展。
     */
    String getName();
}
