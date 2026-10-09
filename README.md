# dataMasking

基于状态机的结构化文本脱敏实现，零正则，低侵入，高性能，适用于日志、JSON、KV 和文本链路脱敏。

## 项目定位

`dataMasking` 不是一个“对象注解脱敏框架”，而是一个面向结构化文本的高性能脱敏核心。

它适合以下场景：

- 日志脱敏
- JSON 字段脱敏
- key=value/KV 格式脱敏
- 接口请求/响应文本脱敏
- 网关、日志链路、消息通道中的敏感字段过滤

## 设计理念

相比传统的正则替换或对象反射脱敏方案，`dataMasking` 采用的是字符流 + 状态机扫描：

- 零正则：避免复杂正则回溯和维护成本
- 零反射：不依赖对象元数据，不存在深拷贝和对象图遍历
- 流式处理：逐字符扫描，适合高吞吐文本场景
- 低侵入：可直接嵌入应用、日志组件、网关或中间件

这使它更适合处理：

- 日志文本
- JSON
- KV 键值对
- 结构化字符串混合内容

## 典型用法

```java
// 注册脱敏字段
SensitiveInfoRegistry.put("name", SensitiveType.CHINESE_NAME);
SensitiveInfoRegistry.put("idCard", SensitiveType.ID_CARD);
SensitiveInfoRegistry.put("mobile", SensitiveType.MOBILE_PHONE);
SensitiveInfoRegistry.put("phone", SensitiveType.FIXED_PHONE);
SensitiveInfoRegistry.put("email", SensitiveType.EMAIL);
SensitiveInfoRegistry.put("address", SensitiveType.ADDRESS);

// JSON 示例
String input = "{\"name\":\"李四\",\"idCard\":\"110101199003072345\",\"mobile\":\"13800138000\",\"email\":\"test@example.com\"}";
String expected = "{\"name\":\"李*\",\"idCard\":\"110******345\",\"mobile\":\"138******8000\",\"email\":\"******.com\"}";
assertEquals(SensitiveReplacer.deSensitiveString(input), expected);
```

## 支持的结构化文本示例

### 1. JSON

```text
{"name":"李四","mobile":"13800138000","email":"test@example.com"}
```

输出：

```text
{"name":"李*","mobile":"138****8000","email":"******.com"}
```

### 2. KV 格式

```text
name=李四&mobile=13800138000&email=test@example.com
```

输出：

```text
name=李*&mobile=138****8000&email=******.com
```

### 3. 日志文本

```text
2025-09-07 10:00:00 user=张三 phone=13800138000 email=test@example.com status=success
```

输出：

```text
2025-09-07 10:00:00 user=张* phone=138****8000 email=******.com status=success
```

## 核心能力

- `SensitiveInfoRegistry`：注册字段名与脱敏类型
- `SensitiveType`：内置敏感字段类型
- `SensitiveReplacer`：对字符串执行脱敏替换
- 状态机处理：识别字段名称、字段值和边界

## 为什么不走注解/对象反射方案

常见脱敏方案通常会通过对象反射、注解或深拷贝实现，它们更适合：

- 业务对象脱敏
- VO/DTO 字段处理
- 返回值统一加工

但在高频日志、链路输出或文本处理场景中，这类方案存在额外成本：

- 反射开销
- 深拷贝对象
- 对象图遍历
- 配置与代码侵入增加

而 `dataMasking` 关注的是“文本层”脱敏，重在：

- 低延迟
- 可嵌入
- 高吞吐
- 低侵入

## 适用场景总结

适合：

- 日志平台
- 调试输出
- 网关/代理层
- 统一安全过滤
- 结构化文本链路处理

不主打：

- 完整 POJO/DTO 注解脱敏体系
- 复杂对象图递归脱敏
- 全功能业务框架封装

## 未来方向

后续可以在此核心上进一步扩展：

- 自定义规则扩展
- 更多字段类型
- 更强的多语言支持
- 日志框架适配（Logback / Log4j2）
- 统一脱敏配置中心接入

## 许可证

Apache License 2.0

## 备注

这是一个“核心脱敏引擎”，目标不是替代所有对象注解脱敏框架，而是提供一个轻量、高性能、可嵌入的文本脱敏能力。
