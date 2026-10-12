package io.github.lancelot.datamasking.log4j2;

import io.github.lancelot.datamasking.MaskEngine;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

/**
 * Log4j2 脱敏重写策略：在 {@code Rewrite} appender 中对格式化后的日志消息执行文本脱敏。
 *
 * <p>配置（log4j2.xml）：</p>
 * <pre>{@code
 * <Appenders>
 *   <Rewrite name="masked">
 *     <MaskingRewritePolicy rules="mobile:MOBILE_PHONE,name:CHINESE_NAME,password:ALL"/>
 *     <Console name="Console" target="SYSTEM_OUT"/>
 *   </Rewrite>
 * </Appenders>
 * <Loggers>
 *   <Root level="info"><AppenderRef ref="masked"/></Root>
 * </Loggers>
 * }</pre>
 *
 * <p>{@code rules} 为规则规格（{@code field:TYPE}，逗号或分号分隔）。</p>
 */
@Plugin(name = "MaskingRewritePolicy", category = Node.CATEGORY,
        elementType = "rewritePolicy", printObject = true)
public final class MaskingRewritePolicy implements RewritePolicy {

    private final MaskEngine engine;

    private MaskingRewritePolicy(MaskEngine engine) {
        this.engine = engine;
    }

    /**
     * 插件工厂：从 {@code rules} 属性构建脱敏引擎。
     *
     * @param rules 规则规格，可为 null
     * @return 重写策略
     */
    @PluginFactory
    public static MaskingRewritePolicy createPolicy(@PluginAttribute("rules") String rules) {
        MaskEngine engine = new MaskEngine();
        engine.registerAll(rules);
        return new MaskingRewritePolicy(engine);
    }

    @Override
    public LogEvent rewrite(LogEvent source) {
        if (source == null) {
            return null;
        }
        Message message = source.getMessage();
        if (message == null) {
            return source;
        }
        String masked;
        try {
            masked = engine.process(message.getFormattedMessage());
        } catch (RuntimeException e) {
            // 日志链路不应因脱敏失败而中断
            return source;
        }
        return new Log4jLogEvent.Builder(source).setMessage(new SimpleMessage(masked)).build();
    }

    /**
     * 获取内部脱敏引擎（便于测试与程序化复用）。
     */
    public MaskEngine getEngine() {
        return engine;
    }
}