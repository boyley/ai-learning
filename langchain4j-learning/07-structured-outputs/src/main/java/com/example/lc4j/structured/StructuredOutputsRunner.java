package com.example.lc4j.structured;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * Structured Outputs 演示：让模型直接返回 Java 对象 / 枚举
 * ============================================================================
 *
 * 【流程图】
 *
 *   接口方法返回类型(Person / Sentiment)
 *          │  AiServices 读取返回类型 + @Description 字段说明
 *          ▼
 *   自动生成 JSON schema 注入提示词("请按这个结构作答")
 *          │
 *          ▼
 *   一段自然语言 ──► ChatModel ──► 模型按 JSON 格式回答
 *          │
 *          ▼
 *   框架自动把 JSON 反序列化成 Person / Sentiment
 *          │
 *          ▼
 *   你拿到强类型对象，直接 .name / .hobbies / switch(枚举)
 * ============================================================================
 */
@Component
public class StructuredOutputsRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块07：Structured Outputs 结构化输出 ==========\n");

        // 构建底层对话模型（同前面模块）。
        // 结构化输出对“按格式作答”要求较高，温度设低些更稳定。
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.0)   // 抽取/分类追求确定性，温度调低
                .build();

        // 用 AiServices 为接口生成实现——返回类型是 POJO/enum，框架会自动结构化。
        InfoExtractor extractor = AiServices.create(InfoExtractor.class, model);

        demo1_extractPojo(extractor);
        demo2_classifyEnum(extractor);

        System.out.println("\n========== 模块07 演示结束 ==========\n");
    }

    /** 演示1：把一段自然语言抽取成 Person 对象，并逐字段打印。 */
    private void demo1_extractPojo(InfoExtractor extractor) {
        System.out.println("===== 演示1：抽取成 POJO（Person） =====");

        String text = "我叫李雷，今年 28 岁，住在上海，平时喜欢打篮球、弹吉他和摄影。";
        System.out.println("输入文本：" + text);

        // 调用返回 Person 的方法：框架自动让模型按 Person 的 JSON 结构作答并反序列化。
        Person person = extractor.extractPerson(text);

        // 拿到的是强类型对象，可直接访问每个字段（无需手写 JSON 解析）。
        System.out.println("抽取结果对象：" + person);
        System.out.println("  姓名 person.name   = " + person.name);
        System.out.println("  年龄 person.age    = " + person.age);
        System.out.println("  城市 person.city   = " + person.city);
        System.out.println("  爱好 person.hobbies= " + person.hobbies + "\n");
    }

    /** 演示2：对多条评论做情感分类，返回枚举 Sentiment。 */
    private void demo2_classifyEnum(InfoExtractor extractor) {
        System.out.println("===== 演示2：分类成枚举（Sentiment） =====");

        String[] reviews = {
                "这家餐厅太棒了，菜好吃服务又好，下次还来！",
                "踩雷了，又贵又难吃，再也不会来了。",
                "就是一家普通的快餐店，没什么特别的。"
        };

        for (String review : reviews) {
            // 返回枚举：模型只能从 POSITIVE/NEGATIVE/NEUTRAL 里选一个，框架转成枚举常量。
            Sentiment sentiment = extractor.classify(review);
            System.out.println("评论：" + review);
            System.out.println("  → 情感：" + sentiment);
        }
    }
}
