# Java AI 学习合集 🚀

> 一个仓库，两个**面向零基础**的 Java AI 框架学习项目。同一批知识点，用两种主流框架分别实现，方便对照学习。

| 子项目 | 框架 | 模块数 | 说明 |
|---|---|:---:|---|
| [`spring-ai-learning`](spring-ai-learning) | **Spring AI** 1.1.7 | 20 | Spring 官方 AI 框架，Spring 风格的自动配置 + ChatClient |
| [`langchain4j-learning`](langchain4j-learning) | **LangChain4j** 1.17.0 | 23 | 对标 Python LangChain 的 Java 库，原生 API |

两个项目都基于 **Spring Boot 3.5.4 + Java 17**，每个知识点一个独立可运行的 Maven 模块，全程详细中文注释 + 流程图 + 模块独立 README。

---

## 一、目录结构

```
ai-learning/                     ← 本仓库根（聚合工程）
├── pom.xml                      ← 外层聚合 pom（一条命令构建两个子项目）
├── README.md                    ← 本文件
├── .gitignore
├── spring-ai-learning/          ← 子项目1：Spring AI（含自己的父 pom + config + 20 模块）
│   ├── pom.xml
│   ├── config/spring-ai-common.yml   ← 该项目的统一配置（填 Key 处）
│   └── 01-…  02-…  …  20-…
└── langchain4j-learning/        ← 子项目2：LangChain4j（含自己的父 pom + config + 23 模块）
    ├── pom.xml
    ├── config/langchain4j-common.yml ← 该项目的统一配置（填 Key 处）
    └── 01-…  02-…  …  23-…
```

> 两个子项目各自独立：各有自己的父 pom、统一配置文件、模块编号。外层 pom 只负责“聚合”，不改动它们任何内容。

---

## 二、环境与配置

| 工具 | 版本 |
|---|---|
| JDK | 17+ |
| Maven | 3.6+ |

**API Key（两个子项目各有一处统一配置，填法相同）：**

```bash
# 设置环境变量即可，两个项目都能读到
export DEEPSEEK_API_KEY=sk-你的deepseek密钥   # 对话类模块（便宜、国内直连）
export OPENAI_API_KEY=sk-你的openai密钥       # 向量/图片/语音/审核类模块
```

- 对话能力走 **DeepSeek**（OpenAI 兼容）；向量/图片/语音/审核走 **OpenAI**。
- 详细说明见各子项目自己的 README 与 `config/*.yml`。

---

## 三、怎么构建 / 运行

```bash
# 在本根目录：一次性编译两个子项目的全部模块
mvn -q compile

# 运行某个具体知识点模块（进入该模块目录再跑，确保能找到 ../config）
cd spring-ai-learning/01-overview-quickstart && mvn spring-boot:run
cd langchain4j-learning/01-get-started      && mvn spring-boot:run
```

---

## 四、两个项目的知识点对照

| 知识点 | spring-ai-learning | langchain4j-learning |
|---|---|---|
| 快速上手 | 01 | 01 |
| 对话客户端 / 底层模型 | 02 | 02 |
| 流式输出 | 02 | 03 |
| 提示词 / 模板 | 03 | （并入 04） |
| 结构化输出 | 04 | 07 |
| 多模态 | 05 | （图像 12） |
| 对话记忆 | 06 | 05 |
| Advisor / 高级 API | 07 | 04（AI Services） |
| 工具调用 | 08 | 08 |
| Embedding / 向量 | 09 | 09 |
| 向量库 | 10 | 09 |
| RAG | 11 | 10 |
| 文生图 | 12 | 12 |
| 语音 | 13 | — |
| MCP 客户端 | 14（含19客户端） | 16 |
| MCP 服务端 | — | 20 |
| 模型评估 | 15 | 15 |
| 可观测 / 测试 | 16 | 14 |
| 提示工程 | 17 | — |
| 智能体 | 18 | 17 |
| 内容审核 | 20 | 19 |
| 分类 / 护栏 / Skills / JSON / HTTP | — | 11 / 13 / 21 / 22 / 23 |
| Spring Boot 集成 | （全程即是） | 18 |

> 详细学习路线、每个模块的讲解与流程图，见各子项目根目录的 `README.md`。

---

## 五、建议

零基础建议从任一项目的 `01` 开始，按编号顺序学。想对比两种框架的写法，可以同一知识点左右对照着看（如对话记忆：`spring-ai-learning/06` vs `langchain4j-learning/05`）。
