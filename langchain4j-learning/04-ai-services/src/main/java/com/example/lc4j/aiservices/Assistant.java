package com.example.lc4j.aiservices;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * ============================================================================
 * AI Service 接口：一个“被 LangChain4j 自动实现”的助手
 * ============================================================================
 *
 * 【关键点】
 *   这只是一个【普通 Java 接口】，我们不写任何实现类。
 *   运行时由 AiServices.create(Assistant.class, model) 动态生成实现：
 *   调用接口方法 -> 按注解拼好提示词 -> 调大模型 -> 把回答转成返回类型。
 * ============================================================================
 */
public interface Assistant {

    /**
     * 方法1：最简单的“一句话进、一句话出”。
     * 没有任何注解时，参数字符串会直接作为 UserMessage 发给模型。
     */
    String chat(String userMessage);

    /**
     * 方法2：带系统指令 + 用户消息模板 + 变量占位。
     *
     * @SystemMessage 给 AI 设定人设（每次调用都生效）。
     * @UserMessage   定义用户消息模板，{{topic}} 是占位符。
     * @V("topic")    把方法参数 topic 绑定到模板里的 {{topic}}。
     *
     * @param topic 主题，会被填入模板
     * @return 一首关于该主题的小诗
     */
    @SystemMessage("你是一位才华横溢的中文诗人，只用中文回答。")
    @UserMessage("请以「{{topic}}」为主题，写一首四行的短诗。")
    String writePoem(@V("topic") String topic);
}
