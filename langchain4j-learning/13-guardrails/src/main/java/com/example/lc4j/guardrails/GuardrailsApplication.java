package com.example.lc4j.guardrails;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 13：护栏（Guardrails）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   给 AiService 加“护栏”——在【调用大模型之前】和【拿到大模型回答之后】各加一道
 *   校验关卡，确保进出的内容都符合我们的规则。这是把 LLM 安全落地到生产的关键手段。
 *
 * 【两类护栏（已通过 javap 确认 API）】
 *   1. 输入护栏 InputGuardrail（包 dev.langchain4j.guardrail）
 *        - 实现接口，重写 validate(UserMessage)；用 userMessage.singleText() 拿到用户文本。
 *        - 返回 success() 放行；返回 failure("原因") 拦截（抛 InputGuardrailException，
 *          根本不会去调用大模型，省钱又安全）。
 *        - 用途：拦截敏感词、违禁内容、超长输入、注入攻击等。
 *   2. 输出护栏 OutputGuardrail（包 dev.langchain4j.guardrail）
 *        - 实现接口，重写 validate(AiMessage)；用 aiMessage.text() 拿到模型回答。
 *        - 返回 success() 放行；返回 failure("原因") 拦截；还能 retry()/reprompt() 让模型重答。
 *        - 用途：校验回答长度/格式、过滤泄露信息、确保 JSON 合法等。
 *
 * 【怎么把护栏挂到接口方法上（注解在 dev.langchain4j.service.guardrail）】
 *   - @InputGuardrails({A.class, B.class})  —— 加在 AiService 接口方法上，按顺序执行输入护栏。
 *   - @OutputGuardrails({C.class})          —— 加在 AiService 接口方法上，执行输出护栏。
 *
 * 【达到的目的】
 *   学会用“声明式护栏”给 AI 接口加上输入/输出校验，理解护栏在安全合规中的位置。
 *   注意：真正触发输出护栏需要调用大模型（要 Key/网络）；本模块按规范只要求编译通过。
 * ============================================================================
 */
@SpringBootApplication
public class GuardrailsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GuardrailsApplication.class, args);
    }
}
