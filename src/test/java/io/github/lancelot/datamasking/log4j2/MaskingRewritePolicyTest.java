package io.github.lancelot.datamasking.log4j2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * {@link MaskingRewritePolicy} 集成测试：重写 Log4j2 日志事件的消息。
 */
public class MaskingRewritePolicyTest {

    private LogEvent eventOf(String message) {
        return Log4jLogEvent.newBuilder()
                .setLoggerName("test")
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage(message))
                .build();
    }

    @Test
    public void testMasksFormattedMessage() {
        MaskingRewritePolicy policy =
                MaskingRewritePolicy.createPolicy("mobile:MOBILE_PHONE,name:CHINESE_NAME");
        LogEvent rewritten = policy.rewrite(eventOf("mobile=13800138000 name=李四 status=ok"));
        assertEquals(rewritten.getMessage().getFormattedMessage(),
                "mobile=138******8000 name=李* status=ok");
    }

    @Test
    public void testMasksJsonMessage() {
        MaskingRewritePolicy policy = MaskingRewritePolicy.createPolicy("email:EMAIL");
        LogEvent rewritten = policy.rewrite(eventOf("{\"email\":\"test@example.com\"}"));
        assertEquals(rewritten.getMessage().getFormattedMessage(), "{\"email\":\"******.com\"}");
    }

    @Test
    public void testNullRulesPassThrough() {
        MaskingRewritePolicy policy = MaskingRewritePolicy.createPolicy(null);
        LogEvent rewritten = policy.rewrite(eventOf("mobile=13800138000"));
        assertEquals(rewritten.getMessage().getFormattedMessage(), "mobile=13800138000");
    }

    @Test
    public void testNullEventReturnsNull() {
        MaskingRewritePolicy policy = MaskingRewritePolicy.createPolicy("mobile:MOBILE_PHONE");
        assertNull(policy.rewrite(null));
    }
}