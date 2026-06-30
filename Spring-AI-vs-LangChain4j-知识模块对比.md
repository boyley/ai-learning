# Spring AI vs LangChain4j 知识模块对比

> 本文把 `spring-ai-learning`(20 模块) 与 `langchain4j-learning`(23 模块) 按**知识点**逐一对照，
> 用表格列出每个知识点在两个框架里的**核心 API、使用步骤、关键差异**，方便对比学习、按需选型。
>
> 两个项目均为 Spring Boot 3.5.4 + Java 17；对话走 DeepSeek(OpenAI 兼容)，向量/图片/审核走真正的 OpenAI。

---

## 〇、设计哲学差异（先看这张，最重要）

| 维度 | Spring AI | LangChain4j |
|---|---|---|
| **核心入口** | `ChatClient`（链式：`prompt().user().call().content()`） | 两层：底层 `ChatModel.chat()` + 高层 `AiServices`（声明式接口） |
| **模型怎么来** | **Spring 自动配置**：引 starter + 配 `application.yml`，自动注入 `ChatClient.Builder` | **手动 builder**：`OpenAiChatModel.builder()....build()`，参数显式传入 |
| **配置方式** | `application.yml` 里 `spring.ai.openai.*`，框架自动读取 | 代码里 builder 传参（本项目用 `@Value` 从 yml 读出来再传） |
| **扩展机制** | **Advisor**（拦截器链：记忆/RAG/日志都是 Advisor） | **AiServices 装配项**（`.chatMemory()/.tools()/.contentRetriever()/.toolProvider()`） |
| **风格归属** | 深度绑定 Spring 生态，"Spring 味"浓 | 框架无关的纯 Java 库，可独立用；也提供 Spring Boot starter |
| **声明式接口** | 无（统一用 ChatClient） | ★ `@AiService` 接口是其招牌特性 |

一句话：**Spring AI = "Spring 自动配置 + ChatClient 链式 + Advisor 拦截器"；LangChain4j = "手动 builder + 声明式 AiServices 接口 + 装配项"。**

---

## 一、知识点总览对照表

> "—" 表示该框架本项目未单独建模块（不代表框架不支持）。

| 知识点 | spring-ai-learning | langchain4j-learning |
|---|---|---|
| 快速上手 / 第一次调用 | `01-overview-quickstart` | `01-get-started` |
| 底层对话 API / 消息类型 | （并入 02） | `02-chat-and-language-models` |
| 对话客户端 / 链式调用 | `02-chat-client` | （即 01/02） |
| 流式输出 | `02-chat-client`(stream) | `03-response-streaming` |
| 声明式高级 API | `07-advisors`(机制) | ★ `04-ai-services` |
| 提示词 / 模板 | `03-prompt` | （并入 04 注解） |
| 结构化输出 | `04-structured-output` | `07-structured-outputs` |
| 多模态（图+文输入） | `05-multimodality` | （图像理解见 12） |
| 对话记忆 | `06-chat-memory` | `05-chat-memory` |
| 模型参数（温度等） | （并入 02 options） | `06-model-parameters` |
| 工具 / 函数调用 | `08-tool-calling` | `08-tools` |
| 文本向量化 Embedding | `09-embedding` | `09-embeddings-and-stores` |
| 向量库 / 语义检索 | `10-vector-store` | `09-embeddings-and-stores` |
| RAG 检索增强 | `11-rag-etl` | `10-rag` |
| 文本分类 | — | `11-classification` |
| 文生图 / 图像模型 | `12-image-model` | `12-image-models` |
| 语音（转写/合成） | `13-audio-model` | — |
| 护栏 Guardrails | — | `13-guardrails` |
| 可观测性 | `16-observability-testing` | `14-observability-logging` |
| 日志 | （并入 16） | `14-observability-logging` |
| 模型评估 / 测试 | `15-model-evaluation` | `15-testing-and-evaluation` |
| 提示工程模式 | `17-prompt-engineering` | — |
| 智能体 Agents | `18-agents` | `17-agents` |
| MCP 服务端 | `14-mcp` | `20-mcp-server` |
| MCP 客户端 | `19-mcp-client` | `16-mcp` |
| 内容审核 Moderation | `20-moderation` | `19-moderation` |
| Skills 技能 | — | `21-skills` |
| JSON 编解码定制 | — | `22-json-codec` |
| 底层 HTTP 客户端定制 | — | `23-http-client` |
| Spring Boot 集成 | （全程即是） | `18-spring-boot-integration` |

---

## 二、各知识点「使用步骤 + 核心 API」逐项对比

### 1. 快速上手

| | Spring AI | LangChain4j |
|---|---|---|
| 步骤 | ① 引 `spring-ai-starter-model-openai` ② 配 yml ③ 注入 `ChatClient.Builder` ④ `.build()` ⑤ 链式调用 | ① 引 `langchain4j-open-ai` ② `OpenAiChatModel.builder()` 传 baseUrl/apiKey/modelName ③ `.build()` ④ `model.chat()` |
| 核心代码 | `chatClient.prompt().user(q).call().content()` | `model.chat(q)` → String |
| 差异 | 模型由 Spring 自动配置注入，代码看不到 builder | 模型由你手动 builder 构建，参数一目了然 |

### 2. 对话 / 流式输出

| | Spring AI | LangChain4j |
|---|---|---|
| 非流式 | `.call().content()` → String | `model.chat(req)` → `ChatResponse.aiMessage().text()` |
| 流式 | `.stream().content()` → `Flux<String>`（响应式） | `StreamingChatModel.chat(q, handler)`，回调 `onPartialResponse/onCompleteResponse/onError` |
| 差异 | 流式基于 Reactor `Flux`，天然适配 WebFlux/SSE | 流式基于回调接口，需 `CountDownLatch` 等异步完成 |

### 3. 消息类型 / 角色

| | Spring AI | LangChain4j |
|---|---|---|
| 系统/用户消息 | `.system("...")` / `.user("...")`（链式方法） | `SystemMessage.from()` / `UserMessage.from()`（对象） |
| 请求/响应对象 | `Prompt` / `ChatResponse`（一般被 ChatClient 隐藏） | `ChatRequest.builder().messages(...)` / `ChatResponse` |
| 差异 | 倾向链式隐藏底层对象 | 显式构造消息对象，更"看得见骨架" |

### 4. 声明式高级 API（LangChain4j 招牌）

| | Spring AI | LangChain4j |
|---|---|---|
| 写法 | 无对应；统一用 ChatClient | ★ 声明接口 + `@SystemMessage/@UserMessage/@V` |
| 装配 | — | `AiServices.create(接口.class, model)` 或 `AiServices.builder(接口).chatModel().build()` |
| 差异 | "一个 ChatClient 打天下" | "一个业务接口 = 一个 AI 功能"，记忆/工具/RAG 都往这个接口上挂 |

### 5. 结构化输出（把回答转成 Java 对象）

| | Spring AI | LangChain4j |
|---|---|---|
| 步骤 | 调用末尾 `.entity(目标类型)`；泛型用 `ParameterizedTypeReference` | AiServices 接口方法**直接声明返回 POJO/enum**；字段加 `@Description` |
| 底层 | `BeanOutputConverter` 生成 JSON Schema 追加到提示词 | 框架据返回类型生成 JSON Schema/格式说明 |
| 核心代码 | `.user(q).call().entity(Movie.class)` | `Person extract(String text);`（返回类型即结果类型） |
| 差异 | 在"调用链末尾"指定类型 | 在"接口方法签名"上指定类型，更声明式 |

### 6. 对话记忆

| | Spring AI | LangChain4j |
|---|---|---|
| 记忆仓库 | `MessageWindowChatMemory.builder().build()` | `MessageWindowChatMemory.withMaxMessages(n)` |
| 挂载方式 | 通过 **Advisor**：`defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())` | 通过 **装配项**：`AiServices.builder().chatMemory(memory)` |
| 多会话隔离 | `advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "user-1"))` | `@MemoryId` 参数 + `chatMemoryProvider` |
| 差异 | 记忆是一种 Advisor（拦截器） | 记忆是 AiServices 的一个装配项 |

### 7. 工具 / 函数调用

| | Spring AI | LangChain4j |
|---|---|---|
| 声明工具 | 方法加 `@Tool(description=...)` + 参数 `@ToolParam` | 方法加 `@Tool("描述")` + 参数 `@P("描述")` |
| 注册工具 | `.tools(new XxxTools())`（本次调用）或 `MethodToolCallbackProvider` | `AiServices.builder().tools(xxxToolsObject)` |
| 差异 | 工具注册在 ChatClient 调用链上 | 工具注册在 AiServices 装配上；注解包不同 |

### 8. Embedding + 向量库 + 语义检索

| | Spring AI | LangChain4j |
|---|---|---|
| 向量模型 | 注入 `EmbeddingModel`，`.embed(...)` | `OpenAiEmbeddingModel.builder()...`，`.embed(seg).content()` |
| 向量库 | `SimpleVectorStore.builder(embeddingModel).build()` | `new InMemoryEmbeddingStore<TextSegment>()` |
| 入库 | `vectorStore.add(List<Document>)`（库内部自动向量化） | 先 `embed` 得向量，再 `store.add(向量, segment)`（手动两步） |
| 检索 | `similaritySearch(SearchRequest.builder().query(q).topK(n).build())` | `store.search(EmbeddingSearchRequest.builder().queryEmbedding(qVec).maxResults(n).minScore(s).build())` |
| 差异 | VectorStore 把"向量化"封装进 add/search，更省事 | 向量化与存储分离，步骤更显式、更可控 |

### 9. RAG 检索增强生成

| | Spring AI | LangChain4j |
|---|---|---|
| ETL（读/切/存） | `TextReader` → `TokenTextSplitter` → `vectorStore.add` | `Document/TextSegment` → `embed` → `store.add` |
| 检索+回答装配 | **Advisor**：`QuestionAnswerAdvisor`（挂到 ChatClient，回答前自动检索） | **装配项**：`EmbeddingStoreContentRetriever` + `AiServices.builder().contentRetriever(...)` |
| 谁回答 | 都是**对话模型**回答；向量模型只负责检索 | 同左（详见 `10-rag/向量模型与对话模型如何协作.md`） |
| 差异 | RAG = 一个 Advisor，挂上即用 | RAG = 一个 ContentRetriever，装到接口上 |

### 10. 文生图 / 图像

| | Spring AI | LangChain4j |
|---|---|---|
| 调用 | 注入 `ImageModel`，`.call(ImagePrompt)` | `OpenAiImageModel.builder()...generate(prompt)` → `Response<Image>` |
| 取结果 | 从响应取图片 URL/Base64 | `response.content().url()`（返回 `java.net.URI`） |

### 11. 模型评估 / 测试

| | Spring AI | LangChain4j |
|---|---|---|
| 方式 | 内置评估器 `RelevancyEvaluator` / `FactCheckingEvaluator` | 自己写 "LLM 当裁判" 的 AiService（`@UserMessage` 给评分标准，返回 boolean/分数） |
| 调用 | `evaluator.evaluate(new EvaluationRequest(问题, 上下文, 回答))` | `judge.score(问题, 回答)` |
| 差异 | 提供开箱即用的评估器类 | 用 AiServices 自定义裁判，更灵活但需自己写 |

### 12. 可观测性 / 日志

| | Spring AI | LangChain4j |
|---|---|---|
| 可观测 | Micrometer + Spring Boot Actuator（指标/链路自动埋点） | 实现 `ChatModelListener`（`onRequest/onResponse/onError`）+ `builder.listeners(...)` |
| 日志 | 走 Spring/Boot 日志体系 | `builder.logRequests(true).logResponses(true)`（SLF4J DEBUG） |
| 差异 | 复用 Spring 监控生态，企业级 | 框架自带轻量监听器接口 |

### 13. 智能体 Agents

| | Spring AI | LangChain4j |
|---|---|---|
| 思路 | 工作流模式（链式/路由/并行/编排/评估优化），用 ChatClient 编排 | `AiServices` + `@Tool` + `ChatMemory` 组合出会多步推理调工具的智能体 |
| 差异 | 偏"用基础件手工编排工作流" | 偏"声明式接口 + 工具/记忆自动驱动" |

### 14. MCP（模型上下文协议）

| | Spring AI | LangChain4j |
|---|---|---|
| 服务端 | `14-mcp`：`spring-ai-starter-mcp-server-webmvc` + `ToolCallbackProvider`（SSE /sse） | `20-mcp-server`：用 LangChain4j 暴露工具 |
| 客户端 | `19-mcp-client`：`spring-ai-starter-mcp-client` + 注入 `ToolCallbackProvider` → `.defaultToolCallbacks(p)` | `16-mcp`：`DefaultMcpClient` + `StreamableHttpMcpTransport` + `McpToolProvider` → `AiServices.builder().toolProvider(p)` |
| 差异 | 客户端工具以 `ToolCallbackProvider` 注入 ChatClient | 客户端工具以 `McpToolProvider` 装到 AiServices |

### 15. 内容审核 Moderation

| | Spring AI | LangChain4j |
|---|---|---|
| 模块 | `20-moderation` | `19-moderation` |
| 调用 | `OpenAiModerationModel` + `ModerationPrompt`，取 flagged/类别/分数 | LangChain4j 的 `ModerationModel`，判定是否违规 |
| 共同点 | **只有 OpenAI 支持**，需 `OPENAI_API_KEY`（DeepSeek 不支持） | 同左 |

### 16. 仅一方覆盖的知识点

| 知识点 | 仅在 | 说明 |
|---|---|---|
| 提示词模板 / 提示工程模式 | Spring AI (`03`,`17`) | LangChain4j 用 `@UserMessage` 模板 + `@V` 变量替代 |
| 多模态输入 / 语音 | Spring AI (`05`,`13`) | 图+文输入、语音转写/合成 |
| 文本分类 | LangChain4j (`11`) | AiServices 方法返回 enum 即分类 |
| 护栏 Guardrails | LangChain4j (`13`) | `InputGuardrail/OutputGuardrail` + 注解，输入/输出校验 |
| Skills 技能 | LangChain4j (`21`) | `Skills.from()` + `toolProvider` |
| JSON 编解码定制 | LangChain4j (`22`) | `Json.toJson/fromJson`、消息序列化 |
| 底层 HTTP 客户端定制 | LangChain4j (`23`) | `JdkHttpClientBuilder` + `builder.httpClientBuilder()` |
| Spring Boot 深度集成 | LangChain4j (`18`) | `@AiService` Bean 自动装配；Spring AI 全程即是 Spring |

---

## 三、选型与学习建议

- **已经在用 Spring 全家桶 / 要企业级监控与自动配置** → 倾向 **Spring AI**（Advisor 机制 + Actuator 可观测性 + 与 Spring 无缝）。
- **想要框架无关、声明式接口最简洁、或非 Spring 项目** → 倾向 **LangChain4j**（`@AiService` 接口极简，builder 透明可控）。
- **学习路径**：两边都从 `01` 开始按编号学；想对比同一知识点，左右对照看，例如：
  - 对话记忆：`spring-ai-learning/06` ↔ `langchain4j-learning/05`
  - 工具调用：`spring-ai-learning/08` ↔ `langchain4j-learning/08`
  - RAG：`spring-ai-learning/11` ↔ `langchain4j-learning/10`
  - MCP：服务端 `spring 14`↔`lc4j 20`，客户端 `spring 19`↔`lc4j 16`

> 每个模块都有独立 README + 流程图；更细的讲解见各模块目录。
