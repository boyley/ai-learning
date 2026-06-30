package com.example.lc4j.tools;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * Tools 演示：让大模型自动调用我们注册的 Java 方法
 * ============================================================================
 *
 * 【流程图】
 *
 *   用户提问("北京天气怎么样？")
 *          │
 *          ▼
 *   Assistant(由 AiServices 生成，已绑定 AssistantTools)
 *          │  把“工具清单 + 描述”一并发给模型
 *          ▼
 *   大模型判断："这个问题我答不了，需要调 getWeather"
 *          │  返回一个“函数调用请求”(tool call)
 *          ▼
 *   LangChain4j 自动执行 AssistantTools.getWeather("北京")
 *          │  把返回值("晴 26℃...")回填给模型
 *          ▼
 *   大模型基于工具结果，组织成自然语言最终回答
 *          │
 *          ▼
 *   你拿到字符串结果（整个来回交互框架已自动完成）
 * ============================================================================
 */
@Component
public class ToolsRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块08：Tools 工具/函数调用 ==========\n");

        // 1) 构建底层对话模型（和前面模块一样）
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        // 2) ★★★ 核心：用 AiServices.builder 装配助手，并用 .tools(...) 注册工具对象 ★★★
        //    传入的是一个【实例】，框架会扫描它里面所有 @Tool 方法，自动暴露给模型。
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)                  // 绑定对话模型
                .tools(new AssistantTools())       // 绑定工具：模型由此获得“算数 + 查天气”的能力
                .build();

        // 3) 提一个需要“计算”的问题——模型会自动调用 add 工具
        System.out.println("===== 演示1：触发计算工具 =====");
        System.out.println("问：帮我算一下 123 加 456 等于多少？");
        System.out.println("AI：" + assistant.chat("帮我算一下 123 加 456 等于多少？") + "\n");

        // 4) 提一个需要“查天气”的问题——模型会自动调用 getWeather 工具
        System.out.println("===== 演示2：触发天气工具 =====");
        System.out.println("问：北京今天天气怎么样？适合穿短袖吗？");
        System.out.println("AI：" + assistant.chat("北京今天天气怎么样？适合穿短袖吗？") + "\n");

        // 5) 提一个普通问题——模型判断不需要工具，直接回答（说明工具是“按需”调用的）
        System.out.println("===== 演示3：无需工具的普通问题 =====");
        System.out.println("问：用一句话介绍一下你自己。");
        System.out.println("AI：" + assistant.chat("用一句话介绍一下你自己。"));

        System.out.println("\n========== 模块08 演示结束 ==========\n");
    }
}
