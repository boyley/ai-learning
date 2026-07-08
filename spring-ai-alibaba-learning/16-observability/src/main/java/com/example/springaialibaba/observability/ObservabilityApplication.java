package com.example.springaialibaba.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 16：可观测性(Observability) —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   带你给“AI 调用”装上“仪表盘”，回答上线后最要命的三类问题：
 *     - 模型到底收到了什么请求、返回了什么？（日志 Logging）
 *     - 调用了多少次、耗时多少、消耗多少 token？（指标 Metrics）
 *     - 一次请求在链路里经过了哪些环节、卡在哪？（链路追踪 Tracing / Span）
 *
 * 【可观测性三大支柱（零基础必读）】
 *   1. Logs   日志：一条条文本记录“发生了什么”。本模块用 SimpleLoggerAdvisor 打印每次请求/响应。
 *   2. Metrics 指标：可聚合的数字（调用次数、耗时分布、token 用量）。由 Micrometer 采集。
 *   3. Traces 链路：把一次请求拆成若干 Span，看清调用链与耗时。Spring AI 会自动产生 observation。
 *
 * 【Spring AI 的“自带可观测性”】
 *   ChatClient/ChatModel/EmbeddingModel 的调用天生接了 Micrometer Observation：
 *   只要类路径上有 ObservationRegistry（本模块通过 spring-boot-starter-actuator 引入），
 *   每次模型调用就会自动产生一个 observation —— 它既能转成 metrics，也能（配了 tracing 后）转成 span。
 *   你无需改业务代码，只要把这些 observation “接出去”到某个后端即可。
 *
 * 【怎么把数据接到线上平台】
 *   - 阿里云 ARMS：加 `spring-ai-alibaba-starter-arms-observation` + ARMS 探针，数据进阿里云控制台。
 *   - Langfuse / Zipkin / Jaeger：加对应的 OpenTelemetry / tracing 桥接依赖即可。
 *   （本模块为“保证零依赖门槛、稳定编译运行”，只用 Actuator + 日志演示，接线方式见 README。）
 * ============================================================================
 */
@SpringBootApplication
public class ObservabilityApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot：自动配置 ChatClient.Builder，
        // 并因引入 Actuator 而具备 ObservationRegistry（AI 调用会自动产生可观测数据）。
        SpringApplication.run(ObservabilityApplication.class, args);
    }
}
