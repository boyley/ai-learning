package com.example.springaialibaba.observability;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 可观测性演示：用 SimpleLoggerAdvisor 观察请求/响应，并理解自动 Metrics/Trace
 * ============================================================================
 *
 * 【核心流程图】
 *
 *   ChatClient.prompt().user("...")
 *        │
 *        ▼
 *   ┌──────────────────────────────────────────────┐
 *   │  Advisor 链（拦截器）                          │
 *   │   SimpleLoggerAdvisor：调用前打印请求，         │  ← 本模块重点：日志
 *   │                        调用后打印响应           │
 *   └──────────────────────────────────────────────┘
 *        │
 *        ▼  调用 DashScopeChatModel（此处自动产生一个 Micrometer Observation）
 *   通义千问(DashScope)
 *        │
 *        ▼
 *   Observation → 可导出为：Metrics(次数/耗时/token) + Trace(Span)
 *                （接了 ARMS / Langfuse / Zipkin 等后端时自动上报）
 *
 * 【关键 API 链路】
 *   new SimpleLoggerAdvisor()          -> Spring AI 内置的“日志顾问”，记录每次请求与响应。
 *   builder.defaultAdvisors(advisor)   -> 把它挂到 ChatClient，之后每次调用都会被记录。
 *   （日志级别需调到 DEBUG 才能看到内容，见 application.yml。）
 *
 * 【关于 Metrics / Trace（无需写代码）】
 *   因为 pom 引入了 spring-boot-starter-actuator（含 Micrometer ObservationRegistry），
 *   上面这次模型调用会自动产生一个名为 "gen_ai.client.operation" 之类的 observation。
 *   - 想看指标：启动为 Web 应用后访问 /actuator/metrics 即可（本命令行模块默认不起 Web）。
 *   - 想上报到线上平台：加一个 tracing/otel 桥接依赖即可，业务代码零改动（见 README）。
 * ============================================================================
 */
@Component
public class ObservabilityRunner implements CommandLineRunner {

    /** 挂了 SimpleLoggerAdvisor 的 ChatClient —— 每次调用都会被记录下来 */
    private final ChatClient chatClient;

    public ObservabilityRunner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                // ★ 把内置的日志顾问设为默认 Advisor：之后每次 prompt() 调用都会自动记录请求/响应 ★
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块16：可观测性(Observability) ==========\n");

        System.out.println("【说明】本次对话已挂 SimpleLoggerAdvisor。");
        System.out.println("        若把日志级别调到 DEBUG（见 application.yml），控制台会额外打印出");
        System.out.println("        它记录的“完整请求”和“完整响应”，这就是最基础的一支柱：日志。\n");

        String question = "请用一句话解释：为什么线上的大模型应用需要“可观测性”？";
        System.out.println("【我问】" + question);

        // 这次调用会：① 经过 SimpleLoggerAdvisor（打印请求/响应日志）
        //            ② 触发一个 Micrometer Observation（可转 metrics / trace）
        String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();

        System.out.println("\n【AI 答】" + answer);

        System.out.println("\n【可观测性三支柱回顾】");
        System.out.println("  1. 日志 Logs   ：SimpleLoggerAdvisor 打印每次请求/响应（本模块已演示）。");
        System.out.println("  2. 指标 Metrics：Actuator + Micrometer 自动采集调用次数/耗时/token。");
        System.out.println("  3. 链路 Traces ：Spring AI 自动产生 observation，接后端后即成 Span。");
        System.out.println("  → 想接阿里云 ARMS / Langfuse？只需加一个桥接依赖，业务代码不用改（见 README）。");

        System.out.println("\n========== 演示结束：给 AI 调用装上“仪表盘” ==========\n");
    }
}
