package io.github.lancelot.datamasking.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreConstants;
import io.github.lancelot.datamasking.MaskEngine;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * {@link MaskingConverter} 集成测试：通过 logback PatternLayout 实际渲染日志消息。
 */
public class MaskingConverterTest {

    private PatternLayout layoutOf(String pattern) {
        LoggerContext context = new LoggerContext();
        Map<String, String> rules = new HashMap<>();
        rules.put("maskedMsg", MaskingConverter.class.getName());
        context.putObject(CoreConstants.PATTERN_RULE_REGISTRY, rules);

        PatternLayout layout = new PatternLayout();
        layout.setContext(context);
        layout.setPattern(pattern);
        layout.start();
        return layout;
    }

    private LoggingEvent eventOf(String message) {
        LoggingEvent event = new LoggingEvent();
        event.setMessage(message);
        return event;
    }

    @Test
    public void testMasksFormattedMessage() {
        PatternLayout layout = layoutOf("%maskedMsg{mobile:MOBILE_PHONE,name:CHINESE_NAME}");
        assertEquals(layout.doLayout(eventOf("mobile=13800138000 name=李四 status=ok")),
                "mobile=138******8000 name=李* status=ok");
    }

    @Test
    public void testMasksJsonMessage() {
        PatternLayout layout = layoutOf("%maskedMsg{email:EMAIL}");
        assertEquals(layout.doLayout(eventOf("{\"email\":\"test@example.com\"}")),
                "{\"email\":\"******.com\"}");
    }

    @Test
    public void testWithoutOptionsPassThrough() {
        PatternLayout layout = layoutOf("%maskedMsg");
        String message = "mobile=13800138000";
        assertEquals(layout.doLayout(eventOf(message)), message);
    }

    @Test
    public void testInjectedEngineWithCustomStrategy() {
        MaskEngine engine = new MaskEngine();
        engine.register("token", value -> "***");

        MaskingConverter converter = new MaskingConverter();
        converter.setEngine(engine);
        converter.start();

        assertEquals(converter.convert(eventOf("token=abc123")), "token=***");
    }
}