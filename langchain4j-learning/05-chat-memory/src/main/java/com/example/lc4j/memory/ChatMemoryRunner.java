package com.example.lc4j.memory;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * Chat Memory 演示：让 AI 记住上文 + 多用户记忆隔离
 * ============================================================================
 *
 * 【流程图】
 *
 *   演示1（单一记忆，共享一份历史）：
 *
 *     第1轮 "我叫小明，养了只猫"
 *            │  AiServices 把 [User] 写入 ChatMemory
 *            ▼
 *     ChatMemory(滑动窗口) ──注入历史──► ChatModel ──► AiMessage 也写回 ChatMemory
 *            │
 *     第2轮 "我的猫叫什么？我叫什么？"
 *            │  请求里自动带上第1轮的 User+Ai 消息
 *            ▼
 *     模型看得到上文 ──► 正确回答(引用第1轮信息)
 *
 *   演示2（多用户记忆，@MemoryId 隔离）：
 *
 *     userA ──┐                          ┌── memory(A)：只存 A 的对话
 *             ├─ ChatMemoryProvider.get(id)
 *     userB ──┘                          └── memory(B)：只存 B 的对话
 *     => A 说过的话，B 完全不知道；互不串台。
 * ============================================================================
 */
@Component
public class ChatMemoryRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块05：Chat Memory 对话记忆 ==========\n");

        // 构建底层对话模型（同前面模块）
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        demo1_sharedMemory(model);
        demo2_memoryPerUser(model);

        System.out.println("\n========== 模块05 演示结束 ==========\n");
    }

    /** 演示1：单一 ChatMemory，连续两轮对话，第二轮引用第一轮信息。 */
    private void demo1_sharedMemory(ChatModel model) {
        System.out.println("===== 演示1：单一记忆（MessageWindowChatMemory）连续对话 =====");

        // MessageWindowChatMemory.withMaxMessages(n)：创建“最多保留 n 条消息”的滑动窗口记忆。
        // 窗口满了会自动丢弃最早的消息，防止上下文无限增长导致 Token 超限。
        MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);

        // AiServices.builder(...).chatMemory(memory)：给助手装上这份记忆。
        // 之后每次调用，框架都会把历史消息自动注入请求、并把新消息写回 memory。
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)        // 指定底层模型
                .chatMemory(memory)      // ★ 装上记忆，实现多轮上下文
                .build();

        // 第1轮：告诉助手一些信息（这些会被存进 memory）。
        String r1 = assistant.chat("你好，我叫小明，我养了一只叫『豆豆』的橘猫。");
        System.out.println("第1轮 用户：你好，我叫小明，我养了一只叫『豆豆』的橘猫。");
        System.out.println("第1轮 AI  ：" + r1 + "\n");

        // 第2轮：只问“我的猫叫什么、我叫什么”，不再重复信息。
        // 如果有记忆，模型能从第1轮上文中找到答案。
        String r2 = assistant.chat("请问我的猫叫什么名字？我又叫什么？");
        System.out.println("第2轮 用户：请问我的猫叫什么名字？我又叫什么？");
        System.out.println("第2轮 AI  ：" + r2 + "（能答对说明记住了上文）\n");
    }

    /** 演示2：用 chatMemoryProvider + @MemoryId 为不同用户分配独立记忆。 */
    private void demo2_memoryPerUser(ChatModel model) {
        System.out.println("===== 演示2：@MemoryId 多用户记忆隔离 =====");

        // chatMemoryProvider：一个“按 id 取记忆”的工厂。
        // 框架在调用带 @MemoryId 的方法时，会用该 id 调用 get(id) 拿到对应的那份记忆。
        // 这里为每个 id 各建一份独立的 MessageWindowChatMemory。
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10)) // ★ 按 id 分配记忆
                .build();

        // 用户 A 说出自己的秘密爱好（存进 A 的记忆）。
        assistant.chat("userA", "记住：我最喜欢的运动是篮球。");
        // 用户 B 说出不同的爱好（存进 B 的记忆）。
        assistant.chat("userB", "记住：我最喜欢的运动是游泳。");

        // 再分别询问，验证两份记忆互不串台。
        String a = assistant.chat("userA", "我最喜欢的运动是什么？");
        String b = assistant.chat("userB", "我最喜欢的运动是什么？");
        System.out.println("userA 的回答：" + a + "（应为篮球）");
        System.out.println("userB 的回答：" + b + "（应为游泳）");
    }
}
