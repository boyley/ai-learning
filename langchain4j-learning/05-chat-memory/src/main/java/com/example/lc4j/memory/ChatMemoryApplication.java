package com.example.lc4j.memory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 05：Chat Memory 对话记忆 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   让 AI 助手“记住”之前说过的话。默认情况下，每次调用大模型都是【无状态】的——
 *   模型不会记得你上一句问了什么。要实现连续多轮对话（第二轮能引用第一轮的信息），
 *   就必须由我们自己把历史消息保存下来，并在下一次请求时一起带给模型。
 *   LangChain4j 用 ChatMemory 抽象帮我们自动做这件事。
 *
 * 【为什么需要记忆】
 *   - 大模型 API 本身不存对话历史，每次请求都是“从零开始”。
 *   - 如果不传历史，第二轮问“它多大了？”模型根本不知道“它”指谁。
 *   - ChatMemory 会把每一轮的 UserMessage / AiMessage 累积起来，下一轮自动注入。
 *
 * 【本模块用到的核心 API】
 *   - MessageWindowChatMemory.withMaxMessages(n)：保留最近 n 条消息的“滑动窗口”记忆。
 *     （窗口满了会丢弃最早的消息，避免上下文无限增长、Token 爆炸。）
 *   - AiServices.builder(接口).chatModel(m).chatMemory(memory).build()：给助手装上记忆。
 *   - @MemoryId + chatMemoryProvider：为不同用户分配【各自独立】的记忆，互不串台。
 *
 * 【达到的目的】
 *   学会给 AI Service 装上记忆，实现真正的多轮连续对话，并理解多用户隔离。
 * ============================================================================
 */
@SpringBootApplication
public class ChatMemoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatMemoryApplication.class, args);
    }
}
