package com.example.lc4j.chatmodel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 02：底层 ChatModel API —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   深入 LangChain4j 的“底层对话 API”——ChatModel。模块 01 用的是最简化的
 *   model.chat("字符串") 形式；本模块教你用“消息(ChatMessage)”和
 *   “请求/响应对象(ChatRequest/ChatResponse)”来做更精细的控制。
 *
 * 【需要先懂的概念】
 *   1. 消息(ChatMessage)：一次对话由多条消息组成，常见三种角色：
 *        - SystemMessage：系统指令，给 AI 设定角色/风格/约束（最高优先级）。
 *        - UserMessage：用户说的话（你的问题）。
 *        - AiMessage：AI 的回答（模型返回的就是它）。
 *   2. ChatRequest：把“多条消息 + 参数”打包成一次请求。
 *   3. ChatResponse：模型的完整响应，里面能取到 AiMessage、Token 用量、结束原因等。
 *
 * 【怎么做】
 *   构建 SystemMessage + UserMessage -> 组成 ChatRequest -> model.chat(request)
 *   -> 从 ChatResponse 取回答文本与 Token 用量。
 *
 * 【达到的目的】
 *   理解 LangChain4j 对话的“底层骨架”，为后续记忆、工具、RAG 等打基础。
 * ============================================================================
 */
@SpringBootApplication
public class ChatModelApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatModelApplication.class, args);
    }
}
