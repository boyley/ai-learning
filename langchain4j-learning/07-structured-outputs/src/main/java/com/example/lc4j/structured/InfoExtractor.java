package com.example.lc4j.structured;

import dev.langchain4j.service.UserMessage;

/**
 * ============================================================================
 * AI Service 接口：结构化抽取器
 * ============================================================================
 *
 * 三个方法演示三种“非字符串”返回类型：
 *   - extractPerson  → 返回 POJO（Person），把一段话抽成结构化对象。
 *   - classify       → 返回 enum（Sentiment），做情感分类。
 *
 * 关键点：返回类型不再是 String，框架会据此生成 JSON schema 引导模型，
 * 并把模型的 JSON 回答自动反序列化成对应类型。我们一行实现都不用写。
 * ============================================================================
 */
public interface InfoExtractor {

    /**
     * 从一段自然语言里抽取出人物信息，直接得到一个 Person 对象。
     * {{it}} 是 @UserMessage 单参数时的默认占位符，代表传入的那段文本。
     */
    @UserMessage("请从下面这段话中抽取人物信息：{{it}}")
    Person extractPerson(String text);

    /**
     * 判断一段评论的情感倾向，返回枚举（只会是 POSITIVE/NEGATIVE/NEUTRAL 之一）。
     */
    @UserMessage("请判断下面这条评论的情感倾向：{{it}}")
    Sentiment classify(String review);
}
