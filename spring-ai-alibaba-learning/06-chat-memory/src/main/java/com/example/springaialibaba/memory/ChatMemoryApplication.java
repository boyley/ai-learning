package com.example.springaialibaba.memory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 06：对话记忆 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   大模型本身是“无记忆”的：每次调用都是独立的，它不会自动记得你上一句说了什么。
 *   本模块演示如何给它加“记忆”，让多轮对话能够联系上下文：
 *     第一轮：告诉 AI “我叫小明”
 *     第二轮：问 “我叫什么名字？” —— AI 能答出“小明”，就证明它记住了。
 *
 * 【记忆是怎么实现的】
 *   - ChatMemory：一块“存历史消息的容器”。这里用 MessageWindowChatMemory(滑动窗口)，
 *     只保留最近 N 条消息（超出就丢最旧的），避免历史无限膨胀。
 *   - MessageChatMemoryAdvisor：一个“顾问(拦截器)”，在每次请求前自动把历史消息塞进去，
 *     请求后又把新的问答存回记忆。挂到 ChatClient 上即可自动生效。
 *   - CONVERSATION_ID：会话 ID。不同用户/会话用不同 ID 隔离各自的记忆，互不串台。
 *
 * 【怎么做】
 *   @SpringBootApplication 启动容器；容器就绪后自动执行 ChatMemoryRunner 演示。
 * ============================================================================
 */
@SpringBootApplication
public class ChatMemoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatMemoryApplication.class, args);
    }
}
