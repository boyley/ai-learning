# 22 · JSON 编解码定制（JSON Codec）

> 本模块目标：搞清楚 LangChain4j 一个“看不见但无处不在”的底层基础设施——
> **JSON 编解码器**。结构化输出、工具调用、对话历史持久化，底层都靠它。

## 一、为什么要懂它

LangChain4j 很多高级特性，本质都是“对象 ↔ JSON 字符串”的互转：

| 场景 | 底层在做的事 |
|---|---|
| 结构化输出 | 把模型回答的 JSON 文本 `fromJson` 成你的 Java 对象 |
| 工具调用 | 把方法签名转成 JSON Schema，把模型给的参数 `fromJson` 出来 |
| **对话历史持久化** | 把 `List<ChatMessage>` 序列化成 JSON 存库，下次读回来恢复记忆 |

默认实现基于 **Jackson**（`dev.langchain4j.internal.JacksonJsonCodec`），开箱即用。
但企业里常需要：统一公司的 `ObjectMapper`、改日期格式、注册自定义序列化器、
甚至换成 Gson —— 这时就要懂下面的 SPI 替换机制。

## 二、两个层次的编解码器（别混淆）

| API | 作用 | 默认实现 |
|---|---|---|
| `dev.langchain4j.internal.Json` | 通用对象工具类：`toJson(Object)` / `fromJson(String, Class)` | `JacksonJsonCodec` |
| `dev.langchain4j.data.message.ChatMessageJsonCodec` | 专给 `ChatMessage`（多态：System/User/Ai/Tool…）做编解码 | `JacksonChatMessageJsonCodec` |

> `ChatMessage` 是多态类型，普通 JSON 序列化无法区分子类型，
> 所以 LangChain4j 专门提供 `ChatMessageJsonCodec`，序列化时带上类型信息，反序列化时按类型还原。

## 三、流程图

```mermaid
flowchart TD
    subgraph 通用对象
      A["Java 对象 (Book)"] -->|"Json.toJson(对象)"| B["JSON 字符串"]
      B -->|"Json.fromJson(串, 类)"| A
    end
    subgraph 对话消息（持久化）
      C["List&lt;ChatMessage&gt;<br/>System/User/Ai"] -->|"codec.messagesToJson(列表)"| D["JSON 字符串<br/>(存数据库/文件/Redis)"]
      D -->|"codec.messagesFromJson(串)"| C
    end
    subgraph SPI 替换
      E["Json 静态初始化"] -->|"ServiceLoader 查找"| F{"找到自定义<br/>JsonCodecFactory?"}
      F -->|"是"| G["用你的实现"]
      F -->|"否"| H["退回内置 Jackson 实现"]
    end
```

## 四、关键代码

```java
// 演示1：通用对象 <-> JSON
String json = Json.toJson(book);              // 对象 -> JSON
Book back = Json.fromJson(json, Book.class);  // JSON -> 对象

// 演示2：对话消息 <-> JSON（对话历史持久化的底层做法）
ChatMessageJsonCodec codec = new JacksonChatMessageJsonCodec();
String historyJson = codec.messagesToJson(history);          // List<ChatMessage> -> JSON
List<ChatMessage> restored = codec.messagesFromJson(historyJson); // JSON -> List<ChatMessage>
```

## 五、SPI 替换机制（原理）

LangChain4j 用 Java 标准的 **SPI（Service Provider Interface）** 让你替换默认 JSON 实现：

1. `Json` 类在静态初始化时，用 `ServiceLoader` 查找 SPI 接口
   `dev.langchain4j.spi.json.JsonCodecFactory` 的实现；
2. 找到就用你的实现，找不到才退回内置的 `JacksonJsonCodec`；
3. `ChatMessageJsonCodec` 同理，对应 SPI 接口
   `dev.langchain4j.spi.data.message.ChatMessageJsonCodecFactory`。

**如果要替换默认实现，你只需三步**（本模块不真正替换，仅讲清原理）：

```java
// 1) 实现 SPI 工厂接口
public class MyJsonCodecFactory implements JsonCodecFactory {
    @Override
    public Json.JsonCodec create() {
        return new MyJsonCodec(); // 你自己的实现（可包一个定制的 ObjectMapper / Gson）
    }
}
```

```
// 2) 在 src/main/resources/META-INF/services/ 下建一个文件，文件名是接口全限定名：
//    META-INF/services/dev.langchain4j.spi.json.JsonCodecFactory
// 文件内容写实现类全限定名：
com.example.MyJsonCodecFactory
```

3. 之后整个 JVM 里 `Json.toJson/fromJson` 就会走你的实现，全局生效，无需改业务代码。

> 这正是“面向接口 + SPI”解耦的威力：框架定义接口与查找逻辑，你只需注册实现即可热插拔。

## 六、运行

```bash
cd 22-json-codec
mvn spring-boot:run
```

> 演示1、2 不需要联网即可看到 JSON 互转；演示3 会真实调用 DeepSeek，需要配好 chat 的 Key。

## 七、小结

- `Json` 是通用对象编解码工具；`ChatMessageJsonCodec` 专管对话消息（多态）。
- 对话历史持久化的底层就是 `messagesToJson` / `messagesFromJson`。
- 通过 SPI（`JsonCodecFactory`）可全局替换默认 Jackson 实现，无需改业务代码。
- 下一站：[23-http-client](../23-http-client) 定制底层 HTTP 客户端。
