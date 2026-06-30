package com.example.lc4j.guardrails;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;

/**
 * ============================================================================
 * 输出护栏：长度/格式校验（在拿到大模型回答之后执行）
 * ============================================================================
 *
 * 【关键点】
 *   - 实现 dev.langchain4j.guardrail.OutputGuardrail 接口。
 *   - 重写 validate(AiMessage)：这是处理“模型回答”的入口。
 *   - 用 aiMessage.text() 取出模型回答的纯文本。
 *   - 不符合规则有多种处理方式（本类只演示前两种最常用的）：
 *       · success()        —— 放行；
 *       · failure("原因")  —— 拦截（抛 OutputGuardrailException）；
 *       · retry("原因")    —— 让框架用同样的输入再问一次模型；
 *       · reprompt(..)     —— 追加一句修正提示后让模型重答。
 *
 *   本例规则：回答不能为空，且长度不能超过 200 字（演示“限制输出长度”）。
 * ============================================================================
 */
public class LengthOutputGuardrail implements OutputGuardrail {

    /** 允许的最大字符数 */
    private static final int MAX_LENGTH = 200;

    /**
     * 校验模型回答。
     *
     * @param responseFromLLM 大模型的回答消息
     * @return success() 放行；failure(原因) 拦截
     */
    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        // 取出模型回答的纯文本
        String text = responseFromLLM.text();

        // 规则1：回答不能为空
        if (text == null || text.isBlank()) {
            return failure("模型回答为空，不符合要求。");
        }

        // 规则2：回答长度不能超过上限
        if (text.length() > MAX_LENGTH) {
            return failure("模型回答过长（" + text.length() + " 字，上限 " + MAX_LENGTH + " 字）。");
        }

        // 通过所有规则：放行
        return success();
    }
}
