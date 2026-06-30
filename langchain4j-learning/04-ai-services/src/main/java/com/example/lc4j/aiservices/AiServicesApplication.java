package com.example.lc4j.aiservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 04：AI Services 声明式高级 API —— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   介绍 LangChain4j 最强大、最常用的高级特性：AI Services。
 *   你只需声明一个【普通 Java 接口】，用注解描述提示词，LangChain4j 就会在运行时
 *   动态生成这个接口的实现——你像调用本地方法一样调用它，背后自动完成与大模型的交互。
 *
 * 【对比：底层 API vs AI Services】
 *   - 底层（模块02）：自己拼 Message、构建 ChatRequest、解析 ChatResponse，啰嗦但灵活。
 *   - AI Services：把这些样板代码全部隐藏，业务代码只剩“一个接口方法”，简洁直观。
 *
 * 【需要先懂的注解】
 *   - @SystemMessage：在接口方法上写死系统指令（AI 的人设/规则）。
 *   - @UserMessage：定义用户消息模板，可用 {{变量}} 占位。
 *   - @V("name")：把方法参数绑定到模板里的 {{name}} 占位符。
 *   - 方法返回类型可以是 String，也可以是任意 POJO（自动结构化，见模块07）。
 *
 * 【达到的目的】
 *   学会用声明式接口快速构建 LLM 功能，这是真实项目里使用 LangChain4j 的主流方式。
 * ============================================================================
 */
@SpringBootApplication
public class AiServicesApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiServicesApplication.class, args);
    }
}
