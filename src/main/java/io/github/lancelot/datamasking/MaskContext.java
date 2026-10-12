package io.github.lancelot.datamasking;

/**
 * 脱敏解析上下文（内部实现）。
 *
 * <p>承载 {@link MaskEngine} 单次扫描过程中的全部可变状态：当前状态机状态、
 * 当前键/值在输入中的字符区间、转义标记与无引号键缓存。引擎为每次
 * {@link MaskEngine#process(String)} 调用创建新实例，故引擎本身可安全并发复用。</p>
 *
 * <p>非公共 API，字段与结构不保证向后兼容。</p>
 */
public final class MaskContext {

    /**
     * 状态机状态。
     *
     * <ul>
     *   <li>START：初始态，可识别引号键与无引号键（token 起点记于 tokenStart）</li>
     *   <li>IN_KEY：引号键内容解析中</li>
     *   <li>AFTER_KEY：键结束，等待分隔符与值</li>
     *   <li>IN_STRING_VALUE：字符串值解析中（是否在字符串内由此状态表达）</li>
     *   <li>IN_VALUE：非字符串值解析中</li>
     *   <li>IN_UNQUOTED_VALUE：无引号值解析中</li>
     *   <li>AFTER_VALUE：值处理完成，等待回到初始态</li>
     * </ul>
     */
    public enum State {
        START, IN_KEY, AFTER_KEY, IN_VALUE, IN_STRING_VALUE, AFTER_VALUE,
        IN_UNQUOTED_VALUE
    }

    State state = State.START;

    /** 当前键在输入中的起始下标（含），-1 表示未开始。 */
    int keyStart = -1;
    /** 当前键在输入中的结束下标（不含），-1 表示未结束。 */
    int keyEnd = -1;
    /** 当前值在输入中的起始下标（含），-1 表示未开始。 */
    int valueStart = -1;
    /** 当前值在输入中的结束下标（不含），-1 表示未结束（输入到结尾未闭合）。 */
    int valueEnd = -1;

    /** 当前无引号键候选 token 的起始下标（在 START 态跟踪），-1 表示不在 token 中。 */
    int tokenStart = -1;

    /** 转义标记：上一个字符是 '\'，当前字符为被转义字符。 */
    boolean escaped;
}