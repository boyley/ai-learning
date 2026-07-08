package com.example.springaialibaba.tool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 模块 08 演示：工具调用——让模型自动调用你的 Java 方法
 * ============================================================================
 *
 * 【两个演示】
 *   演示1：问“现在几点？” → 模型自动调用无参工具 getCurrentDateTime()。
 *   演示2：问“一个 18 岁的人是哪年出生的？” → 模型自动调用带参工具 birthYearOf(age)，
 *          它会先从问题里解析出 age=18，再调用工具拿到结果。
 *
 * 【关键：你不需要手动调用工具！】
 *   你只是用 .tools(new DateTimeTools()) 把工具箱交给模型，
 *   “要不要调、调哪个、传什么参数”全由模型自己决定，框架自动执行并回填结果。
 * ============================================================================
 */
@Component
public class ToolCallingRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    public ToolCallingRunner(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块08：工具调用（@Tool）==========\n");

        // ---------------- 演示1：无参工具（获取当前时间）----------------
        System.out.println("---------- 演示1：问“现在几点”，模型自动调用 getCurrentDateTime 工具 ----------");
        String q1 = "现在几点了？请告诉我完整的日期和时间。";
        System.out.println("【我问】" + q1);
        String a1 = chatClient
                .prompt()
                .user(q1)
                .tools(new DateTimeTools())   // ★ 把工具箱交给模型；调不调由模型自己决定
                .call()
                .content();
        System.out.println("【AI 答】" + a1 + "\n");

        // ---------------- 演示2：带参工具（推算出生年份）----------------
        System.out.println("---------- 演示2：带参数的工具，模型先解析出年龄再调用 birthYearOf ----------");
        String q2 = "我今年 18 岁，请问我大概是哪一年出生的？";
        System.out.println("【我问】" + q2);
        String a2 = chatClient
                .prompt()
                .user(q2)
                .tools(new DateTimeTools())   // 同一个工具箱里有多个工具，模型会挑合适的那个
                .call()
                .content();
        System.out.println("【AI 答】" + a2 + "\n");

        System.out.println("========== 演示结束：模型已能自动调用你的 Java 工具 ==========\n");
    }
}
