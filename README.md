# dataMasking
基于字符流的脱敏实现，零正则，快速高效

## 用法
 
```java
//注册脱敏字段
SensitiveInfoRegistry.put("name", SensitiveType.CHINESE_NAME);
SensitiveInfoRegistry.put("idCard", SensitiveType.ID_CARD);
SensitiveInfoRegistry.put("mobile", SensitiveType.MOBILE_PHONE);
SensitiveInfoRegistry.put("phone", SensitiveType.FIXED_PHONE);
SensitiveInfoRegistry.put("email", SensitiveType.EMAIL);
SensitiveInfoRegistry.put("address", SensitiveType.ADDRESS);


//参考测试用例
@Test
public void testJsonWithMultipleSensitiveFields() {
    String input = "{\"name\":\"李四\",\"idCard\":\"110101199003072345\",\"mobile\":\"13800138000\",\"email\":\"test@example.com\"}";
    String expected = "{\"name\":\"李*\",\"idCard\":\"110******345\",\"mobile\":\"138******8000\",\"email\":\"******.com\"}";
    assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
}
```

