package io.github.lancelot.datamasking.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.github.lancelot.datamasking.MaskEngine;

import java.util.List;

/**
 * Logback 脱敏转换器：作为 pattern 转换词，对格式化后的日志消息执行文本脱敏。
 *
 * <p>注册与使用（logback.xml）：</p>
 * <pre>{@code
 * <conversionRule conversionWord="maskedMsg"
 *     converterClass="io.github.lancelot.datamasking.logback.MaskingConverter"/>
 *
 * <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
 *   <encoder>
 *     <pattern>%d %-5level %logger{36} - %maskedMsg{mobile:MOBILE_PHONE,name:CHINESE_NAME}%n</pattern>
 *   </encoder>
 * </appender>
 * }</pre>
 *
 * <p>转换词选项即规则规格（{@code field:TYPE}，逗号分隔）。也可通过
 * {@link #setEngine(MaskEngine)} 在代码中注入自定义策略的引擎（插件由 logback
 * 实例化，通常做法是子类化本类并在构造器中调用该方法）。</p>
 *
 * <p>脱敏异常不会抛出：发生异常时回退输出原始消息，避免影响日志链路。</p>
 */
public class MaskingConverter extends ClassicConverter {

    private MaskEngine engine;

    /**
     * 注入脱敏引擎（可选）。设置后忽略转换词选项。
     */
    public void setEngine(MaskEngine engine) {
        this.engine = engine;
    }

    @Override
    public void start() {
        if (engine == null) {
            MaskEngine built = new MaskEngine();
            List<String> options = getOptionList();
            if (options != null) {
                for (String option : options) {
                    built.registerAll(option);
                }
            }
            engine = built;
        }
        super.start();
    }

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null || engine == null) {
            return message;
        }
        try {
            return engine.process(message);
        } catch (RuntimeException e) {
            addError("脱敏失败，回退原始消息", e);
            return message;
        }
    }
}