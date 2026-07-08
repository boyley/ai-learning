package com.example.springaialibaba.tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 08：工具调用 / 函数调用 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   大模型只“知道”训练时的知识，它不知道“现在几点”、也不能帮你查数据库、调接口。
 *   工具调用(Tool Calling，又叫 Function Calling) 就是给模型配上“工具箱”：
 *   你把一些 Java 方法标记成“工具”，模型在需要时会【自动决定】调用哪个工具、传什么参数，
 *   框架帮你真正执行该方法，再把结果回填给模型，最终生成答案。
 *
 * 【一次带工具的对话，幕后发生了什么】
 *
 *   你问："现在几点？"
 *      │
 *      ▼
 *   模型判断："我需要当前时间，调用 getCurrentDateTime 工具"   ← 模型自己决定
 *      │
 *      ▼
 *   框架执行 DateTimeTools.getCurrentDateTime() → "2026-07-08 14:30:00"
 *      │
 *      ▼
 *   把工具结果回填给模型 → 模型生成最终回答："现在是 2026年7月8日 下午2点半。"
 *
 * 【核心 API】
 *   - 给普通方法加 @Tool(description="...")，参数加 @ToolParam(description="...")。
 *   - 调用时 chatClient.prompt().user("...").tools(new DateTimeTools()).call().content()。
 *
 * 【怎么做】
 *   @SpringBootApplication 启动容器；容器就绪后自动执行 ToolCallingRunner 演示。
 * ============================================================================
 */
@SpringBootApplication
public class ToolCallingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToolCallingApplication.class, args);
    }
}
