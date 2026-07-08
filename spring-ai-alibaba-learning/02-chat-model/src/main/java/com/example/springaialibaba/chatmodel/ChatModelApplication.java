package com.example.springaialibaba.chatmodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 02：ChatModel 与 ChatClient —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   在模块 01 会调用 ChatClient 的基础上，讲清 Spring AI 对话的两层抽象：
 *     1. ChatModel  —— 底层模型接口，最贴近“一次请求一次响应”的原始能力。
 *     2. ChatClient —— 建立在 ChatModel 之上的“高级客户端”，链式调用、更好用。
 *   并演示三个高频技能：
 *     (a) 非流式：.call().content()          —— 等模型全部想完再一次性拿到完整回答。
 *     (b) 流式  ：.stream().content()         —— 模型边想边吐字，像打字机一样逐段返回。
 *     (c) 运行时覆盖参数：用 DashScopeChatOptions 临时换模型(qwen-max)、调温度。
 *
 * 【需要先懂的概念】
 *   - Flux<String>：来自 Reactor 的“响应式数据流”，可以理解为“会陆续到达的一串字符串”。
 *     流式输出正是用它把模型逐段生成的文字一段段推给你。
 *   - Options（选项）：每次对话可携带的参数，如 model、temperature、maxTokens 等。
 *     DashScopeChatOptions 是 DashScope 专属的选项实现（阿里特有类）。
 *
 * 【怎么做】
 *   @SpringBootApplication 启动容器；容器就绪后自动执行 ChatModelRunner 里的演示。
 * ============================================================================
 */
@SpringBootApplication
public class ChatModelApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot：创建容器 -> 自动配置 ChatModel/ChatClient.Builder -> 执行 Runner
        SpringApplication.run(ChatModelApplication.class, args);
    }
}
