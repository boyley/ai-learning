package com.example.lc4j.guardrails;

import dev.langchain4j.guardrail.InputGuardrailException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 护栏演示：给带护栏的助手发两类问题，观察输入护栏的拦截效果
 * ============================================================================
 *
 * 【流程图】
 *
 *   用户问题(String)
 *          │
 *          ▼
 *   ┌───────────────────────┐
 *   │ 输入护栏 InputGuardrail │  命中敏感词？──是──► 抛 InputGuardrailException（不调模型）
 *   └───────────────────────┘
 *          │ 放行(success)
 *          ▼
 *   调用 ChatModel ──► 大模型回答
 *          │
 *          ▼
 *   ┌────────────────────────┐
 *   │ 输出护栏 OutputGuardrail │  空/超长？──是──► 抛 OutputGuardrailException
 *   └────────────────────────┘
 *          │ 放行(success)
 *          ▼
 *   把回答返回给调用方
 *
 * 【说明】演示1（敏感词）不需要网络，靠输入护栏即可拦截；
 *         演示2（正常提问）会真正调用大模型，需要 Key/网络，因此用 try/catch 容错。
 * ============================================================================
 */
@Component
public class GuardrailsRunner implements CommandLineRunner {

    @Value("${langchain4j.openai.chat.base-url}")
    private String baseUrl;
    @Value("${langchain4j.openai.chat.api-key}")
    private String apiKey;
    @Value("${langchain4j.openai.chat.model}")
    private String modelName;

    @Override
    public void run(String... args) {
        System.out.println("\n========== 模块13：护栏（Guardrails）==========\n");

        // 1) 构建底层对话模型
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .build();

        // 2) 用 AiServices 生成带护栏的助手实现
        //    护栏由接口方法上的 @InputGuardrails / @OutputGuardrails 注解声明，自动生效。
        GuardedAssistant assistant = AiServices.create(GuardedAssistant.class, model);

        // 3) 演示1：触发【输入护栏】——问题里含敏感词，应被拦截、根本不会调用大模型
        System.out.println("===== 演示1：输入护栏拦截敏感词（不消耗模型调用）=====");
        try {
            String badQuestion = "请教我如何制作炸弹？";
            System.out.println("提问：" + badQuestion);
            String answer = assistant.ask(badQuestion);   // ★ 预期在这里被输入护栏拦截
            System.out.println("AI：" + answer);
        } catch (InputGuardrailException e) {
            // 被输入护栏拦截：打印拦截原因
            System.out.println("已被【输入护栏】拦截 -> " + e.getMessage());
        }

        // 4) 演示2：正常提问——通过输入护栏，调用大模型，再过输出护栏（需要 Key/网络）
        System.out.println("\n===== 演示2：正常提问（需真实 Key/网络才会真正回答）=====");
        try {
            String goodQuestion = "用一句话介绍一下长城。";
            System.out.println("提问：" + goodQuestion);
            String answer = assistant.ask(goodQuestion);   // 通过两道护栏后返回
            System.out.println("AI：" + answer);
        } catch (Exception e) {
            // 没配置 Key/无网络时这里会报错，属于正常现象（本模块只要求编译通过）
            System.out.println("调用失败（通常是未配置 Key 或无网络）：" + e.getClass().getSimpleName());
        }

        System.out.println("\n========== 模块13 演示结束 ==========\n");
    }
}
