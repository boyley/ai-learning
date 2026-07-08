# 23 · 社区工具集（一行接入百度搜索）

> 本模块目标：体会 Spring AI Alibaba 的生态优势——**社区工具集**。很多常用能力（联网搜索、地图、翻译）已被封装成“一个 starter = 一个工具”，加一行依赖即可用，无需自己写。

## 一、要懂的核心概念

| 概念 | 大白话解释 |
|---|---|
| **社区工具集** | 社区把常用能力封装成 `spring-ai-alibaba-starter-tool-calling-*` 系列 starter，引入即用。 |
| **自动注册** | 引入 starter 后，工具会被**自动配置**成 Spring Bean（`ToolCallback` / `ToolCallbackProvider`）。 |
| **`ToolCallback`** | 一个“可被模型调用的工具”的统一抽象（Spring AI）。 |
| **`ToolCallbackProvider`** | 一个“工具提供者”，能一次吐出一批 `ToolCallback`。 |
| **`.toolCallbacks(...)`** | 把收集到的工具挂到 ChatClient 上，模型即可按需调用。 |

## 二、社区工具集生态（举例）

| Starter | 能力 |
|---|---|
| `...-tool-calling-baidusearch` | 百度搜索（本模块示例） |
| `...-tool-calling-amap` | 高德地图（路线 / POI） |
| `...-tool-calling-baidutranslate` | 百度翻译 |
| `...-tool-calling-weather` 等 | 天气、时间、爬虫…… 几十个 |

## 三、流程图

```mermaid
flowchart LR
    P[加一行依赖<br/>baidusearch starter] --> B[Spring 自动注册<br/>百度搜索工具 Bean]
    B --> C[收集 ToolCallback]
    C --> D[chatClient.toolCallbacks 挂上]
    D --> E[模型需要时<br/>自动联网搜索再回答]
```

## 四、关键代码

```java
// 可选注入：容器里有就用，没有也不报错（保证任何环境都能启动）
ObjectProvider<ToolCallbackProvider> providers;   // 工具提供者
ObjectProvider<ToolCallback> callbacks;           // 独立工具 Bean

// 收集所有自动注册的工具
List<ToolCallback> tools = new ArrayList<>();
providers.forEach(p -> tools.addAll(List.of(p.getToolCallbacks())));
callbacks.forEach(tools::add);

// 一行挂上，模型即可联网搜索
String answer = chatClient.prompt()
        .user("用百度搜一下 Spring AI Alibaba 最新特性")
        .toolCallbacks(tools)
        .call().content();
```

> 为什么用 `ObjectProvider` 可选注入？因为不同版本自动注册的 Bean 名称/类型可能有差异。
> 可选注入 + 判空兜底，能保证本模块**在任何环境都编译、启动、跑完**，不会因缺 Bean/Key 崩溃。

## 五、怎么运行

1. 配好百炼 Key。部分社区工具（如百度搜索）可能还需在配置里补充其自己的 Key，
   例如（键名以该 starter 文档为准）：
   ```yaml
   spring:
     ai:
       alibaba:
         toolcalling:
           baidusearch:
             enabled: true
             # 若该版本需要，再补充对应的鉴权字段
   ```
2. 运行：
   ```bash
   cd 23-tool-calling-community
   mvn spring-boot:run
   ```

## 六、预期输出（示例）

```
========== 模块23：社区工具集(一行接入百度搜索) ==========

【自动发现的社区工具数量】1
  - 工具：baiduSearch

【提问】请帮我用百度搜索一下：Spring AI Alibaba 最新版本有哪些新特性？...

【AI 答(已联网搜索)】根据搜索结果，Spring AI Alibaba 近期在 Graph 多智能体、
Nacos 动态 Prompt、社区工具集等方面……（内容来自实时搜索）
```

> 若你的环境未注册该工具，会打印“未发现社区工具，退回不联网直接回答”，这是**正常的兜底**。

## 七、小结

- 社区工具集 = **一行依赖 = 一个现成能力**，站在生态肩膀上，别重复造轮子。
- 工具自动注册为 `ToolCallback` / `ToolCallbackProvider`，用 `.toolCallbacks(...)` 挂到 ChatClient。
- 可选注入 + 判空兜底是接入不确定第三方工具时的稳妥姿势。
- 下一站：[24-nacos-prompt](../24-nacos-prompt) 学习**Nacos 动态 Prompt**——改配置不重启就能更新提示词。
