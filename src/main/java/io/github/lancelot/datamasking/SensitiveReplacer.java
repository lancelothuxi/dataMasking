package io.github.lancelot.datamasking;
/**
 * 敏感信息替换器
 *
 * ┌───────────────────────────────────────────────────────┐
 * │               敏感信息替换器状态机                     │
 * ├─────────────┬─────────────┬───────────┬───────────────┤
 * │   当前状态   │   触发条件   │   动作    │   下一状态    │
 * ├─────────────┼─────────────┼───────────┼───────────────┤
 * │ 初始(START)  │ 遇到 '"'     │ 开始记录键│ 键解析中(IN_KEY)│
 * │             │ 其他字符     │ 直接输出  │ (保持状态)     │
 * ├─────────────┼─────────────┼───────────┼───────────────┤
 * │ 键解析中      │ 遇到 '"'     │ 结束记录键│ 键结束(AFTER_KEY)│
 * │ (IN_KEY)    │ 其他字符     │ 记录键字符│ (保持状态)     │
 * ├─────────────┼─────────────┼───────────┼───────────────┤
 * │ 键结束       │ 遇到 :/=     │           │               │
 * │ (AFTER_KEY) │   → 下个'"' │ 跳过空白符│ 字符串值解析中  │
 * │             │   → 下个'{' │ 直接输出  │ 初始(START)    │
 * │             │   → 其他    │           │ 非字符串值解析中│
 * ├─────────────┼─────────────┼───────────┼───────────────┤
 * │ 字符串值     │ 遇到 '"'     │ 执行脱敏  │ 值处理完成     │
 * │ 解析中       │ 其他字符     │ 记录值字符│ (保持状态)     │
 * ├─────────────┼─────────────┼───────────┼───────────────┤
 * │ 非字符串值    │ 遇到 ,/}     │ 执行脱敏  │ 初始(START)    │
 * │ 解析中       │ 其他字符     │ 记录值字符│ (保持状态)     │
 * ├─────────────┼─────────────┼───────────┼───────────────┤
 * │ 值处理完成    │ 任意字符     │ 重置状态  │ 初始(START)    │
 * └─────────────┴─────────────┴───────────┴───────────────┘
 *
 * 关键动作说明：
 * 1. "执行脱敏" = 检查敏感字段注册表 + 按规则转换值
 * 2. "记录键/值字符" = 将字符追加到当前键/值缓冲区
 * 3. "跳过空白符" = 忽略 : 后的空格/换行等空白字符
 */
public class SensitiveReplacer {
    private enum State {
        START, IN_KEY, AFTER_KEY, IN_VALUE, IN_STRING_VALUE, AFTER_VALUE
    }

    public static String deSensitiveString(String json) {
        if (json==null || json.isEmpty()) {
            return json;
        }
        State currentState = State.START;
        char[] jsonChars = json.toCharArray();
        StringBuilder result = new StringBuilder(json.length());

        // 用于跟踪键和值的起止位置
        int keyStart = -1;
        int keyEnd = -1;
        int valueStart = -1;
        int valueEnd = -1;

        for (int i = 0; i < jsonChars.length; i++) {
            char c = jsonChars[i];

            switch (currentState) {
                case START:
                    if (c == '"') {
                        keyStart = i + 1; // 跳过开头的引号
                        currentState = State.IN_KEY;
                    }
                    result.append(c);
                    break;

                case IN_KEY:
                    if (c == '"') {
                        keyEnd = i;
                        currentState = State.AFTER_KEY;
                    }
                    result.append(c);
                    break;

                case AFTER_KEY:
                    if (c == ':' || c == '=') {
                        result.append(c);
                        // 跳过冒号后的空白字符
                        while (i+1 < jsonChars.length && Character.isWhitespace(jsonChars[i+1])) {
                            i++;
                            result.append(jsonChars[i]);
                        }

                        // 检查值类型
                        if (i+1 < jsonChars.length && jsonChars[i+1] == '"') {
                            currentState = State.IN_STRING_VALUE;
                            i++; // 跳过引号
                            valueStart = i + 1; // 跳过开头的引号
                            result.append('"');
                        //处理 [或者{或者 [{这种开始嵌套结构
                        } else if (i+1 < jsonChars.length && (jsonChars[i+1] == '{'|| jsonChars[i+1] == '[')) {
                            currentState = State.START;
                            i++;
                            result.append(jsonChars[i]);
                        } else {
                            currentState = State.IN_VALUE;
                            valueStart = i + 1;
                        }
                    } else {
                        result.append(c);
                    }
                    break;

                case IN_STRING_VALUE:
                    if (c == '"') {
                        valueEnd = i;
                        processSensitiveValue(jsonChars, keyStart, keyEnd, valueStart, valueEnd, result);
                        result.append(c);
                        currentState = State.AFTER_VALUE;
                    }
                    break;

                case IN_VALUE:
                    if (c == ',' || c == '}') {
                        valueEnd = i;
                        processSensitiveValue(jsonChars, keyStart, keyEnd, valueStart, valueEnd, result);
                        result.append(c);
                        currentState = State.START;
                    }
                    break;

                case AFTER_VALUE:
                    result.append(c);
                    currentState = State.START;
                    break;
            }
        }

        return result.toString();
    }

    private static void processSensitiveValue(char[] jsonChars, int keyStart, int keyEnd,
                                              int valueStart, int valueEnd, StringBuilder result) {
        String key = new String(jsonChars, keyStart, keyEnd - keyStart);
        String value = new String(jsonChars, valueStart, valueEnd - valueStart);

        if (valueEnd > valueStart) { // 确保有值需要处理
            value = value.trim(); // 对非字符串值进行trim

            SensitiveType sensitiveType = SensitiveInfoRegistry.getSensitiveType(key);
            if (sensitiveType != null) {
                value = SensitiveConvertor.convertMsg(sensitiveType, value);
                result.append(value);
            } else {
                result.append(jsonChars, valueStart, valueEnd - valueStart);
            }
        }
    }
}