package com.example.lc4j.guardrails;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.List;

/**
 * ============================================================================
 * 输入护栏：敏感词拦截（在调用大模型之前执行）
 * ============================================================================
 *
 * 【关键点】
 *   - 实现 dev.langchain4j.guardrail.InputGuardrail 接口。
 *   - 重写 validate(UserMessage)：这是处理“用户输入”的入口。
 *   - 用 userMessage.singleText() 取出用户输入的纯文本。
 *   - 命中敏感词 -> 返回 failure("原因")：框架会抛 InputGuardrailException，
 *     【直接终止，不会去调用大模型】（既安全又省钱）。
 *   - 一切正常 -> 返回 success() 放行。
 *
 *   注意：护栏类需要有一个无参构造器（这里用默认构造器即可），
 *         框架会通过 @InputGuardrails 注解里写的 Class 反射创建它。
 * ============================================================================
 */
public class SensitiveWordInputGuardrail implements InputGuardrail {

    /** 简单的敏感词黑名单（真实项目通常来自配置中心或风控服务） */
    private static final List<String> BLACKLIST = List.of("炸弹", "毒品", "黑客攻击");

    /**
     * 校验用户输入。
     *
     * @param userMessage 用户消息（封装了用户输入的内容）
     * @return success() 放行；failure(原因) 拦截
     */
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        // 取出用户输入的纯文本
        String text = userMessage.singleText();

        // 逐个比对黑名单
        for (String word : BLACKLIST) {
            if (text != null && text.contains(word)) {
                // 命中敏感词：拦截，并说明原因（不会再去调用大模型）
                return failure("输入命中敏感词「" + word + "」，已拦截。");
            }
        }

        // 没有问题：放行
        return success();
    }
}
