package com.example.lc4j.memory;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

/**
 * ============================================================================
 * AI Service 接口：带“记忆”的助手
 * ============================================================================
 *
 * 这里声明了两个方法，演示两种记忆用法：
 *   1) chat(String)            —— 配合单个 ChatMemory，所有调用共享同一份对话历史。
 *   2) chat(memoryId, message) —— 配合 ChatMemoryProvider，每个 memoryId 拥有独立历史。
 *
 * 关键点：接口本身不写实现，记忆的保存/注入全部由 LangChain4j 在代理里自动完成。
 * ============================================================================
 */
public interface Assistant {

    /**
     * 单一记忆版：所有调用共用一份对话历史。
     * 适合“只有一个会话”的场景（如命令行小助手）。
     */
    String chat(String userMessage);

    /**
     * 多用户记忆版：第一个参数用 @MemoryId 标记为“记忆的身份”。
     * LangChain4j 会按 memoryId 去 ChatMemoryProvider 取/建对应的那份记忆，
     * 从而让不同用户（不同 id）的对话互相隔离、互不影响。
     *
     * @param memoryId    记忆身份（可用用户名、会话 ID 等任意对象）
     * @param userMessage 本轮用户输入（用 @UserMessage 标记其为用户消息文本）
     */
    String chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
