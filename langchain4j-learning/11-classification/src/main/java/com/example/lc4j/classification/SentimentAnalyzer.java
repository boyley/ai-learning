package com.example.lc4j.classification;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * ============================================================================
 * AI Service 接口：情感分析器（分类任务）
 * ============================================================================
 *
 * 【关键点】
 *   - 这只是一个普通 Java 接口，由 AiServices.create(...) 在运行时自动实现。
 *   - classify(...) 方法的【返回类型是枚举 Sentiment】，这就是“分类”的关键：
 *     LangChain4j 会把 Sentiment 的所有常量写进提示词，并把模型回答解析成枚举值。
 * ============================================================================
 */
public interface SentimentAnalyzer {

    /**
     * 对一条文本做情感分类。
     *
     * @SystemMessage 设定模型角色（一个严谨的情感分析器）。
     * @UserMessage   用户消息模板，{{text}} 会被待分类文本替换。
     * @V("text")     把方法参数绑定到模板里的 {{text}} 占位符。
     *
     * 返回类型是 Sentiment（枚举）——AiServices 会自动：
     *   1) 把 POSITIVE/NEGATIVE/NEUTRAL 作为可选项写进提示词；
     *   2) 把模型回答解析成对应的枚举常量。
     *
     * @param text 待分析的评论文本
     * @return 情感类别（三选一）
     */
    @SystemMessage("你是一个严谨的中文情感分析器，只根据文本本身判断情感倾向。")
    @UserMessage("请分析下面这段文本的情感倾向：\n{{text}}")
    Sentiment classify(@V("text") String text);
}
