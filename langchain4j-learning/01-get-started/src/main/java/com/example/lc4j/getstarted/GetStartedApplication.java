package com.example.lc4j.getstarted;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 01：LangChain4j 概念与快速上手 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   带你认识 LangChain4j 的最核心概念，并完成"人生第一个" LangChain4j 调用：
 *   向大模型提一个问题，拿到它的回答并打印出来。
 *
 * 【LangChain4j 是什么】
 *   LangChain4j 是一个面向 Java 的开源库，用统一的 API 封装了各家大模型
 *   （OpenAI、DeepSeek、Ollama、Anthropic 等）和向量库，让你在 JVM 上轻松构建
 *   LLM 应用（对话、RAG、工具调用、智能体等）。它对标 Python 的 LangChain。
 *
 * 【需要先懂的几个概念（零基础必读）】
 *   1. 大模型(LLM)：像 DeepSeek、GPT 这样的 AI，输入文字、输出文字。
 *   2. ChatModel：LangChain4j 对"对话大模型"的统一抽象（底层 API），核心方法是 chat(...)。
 *   3. OpenAiChatModel：ChatModel 的一个实现，对接 OpenAI 兼容接口（DeepSeek 也兼容）。
 *   4. 构建器(Builder)：LangChain4j 用 XxxModel.builder()....build() 的方式手动构建模型对象，
 *      参数（apiKey、baseUrl、modelName 等）都在构建时显式传入，一目了然。
 *
 * 【本项目的运行外壳说明】
 *   我们用 Spring Boot 当“运行外壳”：@SpringBootApplication 启动容器后会自动执行
 *   实现了 CommandLineRunner 的 Bean（见 GetStartedRunner），从而触发演示代码。
 *   而真正的 AI 调用全部用 LangChain4j 原生 API 完成（不依赖 Spring 的 AI 自动配置）。
 *
 * 【达到的目的】
 *   跑起来后，控制台会打印出大模型对一个问题的真实回答，证明你的环境与 Key 配置成功。
 * ============================================================================
 */
@SpringBootApplication
public class GetStartedApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用：创建容器 -> 注入共享配置 -> 执行 CommandLineRunner（演示在 GetStartedRunner）
        SpringApplication.run(GetStartedApplication.class, args);
    }
}
