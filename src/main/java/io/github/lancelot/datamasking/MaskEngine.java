package io.github.lancelot.datamasking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文本脱敏引擎。
 *
 * <p>基于字符流 + 状态机的单次扫描实现，零正则、零反射、零对象拷贝。
 * 识别字段名、字段值与边界，仅对注册过的字段执行脱敏，其余内容原样透传。</p>
 *
 * <p>引擎实例持有字段→策略的映射；单次扫描的可变状态在
 * {@link MaskContext} 中按次创建，因此配置完成后同一引擎可被多线程安全复用
 * （约定：启动期注册完规则，运行期只读）。</p>
 *
 * <p>支持的结构化文本：</p>
 * <ul>
 *   <li>JSON：{@code {"name":"李四"}}、嵌套对象、数组</li>
 *   <li>KV：{@code name=李四&mobile=13800138000}&gt;</li>
 *   <li>日志文本：{@code user=张三 phone=13800138000 status=success}</li>
 *   <li>字符串中的 {@code \"} 与 {@code \\} 转义会被正确识别，不会截断键/值</li>
 * </ul>
 *
 * <p>无引号键值对规则：标识符（字母/数字/下划线）后紧跟 {@code =} 视为键值对，
 * 值以空白、{@code &}、{@code ,}、{@code ;}、{@code }}、{@code ]} 或输入结尾为界。
 * 键未注册时值原样透传，不会误伤普通文本。</p>
 *
 * <p>用法：</p>
 * <pre>{@code
 * MaskEngine engine = new MaskEngine();
 * engine.register("mobile", MaskType.MOBILE_PHONE);
 * engine.register("token", value -> "***");
 * engine.process(text);
 * }</pre>
 */
public final class MaskEngine {

    private final Map<String, MaskStrategy> strategyMap = new ConcurrentHashMap<>();

    /**
     * 创建空的脱敏引擎（不含任何规则）。
     */
    public MaskEngine() {
    }

    /**
     * 注册内置脱敏规则。
     *
     * @param fieldName 字段名，null 或空白将被忽略
     * @param type 内置敏感类型，null 等价于注销该字段
     */
    public void register(String fieldName, MaskType type) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            return;
        }
        if (type == null) {
            strategyMap.remove(fieldName);
            return;
        }
        strategyMap.put(fieldName, BasicMaskStrategies.forType(type));
    }

    /**
     * 注册自定义脱敏策略。
     *
     * @param fieldName 字段名，null 或空白将被忽略
     * @param strategy 自定义策略，null 等价于注销该字段
     */
    public void register(String fieldName, MaskStrategy strategy) {
        if (fieldName == null || fieldName.trim().isEmpty()) {
            return;
        }
        if (strategy == null) {
            strategyMap.remove(fieldName);
            return;
        }
        strategyMap.put(fieldName, strategy);
    }

    /**
     * 注销字段的脱敏规则。
     */
    public void unregister(String fieldName) {
        if (fieldName != null) {
            strategyMap.remove(fieldName);
        }
    }

    /**
     * 清空全部规则。
     */
    public void clear() {
        strategyMap.clear();
    }

    /**
     * 按规格串批量注册内置规则，便于配置文件驱动。
     *
     * <p>规格格式：{@code field:TYPE}，多项以逗号或分号分隔，空白忽略，类型名大小写不敏感。
     * 例如 {@code "mobile:MOBILE_PHONE,name:CHINESE_NAME,password:ALL"}。</p>
     *
     * @param ruleSpec 规格串；null 或空白忽略
     * @throws IllegalArgumentException 条目缺少字段/类型，或类型名未知
     */
    public void registerAll(String ruleSpec) {
        if (ruleSpec == null || ruleSpec.trim().isEmpty()) {
            return;
        }
        for (String entry : ruleSpec.split("[,;]")) {
            String item = entry.trim();
            if (item.isEmpty()) {
                continue;
            }
            int sep = item.indexOf(':');
            if (sep <= 0 || sep == item.length() - 1) {
                throw new IllegalArgumentException("非法脱敏规则 '" + item + "'，应为 field:TYPE");
            }
            String field = item.substring(0, sep).trim();
            String typeName = item.substring(sep + 1).trim();
            MaskType type = MaskType.getByNameIgnoreCase(typeName);
            if (type == null) {
                throw new IllegalArgumentException("未知脱敏类型 '" + typeName + "'（字段 " + field + "）");
            }
            register(field, type);
        }
    }

    /**
     * 对输入文本执行脱敏。
     *
     * @param input 待脱敏文本；null 或空串原样返回
     * @return 脱敏后的文本
     */
    public String process(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        MaskContext ctx = new MaskContext();
        char[] chars = input.toCharArray();
        StringBuilder result = new StringBuilder(input.length());

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            switch (ctx.state) {
                case START:
                    ctx.escaped = false;
                    if (c == '"') {
                        ctx.keyStart = i + 1; // 跳过开头的引号
                        ctx.tokenStart = -1;
                        ctx.state = MaskContext.State.IN_KEY;
                        result.append(c);
                    } else if (isIdentifierStart(c)) {
                        // 无引号键候选：记录 token 起点，字符照常输出
                        if (ctx.tokenStart < 0) {
                            ctx.tokenStart = i;
                        }
                        result.append(c);
                    } else if (ctx.tokenStart >= 0 && isIdentifierChar(c)) {
                        // 数字等标识符后续字符，延续当前 token
                        result.append(c);
                    } else if (c == '=' && ctx.tokenStart >= 0) {
                        // 标识符紧跟 '='：确认键值对
                        ctx.keyStart = ctx.tokenStart;
                        ctx.keyEnd = i;
                        ctx.tokenStart = -1;
                        result.append(c);
                        // 跳过等号后的空白字符
                        while (i + 1 < chars.length && Character.isWhitespace(chars[i + 1])) {
                            i++;
                            result.append(chars[i]);
                        }
                        // 值以引号开头则复用字符串值解析
                        if (i + 1 < chars.length && chars[i + 1] == '"') {
                            i++;
                            ctx.valueStart = i + 1; // 跳过开头的引号
                            result.append('"');
                            ctx.state = MaskContext.State.IN_STRING_VALUE;
                        } else {
                            ctx.valueStart = i + 1;
                            ctx.state = MaskContext.State.IN_UNQUOTED_VALUE;
                        }
                    } else {
                        ctx.tokenStart = -1;
                        result.append(c);
                    }
                    break;

                case IN_KEY:
                    result.append(c);
                    if (ctx.escaped) {
                        ctx.escaped = false;
                    } else if (c == '\\') {
                        ctx.escaped = true;
                    } else if (c == '"') {
                        ctx.keyEnd = i;
                        ctx.state = MaskContext.State.AFTER_KEY;
                    }
                    break;

                case AFTER_KEY:
                    if (c == ':' || c == '=') {
                        result.append(c);
                        // 跳过冒号后的空白字符
                        while (i + 1 < chars.length && Character.isWhitespace(chars[i + 1])) {
                            i++;
                            result.append(chars[i]);
                        }

                        // 检查值类型
                        if (i + 1 < chars.length && chars[i + 1] == '"') {
                            ctx.state = MaskContext.State.IN_STRING_VALUE;
                            i++; // 跳过引号
                            ctx.valueStart = i + 1; // 跳过开头的引号
                            result.append('"');
                        // 处理 [ 或 { 或 [{ 这种开始嵌套结构
                        } else if (i + 1 < chars.length && (chars[i + 1] == '{' || chars[i + 1] == '[')) {
                            ctx.state = MaskContext.State.START;
                            i++;
                            result.append(chars[i]);
                        } else {
                            ctx.state = MaskContext.State.IN_VALUE;
                            ctx.valueStart = i + 1;
                        }
                    } else {
                        result.append(c);
                    }
                    break;

                case IN_STRING_VALUE:
                    if (ctx.escaped) {
                        ctx.escaped = false;
                    } else if (c == '\\') {
                        ctx.escaped = true;
                    } else if (c == '"') {
                        ctx.valueEnd = i;
                        processSensitiveValue(ctx, chars, result);
                        resetKeyValue(ctx);
                        result.append(c);
                        ctx.state = MaskContext.State.AFTER_VALUE;
                    }
                    break;

                case IN_VALUE:
                    if (c == ',' || c == '}') {
                        ctx.valueEnd = i;
                        processSensitiveValue(ctx, chars, result);
                        resetKeyValue(ctx);
                        result.append(c);
                        ctx.state = MaskContext.State.START;
                    }
                    break;

                case IN_UNQUOTED_VALUE:
                    if (isUnquotedValueEnd(c)) {
                        ctx.valueEnd = i;
                        processSensitiveValue(ctx, chars, result);
                        resetKeyValue(ctx);
                        result.append(c);
                        ctx.state = MaskContext.State.START;
                    }
                    break;

                case AFTER_VALUE:
                    result.append(c);
                    ctx.state = MaskContext.State.START;
                    break;
            }
        }

        // 收尾：输入到结尾仍未闭合的值（缺少结束分隔符或引号）
        if (ctx.valueStart >= 0 && ctx.valueEnd == -1) {
            ctx.valueEnd = chars.length;
            processSensitiveValue(ctx, chars, result);
        }

        return result.toString();
    }

    private void processSensitiveValue(MaskContext ctx, char[] chars, StringBuilder result) {
        if (ctx.valueEnd <= ctx.valueStart) { // 空值无需处理
            return;
        }
        String key = new String(chars, ctx.keyStart, ctx.keyEnd - ctx.keyStart);
        String value = new String(chars, ctx.valueStart, ctx.valueEnd - ctx.valueStart).trim();

        MaskStrategy strategy = strategyMap.get(key);
        if (strategy != null) {
            result.append(strategy.mask(value));
        } else {
            result.append(chars, ctx.valueStart, ctx.valueEnd - ctx.valueStart);
        }
    }

    private static void resetKeyValue(MaskContext ctx) {
        ctx.keyStart = ctx.keyEnd = ctx.valueStart = ctx.valueEnd = -1;
    }

    private static boolean isIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static boolean isIdentifierChar(char c) {
        return isIdentifierStart(c) || (c >= '0' && c <= '9');
    }

    private static boolean isUnquotedValueEnd(char c) {
        return Character.isWhitespace(c) || c == '&' || c == ',' || c == ';' || c == '}' || c == ']';
    }
}