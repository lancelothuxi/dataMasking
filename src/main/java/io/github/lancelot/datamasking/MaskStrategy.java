package io.github.lancelot.datamasking;

/**
 * 脱敏策略：将单个字段值转换为脱敏后的值。
 *
 * <p>函数式接口，可直接以 lambda 注册自定义规则：</p>
 * <pre>{@code
 * registry.register("password", value -> "******");
 * }</pre>
 *
 * <p>内置策略可通过 {@link BasicMaskStrategies#forType(MaskType)} 获取，
 * {@link MaskEngine#register(String, MaskType)} 内部会自动完成适配。</p>
 */
@FunctionalInterface
public interface MaskStrategy {

    /**
     * 处理单个字段值，返回脱敏后的值。
     *
     * @param value 原始字段值
     * @return 脱敏后的字段值
     */
    String mask(String value);
}