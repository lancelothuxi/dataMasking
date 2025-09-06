package io.github.lancelot.datamasking;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SensitiveReplacerTest {
    
    @BeforeClass
    public void setup() {
        // Register sensitive fields according to your business requirements
        SensitiveInfoRegistry.put("name", SensitiveType.CHINESE_NAME);
        SensitiveInfoRegistry.put("idCard", SensitiveType.ID_CARD);
        SensitiveInfoRegistry.put("mobile", SensitiveType.MOBILE_PHONE);
        SensitiveInfoRegistry.put("phone", SensitiveType.FIXED_PHONE);
        SensitiveInfoRegistry.put("email", SensitiveType.EMAIL);
        SensitiveInfoRegistry.put("address", SensitiveType.ADDRESS);
        SensitiveInfoRegistry.put("bankCard", SensitiveType.BANK_CARD);
        SensitiveInfoRegistry.put("bankName", SensitiveType.BANK_NAME);
        SensitiveInfoRegistry.put("cnaps", SensitiveType.CNAPS_CODE);
        SensitiveInfoRegistry.put("expiryDate", SensitiveType.BANK_CARD_DATE);
        SensitiveInfoRegistry.put("password", SensitiveType.ALL);
    }

    @Test
    public void testNullInput() {
        assertNull(SensitiveReplacer.deSensitiveString(null));
    }

    @Test
    public void testEmptyInput() {
        assertEquals(SensitiveReplacer.deSensitiveString(""), "");
    }

    @Test
    public void testNonJsonInput() {
        String input = "This is not a JSON string";
        assertEquals(SensitiveReplacer.deSensitiveString(input), input);
    }

    @Test
    public void testSimpleJsonWithChineseName() {
        String input = "{\"name\":\"张三\"}";
        String expected = "{\"name\":\"张*\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithIdCard() {
        String input = "{\"idCard\":\"110101199003072345\"}";
        String expected = "{\"idCard\":\"110******345\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithMobilePhone() {
        String input = "{\"mobile\":\"13800138000\"}";
        String expected = "{\"mobile\":\"138******8000\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithFixedPhone() {
        String input = "{\"phone\":\"01012345678\"}";
        String expected = "{\"phone\":\"*******5678\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithEmail() {
        String input = "{\"email\":\"test@example.com\"}";
        String expected = "{\"email\":\"******.com\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithAddress() {
        String input = "{\"address\":\"北京市海淀区中关村大街1号\"}";
        String expected = "{\"address\":\"北京******1号\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithBankCard() {
        String input = "{\"bankCard\":\"6225880123456789\"}";
        String expected = "{\"bankCard\":\"62******6789\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithBankName() {
        String input = "{\"bankName\":\"中国工商银行\"}";
        String expected = "{\"bankName\":\"中国工商**\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithCnapsCode() {
        String input = "{\"cnaps\":\"123456789012\"}";
        String expected = "{\"cnaps\":\"1234******9012\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithCardExpiryDate() {
        String input = "{\"expiryDate\":\"0126\"}";
        String expected = "{\"expiryDate\":\"0**6\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithPassword() {
        String input = "{\"password\":\"mySecret123\"}";
        String expected = "{\"password\":\"***\"}"; // Using ALL type with default size 3
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithMultipleSensitiveFields() {
        String input = "{\"name\":\"李四\",\"idCard\":\"110101199003072345\",\"mobile\":\"13800138000\",\"email\":\"test@example.com\"}";
        String expected = "{\"name\":\"李*\",\"idCard\":\"110******345\",\"mobile\":\"138******8000\",\"email\":\"******.com\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithNestedObjects() {
        String input = "{\"user\":{\"name\":\"王五\",\"idCard\":\"110101199003072345\"},\"contact\":{\"mobile\":\"13900139000\"}}";
        String expected = "{\"user\":{\"name\":\"王*\",\"idCard\":\"110******345\"},\"contact\":{\"mobile\":\"139******9000\"}}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithArray() {
        String input = "{\"users\": [ {\"name\":\"赵六\",\"mobile\":\"13700137000\"},{\"name\":\"钱七\",\"mobile\":\"13600136000\"}]}";
        String expected = "{\"users\": [ {\"name\":\"赵*\",\"mobile\":\"137******7000\"},{\"name\":\"钱*\",\"mobile\":\"136******6000\"}]}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithNonSensitiveFields() {
        String input = "{\"username\":\"user123\",\"age\":30,\"active\":true}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), input);
    }

    @Test
    public void testJsonWithMixedSensitiveAndNonSensitive() {
        String input = "{\"name\":\"孙八\",\"age\":25,\"mobile\":\"13500135000\",\"active\":false}";
        String expected = "{\"name\":\"孙*\",\"age\":25,\"mobile\":\"135******5000\",\"active\":false}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithWhitespace() {
        String input = "{\n  \"name\" : \"周九\", \n  \"mobile\" : \"13400134000\" \n}";
        String expected = "{\n  \"name\" : \"周*\", \n  \"mobile\" : \"134******4000\" \n}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithEqualsSign() {
        String input = "{\"mobile\"=\"13500135000\"}";
        String expected = "{\"mobile\"=\"135******5000\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }

    @Test
    public void testJsonWithNullValue() {
        String input = "{\"name\":null}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), input);
    }

    @Test
    public void testJsonWithEmptyStringValue() {
        String input = "{\"name\":\"\"}";
        assertEquals(SensitiveReplacer.deSensitiveString(input), input);
    }

    @Test
    public void testJsonWithStringNullValue() {
        String input = "{\"name\":\"null\"}";
        String expected = "{\"name\":\"null\"}"; // Should remain unchanged per your convertor
        assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
    }
}