package com.example.springaialibaba.quickstart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 01：Spring AI Alibaba 概念与快速上手 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   带你认识 Spring AI Alibaba 的最核心概念，并完成“人生第一个”调用：
 *   向通义千问大模型提一个问题，拿到它的回答并打印出来。
 *
 * 【需要先懂的几个概念（零基础必读）】
 *   1. 大模型(LLM)：像通义千问(Qwen)、GPT 这样的 AI，输入文字、输出文字。
 *   2. DashScope(百炼)：阿里云的大模型服务平台，通义千问就跑在上面。
 *   3. Spring AI：Spring 官方的 AI 框架，定义了 ChatModel / ChatClient 等通用抽象。
 *   4. Spring AI Alibaba：构建在 Spring AI 之上的阿里开源框架，
 *      负责把 DashScope 原生接入 Spring，并额外提供 Graph 多智能体等能力。
 *   5. ChatClient：与大模型对话的“高级客户端”，链式调用，最常用（本项目主角）。
 *   6. Starter + 自动配置：我们只在 pom 里引入 spring-ai-alibaba-starter-dashscope，
 *      Spring Boot 就会自动帮我们创建好 ChatClient.Builder，拿来即用。
 *
 * 【怎么做】
 *   - @SpringBootApplication 标记这是一个 Spring Boot 应用的入口。
 *   - main 方法启动 Spring 容器，容器启动后会自动执行实现了 CommandLineRunner
 *     的 Bean（见 QuickStartRunner），从而触发我们的演示代码。
 *
 * 【达到的目的】
 *   跑起来后，控制台会打印出通义千问对一个问题的真实回答，证明你的环境与 Key 配置成功。
 * ============================================================================
 */
@SpringBootApplication
public class QuickStartApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用。这一行会：
        //   1. 创建 Spring 容器（IoC 容器）
        //   2. 触发自动配置（根据 starter + 配置文件，自动创建 ChatClient.Builder 等 Bean）
        //   3. 容器就绪后，执行所有 CommandLineRunner（我们的演示在 QuickStartRunner 里）
        SpringApplication.run(QuickStartApplication.class, args);
    }
}
