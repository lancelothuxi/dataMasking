# dataMasking

**零反射、零正则、零注解、零对象图遍历，面向字符流的高性能文本脱敏引擎。**

适用于日志、JSON、KV 与文本链路的高频热路径脱敏，不依赖对象元数据，无深拷贝与对象图遍历。

## 项目定位

`dataMasking` 是**文本级**脱敏引擎，不是对象注解脱敏框架。

- 文本级：直接处理日志字符串、JSON body、KV 文本，逐字符扫描一次完成替换
- 热路径友好：单次扫描 O(n)，无正则回溯，无反射，无注解解析
- 低侵入：可嵌入日志组件、网关、消息通道或统一安全过滤层

定位一句话：**面向字符流的高性能脱敏引擎，只做文本脱敏，不做对象模型脱敏。**

## 设计理念

字符流 + 状态机：

- 识别字段名、字段值与边界（JSON 引号键 / 日志与 KV 的无引号键）
- 只对注册的字段执行脱敏，其余内容原样透传
- 正确处理字符串内的 `\"` 与 `\\` 转义，不会截断键/值
- 单次扫描，无正则、无反射、无注解解析、无对象图遍历
- 分配开销：输入一次 `char[]` 复制 + 输出 `StringBuilder`；每个命中字段构造键/值两个 `String`（非零分配）

与反射/注解方案的成本差异：

| 维度 | 反射/注解方案 | dataMasking（字符流） |
|---|---|---|
| 定位 | 业务对象脱敏（DTO / VO / Entity） | 日志 / JSON / KV / HTTP body 热路径 |
| 成本 | 反射拿字段、解析注解、递归对象图、深拷贝 | 逐字符扫描，无对象图与元数据成本 |
| 侵入性 | 需要注解与对象模型配合 | 嵌入输出层 / 网关 / 日志组件即可 |
| 典型载体 | 返回值统一加工 | 网关过滤器、日志 appender、消息通道 |

## 快速开始

依赖（Maven）：

```xml
<dependency>
    <groupId>io.github.lancelothuxi</groupId>
    <artifactId>data-masking</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

使用：

```java
// 1. 创建引擎并注册脱敏字段
MaskEngine engine = new MaskEngine();
engine.register("name", MaskType.CHINESE_NAME);
engine.register("user", MaskType.CHINESE_NAME);
engine.register("idCard", MaskType.ID_CARD);
engine.register("mobile", MaskType.MOBILE_PHONE);
engine.register("phone", MaskType.FIXED_PHONE);
engine.register("email", MaskType.EMAIL);
engine.register("address", MaskType.ADDRESS);
engine.register("password", MaskType.ALL);

// 2. 执行脱敏
String json = "{\"name\":\"李四\",\"idCard\":\"110101199003072345\",\"mobile\":\"13800138000\",\"email\":\"test@example.com\"}";
engine.process(json);
// => {"name":"李*","idCard":"110******345","mobile":"138******8000","email":"******.com"}

String log = "2025-09-07 10:00:00 user=张三 phone=13800138000 email=test@example.com status=success";
engine.process(log);
// => 2025-09-07 10:00:00 user=张* phone=*******8000 email=******.com status=success
```

## 支持的结构化文本

### 1. JSON

```text
{"name":"李四","mobile":"13800138000","email":"test@example.com"}
```

输出：

```text
{"name":"李*","mobile":"138******8000","email":"******.com"}
```

支持嵌套对象、数组、空白与 `=` 分隔符；字符串值内的转义引号（`\"`）不会被误判为边界。

### 2. KV 格式

```text
name=李四&mobile=13800138000&email=test@example.com
```

输出：

```text
name=李*&mobile=138******8000&email=******.com
```

### 3. 日志文本

无引号键值对：标识符后紧跟 `=` 即视为键值对，值以空白、`&`、`,`、`;`、`}`、`]` 或输入结尾为界。

```text
2025-09-07 10:00:00 user=张三 phone=13800138000 email=test@example.com status=success
```

输出：

```text
2025-09-07 10:00:00 user=张* phone=*******8000 email=******.com status=success
```

键未注册时值原样透传，普通文本与时间戳（如 `10:00:00`）不会被误伤。

边界：键与 `=` 之间不能有空白（`name = x` 不识别）；键必须是字母/下划线开头、由字母/数字/下划线组成；不支持单引号键（`'name':'x'`）。

## 核心 API

| 类 | 职责 |
|---|---|
| `MaskEngine` | 脱敏引擎：`register(...)` 配置规则，`process(String)` 单次扫描；配置完成后可并发复用 |
| `MaskType` | 内置敏感类型枚举 |
| `MaskStrategy` | 函数式接口 `String mask(String)`，支持 lambda 自定义策略 |
| `Maskers` | 内置脱敏规则实现 |
| `MaskContext` | 单次扫描的解析上下文（状态、字段名、值区间、是否在字符串中），内部使用 |

注册方式：

```java
MaskEngine engine = new MaskEngine();

engine.register("mobile", MaskType.MOBILE_PHONE);   // 内置类型
engine.register("password", value -> "******");      // 自定义策略（lambda）

engine.unregister("mobile");                         // 注销
engine.clear();                                      // 清空
```

## 内置脱敏类型

| MaskType | 规则 | 示例 |
|---|---|---|
| `CHINESE_NAME` | 显示第 1 个汉字，其余隐藏 | 张三 → 张* |
| `ID_CARD` | 前 3 后 3 显示，中间 ****** | 110101199003072345 → 110******345 |
| `FIXED_PHONE` | 后 4 位显示，其余隐藏 | 01012345678 → *******5678 |
| `MOBILE_PHONE` | 前 3 后 4 显示，中间 ****** | 13800138000 → 138******8000 |
| `ADDRESS` | 前 2 后 2 显示，中间 ****** | 北京市海淀区中关村大街1号 → 北京******1号 |
| `EMAIL` | 最后一个点之前 ****** | test@example.com → ******.com |
| `BANK_CARD` | 前 2 后 4 显示，中间 ****** | 6225880123456789 → 62******6789 |
| `BANK_NAME` | 前 4 位显示，其余隐藏 | 中国工商银行 → 中国工商** |
| `CNAPS_CODE` | 前 4 后 4 显示，中间 ****** | 123456789012 → 1234******9012 |
| `BANK_CARD_DATE` | 前 1 后 1 显示，其余隐藏 | 0126 → 0**6 |
| `ALL` | 固定 3 个星号 | mySecret123 → *** |
| `NULL` | 值清空 | mySecret123 → （空） |

约定：值为字符串 `"null"` 时保持原样返回，便于排查数据问题；空值返回空串。

## 日志框架集成

内置 Logback 与 Log4j2 适配器，规则规格通用：`field:TYPE`，逗号或分号分隔，类型名大小写不敏感。
两个框架的依赖在 pom 中均为 `provided`——你用自己的日志框架版本即可。

### Logback：`%maskedMsg` 转换词

```xml
<configuration>
  <conversionRule conversionWord="maskedMsg"
      converterClass="io.github.lancelot.datamasking.logback.MaskingConverter"/>

  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d %-5level %logger{36} - %maskedMsg{mobile:MOBILE_PHONE,name:CHINESE_NAME}%n</pattern>
    </encoder>
  </appender>

  <root level="INFO"><appender-ref ref="CONSOLE"/></root>
</configuration>
```

自定义策略：子类化 `MaskingConverter` 并在构造器中 `setEngine(engine)` 注入。

### Log4j2：`MaskingRewritePolicy`

```xml
<Configuration>
  <Appenders>
    <Rewrite name="masked">
      <MaskingRewritePolicy rules="mobile:MOBILE_PHONE,name:CHINESE_NAME,password:ALL"/>
      <Console name="Console" target="SYSTEM_OUT"/>
    </Rewrite>
  </Appenders>
  <Loggers>
    <Root level="info"><AppenderRef ref="masked"/></Root>
  </Loggers>
</Configuration>
```

两者均只脱敏格式化后的消息；脱敏异常不会中断日志链路（回退原始消息）。

## 线程安全

- `MaskEngine` 单次扫描状态在 `MaskContext` 中按次创建，引擎可并发复用
- 规则存储基于 `ConcurrentHashMap`，注册与查询均线程安全
- 建议：应用启动期完成规则注册，运行期只读

## 性能（参考）

单线程、1 MB 输入、best-of-N、JDK 8；仅作量级参考，非 JMH 严格基准，随机器/JDK 波动。

| 场景 | 吞吐 | 备注 |
|---|---|---|
| 1 MB 日志文本（4 字段脱敏） | ~190 MB/s | 含无引号 KV 解析 + 4 字段替换 |
| 1 MB JSON 数组（4 字段脱敏） | ~240 MB/s | |
| 1 MB 日志纯扫描（无命中） | ~240 MB/s | |
| 单条日志（113 B） | ~0.63 µs/条 | 逐条调用延迟 |
| 正则 `replaceAll`（同任务简化规则） | ~110 MB/s | 参考系，整值替换、非同口径 |

要点：

- 无引号 KV 解析在**标识符密集**的日志文本上带来约 2 倍扫描开销；在 **JSON** 上开销可忽略（大多字符位于引号内，不走无引号键判断）
- 开销集中在逐字符判断本身；全程无正则回溯、无反射

## 为什么不做注解/对象反射

常见方案通过对象反射、注解或深拷贝实现业务对象脱敏，适合返回 DTO/VO 的统一加工；但高频日志、链路输出或文本处理场景中，反射开销、深拷贝与对象图遍历是额外成本。`dataMasking` 关注文本层，延迟低、可嵌入、吞吐高、配置侵入小。

## 项目结构

```text
src/main/java/io/github/lancelot/datamasking/
├── MaskEngine.java        引擎入口：注册规则 + 状态机扫描
├── MaskType.java          内置敏感类型枚举
├── MaskStrategy.java      自定义策略函数式接口
├── Maskers.java           内置脱敏规则实现
├── BasicMaskStrategies.java  内置策略适配（内部）
├── MaskContext.java       解析上下文（内部）
├── logback/
│   └── MaskingConverter.java      Logback %maskedMsg 转换词
└── log4j2/
    └── MaskingRewritePolicy.java  Log4j2 RewritePolicy 插件
src/test/java/io/github/lancelot/datamasking/
├── MaskEngineTest.java             引擎行为测试
├── MaskEngineRegisterTest.java     注册语义测试
├── logback/MaskingConverterTest.java         Logback 集成测试
└── log4j2/MaskingRewritePolicyTest.java      Log4j2 集成测试
```

## 适用场景

适合：

- 日志平台与调试输出脱敏
- JSON / KV / HTTP body 文本脱敏
- 网关、代理层、消息通道的敏感字段过滤
- 统一安全过滤链路

不主打：

- POJO/DTO 注解脱敏体系
- 复杂对象图递归脱敏
- 全功能业务框架封装

## 未来方向

- 更多内置字段类型与自定义规则扩展
- 日志框架适配（Logback / Log4j2）
- 统一脱敏配置中心接入

## 许可证

Apache License 2.0