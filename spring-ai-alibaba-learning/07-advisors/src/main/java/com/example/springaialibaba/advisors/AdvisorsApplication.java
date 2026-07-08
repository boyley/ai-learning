package com.example.springaialibaba.advisors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 07：Advisor 顾问/拦截器机制 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   Advisor(顾问) 是 Spring AI 的“拦截器 / 中间件”机制：它像一层层洋葱，
 *   包在“真正调用模型”的外面，能在【请求前】和【响应后】插入自己的逻辑，
 *   例如：打印日志、统计耗时、注入记忆(见模块06)、做 RAG 检索(见模块11)等。
 *
 *   前面模块06 的 MessageChatMemoryAdvisor 就是一个 Advisor。本模块专门讲 Advisor 本身：
 *     1. 内置 SimpleLoggerAdvisor —— 自动按 DEBUG 日志打印每次请求/响应内容。
 *     2. 自定义 CallAdvisor      —— 我们自己写一个，测量并打印每次调用的耗时 + 时间戳。
 *
 * 【洋葱模型（调用顺序）】
 *
 *   请求 ─► [Advisor A 前置] ─► [Advisor B 前置] ─► 真正调用模型
 *                                                        │
 *   响应 ◄─ [Advisor A 后置] ◄─ [Advisor B 后置] ◄───────┘
 *
 *   每个 Advisor 通过 chain.nextCall(request) “放行”给链上的下一环，
 *   在这行代码的前后就能分别插入“前置/后置”逻辑。
 *
 * 【怎么做】
 *   @SpringBootApplication 启动容器；容器就绪后自动执行 AdvisorsRunner 演示。
 * ============================================================================
 */
@SpringBootApplication
public class AdvisorsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdvisorsApplication.class, args);
    }
}
