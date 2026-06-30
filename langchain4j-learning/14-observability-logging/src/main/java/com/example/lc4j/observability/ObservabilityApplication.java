package com.example.lc4j.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 14：可观测性与日志（Observability & Logging）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   生产环境里，你必须能“看见”应用和大模型之间到底发生了什么：
 *   发了什么请求、模型回了什么、耗时多久、烧了多少 Token、有没有报错。
 *   这就是“可观测性（Observability）”。本模块演示 LangChain4j 提供的两种手段：
 *
 *   (1) 内置日志开关 ——
 *       OpenAiChatModel.builder().logRequests(true).logResponses(true)
 *       打开后，框架会把每一次 HTTP 请求体、响应体打到日志里（DEBUG 级别），
 *       适合开发期调试“提示词到底拼成了什么样、模型原始返回是什么”。
 *
 *   (2) 模型监听器 ChatModelListener ——
 *       实现 dev.langchain4j.model.chat.listener.ChatModelListener 接口，
 *       重写 onRequest / onResponse / onError 三个回调，
 *       再通过 builder.listeners(List.of(listener)) 注册到模型上。
 *       每次调用模型时框架会自动触发这些回调，你可在其中统计【耗时】【Token 用量】、
 *       上报监控系统（Prometheus / Micrometer / SkyWalking 等）。
 *
 * 【日志 vs 监听器 怎么选】
 *   - 日志：零代码，开个开关就有，但内容是“给人看的文本”，不便于做统计/告警。
 *   - 监听器：拿到的是结构化对象（ChatRequest / ChatResponse / Throwable），
 *     可精确计算耗时、累加 Token、做埋点，是生产级监控的正确姿势。
 *
 * 【达到的目的】
 *   学会给 LangChain4j 应用装上“仪表盘”，让每一次 LLM 调用都可观测、可追踪。
 *
 * 【注意】
 *   真正触发回调需要联网调用模型（需有效 Key）。本模块只要求 mvn compile 通过；
 *   若要看实际效果，配好 Key 后用 spring-boot:run 运行即可。
 * ============================================================================
 */
@SpringBootApplication
public class ObservabilityApplication {
    public static void main(String[] args) {
        SpringApplication.run(ObservabilityApplication.class, args);
    }
}
