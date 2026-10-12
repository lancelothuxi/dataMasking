package io.github.lancelot.datamasking;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * {@link MaskEngine} 行为测试：覆盖 JSON、KV、日志文本，以及转义与边界输入。
 */
public class MaskEngineTest {

    private MaskEngine engine;

    @BeforeClass
    public void setup() {
        engine = new MaskEngine();
        engine.register("name", MaskType.CHINESE_NAME);
        engine.register("user", MaskType.CHINESE_NAME);
        engine.register("idCard", MaskType.ID_CARD);
        engine.register("mobile", MaskType.MOBILE_PHONE);
        engine.register("phone", MaskType.FIXED_PHONE);
        engine.register("email", MaskType.EMAIL);
        engine.register("address", MaskType.ADDRESS);
        engine.register("bankCard", MaskType.BANK_CARD);
        engine.register("bankName", MaskType.BANK_NAME);
        engine.register("cnaps", MaskType.CNAPS_CODE);
        engine.register("expiryDate", MaskType.BANK_CARD_DATE);
        engine.register("password", MaskType.ALL);
    }

    private void assertMasked(String input, String expected) {
        assertEquals(engine.process(input), expected);
    }

    // ===== 基本输入 =====

    @Test
    public void testNullInput() {
        assertNull(engine.process(null));
    }

    @Test
    public void testEmptyInput() {
        assertEquals(engine.process(""), "");
    }

    @Test
    public void testNonJsonInput() {
        assertMasked("This is not a JSON string", "This is not a JSON string");
    }

    // ===== JSON =====

    @Test
    public void testJsonWithChineseName() {
        assertMasked("{\"name\":\"张三\"}", "{\"name\":\"张*\"}");
    }

    @Test
    public void testJsonWithIdCard() {
        assertMasked("{\"idCard\":\"110101199003072345\"}", "{\"idCard\":\"110******345\"}");
    }

    @Test
    public void testJsonWithMobilePhone() {
        assertMasked("{\"mobile\":\"13800138000\"}", "{\"mobile\":\"138******8000\"}");
    }

    @Test
    public void testJsonWithFixedPhone() {
        assertMasked("{\"phone\":\"01012345678\"}", "{\"phone\":\"*******5678\"}");
    }

    @Test
    public void testJsonWithEmail() {
        assertMasked("{\"email\":\"test@example.com\"}", "{\"email\":\"******.com\"}");
    }

    @Test
    public void testJsonWithAddress() {
        assertMasked("{\"address\":\"北京市海淀区中关村大街1号\"}", "{\"address\":\"北京******1号\"}");
    }

    @Test
    public void testJsonWithBankCard() {
        assertMasked("{\"bankCard\":\"6225880123456789\"}", "{\"bankCard\":\"62******6789\"}");
    }

    @Test
    public void testJsonWithBankName() {
        assertMasked("{\"bankName\":\"中国工商银行\"}", "{\"bankName\":\"中国工商**\"}");
    }

    @Test
    public void testJsonWithCnapsCode() {
        assertMasked("{\"cnaps\":\"123456789012\"}", "{\"cnaps\":\"1234******9012\"}");
    }

    @Test
    public void testJsonWithCardExpiryDate() {
        assertMasked("{\"expiryDate\":\"0126\"}", "{\"expiryDate\":\"0**6\"}");
    }

    @Test
    public void testJsonWithPassword() {
        // ALL 类型固定输出 3 个星号
        assertMasked("{\"password\":\"mySecret123\"}", "{\"password\":\"***\"}");
    }

    @Test
    public void testJsonWithMultipleSensitiveFields() {
        assertMasked(
                "{\"name\":\"李四\",\"idCard\":\"110101199003072345\",\"mobile\":\"13800138000\",\"email\":\"test@example.com\"}",
                "{\"name\":\"李*\",\"idCard\":\"110******345\",\"mobile\":\"138******8000\",\"email\":\"******.com\"}");
    }

    @Test
    public void testJsonWithNestedObjects() {
        assertMasked(
                "{\"user\":{\"name\":\"王五\",\"idCard\":\"110101199003072345\"},\"contact\":{\"mobile\":\"13900139000\"}}",
                "{\"user\":{\"name\":\"王*\",\"idCard\":\"110******345\"},\"contact\":{\"mobile\":\"139******9000\"}}");
    }

    @Test
    public void testJsonWithArray() {
        assertMasked(
                "{\"users\": [ {\"name\":\"赵六\",\"mobile\":\"13700137000\"},{\"name\":\"钱七\",\"mobile\":\"13600136000\"}]}",
                "{\"users\": [ {\"name\":\"赵*\",\"mobile\":\"137******7000\"},{\"name\":\"钱*\",\"mobile\":\"136******6000\"}]}");
    }

    @Test
    public void testJsonWithNonSensitiveFields() {
        assertMasked("{\"username\":\"user123\",\"age\":30,\"active\":true}",
                "{\"username\":\"user123\",\"age\":30,\"active\":true}");
    }

    @Test
    public void testJsonWithMixedSensitiveAndNonSensitive() {
        assertMasked("{\"name\":\"孙八\",\"age\":25,\"mobile\":\"13500135000\",\"active\":false}",
                "{\"name\":\"孙*\",\"age\":25,\"mobile\":\"135******5000\",\"active\":false}");
    }

    @Test
    public void testJsonWithWhitespace() {
        assertMasked("{\n  \"name\" : \"周九\", \n  \"mobile\" : \"13400134000\" \n}",
                "{\n  \"name\" : \"周*\", \n  \"mobile\" : \"134******4000\" \n}");
    }

    @Test
    public void testJsonWithEqualsSign() {
        assertMasked("{\"mobile\"=\"13500135000\"}", "{\"mobile\"=\"135******5000\"}");
    }

    @Test
    public void testJsonWithNullValue() {
        assertMasked("{\"name\":null}", "{\"name\":null}");
    }

    @Test
    public void testJsonWithEmptyStringValue() {
        assertMasked("{\"name\":\"\"}", "{\"name\":\"\"}");
    }

    @Test
    public void testJsonWithStringNullValue() {
        // 字符串 "null" 按转换规则保持原样返回
        assertMasked("{\"name\":\"null\"}", "{\"name\":\"null\"}");
    }

    // ===== KV / 日志文本（无引号键值对） =====

    @Test
    public void testKvFormat() {
        assertMasked("name=李四&mobile=13800138000&email=test@example.com",
                "name=李*&mobile=138******8000&email=******.com");
    }

    @Test
    public void testLogLine() {
        assertMasked("2025-09-07 10:00:00 user=张三 phone=13800138000 email=test@example.com status=success",
                "2025-09-07 10:00:00 user=张* phone=*******8000 email=******.com status=success");
    }

    @Test
    public void testKvUnregisteredKeysPassThrough() {
        assertMasked("a=1&b=2&size=10", "a=1&b=2&size=10");
    }

    @Test
    public void testKvTrailingWithoutSeparator() {
        assertMasked("user=张三", "user=张*");
    }

    @Test
    public void testKvRepeatedFields() {
        assertMasked("mobile=13800138000 name=李四 mobile=13900139000",
                "mobile=138******8000 name=李* mobile=139******9000");
    }

    @Test
    public void testKvValueContainsEquals() {
        assertMasked("url=http://x.com/a=1&name=张三", "url=http://x.com/a=1&name=张*");
    }

    @Test
    public void testKvSemicolonSeparated() {
        assertMasked("name=李四;mobile=13800138000", "name=李*;mobile=138******8000");
    }

    @Test
    public void testKvQuotedValue() {
        assertMasked("user=\"李四\"", "user=\"李*\"");
    }

    @Test
    public void testTimestampNotTreatedAsKey() {
        assertMasked("10:00:00 now", "10:00:00 now");
    }

    // ===== 转义与边界输入 =====

    @Test
    public void testEscapedQuoteInValue() {
        assertMasked("{\"name\":\"张\\\"三\"}", "{\"name\":\"张*\"}");
    }

    @Test
    public void testEscapedQuoteInKey() {
        // 含转义引号的键不在注册表中，整体原样透传
        assertMasked("{\"na\\\"me\":\"张三\"}", "{\"na\\\"me\":\"张三\"}");
    }

    @Test
    public void testDoubleBackslashBeforeQuote() {
        // \\ 是转义的反斜杠，其后的 " 仍是值结束符；值外的杂散字符原样透传
        assertMasked("{\"name\":\"a\\\\\"x\"}", "{\"name\":\"a*\"x\"}");
    }

    @Test
    public void testUnterminatedStringValue() {
        assertMasked("{\"name\":\"张三", "{\"name\":\"张*");
    }

    @Test
    public void testUnterminatedNumberValue() {
        assertMasked("{\"age\": 30", "{\"age\": 30");
    }

    @Test
    public void testUnterminatedMultiFieldJson() {
        // 前一个值闭合后，末尾未闭合的值不能被丢弃
        assertMasked("{\"mobile\":\"13800138000\",\"age\": 30", "{\"mobile\":\"138******8000\",\"age\": 30");
    }

    @Test
    public void testTrailingWhitespaceAfterKv() {
        // 值已由空白闭合，结尾不应重复处理
        assertMasked("name=李四 ", "name=李* ");
    }

    @Test
    public void testUnterminatedKvValue() {
        assertMasked("user=张三", "user=张*");
    }

    // ===== 自定义策略 =====

    @Test
    public void testCustomStrategy() {
        MaskEngine custom = new MaskEngine();
        custom.register("token", value -> "***");
        assertMasked0(custom, "{\"token\":\"abc123\"}", "{\"token\":\"***\"}");
    }

    private void assertMasked0(MaskEngine e, String input, String expected) {
        assertEquals(e.process(input), expected);
    }
}