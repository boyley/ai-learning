# Spring AI Alibaba 零基础学习项目 🐫

> 阿里巴巴开源的 **Agentic AI 框架**，构建在 Spring AI 之上。同一批对话 API 与 Spring AI 完全一致，
> 额外提供 **DashScope(通义千问/百炼) 原生接入** 与 **Graph 多智能体编排** 等阿里特色能力。
>
> 24 个知识点，每个是一个独立可运行的 Maven 模块，全程详细中文注释 + 流程图 + 独立 README。

---

## 一、它和 Spring AI 是什么关系？

```
        ┌─────────────────────────────────────────────┐
        │        你的业务代码（ChatClient / @Tool）      │   ← 写法与 Spring AI 一模一样
        ├─────────────────────────────────────────────┤
        │   Spring AI Alibaba 特色扩展                   │
        │   · DashScope 原生模型（qwen / wanx / cosyvoice）│
        │   · Graph 多智能体编排（StateGraph / ReactAgent）│
        │   · Nacos 动态 Prompt / NL2SQL / 社区工具集      │
        ├─────────────────────────────────────────────┤
        │   Spring AI（ChatModel/Advisor/Tool/RAG 抽象）  │   ← 底座
        ├─────────────────────────────────────────────┤
        │            Spring Boot 3.5 + Java 17           │
        └─────────────────────────────────────────────┘
```

一句话：**Spring AI 定规范，Spring AI Alibaba 接阿里模型 + 加多智能体能力**。
所以学过隔壁 `spring-ai-learning` 的话，01~18 这些模块会非常眼熟，重点看 **19~24 的阿里特色**。

---

## 二、环境与配置

| 工具 | 版本 |
|---|---|
| JDK | 17+ |
| Maven | 3.6+ |
| Spring AI Alibaba | 1.1.2.3（对齐 Spring AI 1.1 / Spring Boot 3.5.4） |

**只需一个百炼 Key（对话/向量/文生图/语音/多模态全打通）：**

```bash
export AI_DASHSCOPE_API_KEY=sk-你的百炼密钥
```

- 申请地址：<https://bailian.console.aliyun.com/> → “API-KEY 管理”创建 `sk-` 开头的 Key。
- 所有模块共享一处配置：[`config/spring-ai-alibaba-common.yml`](config/spring-ai-alibaba-common.yml)（Key 只填这一次）。

---

## 三、怎么运行

```bash
# 一次性编译全部 24 个模块（在本子项目根目录）
mvn -q compile

# 运行某个知识点模块（进入该模块目录再跑，确保能找到 ../config）
cd 01-quickstart && mvn spring-boot:run
```

---

## 四、学习路线（24 个模块）

### 🟢 第一阶段 · 对话核心（Spring AI 通用能力，用 DashScope 实现）

| # | 模块 | 学到什么 |
|---|---|---|
| 01 | [quickstart](01-quickstart) | 核心概念 + 第一个通义千问调用 |
| 02 | [chat-model](02-chat-model) | ChatModel 底层 / 流式输出 / `DashScopeChatOptions` 运行时参数 |
| 03 | [prompt](03-prompt) | Prompt 模板、System/User 角色 |
| 04 | [structured-output](04-structured-output) | 结构化输出：回答直接转 Java 对象 / List / Map |
| 05 | [multimodality](05-multimodality) | 多模态：图片 + 文字一起输入（qwen-vl） |
| 06 | [chat-memory](06-chat-memory) | 对话记忆：让 AI 记住上下文 |
| 07 | [advisors](07-advisors) | Advisor 顾问/拦截器机制 |
| 08 | [tool-calling](08-tool-calling) | 工具调用 / 函数调用（`@Tool`） |

### 🟡 第二阶段 · 向量 / RAG / 多模态

| # | 模块 | 学到什么 |
|---|---|---|
| 09 | [embedding](09-embedding) | 文本向量化（`text-embedding-v3`） |
| 10 | [vector-store](10-vector-store) | 向量库（内存版 SimpleVectorStore）与相似度检索 |
| 11 | [rag](11-rag) | 检索增强生成 RAG：让 AI 基于你的资料回答 |
| 12 | [document-etl](12-document-etl) | 文档 ETL 管道：读取 → 切分 → 写入向量库 |
| 13 | [image-model](13-image-model) | 文生图（通义万相 wanx） |
| 14 | [audio-model](14-audio-model) | 语音：合成(TTS) + 识别(ASR) |

### 🟠 第三阶段 · 工程化

| # | 模块 | 学到什么 |
|---|---|---|
| 15 | [mcp](15-mcp) | 模型上下文协议 MCP 客户端：让 AI 用外部工具服务 |
| 16 | [observability](16-observability) | 可观测性：链路追踪 / 指标 / ARMS |
| 17 | [evaluation](17-evaluation) | 模型评估：相关性 / 事实性打分 |
| 18 | [prompt-engineering](18-prompt-engineering) | 提示工程模式（角色 / 少样本 / 思维链） |

### 🔴 第四阶段 · Graph 多智能体编排（★ 阿里旗舰能力）

| # | 模块 | 学到什么 |
|---|---|---|
| 19 | [graph-basic](19-graph-basic) | `StateGraph` 工作流：节点 + 边 + 全局状态 |
| 20 | [graph-parallel](20-graph-parallel) | 并行节点：多分支并发执行再汇聚 |
| 21 | [graph-human-in-the-loop](21-graph-human-in-the-loop) | 人类介入：中断 → 等人审批 → 恢复 |
| 22 | [agent-react](22-agent-react) | `ReactAgent`：推理-行动(ReAct)循环智能体 |

### 🟣 第五阶段 · 阿里特色扩展

| # | 模块 | 学到什么 |
|---|---|---|
| 23 | [tool-calling-community](23-tool-calling-community) | 社区工具集：一行接入百度搜索等现成工具 |
| 24 | [nacos-prompt](24-nacos-prompt) | Nacos 动态 Prompt：不重启即可改提示词 |

> 📌 **更大的平台产品**（不在本入门项目内，了解即可）：
> **JManus**（通用多智能体平台）、**DeepResearch**（深度研究智能体）、**NL2SQL/ChatBI**（自然语言查数据库）。
> 它们都基于上面的 Graph 能力构建，学完 19~22 后可去官网深入：<https://java2ai.com/>。

---

## 五、建议

零基础从 `01` 按编号顺序学。若已学过 `spring-ai-learning`，可快速过一遍 01~18（写法一致，只是模型换成通义千问），把精力放在 **19~24 的 Graph 多智能体与阿里特色**上——这才是 Spring AI Alibaba 区别于纯 Spring AI 的价值所在。
