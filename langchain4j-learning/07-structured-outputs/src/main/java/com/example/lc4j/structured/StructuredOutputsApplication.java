package com.example.lc4j.structured;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 07：Structured Outputs 结构化输出 —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   大模型默认返回的是一段【纯文本】，程序很难直接拿来用。
 *   本模块演示 LangChain4j 的“结构化输出”：让 AiService 接口方法的返回类型
 *   直接是一个【Java 对象（POJO）】或【枚举（enum）】，框架会自动：
 *     1) 根据返回类型生成对应的 JSON 结构说明，加进提示词里要求模型按此格式回答；
 *     2) 把模型返回的 JSON 自动反序列化成你的 Java 对象。
 *   于是你拿到的不再是字符串，而是字段齐全、可直接 getter 的对象。
 *
 * 【典型用途】
 *   信息抽取：把一段自然语言（简历、订单描述、用户评论）抽成结构化数据；
 *   分类：让方法返回一个枚举（如情感 POSITIVE/NEGATIVE/NEUTRAL）。
 *
 * 【关键点】
 *   - 接口方法返回类型写成 POJO / List / enum，框架自动结构化。
 *   - 用 dev.langchain4j.model.output.structured.@Description 给字段写说明，
 *     这些说明会进入给模型的 JSON schema，提示模型每个字段该放什么 → 抽取更准。
 *
 * 【达到的目的】
 *   学会让 LLM 输出可直接被程序消费的强类型对象，告别手写 JSON 解析。
 * ============================================================================
 */
@SpringBootApplication
public class StructuredOutputsApplication {
    public static void main(String[] args) {
        SpringApplication.run(StructuredOutputsApplication.class, args);
    }
}
