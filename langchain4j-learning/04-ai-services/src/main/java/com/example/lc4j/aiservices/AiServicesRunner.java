package com.example.lc4j.aiservices;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * AI Services 演示：用接口 + AiServices 自动实现 LLM 调用
 * ============================================================================
 *
 * 【流程图】
 *
 *   Assistant 接口(只声明，不实现)
 *          │  AiServices.create(Assistant.class, model)
 *          ▼
 *   LangChain4j 动态生成实现(代理对象)
 *          │  你调用 assistant.writePoem("春天")
 *          ▼
 *   按注解拼提示词(@SystemMessage + @UserMessage 填入 {{topic}})
 *          │
 *          ▼
 *   调用 ChatModel ──► 大模型 ──► 回答 ──► 转成方法返回类型(String)
 *          │
 *          ▼
 *   你拿到结果，就像调用了一个普通本地方法
 * ============================================================================
 */
@Component
public class AiServicesRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块04：AI Services 声明式高级 API ==========\n");

        // 1) 构建底层对话模型（和前面模块一样）
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        // 2) ★★★ 核心：用 AiServices 为接口生成实现 ★★★
        //    create(接口Class, 模型) -> 返回一个实现了该接口的代理对象。
        Assistant assistant = AiServices.create(Assistant.class, model);

        // 3) 像调用普通方法一样使用它——底层自动完成与大模型的交互。
        System.out.println("===== 演示1：无注解方法 chat(String) =====");
        String q1 = "用一句话解释什么是接口（interface）？";
        System.out.println("我问：" + q1);                     // ★ 先打印问题
        System.out.println("AI ：" + assistant.chat(q1) + "\n");

        System.out.println("===== 演示2：@SystemMessage + @UserMessage + @V 变量 =====");
        System.out.println("我让 AI 以「春天」为主题写一首四行短诗：");   // ★ 说明这次的输入
        System.out.println(assistant.writePoem("春天") + "\n");
        System.out.println("我让 AI 以「代码」为主题写一首四行短诗：");
        System.out.println(assistant.writePoem("代码"));

        System.out.println("\n========== 模块04 演示结束 ==========\n");
    }
}
