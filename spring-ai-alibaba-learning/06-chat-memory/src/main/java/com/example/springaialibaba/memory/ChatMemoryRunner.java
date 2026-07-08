package com.example.springaialibaba.memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 模块 06 演示：对话记忆——让 AI 记住上一轮说过的话
 * ============================================================================
 *
 * 【流程图】
 *
 *   第1轮  user:"我叫小明"
 *        └─► Advisor 把[历史(空)]+本轮 一起发给模型 ─► 模型答"你好小明"
 *        └─► Advisor 把[我叫小明 / 你好小明]存入 ChatMemory(会话ID=user-1)
 *
 *   第2轮  user:"我叫什么名字？"
 *        └─► Advisor 把[历史:我叫小明...]+本轮 一起发给模型 ─► 模型看到历史，答"你叫小明"
 *
 *   关键：模型本身没记忆，是 Advisor 每次把“历史消息”一并带上，制造出“记得”的效果。
 *
 * 【核心 API】
 *   ChatMemory memory = MessageWindowChatMemory.builder().maxMessages(10).build();
 *   ChatClient client = builder
 *       .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())  // 挂记忆顾问
 *       .build();
 *   // 调用时用会话ID区分不同会话的记忆：
 *   client.prompt().user("...")
 *       .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "user-1"))
 *       .call().content();
 * ============================================================================
 */
@Component
public class ChatMemoryRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    public ChatMemoryRunner(ChatClient.Builder chatClientBuilder) {
        // 1) 创建“滑动窗口记忆”：最多保留最近 10 条消息，超出自动丢弃最旧的。
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();

        // 2) 把“记忆顾问”设为默认 Advisor，挂到 ChatClient 上。
        //    从此每次调用都会自动带上历史、并把新问答存回记忆。
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块06：对话记忆（多轮上下文）==========\n");

        // 用一个会话 ID 把这几轮对话归到同一段“记忆”里
        String conversationId = "user-1";

        // ---------------- 第1轮：告诉 AI 我的名字 ----------------
        System.out.println("---------- 第1轮：自我介绍 ----------");
        String q1 = "你好，我叫小明，今年 18 岁。";
        System.out.println("【我说】" + q1);
        String a1 = chatClient
                .prompt()
                .user(q1)
                // 指定会话 ID：告诉记忆顾问“这轮属于 user-1 的对话”
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        System.out.println("【AI 答】" + a1 + "\n");

        // ---------------- 第2轮：考考它记不记得 ----------------
        System.out.println("---------- 第2轮：验证记忆（不再重复我的名字）----------");
        String q2 = "请问我叫什么名字？今年多大？";
        System.out.println("【我问】" + q2);
        String a2 = chatClient
                .prompt()
                .user(q2)
                // 用同一个会话 ID，才能读到第1轮存下的历史
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        System.out.println("【AI 答】" + a2);
        System.out.println("（若回答里出现“小明 / 18”，说明记忆生效！）\n");

        // ---------------- 对比：换一个新会话 ID，记忆是隔离的 ----------------
        System.out.println("---------- 对比：换会话 ID = user-2（全新对话，不该知道小明）----------");
        String a3 = chatClient
                .prompt()
                .user("请问我叫什么名字？")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "user-2"))  // 不同会话，记忆互相隔离
                .call()
                .content();
        System.out.println("【AI 答（user-2）】" + a3);
        System.out.println("（这里 AI 应表示不知道，证明不同会话的记忆互不串台）\n");

        System.out.println("========== 演示结束：AI 已能记住同一会话的上下文 ==========\n");
    }
}
