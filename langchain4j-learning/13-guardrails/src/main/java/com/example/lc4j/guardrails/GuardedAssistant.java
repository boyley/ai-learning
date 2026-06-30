package com.example.lc4j.guardrails;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.guardrail.OutputGuardrails;

/**
 * ============================================================================
 * AI Service 接口：带护栏的助手
 * ============================================================================
 *
 * 【关键点】
 *   - 普通 AiService 接口，由 AiServices.create(...) 自动实现。
 *   - 在方法上挂两道护栏（注解在 dev.langchain4j.service.guardrail）：
 *       @InputGuardrails(...)  —— 调用大模型【之前】执行：先过敏感词输入护栏。
 *       @OutputGuardrails(...) —— 拿到回答【之后】执行：再过长度/格式输出护栏。
 *   - 注解的 value 是“护栏类的 Class”，框架会反射创建它们的实例来执行校验。
 *
 *   执行顺序：用户输入 → 输入护栏 → 大模型 → 输出护栏 → 返回给调用方。
 *   任意一道护栏 failure 都会抛出对应异常，终止本次调用。
 * ============================================================================
 */
public interface GuardedAssistant {

    /**
     * 一个带护栏的问答方法。
     *
     * @InputGuardrails  指定输入护栏（这里只有敏感词拦截）。
     * @OutputGuardrails 指定输出护栏（这里只有长度/格式校验）。
     *
     * @param question 用户的问题
     * @return 经过输出护栏校验后的模型回答
     */
    @SystemMessage("你是一个乐于助人的中文助手，请用简洁的中文回答，控制在 200 字以内。")
    @InputGuardrails(SensitiveWordInputGuardrail.class)
    @OutputGuardrails(LengthOutputGuardrail.class)
    String ask(String question);
}
