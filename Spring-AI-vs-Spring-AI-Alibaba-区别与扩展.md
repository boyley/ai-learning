# Spring AI vs Spring AI Alibaba：区别与扩展全解

> 一句话结论：**Spring AI 是“地基和规范”，Spring AI Alibaba 是“盖在这块地基上、面向中国开发者和阿里云生态、且额外带了多智能体编排能力的高层框架”。**
> 二者不是竞争关系，而是 **上下层关系**——Spring AI Alibaba 依赖并复用 Spring AI，绝大多数 API（`ChatClient` / `Advisor` / `@Tool` / `VectorStore`）**写法一模一样**。

本文对应本仓库两个子项目：[`spring-ai-learning`](spring-ai-learning)（Spring AI）与 [`spring-ai-alibaba-learning`](spring-ai-alibaba-learning)（Spring AI Alibaba）。可左右对照着看。

---

## 一、它们是什么？

| | Spring AI | Spring AI Alibaba |
|---|---|---|
| **出品方** | Spring 官方（VMware / Broadcom） | 阿里巴巴开源 |
| **定位** | Java 生态的 **AI 应用开发标准框架** | **Agentic AI（智能体）开发框架**，构建在 Spring AI 之上 |
| **groupId** | `org.springframework.ai` | `com.alibaba.cloud.ai` |
| **本质** | 定义抽象 + 官方模型适配（OpenAI、Anthropic、Ollama…） | 复用 Spring AI 抽象 + 阿里模型/中间件适配 + **Graph 多智能体** |
| **官网** | <https://docs.spring.io/spring-ai/reference/> | <https://java2ai.com/> |
| **类比** | 相当于 `JDBC`（规范 + 驱动） | 相当于 `MyBatis-Plus`（在规范之上做增强 + 生态整合） |

### 分层关系图

```
        ┌───────────────────────────────────────────────────────────┐
        │                你的业务代码                                  │
        │        ChatClient / Advisor / @Tool / VectorStore           │  ← 这一层两者完全一样
        ├───────────────────────────────────────────────────────────┤
        │   ★ Spring AI Alibaba 独有的扩展层                          │
        │   · DashScope 原生模型（qwen / wanx / cosyvoice / paraformer）│
        │   · Graph 多智能体编排（StateGraph / ReactAgent / A2A）      │
        │   · 企业中间件：Nacos 配置/注册、ARMS 观测、多种向量库/记忆库  │
        │   · 平台产品：JManus / DeepResearch / NL2SQL(ChatBI) / Studio │
        ├───────────────────────────────────────────────────────────┤
        │   Spring AI（核心抽象层）                                    │  ← 地基
        │   ChatModel · EmbeddingModel · ImageModel · VectorStore      │
        │   Advisor · Tool Calling · RAG · Evaluation · Observability   │
        ├───────────────────────────────────────────────────────────┤
        │              Spring Boot 3.x + Java 17                       │
        └───────────────────────────────────────────────────────────┘
```

> 关键认知：**Spring AI Alibaba 没有“重写”对话/RAG/工具这些能力，而是直接用 Spring AI 的。**
> 它真正“加”的东西是：① 把阿里的模型和云中间件接进来；② 在上面盖了一套 **Graph 多智能体编排**（这是纯 Spring AI 目前没有的）。

---

## 二、核心区别速览

| 维度 | Spring AI | Spring AI Alibaba |
|---|---|---|
| **对话 API** | `ChatClient` | 同一个 `ChatClient`（就是 Spring AI 的） |
| **默认接哪个模型** | OpenAI（还有 Anthropic/Azure/Ollama/Vertex 等一大批） | **DashScope（百炼/通义千问）** 原生优先 |
| **国内可用性** | 需科学上网 / 用兼容层接国产模型 | 国内直连，一个百炼 Key 打通全部能力 |
| **多模型能力** | 对话/向量/图片/语音常要接多家拼配 | DashScope 一家全给：qwen(对话/视觉) + wanx(文生图) + cosyvoice(TTS) + paraformer(ASR) + text-embedding |
| **多智能体编排** | ❌ 无内置图编排（只有基础 Agent 样例） | ✅ **Graph**：节点/边/全局状态/并行/条件/人类介入（对标 LangGraph） |
| **现成 Agent** | 需自己用 Advisor/Tool 拼 | ✅ `ReactAgent` 等高层 Agent 封装 + 多智能体协作 |
| **配置中心集成** | ❌ | ✅ **Nacos 动态 Prompt / 动态模型配置**，不重启改提示词 |
| **可观测** | Micrometer/OpenTelemetry 标准埋点 | 同上 + **阿里云 ARMS** 一键接入 |
| **企业中间件生态** | 社区各自实现 | ✅ 官方成套：多向量库(AnalyticDB/OpenSearch/Tair/OceanBase)、多记忆库(Redis/JDBC/ES)、几十种文档读取器 |
| **平台级产品** | ❌ | ✅ JManus、DeepResearch、NL2SQL(ChatBI)、Studio 可视化 |
| **版本号** | `1.1.x`（如 1.1.7） | `1.1.2.x`（前三位跟随 Spring AI，末位是阿里迭代号） |

---

## 三、Spring AI Alibaba 到底“扩展”了什么？（重点）

把扩展分成 5 大类。**第 1、5 类是“接生态”，第 2、3 类才是它真正的技术护城河。**

### 扩展 1️⃣：DashScope 原生模型接入（一个 Key，全部能力）

Spring AI 里接国产模型通常走“OpenAI 兼容层”拼 `base-url`；Spring AI Alibaba 直接给了 **DashScope 原生 Starter**，模型能力更全、参数更贴合：

| 能力 | 实现类 | 模型 | 本仓库模块 |
|---|---|---|---|
| 对话 | `DashScopeChatModel` / `DashScopeChatOptions` | qwen-turbo/plus/max | [02](spring-ai-alibaba-learning/02-chat-model) |
| 视觉多模态 | 同上 | qwen-vl-max | [05](spring-ai-alibaba-learning/05-multimodality) |
| 文本向量 | `DashScopeEmbeddingModel` | text-embedding-v3 (1024维) | [09](spring-ai-alibaba-learning/09-embedding) |
| 文生图 | `DashScopeImageModel` | 通义万相 wanx2.1 | [13](spring-ai-alibaba-learning/13-image-model) |
| 语音合成 TTS | `DashScopeAudioSpeechModel` | cosyvoice | [14](spring-ai-alibaba-learning/14-audio-model) |
| 语音识别 ASR | `DashScopeAudioTranscriptionModel` | paraformer | [14](spring-ai-alibaba-learning/14-audio-model) |

```xml
<!-- 一个依赖搞定上面所有能力 -->
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
</dependency>
```
```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY}   # 只填这一个
```

### 扩展 2️⃣：Graph —— 多智能体编排（★ 最核心的差异化）

这是 Spring AI **没有**、Spring AI Alibaba **独有** 的能力，可理解为 **Java 版 LangGraph**。
它把复杂的 AI 流程建模成一张 **有向图**：节点(Node) 做事、边(Edge) 决定流向、全局状态(OverAllState) 贯穿始终。

| Graph 特性 | 说明 | 本仓库模块 |
|---|---|---|
| `StateGraph` 基础 | 节点 + 边 + `KeyStrategy` 全局状态 | [19](spring-ai-alibaba-learning/19-graph-basic) |
| 并行节点 | 多分支并发执行再汇聚（`AppendStrategy`） | [20](spring-ai-alibaba-learning/20-graph-parallel) |
| 条件边 | `addConditionalEdges` 按状态动态路由 | [21](spring-ai-alibaba-learning/21-graph-human-in-the-loop) |
| 人类介入 | 中断 → 等人审批 → 恢复（human-in-the-loop） | [21](spring-ai-alibaba-learning/21-graph-human-in-the-loop) |
| 流式输出 | 节点级 streaming | 官网 |

```java
// 把“翻译 → 加工”建成一张图（简化）
StateGraph graph = new StateGraph(keyStrategyFactory)
    .addNode("translate", node_async(translateNode))
    .addNode("process",   node_async(processNode))
    .addEdge(StateGraph.START, "translate")
    .addEdge("translate", "process")
    .addEdge("process", StateGraph.END);
CompiledGraph app = graph.compile();
app.invoke(Map.of("input", "你好世界"));
```

### 扩展 3️⃣：Agent 框架 —— 现成的智能体

Spring AI 让你“用 Advisor + Tool 自己拼智能体”；Spring AI Alibaba 直接给封装好的 Agent：

- **`ReactAgent`**：内置 ReAct（推理-行动循环）智能体，绑好工具即用 → [22](spring-ai-alibaba-learning/22-agent-react)
- **A2A（Agent-to-Agent）**：多智能体互相调用协作
- **Graph + Agent 组合**：用图编排把多个 Agent 串成工作流

### 扩展 4️⃣：企业中间件生态（阿里云 / 云原生整合）

Spring AI 只定义 `VectorStore`、`ChatMemory` 接口；Spring AI Alibaba 官方成套提供实现：

| 类别 | Spring AI Alibaba 提供的 Starter（举例） |
|---|---|
| **动态配置** | `spring-ai-alibaba-starter-nacos-prompt`（Nacos 动态 Prompt，不重启改提示词）→ [24](spring-ai-alibaba-learning/24-nacos-prompt) |
| **可观测** | `spring-ai-alibaba-starter-arms-observation`（阿里云 ARMS 链路追踪） |
| **向量库** | AnalyticDB、OpenSearch、Tair、OceanBase 等云上向量库连接器 |
| **对话记忆** | `starter-memory-redis` / `-jdbc` / `-elasticsearch` |
| **文档读取器** | GitHub、Notion、飞书、语雀、Bilibili、YouTube、Arxiv、MongoDB、MySQL… 几十种 |
| **社区工具集** | 百度搜索、高德地图、百度翻译、必应搜索等现成 `@Tool` → [23](spring-ai-alibaba-learning/23-tool-calling-community) |
| **MCP + Nacos** | 用 Nacos 做 MCP 服务的注册发现 |

### 扩展 5️⃣：平台级产品（开箱即用的完整应用，非库）

这些是基于上面 Graph 能力构建的**成品**，超出“类库”范畴：

- **JManus**：通用多智能体平台（对标 Manus）
- **DeepResearch**：深度研究智能体
- **NL2SQL / ChatBI**：自然语言查数据库、自动生成 SQL
- **Spring AI Alibaba Studio**：可视化调试 / 编排界面
- **Dify → SAA**：把 Dify 工作流转成 Spring AI Alibaba 代码

---

## 四、同一件事，两者写法对比

**结论先行：对话、RAG、工具、结构化输出这些，两者代码几乎一字不差**，只有“底层模型 + 依赖坐标 + 配置 key”不同。

### 4.1 基础对话（几乎相同）

```java
// Spring AI                                    // Spring AI Alibaba
String a = chatClient.prompt()                  String a = chatClient.prompt()
        .user("你好")                                   .user("你好")
        .call().content();                              .call().content();
// ↑ 完全一样，区别只在底层是 OpenAI 还是通义千问
```

### 4.2 依赖 / 配置（这里才有区别）

| | Spring AI | Spring AI Alibaba |
|---|---|---|
| Starter | `spring-ai-starter-model-openai` | `spring-ai-alibaba-starter-dashscope` |
| 配置前缀 | `spring.ai.openai.*` | `spring.ai.dashscope.*` |
| BOM | `spring-ai-bom` | `spring-ai-alibaba-bom` **+** `spring-ai-bom`（见下方“坑”） |

### 4.3 只有 Spring AI Alibaba 能做的（Graph）

```java
// Spring AI：想做多步/分支/并行的智能体工作流 → 需自己写编排逻辑，无官方图引擎
// Spring AI Alibaba：直接用 StateGraph 声明式建图（见第三节 扩展2）
```

---

## 五、依赖与 BOM 的实践差异（踩坑提示）

这是实际接入时最容易翻车的地方，本仓库已经处理好：

1. **Spring AI 单 BOM 即可**：`spring-ai-bom` 管好所有 `org.springframework.ai:*`。
2. **Spring AI Alibaba 从 1.1.2.x 起 BOM 变“瘦”了**：
   - `spring-ai-alibaba-bom` **只**管 `graph-core` 等少数坐标；
   - **不再收录** `starter-dashscope`、`starter-nacos-prompt`、工具集等坐标；
   - 也**不帮你导入** `spring-ai-bom`。
3. 所以正确姿势是父 pom 里 **同时导入两个 BOM**，并给漏掉的阿里 starter 显式登记版本：

```xml
<dependencyManagement>
  <dependencies>
    <!-- ① Spring AI 官方 BOM（管 ChatClient/VectorStore/RAG/MCP 等） -->
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-bom</artifactId>
      <version>1.1.7</version><type>pom</type><scope>import</scope>
    </dependency>
    <!-- ② Spring AI Alibaba BOM（管 graph-core 等） -->
    <dependency>
      <groupId>com.alibaba.cloud.ai</groupId>
      <artifactId>spring-ai-alibaba-bom</artifactId>
      <version>1.1.2.3</version><type>pom</type><scope>import</scope>
    </dependency>
    <!-- ③ 补齐 slim BOM 漏掉的阿里 starter（dashscope / nacos-prompt / 工具集…） -->
    <dependency>
      <groupId>com.alibaba.cloud.ai</groupId>
      <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
      <version>1.1.2.3</version>
    </dependency>
  </dependencies>
</dependencyManagement>
```

> 版本对应关系：Spring AI Alibaba `1.1.2.3` 的前三位 `1.1.2` 跟随 Spring AI 1.1，末位 `.3` 是阿里自己的迭代号，可与 `spring-ai 1.1.7`、`Spring Boot 3.5.4` 共存。

---

## 六、该怎么选？

| 你的场景 | 建议 |
|---|---|
| 只调用 OpenAI/Anthropic/本地 Ollama，做通用 AI 功能 | **Spring AI** 足够 |
| 主要用**通义千问/百炼**、要国内直连、要文生图/语音一站式 | **Spring AI Alibaba** |
| 需要**多智能体编排 / 复杂工作流 / 人类介入** | **Spring AI Alibaba**（Graph 是杀手锏） |
| 已经在**阿里云**上（Nacos、ARMS、AnalyticDB…） | **Spring AI Alibaba**（生态无缝） |
| 想两头下注 | 放心用 Alibaba——上层 API 就是 Spring AI，**学的东西完全通用**，将来换回纯 Spring AI 成本极低 |

> 记住：**用 Spring AI Alibaba ≠ 放弃 Spring AI。** 你写的 `ChatClient`、`Advisor`、`@Tool`、`VectorStore` 代码，在两个框架里可直接搬。真正“绑定”的只是 Graph 那一层——而那一层恰恰是纯 Spring AI 给不了你的价值。

---

## 七、参考链接

- Spring AI 官方文档：<https://docs.spring.io/spring-ai/reference/>
- Spring AI Alibaba 官网：<https://java2ai.com/>
- Spring AI Alibaba GitHub：<https://github.com/alibaba/spring-ai-alibaba>
- 本仓库对照学习：[`spring-ai-learning`](spring-ai-learning) ↔ [`spring-ai-alibaba-learning`](spring-ai-alibaba-learning)
- 三框架知识点对照：[`Spring-AI-vs-LangChain4j-知识模块对比.md`](Spring-AI-vs-LangChain4j-知识模块对比.md)
