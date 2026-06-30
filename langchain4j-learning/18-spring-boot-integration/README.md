# 18 · Spring Boot 集成（官方 starter + @AiService）

> 本模块目标：与前面 17 个"仅把 Spring Boot 当外壳"的模块不同，这里**真正接入 LangChain4j
> 官方的 Spring Boot starter**，享受自动配置——业务代码里一行模型构建代码都不用写。

## 一、和前面模块的根本区别

| | 模块 01~17 | 本模块 18 |
|---|---|---|
| 模型怎么来 | 手写 `OpenAiChatModel.builder()` + `@Value` 读属性 | starter 读 `langchain4j.open-ai.*` **自动创建 Bean** |
| AiService 怎么来 | 手动 `AiServices.create(接口, model)` | 接口加 `@AiService`，**自动实现 + 自动注册为 Bean** |
| 怎么使用 | new / create 后调用 | 直接 `@Autowired` 注入接口 |
| 风格 | Spring 当运行外壳，纯 LangChain4j 原生 API | "约定优于配置"，融入 Spring 生态 |

## 二、用到的依赖与注解（均经 jar 查证）

| 依赖 / 注解 | 作用 |
|---|---|
| `langchain4j-open-ai-spring-boot-starter` | 读 `langchain4j.open-ai.chat-model.*`，自动建 `OpenAiChatModel` Bean |
| `langchain4j-spring-boot-starter` | 扫描 `@AiService` 接口，自动实现并注册为 Bean |
| `@dev.langchain4j.service.spring.AiService` | 标注在接口上；`wiringMode` 默认 `AUTOMATIC`，自动注入唯一的 ChatModel |

> 版本说明：这两个 starter 属于 LangChain4j 的 **beta 线**（实际版本 `1.17.0-betaXX`），
> 由父 pom 导入的 `langchain4j-bom` 统一管理，因此模块 pom 里**不写 version**。

## 三、属性前缀（务必区分）

| 前缀 | 谁在用 | 谁来读 |
|---|---|---|
| `langchain4j.openai.chat.*` | 本项目共享配置(`config/langchain4j-common.yml`) | 我们自定义，靠 `@Value` 读（01~17 模块） |
| `langchain4j.open-ai.chat-model.*` | 本模块 `application.yml` | **官方 starter 自动识别**（`@ConfigurationProperties(prefix="langchain4j.open-ai")`） |

注意是 `open-ai`（带连字符），字段为 `chat-model.base-url / api-key / model-name / temperature`。

## 四、流程图

```mermaid
flowchart TD
    Y["application.yml<br/>langchain4j.open-ai.chat-model.*"] -->|OpenAI starter AutoConfig| M["自动创建 OpenAiChatModel Bean"]
    I["@AiService interface Assistant"] -->|langchain4j-spring-boot-starter 扫描| B["自动实现 + 注册为 Bean"]
    M -->|AUTOMATIC 自动装配| B
    B -->|构造器注入| R["SpringBootRunner"]
    R -->|assistant.chat(...)| O["AI 回答（零样板模型代码）"]
```

## 五、关键代码

```java
// application.yml
// langchain4j:
//   open-ai:
//     chat-model:
//       base-url: https://api.deepseek.com/v1
//       api-key: ${DEEPSEEK_API_KEY}
//       model-name: deepseek-chat

// 接口：仅一个注解，自动实现 + 自动注册 + 自动注入模型
@AiService
public interface Assistant {
    @SystemMessage("你是一个简洁的中文助手……")
    String chat(String userMessage);
}

// 使用：像普通 Bean 一样注入
@Component
public class SpringBootRunner implements CommandLineRunner {
    private final Assistant assistant;            // 直接注入
    public SpringBootRunner(Assistant assistant) { this.assistant = assistant; }
    public void run(String... args) {
        System.out.println(assistant.chat("..."));
    }
}
```

## 六、运行

```bash
cd 18-spring-boot-integration
export DEEPSEEK_API_KEY=sk-你的密钥
mvn spring-boot:run
```

## 七、全项目小结（学习路线回顾）

恭喜你走到最后一站！整套项目从易到难覆盖了 LangChain4j 的核心能力：

- **基础对话**：01 入门 → 02 底层 ChatModel/消息 → 03 流式输出。
- **高级声明式 API**：04 AiServices → 05 记忆 → 06 参数 → 07 结构化输出。
- **赋予模型能力**：08 工具调用 → 09 向量化与存储 → 10 RAG → 11 分类 → 12 图像。
- **工程化保障**：13 护栏 → 14 可观测性与日志 → 15 测试与评估。
- **进阶与集成**：16 MCP（远程工具生态）→ 17 Agents（多步推理智能体）→ **18 Spring Boot 集成（本模块）**。

一条主线贯穿始终：**ChatModel 是地基，AiServices 是高级 API，工具/记忆/检索/护栏是能力拼装件，
而 Spring Boot starter 让这一切自动配置化、生产可用。** 掌握这些，你已经具备用 LangChain4j
构建真实 AI 应用的完整能力。继续动手改造每个模块的提示词与工具，把它变成你自己的项目吧 🚀
